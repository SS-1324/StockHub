// 프로필 수정 화면에서 사용할 요소
const profileForm = document.querySelector("#profile-form");
const profileInput = document.querySelector("#profile-image");
const nicknameInput = document.querySelector("#nickname");
const nicknameResult = document.querySelector("#profile-nickname-result");
const passwordInput = document.querySelector("#new-password");
const passwordConfirmInput = document.querySelector("#new-password-confirm");
const passwordRuleResult = document.querySelector("#password-rule-result");
const passwordConfirmResult = document.querySelector("#password-confirm-result");
const brokerageInput = document.querySelector("#brokerage-id");
const accountInput = document.querySelector("#account-no");
const accountResult = document.querySelector("#account-result");
const deleteProfileForm = document.querySelector("#delete-profile-image-form");
const currentProfileUrl = profileForm.dataset.currentProfileUrl;
const contextPath = profileForm.dataset.contextPath || "";

// 닉네임과 프로필 이미지 입력 규칙
const nicknamePattern = /^[가-힣A-Za-z0-9]{2,10}$/;
const passwordPattern =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])[\x21-\x7E]{10,100}$/;
const allowedProfileExtensionPattern = /\.(jpe?g|png|webp)$/i;
const allowedProfileContentTypes = new Set([
    "image/jpeg",
    "image/png",
    "image/webp"
]);
const maxProfileImageSize = 3 * 1024 * 1024;

// 현재 비밀번호와 다른지 확인한 비밀번호와 비동기 요청 상태
let checkedDifferentPassword = null;
let sameAsCurrentPassword = false;
let passwordCheckTimer = null;
let passwordCheckSequence = 0;

