// 프로필 수정 화면에서 사용할 요소
const profileForm = document.querySelector("#profile-form");
const profileInput = document.querySelector("#profile-image");
const nicknameInput = document.querySelector("#nickname");
const nicknameResult = document.querySelector("#profile-nickname-result");
const passwordInput = document.querySelector("#new-password");
const passwordConfirmInput = document.querySelector("#new-password-confirm");
const passwordRuleResult = document.querySelector("#password-rule-result");
const passwordConfirmResult = document.querySelector("#password-confirm-result");
const brokerageInput = document.querySelector("#brokerage");
const accountInput = document.querySelector("#account-no");
const accountResult = document.querySelector("#account-result");

// 비밀번호 필수 조합을 검사하는 규칙
const nicknamePattern = /^[가-힣A-Za-z0-9]{2,10}$/;
const passwordPattern =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])[\x21-\x7E]{10,100}$/;

// 선택한 프로필 이미지를 화면에 미리 표시
profileInput.addEventListener("change", function (e) {
    // 선택한 첫 번째 파일을 가져옴
    const file = e.target.files[0];
    if (!file) {
        return;
    }

    // 이미지가 아니면 선택을 취소
    if (!file.type.startsWith("image/")) {
        alert("이미지 파일만 선택할 수 있습니다.");
        profileInput.value = "";
        return;
    }

    // 브라우저에서 파일을 읽는 객체
    const reader = new FileReader();

    // 파일 읽기가 끝나면 미리보기를 표시
    reader.onload = function (event) {
        const preview = document.querySelector("#profile-preview");
        const placeholder = document.querySelector("#profile-preview-placeholder");

        preview.src = event.target.result;
        preview.style.display = "block";
        placeholder.style.display = "none";
    };

    // 이미지를 브라우저용 주소로 읽음
    reader.readAsDataURL(file);
});

// 비밀번호 입력이 바뀔 때마다 규칙과 일치 여부를 확인
passwordInput.addEventListener("input", checkPassword);
passwordConfirmInput.addEventListener("input", checkPassword);

// 닉네임이 바뀔 때마다 입력 형식을 확인
nicknameInput.addEventListener("input", function () {
    const nickname = nicknameInput.value.trim();
    const valid = nicknamePattern.test(nickname);

    nicknameResult.textContent = valid
        ? "사용 가능한 형식입니다."
        : "특수문자 없이 한글·영문·숫자로 2자 이상 10자 이하로 입력해주세요.";
    nicknameResult.className = valid
        ? "form-tip form-tip-ok"
        : "form-tip form-tip-error";
});

// 계좌 입력값에서 숫자가 아닌 문자를 바로 제거
accountInput.addEventListener("input", function () {
    const originalValue = accountInput.value;
    const onlyNumber = originalValue.replace(/[^0-9]/g, "").slice(0, 15);

    // 문자 또는 15자 초과 값이 제거되었으면 입력 안내를 표시
    if (originalValue !== onlyNumber) {
        accountResult.textContent = "계좌번호는 숫자만 최대 15자리까지 입력할 수 있습니다.";
        accountResult.className = "form-tip form-tip-error";
    } else {
        accountResult.textContent = "";
    }

    accountInput.value = onlyNumber;
});

// 전송 전에 비밀번호와 계좌 입력값을 다시 확인
profileForm.addEventListener("submit", function (e) {
    // 닉네임 형식이 올바르지 않으면 전송 중단
    if (!nicknamePattern.test(nicknameInput.value.trim())) {
        e.preventDefault();
        alert("닉네임 입력 규칙을 확인해주세요.");
        nicknameInput.focus();
        return;
    }

    // 새 비밀번호가 규칙에 맞지 않으면 전송 중단
    if (passwordInput.value && !passwordPattern.test(passwordInput.value)) {
        e.preventDefault();
        alert("새 비밀번호의 입력 규칙을 확인해주세요.");
        passwordInput.focus();
        return;
    }

    // 새 비밀번호와 확인 값이 다르면 전송 중단
    if (passwordInput.value !== passwordConfirmInput.value) {
        e.preventDefault();
        alert("변경할 비밀번호가 서로 일치하지 않습니다.");
        passwordConfirmInput.focus();
        return;
    }

    // 증권사와 계좌번호 중 하나만 입력하면 전송 중단
    const hasBrokerage = Boolean(brokerageInput.value);
    const hasAccount = Boolean(accountInput.value);
    if (hasBrokerage !== hasAccount) {
        e.preventDefault();
        alert("증권사와 계좌번호를 함께 입력해주세요.");
        return;
    }

    // 입력한 계좌번호에 숫자 이외 문자가 있으면 전송 중단
    if (hasAccount && !/^[0-9]{1,15}$/.test(accountInput.value)) {
        e.preventDefault();
        alert("계좌번호는 숫자만 최대 15자리까지 입력해주세요.");
        accountInput.focus();
    }
});

// 새 비밀번호 규칙과 확인 값의 상태를 표시
function checkPassword() {
    const password = passwordInput.value;
    const passwordConfirm = passwordConfirmInput.value;

    // 두 칸을 비우면 기존 비밀번호 유지 안내를 표시
    if (!password && !passwordConfirm) {
        passwordRuleResult.textContent = "비워두면 기존 비밀번호가 유지됩니다.";
        passwordRuleResult.className = "form-tip";
        passwordConfirmResult.textContent = "";
        return;
    }

    // 새 비밀번호가 필수 조합에 맞는지 표시
    const rulePassed = passwordPattern.test(password);
    passwordRuleResult.textContent = rulePassed
        ? "사용 가능한 비밀번호입니다."
        : "한글 없이 대문자·소문자·숫자·특수문자를 포함한 10자 이상이 필요합니다.";
    passwordRuleResult.className = rulePassed
        ? "form-tip form-tip-ok"
        : "form-tip form-tip-error";

    // 확인 값이 있을 때 두 비밀번호의 일치 여부를 표시
    if (!passwordConfirm) {
        passwordConfirmResult.textContent = "";
        return;
    }

    const matched = password === passwordConfirm;
    passwordConfirmResult.textContent = matched
        ? "비밀번호가 일치합니다."
        : "비밀번호가 일치하지 않습니다.";
    passwordConfirmResult.className = matched
        ? "form-tip form-tip-ok"
        : "form-tip form-tip-error";
}
