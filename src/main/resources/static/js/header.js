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
    const panel = modalOverlay.querySelector("[data-profile-panel]");
    const inquiryTab = modalOverlay.querySelector("[data-profile-inquiry-tab]");
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
        modal.style.maxWidth = Math.max(280, Math.min(430, availableWidth)) + "px";
        const modalRect = modal.getBoundingClientRect();

        /* [프로필위치-2] 좌측 전환이나 카드 끝 보정을 하지 않고 이미지 바로 오른쪽에 고정한다. */
        const left = triggerRect.right + gap;

        let top = triggerRect.top;
        if (top + modalRect.height > window.innerHeight - margin) {
            top = triggerRect.bottom - modalRect.height;
        }
        top = Math.max(margin, Math.min(top, window.innerHeight - modalRect.height - margin));

        modal.style.left = left + "px";
        modal.style.top = top + "px";
    };

    const assetUrl = (path) => {
        if (!path) return defaultProfile;
        if (/^(https?:|data:)/i.test(path) || path.startsWith(contextPath + "/")) return path;
        return contextPath + (path.startsWith("/") ? path : "/" + path);
    };

    const createTextElement = (tagName, className, text) => {
        const element = document.createElement(tagName);
        if (className) element.className = className;
        element.textContent = text ?? "";
        return element;
    };

    const showMessage = (message) => {
        panel.replaceChildren(createTextElement("p", "member-profile-empty", message));
    };

    const setActiveTab = (tabName) => {
        modalOverlay.querySelectorAll("[data-profile-tab]").forEach((button) => {
            button.classList.toggle("is-active", button.dataset.profileTab === tabName);
        });
    };

    const renderFollowList = (members, emptyMessage) => {
        if (!members.length) {
            showMessage(emptyMessage);
            return;
        }

        const list = document.createElement("ul");
        list.className = "member-profile-list member-profile-follow-list";
        members.forEach((member) => {
            const item = document.createElement("li");
            const button = document.createElement("button");
            button.type = "button";
            button.dataset.userProfile = member.memberId;
            button.className = "member-profile-follow-member";

            const image = document.createElement("img");
            image.src = assetUrl(member.profile);
            image.alt = "";
            image.addEventListener("error", () => {
                image.src = defaultProfile;
            }, { once: true });

            const identity = document.createElement("span");
            identity.append(
                createTextElement("strong", "", member.nickname),
                createTextElement("small", "", "@" + member.memberId)
            );
            button.append(image, identity);
            item.append(button);
            list.append(item);
        });
        panel.replaceChildren(list);
    };

    const renderPanel = (tabName) => {
        if (!currentProfile) return;
        setActiveTab(tabName);
        if (tabName === "followers") {
            renderFollowList(currentProfile.followers, "팔로우한 회원이 없습니다.");
        }
        if (tabName === "following") {
            renderFollowList(currentProfile.following, "팔로잉 중인 회원이 없습니다.");
        }
        requestAnimationFrame(positionProfilePopover);
    };

    const fillProfile = (profile) => {
        currentProfile = profile;
        const avatar = modalOverlay.querySelector("[data-profile-avatar]");
        avatar.src = assetUrl(profile.profile);
        avatar.onerror = () => {
            avatar.onerror = null;
            avatar.src = defaultProfile;
        };
        modalOverlay.querySelector("[data-profile-nickname]").textContent = profile.nickname;
        modalOverlay.querySelector("[data-profile-member-id]").textContent = "@" + profile.memberId;
        modalOverlay.querySelector("[data-profile-heading]").textContent =
            profile.ownProfile ? "내 정보" : "회원 정보";
        modalOverlay.querySelector("[data-profile-edit-link]").hidden = !profile.ownProfile;

        const badge = modalOverlay.querySelector("[data-profile-badge]");
        /* [프로필순위-6] 서버가 계산한 기준을 배지 문구에도 명시하여 두 랭킹을 혼동하지 않게 한다. */
        const rankLabel = profile.rankType === "profit" ? "수익금 RANKER" : "수익률 RANKER";
        badge.textContent = profile.badge === "RANKER"
            ? rankLabel + (profile.rankPosition ? " · " + profile.rankPosition + "위" : "")
            : "USER";
        badge.classList.toggle("is-ranker", profile.badge === "RANKER");

        modalOverlay.querySelector("[data-profile-post-count]").textContent = profile.postCount;
        modalOverlay.querySelector("[data-profile-post-label]").textContent =
            profile.ownProfile ? "내가 쓴 글" : "작성글";
        modalOverlay.querySelector("[data-profile-follower-count]").textContent = profile.followerCount;
        modalOverlay.querySelector("[data-profile-following-count]").textContent = profile.followingCount;
        modalOverlay.querySelector("[data-profile-inquiry-count]").textContent = profile.inquiryCount;
        modalOverlay.querySelector("[data-profile-inquiry-label]").textContent =
            profile.ownProfile ? "내 문의글" : "문의글";
        inquiryTab.hidden = false;
        /* [프로필UI-1] 처음에는 참고 화면처럼 원형 지표까지만 보이고, 선택할 때 목록을 펼친다. */
        panel.hidden = true;
        setActiveTab("");
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

    modalOverlay.querySelectorAll("[data-profile-tab]").forEach((button) => {
        button.addEventListener("click", () => {
            if (button.dataset.profileTab === "posts") {
                /* [프로필게시글-2] 작성글 숫자는 모달 목록 대신 전용 공개 게시글 페이지로 이동한다. */
                const memberId = encodeURIComponent(currentProfile.memberId);
                window.location.href = currentProfile.ownProfile
                    ? contextPath + "/member/stocks/posts"
                    : contextPath + "/member/profile/" + memberId + "/posts";
                return;
            }
            if (button.dataset.profileTab === "inquiries") {
                /* [프로필문의-2] 문의 숫자도 작성글처럼 공개 목록 페이지로 이동한다. */
                const memberId = encodeURIComponent(currentProfile.memberId);
                window.location.href = contextPath + "/member/profile/" + memberId + "/inquiries";
                return;
            }
            panel.hidden = false;
            renderPanel(button.dataset.profileTab);
        });
    });
    closeButton.addEventListener("click", closeProfile);
    modalOverlay.addEventListener("click", (event) => {
        if (event.target === modalOverlay) closeProfile();
    });
    modal.addEventListener("click", (event) => event.stopPropagation());
    window.addEventListener("resize", positionProfilePopover);
    window.addEventListener("scroll", positionProfilePopover, true);
});
