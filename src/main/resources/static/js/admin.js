const adminRoot = document.querySelector("[data-admin-root]");
const adminTabButtons = document.querySelectorAll("[data-admin-tab]");
const adminPanels = document.querySelectorAll("[data-admin-panel]");

// 주소의 tab 값 또는 선택한 버튼에 맞는 관리 영역만 표시
function activateAdminTab(tabName, updateAddress) {
    const targetPanel = document.querySelector(
        `[data-admin-panel="${tabName}"]`
    );
    if (!targetPanel) {
        tabName = "dashboard";
    }

    adminTabButtons.forEach(function (button) {
        const active = button.dataset.adminTab === tabName;
        button.classList.toggle("is-active", active);
        button.setAttribute("aria-selected", String(active));
    });

    adminPanels.forEach(function (panel) {
        panel.hidden = panel.dataset.adminPanel !== tabName;
    });

    if (updateAddress) {
        const url = new URL(window.location.href);
        if (tabName === "dashboard") {
            url.searchParams.delete("tab");
        } else {
            url.searchParams.set("tab", tabName);
        }
        window.history.replaceState({}, "", url);
    }
}

adminTabButtons.forEach(function (button) {
    button.addEventListener("click", function () {
        activateAdminTab(button.dataset.adminTab, true);
    });
});

if (adminRoot) {
    const initialTab = new URLSearchParams(window.location.search).get("tab")
        || "dashboard";
    activateAdminTab(initialTab, false);
}

// 답변 입력칸의 현재 글자 수를 표시
document.querySelectorAll("[data-reply-input]").forEach(function (textarea) {
    const count = textarea.parentElement.querySelector("[data-reply-count]");

    function updateReplyCount() {
        if (count) {
            count.textContent = textarea.value.length;
        }
    }

    textarea.addEventListener("input", updateReplyCount);
    updateReplyCount();
});

// 영구 삭제처럼 되돌리기 어려운 작업 전에 확인
document.querySelectorAll("form[data-confirm-message]")
    .forEach(function (form) {
        form.addEventListener("submit", function (event) {
            if (!window.confirm(form.dataset.confirmMessage)) {
                event.preventDefault();
            }
        });
    });
