const profileInput = document.querySelector("#profile-image");
const idButton = document.querySelector("#check-id-btn");
const idInput = document.querySelector("#member-id");
const idResult = document.querySelector("#check-id-result");
const pwInput = document.querySelector("#member-pwd");
const pwConfirm = document.querySelector("#member-pwd-confirm");
const pwResult = document.querySelector("#check-pwd-result");
const joinForm = document.querySelector("#join-form");

let checkedId = null;
let checkedPwd = false;

profileInput.addEventListener("change", function (e) {
    const file = e.target.files[0];

    if (!file) {
        return;
    }

    const reader = new FileReader();

    reader.onload = function (e) {
        const preview = document.querySelector("#profile-preview");
        const placeholder = document.querySelector("#profile-preview-placeholder");

        preview.src = e.target.result;
        preview.style.display = "block";
        placeholder.style.display = "none";
    };

    reader.readAsDataURL(file);
});

idButton.addEventListener("click", async function () {
    const memberId = idInput.value.trim();

    if (!memberId) {
        showId("아이디를 입력해주세요.", false);
        checkedId = null;
        return;
    }

    try {
        const response = await fetch(
            `/member/checkId?memberId=${encodeURIComponent(memberId)}`,
            {headers: {"X-Requested-With": "XMLHttpRequest"}}
        );
        const result = await response.json();
        const duplicate = result.data;

        showId(result.message, !duplicate);
        checkedId = duplicate ? null : memberId;
    } catch (e) {
        showId("중복확인 중 오류가 발생했습니다.", false);
        checkedId = null;
    }
});

idInput.addEventListener("input", function () {
    checkedId = null;
    idResult.textContent = "";
});

pwInput.addEventListener("input", checkPwd);
pwConfirm.addEventListener("input", checkPwd);

joinForm.addEventListener("submit", function (e) {
    if (checkedId !== idInput.value.trim()) {
        e.preventDefault();
        alert("아이디 중복확인을 진행해주세요.");
        return;
    }

    if (!checkedPwd) {
        e.preventDefault();
        alert("비밀번호가 일치하지 않습니다.");
    }
});

function checkPwd() {
    if (!pwConfirm.value) {
        checkedPwd = false;
        pwResult.textContent = "";
        return;
    }

    checkedPwd = pwInput.value === pwConfirm.value;
    pwResult.textContent = checkedPwd
        ? "비밀번호가 일치합니다."
        : "비밀번호가 일치하지 않습니다.";
    pwResult.className = checkedPwd
        ? "form-tip form-tip-ok"
        : "form-tip form-tip-error";
}

function showId(message, ok) {
    idResult.textContent = message;
    idResult.className = ok
        ? "form-tip form-tip-ok"
        : "form-tip form-tip-error";
}
