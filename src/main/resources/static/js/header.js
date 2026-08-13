// 모든 fetch(POST/PUT/DELETE/PATCH) 요청에 CSRF 토큰 헤더를 자동으로 붙인다.
// header.js는 모든 페이지에서 가장 먼저 로드되므로, 다른 JS 파일들은 fetch 호출부를
// 각각 수정할 필요 없이 여기서 한 번만 window.fetch를 감싸서 해결한다.
(() => {
    const tokenMeta = document.querySelector('meta[name="_csrf"]');
    const headerMeta = document.querySelector('meta[name="_csrf_header"]');
    if (!tokenMeta || !headerMeta) {
        return;
    }

    const csrfToken = tokenMeta.content;
    const csrfHeaderName = headerMeta.content;
    const unsafeMethods = new Set(["POST", "PUT", "DELETE", "PATCH"]);
    const originalFetch = window.fetch.bind(window);

    window.fetch = (input, init = {}) => {
        const method = (init.method || "GET").toUpperCase();
        const url = typeof input === "string" ? input : input.url;

        // 다른 사이트로 나가는 요청에는 우리 서버의 CSRF 토큰을 실어 보낼 이유가 없다.
        const isSameOrigin = !/^https?:\/\//i.test(url) || url.startsWith(window.location.origin);

        if (!unsafeMethods.has(method) || !isSameOrigin) {
            return originalFetch(input, init);
        }

        const headers = new Headers(init.headers);
        if (!headers.has(csrfHeaderName)) {
            headers.set(csrfHeaderName, csrfToken);
        }

        return originalFetch(input, { ...init, headers });
    };
})();

const root = document.documentElement;

const saveTheme = (theme) => {
    try {
        localStorage.setItem("stockhub-theme", theme);
    } catch {
        // 저장할 수 없는 환경에서도 현재 화면의 색상 모드는 정상적으로 변경
    }
};

// 세션 타이머 관련 함수 (전역 함수로 등록)
let sessionTimerInterval = null;

