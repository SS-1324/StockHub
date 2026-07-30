// 관리자 답변 입력칸의 현재 글자 수를 표시
document.querySelectorAll("[data-reply-input]").forEach(function (textarea) {
    const count = textarea.parentElement.querySelector("[data-reply-count]");

    textarea.addEventListener("input", function () {
        count.textContent = textarea.value.length;
    });
});

// 삭제 전에 영구 삭제 여부를 한 번 더 확인
document.querySelectorAll("[data-inquiry-delete-form]")
    .forEach(function (deleteForm) {
        deleteForm.addEventListener("submit", function (event) {
            const confirmed = window.confirm(
                "이 문의를 DB에서 완전히 삭제하시겠습니까?"
            );

            if (!confirmed) {
                event.preventDefault();
            }
        });
    });