// 선택한 프로필 이미지를 화면에 미리 표시
profileInput.addEventListener("change", function (e) {
    // 선택한 첫 번째 파일을 가져옴
    const file = e.target.files[0];
    if (!file) {
        return;
    }

    // 3MB를 초과하는 파일은 선택을 취소
    if (file.size > maxProfileImageSize) {
        alert("프로필 이미지는 3MB 이하의 파일만 선택할 수 있습니다.");
        profileInput.value = "";
        resetProfilePreview();
        return;
    }

    // GIF와 허용되지 않은 이미지 형식은 선택 즉시 취소
    if (!isAllowedProfileImage(file)) {
        alert("JPG, PNG, WEBP 파일만 선택할 수 있습니다. GIF 파일은 업로드할 수 없습니다.");
        profileInput.value = "";
        resetProfilePreview();
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

// 선택한 파일의 확장자와 브라우저가 전달한 형식을 함께 검사
function isAllowedProfileImage(file) {
    return allowedProfileExtensionPattern.test(file.name)
        && allowedProfileContentTypes.has(file.type.toLowerCase());
}

// 잘못된 파일을 선택하면 서버에 저장된 현재 이미지로 되돌림
function resetProfilePreview() {
    const preview = document.querySelector("#profile-preview");
    const placeholder = document.querySelector("#profile-preview-placeholder");

    preview.src = currentProfileUrl;
    preview.style.display = "block";
    placeholder.style.display = "none";
}

// 프로필 이미지 삭제 버튼을 누르면 한 번 더 확인
deleteProfileForm.addEventListener("submit", function (e) {
    if (!confirm("프로필 이미지를 삭제하고 기본 이미지로 변경할까요?")) {
        e.preventDefault();
    }
});

// 비밀번호 입력이 바뀔 때마다 규칙과 일치 여부를 확인
passwordInput.addEventListener("input", checkPassword);
passwordConfirmInput.addEventListener("input", checkPasswordConfirm);

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
    const onlyNumber = originalValue.replace(/[^0-9]/g, "").slice(0, 50);

    // 숫자가 아닌 문자가 제거되었으면 입력 안내를 표시
    if (originalValue !== onlyNumber) {
        accountResult.textContent = "계좌번호는 - 없이 숫자만 입력해주세요.";
        accountResult.className = "form-tip form-tip-error";
    } else {
        accountResult.textContent = "";
    }

    accountInput.value = onlyNumber;
});

// 전송 전에 프로필 이미지·닉네임·계좌 입력값을 다시 확인
profileForm.addEventListener("submit", function (e) {
    // 프로필 이미지가 선택된 경우 허용된 형식인지 다시 확인
    const profileFile = profileInput.files[0];
    if (profileFile && profileFile.size > maxProfileImageSize) {
        e.preventDefault();
        alert("프로필 이미지는 3MB 이하의 파일만 선택할 수 있습니다.");
        profileInput.focus();
        return;
    }

    if (profileFile && !isAllowedProfileImage(profileFile)) {
        e.preventDefault();
        alert("프로필 이미지 형식을 다시 확인해주세요.");
        profileInput.focus();
        return;
    }

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

    // 현재 비밀번호와 같은 비밀번호는 사용할 수 없음
    if (sameAsCurrentPassword) {
        e.preventDefault();
        alert("현재 비밀번호와 동일한 비밀번호는 사용 불가합니다.");
        passwordInput.focus();
        return;
    }

    // 서버에서 현재 비밀번호와 다른지 확인된 값만 전송
    if (passwordInput.value
            && checkedDifferentPassword !== passwordInput.value) {
        e.preventDefault();
        alert("현재 비밀번호와 동일한지 확인이 끝난 뒤 다시 시도해주세요.");
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
    if (hasAccount && !/^[0-9]{1,50}$/.test(accountInput.value)) {
        e.preventDefault();
        alert("계좌번호는 - 없이 숫자만 입력해주세요.");
        accountInput.focus();
    }
});

// 새 비밀번호 규칙과 확인 값의 상태를 표시
function checkPassword() {
    const password = passwordInput.value;

    clearTimeout(passwordCheckTimer);
    passwordCheckSequence += 1;
    checkedDifferentPassword = null;
    sameAsCurrentPassword = false;

    // 두 칸을 비우면 기존 비밀번호 유지 안내를 표시
    if (!password && !passwordConfirmInput.value) {
        passwordRuleResult.textContent = "비워두면 기존 비밀번호가 유지됩니다.";
        passwordRuleResult.className = "form-tip";
        passwordConfirmResult.textContent = "";
        return;
    }

    // 새 비밀번호가 필수 조합에 맞는지 표시
    const rulePassed = passwordPattern.test(password);
    if (!rulePassed) {
        passwordRuleResult.textContent =
            "한글 없이 대문자·소문자·숫자·특수문자를 포함한 10자 이상이 필요합니다.";
        passwordRuleResult.className = "form-tip form-tip-error";
        checkPasswordConfirm();
        return;
    }

    // 입력을 잠시 멈추면 서버에서 현재 비밀번호와 같은지 확인
    passwordRuleResult.textContent = "현재 비밀번호와 동일한지 확인 중입니다.";
    passwordRuleResult.className = "form-tip form-tip-info";
    checkPasswordConfirm();

    const requestSequence = passwordCheckSequence;
    passwordCheckTimer = setTimeout(function () {
        checkCurrentPassword(password, requestSequence);
    }, 300);
}

// 서버에서 현재 비밀번호와 같은지 안전하게 확인
async function checkCurrentPassword(password, requestSequence) {
    try {
        const response = await fetch(
            `${contextPath}/member/mypage/password/current-check`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                    "X-Requested-With": "XMLHttpRequest"
                },
                body: new URLSearchParams({newPassword: password})
            }
        );

        const result = await response.json();

        // 확인 중 입력값이 바뀌었으면 이전 응답을 사용하지 않음
        if (requestSequence !== passwordCheckSequence
                || password !== passwordInput.value) {
            return;
        }

        if (!response.ok || !result.success) {
            passwordRuleResult.textContent =
                result.message || "비밀번호 확인 중 오류가 발생했습니다.";
            passwordRuleResult.className = "form-tip form-tip-error";
            return;
        }

        sameAsCurrentPassword = result.data === true;
        checkedDifferentPassword = sameAsCurrentPassword ? null : password;
        passwordRuleResult.textContent = sameAsCurrentPassword
            ? "현재 비밀번호와 동일한 비밀번호는 사용 불가합니다."
            : "사용 가능한 비밀번호입니다.";
        passwordRuleResult.className = sameAsCurrentPassword
            ? "form-tip form-tip-error"
            : "form-tip form-tip-ok";
    } catch (e) {
        if (requestSequence !== passwordCheckSequence
                || password !== passwordInput.value) {
            return;
        }

        passwordRuleResult.textContent = "비밀번호 확인 중 오류가 발생했습니다.";
        passwordRuleResult.className = "form-tip form-tip-error";
    }
}

// 새 비밀번호와 확인 값이 같은지 표시
function checkPasswordConfirm() {
    const password = passwordInput.value;
    const passwordConfirm = passwordConfirmInput.value;

    // 확인 값이 있을 때 두 비밀번호의 일치 여부를 표시
    if (!passwordConfirm) {
        passwordConfirmResult.textContent = "";
        passwordConfirmResult.className = "form-tip";
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