function updateSessionTimer() {
    const timerContainer = document.querySelector(".header-session-timer");
    const timeDisplay = document.getElementById("header-session-remaining-time");

    if (!timerContainer || !timeDisplay) {
        return;
    }

    const expiresAtStr = timerContainer.getAttribute("data-session-expires-at");
    if (!expiresAtStr) {
        return;
    }

    // 서버에서 전달받은 만료 시각 (밀리초)
    const expiresAt = parseInt(expiresAtStr, 10);
    const now = new Date().getTime();
    const remainingMs = expiresAt - now;

    if (remainingMs <= 0) {
        timeDisplay.textContent = "00:00";
        if (sessionTimerInterval) clearInterval(sessionTimerInterval);
        alert("세션이 만료되었습니다. 다시 로그인해 주세요.");
        window.location.reload();
        return;
    }

    const totalSeconds = Math.floor(remainingMs / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    const formattedMinutes = String(minutes).padStart(2, "0");
    const formattedSeconds = String(seconds).padStart(2, "0");

    timeDisplay.textContent = `${formattedMinutes}:${formattedSeconds}`;
}

// 세션 연장 버튼 클릭 시 호출
async function extendSession(e) {
    // 1. 버튼 클릭 시 발생할 수 있는 폼 제출/새로고침 기본 동작 방지
    if (e && e.preventDefault) {
        e.preventDefault();
    }

    try {
        const contextPath = document.getElementById("member-profile-modal")?.dataset.contextPath || "";

        // 2. 백엔드 Controller 주소(/member/session/extend)와 정확히 일치시킴
        const response = await fetch(`${contextPath}/member/session/extend`, {
            method: "POST",
            headers: {
                "X-Requested-With": "XMLHttpRequest",
                "Content-Type": "application/json"
            }
        });

        if (response.ok) {
            const data = await response.json();

            // 3. ApiResponse의 실제 데이터 필드(data.data)에서 만료 시각 추출
            const newExpiresAt = data.data;

            if (data.success && newExpiresAt) {
                const timerContainer = document.querySelector(".header-session-timer");
                if (timerContainer) {
                    timerContainer.setAttribute("data-session-expires-at", newExpiresAt);
                }

                // 전역 변수(expiresAtMs)가 존재할 경우 함께 업데이트
                if (typeof expiresAtMs !== 'undefined') {
                    expiresAtMs = newExpiresAt;
                }

                // 타이머 UI 즉시 갱신
                if (typeof updateSessionTimer === 'function') {
                    updateSessionTimer();
                } else if (typeof updateTimer === 'function') {
                    updateTimer();
                }

            } else {
                alert(data.message || "세션 연장에 실패했습니다.");
            }
        } else {
            alert("서버 통신에 실패했습니다.");
        }
    } catch (err) {
        console.error("세션 연장 오류:", err);
        alert("세션 연장 중 오류가 발생했습니다.");
    }
}

document.addEventListener("DOMContentLoaded", () => {
    // 세션 타이머 초기화 및 1초마다 갱신
    const timerContainer = document.querySelector(".header-session-timer");
    if (timerContainer) {
        updateSessionTimer();
        sessionTimerInterval = setInterval(updateSessionTimer, 1000);
    }

    const themeToggle = document.getElementById("theme-toggle");
    const mobileMenuToggle = document.getElementById("mobile-menu-toggle");
    const mainNavigation = document.getElementById("main-navigation");
    const profileToggle = document.querySelector(".header-profile-toggle");
    const profileDropdown = document.getElementById("header-profile-dropdown");
    const mobileMenuQuery = window.matchMedia("(max-width: 840px)");

    // 서버 처리 결과로 표시된 성공·실패 알림은 5초 뒤 화면에서 제거
    document.querySelectorAll(".alert").forEach((alertElement) => {
        window.setTimeout(() => {
            alertElement.remove();
        }, 5000);
    });

    const closeMobileMenu = () => {
        mainNavigation?.classList.remove("is-open");
        mobileMenuToggle?.setAttribute("aria-expanded", "false");
        mobileMenuToggle?.setAttribute("aria-label", "메뉴 열기");

        const menuIcon = mobileMenuToggle?.querySelector("span");
        if (menuIcon) {
            menuIcon.textContent = "☰";
        }
    };

    themeToggle?.addEventListener("click", () => {
        root.classList.add("theme-changing");

        const nextTheme = root.dataset.theme === "dark" ? "light" : "dark";
        root.dataset.theme = nextTheme;
        saveTheme(nextTheme);

        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                root.classList.remove("theme-changing");
            });
        });
    });

    mobileMenuToggle?.addEventListener("click", () => {
        const isOpen = mainNavigation?.classList.toggle("is-open") ?? false;
        mobileMenuToggle.setAttribute("aria-expanded", String(isOpen));
        mobileMenuToggle.setAttribute("aria-label", isOpen ? "메뉴 닫기" : "메뉴 열기");
        mobileMenuToggle.querySelector("span").textContent = isOpen ? "✕" : "☰";
    });

    mainNavigation?.querySelectorAll("a").forEach((link) => {
        link.addEventListener("click", () => {
            closeMobileMenu();
        });
    });

    const closeProfileDropdown = () => {
        if (!profileToggle || !profileDropdown) {
            return;
        }

        profileDropdown.hidden = true;
        profileToggle.setAttribute("aria-expanded", "false");
    };

    profileToggle?.addEventListener("click", () => {
        if (!profileDropdown) {
            return;
        }

        const willOpen = profileDropdown.hidden;
        profileDropdown.hidden = !willOpen;
        profileToggle.setAttribute("aria-expanded", String(willOpen));
    });

    // 회원 메뉴의 링크나 모달 버튼을 누르면 드롭다운을 닫음
    profileDropdown?.querySelectorAll("a, button").forEach((menuItem) => {
        menuItem.addEventListener("click", closeProfileDropdown);
    });

    document.addEventListener("click", (event) => {
        if (!profileToggle || !profileDropdown) {
            return;
        }

        const clickedElement = event.target;
        if (!(clickedElement instanceof Node)) {
            return;
        }

        if (!profileToggle.contains(clickedElement) && !profileDropdown.contains(clickedElement)) {
            closeProfileDropdown();
        }
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeProfileDropdown();
            closeMobileMenu();
        }
    });

    mobileMenuQuery.addEventListener("change", (event) => {
        if (!event.matches) {
            closeMobileMenu();
        }
    });
});

/* =========================================================
   공통 회원 프로필 모달
   ========================================================= */
