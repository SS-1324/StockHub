const root = document.documentElement;

const saveTheme = (theme) => {
    try {
        localStorage.setItem("stockhub-theme", theme);
    } catch {
        // 저장할 수 없는 환경에서도 현재 화면의 색상 모드는 정상적으로 변경
    }
};

document.addEventListener("DOMContentLoaded", () => {
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
            requestAnimationFrame(()=>{
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
   커뮤니티·랭킹의 [data-user-profile] 요소가 모두 이 로직을 공유한다.
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

    /* [프로필위치-1]
     * 카드 작성자 영역은 카드 폭 전체를 차지하므로 그 사각형을 사용하면 팝업이 멀리 떨어진다.
     * 실제 프로필 이미지(또는 기본 프로필 원)를 찾아 그 오른쪽 좌표만 위치 기준으로 사용한다.
     */
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
        /*
         * [랭킹프로필클릭-2] 랭킹 팝업은 360px까지만 사용한다.
         * 프로필 오른쪽의 수익률·수익금 숫자를 가리지 않으면서도 팝업 안의
         * 작성글·팔로우·투자 수치는 모두 읽을 수 있는 폭이다.
         */
        const preferredMaxWidth = isRankingProfile ? 360 : 430;
        modal.style.maxWidth = Math.max(280, Math.min(preferredMaxWidth, availableWidth)) + "px";

        /*
         * [프로필위치-3] viewport 고정 좌표가 아니라 문서 좌표를 사용한다.
         * 팝업은 클릭한 프로필 오른쪽에 한 번만 놓이고 스크롤을 따라오지 않는다.
         */
        const left = window.scrollX + triggerRect.right + gap;
        let top = window.scrollY + triggerRect.top;

        /* 커뮤니티 첫 행에서 열어도 카테고리·검색 도구 영역 위로 올라가지 않게 한다. */
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

        /* [프로필랭커프레임-2] 현재 랭킹 기준의 1~3위에 맞춰 팝업 사진에도 금·은·동 프레임을 적용한다. */
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
        /* [프로필간소화-2] 본인·타인 여부와 관계없이 같은 제목과 같은 정보 구조를 사용한다. */
        modalOverlay.querySelector("[data-profile-heading]").textContent = "회원 정보";

        /* [팔로우토글-5] 서버 상태를 기준으로 버튼 문구·색상·접근성 상태를 함께 갱신한다. */
        followToggle.hidden = !profile.canFollow;
        followToggle.disabled = false;
        followToggle.textContent = profile.followingTarget ? "팔로잉" : "팔로우";
        followToggle.setAttribute("aria-pressed", String(profile.followingTarget));
        followToggle.classList.toggle("is-following", profile.followingTarget);

        const badge = modalOverlay.querySelector("[data-profile-badge]");
        /* [프로필권한배지-2]
         * 서버가 ADMIN을 반환하면 관리자 배지를 최우선으로 표시한다.
         * 그 외에는 기존처럼 랭킹 기준과 순위를 함께 보여준다.
         */
        const rankLabel = profile.rankType === "profit" ? "수익금 RANKER" : "수익률 RANKER";
        badge.textContent = profile.badge === "ADMIN"
            ? "ADMIN"
            : (profile.badge === "RANKER"
                ? rankLabel + (profile.rankPosition ? " · " + profile.rankPosition + "위" : "")
                : "USER");
        badge.classList.toggle("is-ranker", profile.badge === "RANKER");
        badge.classList.toggle("is-admin", profile.badge === "ADMIN");

        /* [프로필공개정보-5]
         * 활동 수치는 항상 표시하고 투자정보 비공개 회원은 수익률·수익금 행만 접는다.
         * 별도 안내 영역이 없어져 숨긴 행의 높이만큼 팝업도 자연스럽게 짧아진다.
         */
        publicStats.hidden = false;
        investmentStats.forEach((stat) => {
            stat.hidden = !profile.detailsPublic;
        });
        postsLink.href = profileApi + encodeURIComponent(profile.memberId) + "/posts";
        modalOverlay.querySelector("[data-profile-post-count]").textContent = profile.postCount;
        modalOverlay.querySelector("[data-profile-follower-count]").textContent = profile.followerCount;
        modalOverlay.querySelector("[data-profile-following-count]").textContent = profile.followingCount;

        /* [프로필비공개표시-3] 비공개 회원에게 0원·0%를 만들지 않고 투자 지표 렌더링만 생략한다. */
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

        /* [프로필손익색상-1]
         * 국내 증권 화면 관례에 맞춰 상승(양수)은 빨강, 하락(음수)은 파랑으로 표시한다.
         * 0은 방향성이 없으므로 두 클래스를 모두 제거해 기본 글자색을 사용한다.
         */
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
            /*
             * [프로필순위-5 / 프로필AJAX-1]
             * 랭킹 행에 기록한 data-profile-rank-type을 쿼리스트링으로 보낸다.
             * 커뮤니티 등 기준 속성이 없는 화면은 기존 동작과 같은 returnRate를 사용한다.
             */
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
            /*
             * [팔로우토글-6] 페이지 이동 없이 POST 요청으로 관계를 반전한다.
             * 서버가 반환한 최신 프로필 전체를 다시 그려 팔로워 숫자도 같은 상태로 맞춘다.
             */
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

    /* 캡처 단계에서 처리해 커뮤니티 카드 이동 및 랭킹 아코디언 클릭과 충돌하지 않게 한다. */
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
