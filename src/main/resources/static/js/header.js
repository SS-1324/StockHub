const root = document.documentElement;

const getSavedTheme = () => {
    try {
        return localStorage.getItem("stockhub-theme");
    } catch {
        return null;
    }
};

const saveTheme = (theme) => {
    try {
        localStorage.setItem("stockhub-theme", theme);
    } catch {
        // 저장할 수 없는 환경에서도 현재 화면의 색상 모드는 정상적으로 변경
    }
};

const savedTheme = getSavedTheme();
const prefersDark = window.matchMedia("(prefers-color-scheme: dark)").matches;
root.dataset.theme = savedTheme || (prefersDark ? "dark" : "light");

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
        const nextTheme = root.dataset.theme === "dark" ? "light" : "dark";
        root.dataset.theme = nextTheme;
        saveTheme(nextTheme);
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