document.addEventListener("DOMContentLoaded", () => {
    const modalOverlay = document.getElementById("member-profile-modal");
    if (!modalOverlay) return;

    const modal = modalOverlay.querySelector(".member-profile-modal");
    const closeButton = modalOverlay.querySelector("[data-profile-modal-close]");
    const loading = modalOverlay.querySelector("[data-profile-loading]");
    const error = modalOverlay.querySelector("[data-profile-error]");
    const content = modalOverlay.querySelector("[data-profile-content]");
    const followToggle = modalOverlay.querySelector("[data-profile-follow-toggle]");
    const publicStats = modalOverlay.querySelector("[data-profile-public-stats]");
    const investmentStats = modalOverlay.querySelectorAll("[data-profile-investment-stat]");
    const postsLink = modalOverlay.querySelector("[data-profile-posts-link]");
    const profileApi = modalOverlay.dataset.profileApi;
    const contextPath = modalOverlay.dataset.contextPath || "";
    const defaultProfile = modalOverlay.dataset.defaultProfile;
    let currentProfile = null;
    let requestSequence = 0;
    let lastFocusedElement = null;
    let currentTrigger = null;

    const resolveProfileAnchor = () => {
        if (!currentTrigger) return null;

        const ownAvatar = currentTrigger.querySelector?.(
            ".board-card-avatar, .board-header-avatar, .comment-avatar, "
            + ".comment-avatar-placeholder, .ranking-avatar"
        );
        if (ownAvatar) return ownAvatar;

        const rankingRow = currentTrigger.closest?.(".ranking-row");
        return rankingRow?.querySelector(".ranking-avatar-frame") || currentTrigger;
    };

    const positionProfilePopover = () => {
        if (!currentTrigger || modalOverlay.hidden) return;

        const margin = 12;
        const gap = 8;
        const anchor = resolveProfileAnchor();
        if (!anchor) return;

        const triggerRect = anchor.getBoundingClientRect();
        const availableWidth = window.innerWidth - triggerRect.right - gap - margin;
        const isRankingProfile = Boolean(currentTrigger.closest?.(".ranking-row"));
        const preferredMaxWidth = isRankingProfile ? 360 : 430;
        modal.style.maxWidth = Math.max(280, Math.min(preferredMaxWidth, availableWidth)) + "px";

        const left = window.scrollX + triggerRect.right + gap;
        let top = window.scrollY + triggerRect.top;

        const boardToolbar = document.querySelector(".board-toolbar");
        if (boardToolbar) {
            const toolbarRect = boardToolbar.getBoundingClientRect();
            top = Math.max(top, window.scrollY + toolbarRect.bottom + margin);
        }

        modal.style.left = left + "px";
        modal.style.top = top + "px";
    };

    const assetUrl = (path) => {
        if (!path) return defaultProfile;
        if (/^(https?:|data:)/i.test(path) || path.startsWith(contextPath + "/")) return path;
        return contextPath + (path.startsWith("/") ? path : "/" + path);
    };

    const fillProfile = (profile) => {
        currentProfile = profile;
        const avatar = modalOverlay.querySelector("[data-profile-avatar]");
        const avatarFrame = modalOverlay.querySelector("[data-profile-avatar-frame]");
        avatar.src = assetUrl(profile.profile);
        avatar.onerror = () => {
            avatar.onerror = null;
            avatar.src = defaultProfile;
        };

        avatarFrame.classList.remove("rank-first", "rank-second", "rank-third");
        if (profile.badge === "RANKER" && profile.rankPosition >= 1 && profile.rankPosition <= 3) {
            avatarFrame.classList.add([
                "rank-first",
                "rank-second",
                "rank-third"
            ][profile.rankPosition - 1]);
        }
        modalOverlay.querySelector("[data-profile-nickname]").textContent = profile.nickname;
        modalOverlay.querySelector("[data-profile-member-id]").textContent = "@" + profile.memberId;
        modalOverlay.querySelector("[data-profile-heading]").textContent = "회원 정보";

        followToggle.hidden = !profile.canFollow;
        followToggle.disabled = false;
        followToggle.textContent = profile.followingTarget ? "팔로잉" : "팔로우";
        followToggle.setAttribute("aria-pressed", String(profile.followingTarget));
        followToggle.classList.toggle("is-following", profile.followingTarget);

        const badge = modalOverlay.querySelector("[data-profile-badge]");
        const rankLabel = profile.rankType === "profit" ? "수익금 RANKER" : "수익률 RANKER";
        badge.textContent = profile.badge === "ADMIN"
            ? "ADMIN"
            : (profile.badge === "RANKER"
                ? rankLabel + (profile.rankPosition ? " · " + profile.rankPosition + "위" : "")
                : "USER");
        badge.classList.toggle("is-ranker", profile.badge === "RANKER");
        badge.classList.toggle("is-admin", profile.badge === "ADMIN");

        publicStats.hidden = false;
        investmentStats.forEach((stat) => {
            stat.hidden = !profile.detailsPublic;
        });
        postsLink.href = profileApi + encodeURIComponent(profile.memberId) + "/posts";
        modalOverlay.querySelector("[data-profile-post-count]").textContent = profile.postCount;
        modalOverlay.querySelector("[data-profile-follower-count]").textContent = profile.followerCount;
        modalOverlay.querySelector("[data-profile-following-count]").textContent = profile.followingCount;

        if (!profile.detailsPublic) {
            return;
        }
        const returnRate = Number(profile.returnRate ?? 0);
        const profit = Number(profile.profit ?? 0);
        const returnRateElement = modalOverlay.querySelector("[data-profile-return-rate]");
        const profitElement = modalOverlay.querySelector("[data-profile-profit]");

        returnRateElement.textContent =
            returnRate.toLocaleString("ko-KR", { maximumFractionDigits: 2 }) + "%";
        profitElement.textContent = profit.toLocaleString("ko-KR") + "원";

        [
            [returnRateElement, returnRate],
            [profitElement, profit]
        ].forEach(([element, value]) => {
            element.classList.toggle("value-positive", value > 0);
            element.classList.toggle("value-negative", value < 0);
        });
    };

    const openProfile = async (memberId, trigger) => {
        if (!memberId) return;
        const currentRequest = ++requestSequence;
        lastFocusedElement = trigger || document.activeElement;
        currentTrigger = trigger;
        modalOverlay.hidden = false;
        document.body.classList.add("member-profile-modal-open");
        loading.hidden = false;
        error.hidden = true;
        content.hidden = true;
        positionProfilePopover();
        closeButton.focus();

        try {
            const rankType = trigger?.dataset.profileRankType || "returnRate";
            const query = new URLSearchParams({ rankType });
            const response = await fetch(
                profileApi + encodeURIComponent(memberId) + "?" + query.toString(),
                { headers: { Accept: "application/json" } }
            );
            const payload = await response.json();
            if (!response.ok || !payload.success) {
                throw new Error(payload.message || "프로필을 불러오지 못했습니다.");
            }
            if (currentRequest !== requestSequence) return;
            fillProfile(payload.data);
            loading.hidden = true;
            content.hidden = false;
            requestAnimationFrame(positionProfilePopover);
        } catch (fetchError) {
            if (currentRequest !== requestSequence) return;
            loading.hidden = true;
            error.textContent = fetchError.message || "프로필을 불러오지 못했습니다.";
            error.hidden = false;
            requestAnimationFrame(positionProfilePopover);
        }
    };

    const closeProfile = () => {
        requestSequence += 1;
        modalOverlay.hidden = true;
        document.body.classList.remove("member-profile-modal-open");
        currentProfile = null;
        currentTrigger = null;
        if (lastFocusedElement instanceof HTMLElement) lastFocusedElement.focus();
    };

    followToggle.addEventListener("click", async () => {
        if (!currentProfile || !currentProfile.canFollow || followToggle.disabled) return;

        followToggle.disabled = true;
        try {
            const memberId = encodeURIComponent(currentProfile.memberId);
            const query = new URLSearchParams({ rankType: currentProfile.rankType || "returnRate" });
            const response = await fetch(
                profileApi + memberId + "/follow?" + query.toString(),
                {
                    method: "POST",
                    headers: { Accept: "application/json" }
                }
            );
            const payload = await response.json();
            if (!response.ok || !payload.success) {
                throw new Error(payload.message || "팔로우 상태를 변경하지 못했습니다.");
            }
            fillProfile(payload.data);
            requestAnimationFrame(positionProfilePopover);
        } catch (followError) {
            followToggle.disabled = false;
            alert(followError.message || "팔로우 상태를 변경하지 못했습니다.");
        }
    });

    document.addEventListener("click", (event) => {
        const trigger = event.target.closest?.("[data-user-profile]");
        if (!trigger || !trigger.dataset.userProfile) return;
        event.preventDefault();
        event.stopPropagation();
        openProfile(trigger.dataset.userProfile, trigger);
    }, true);

    document.addEventListener("keydown", (event) => {
        const trigger = event.target.closest?.("[data-user-profile]");
        if (trigger && (event.key === "Enter" || event.key === " ")) {
            event.preventDefault();
            event.stopPropagation();
            openProfile(trigger.dataset.userProfile, trigger);
            return;
        }
        if (event.key === "Escape" && !modalOverlay.hidden) closeProfile();
    }, true);

    closeButton.addEventListener("click", closeProfile);
    modalOverlay.addEventListener("click", (event) => {
        if (event.target === modalOverlay) closeProfile();
    });
    modal.addEventListener("click", (event) => event.stopPropagation());
    window.addEventListener("resize", positionProfilePopover);
});