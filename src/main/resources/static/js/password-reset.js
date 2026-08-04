// 비밀번호 재설정 화면에서 사용할 요소
const resetForm = document.querySelector("#password-reset-form");
const emailLocalInput = document.querySelector("#reset-email-local");
const emailDomainInput = document.querySelector("#reset-email-domain");
const emailDomainSelect = document.querySelector("#reset-email-domain-select");
const emailSendButton = document.querySelector("#send-reset-code-btn");
const emailResult = document.querySelector("#reset-email-result");
const emailCodeArea = document.querySelector("#reset-code-area");
const emailCodeInput = document.querySelector("#reset-email-code");
const emailVerifyButton = document.querySelector("#verify-reset-code-btn");
const emailCodeResult = document.querySelector("#reset-code-result");
const resetTokenInput = document.querySelector("#reset-token");
const passwordArea = document.querySelector("#reset-password-area");
const passwordInput = document.querySelector("#new-password");
const passwordConfirmInput = document.querySelector("#new-password-confirm");
const passwordRuleResult = document.querySelector("#reset-password-rule-result");
const passwordConfirmResult = document.querySelector("#reset-password-confirm-result");
const contextPath = resetForm.dataset.contextPath || "";

// 회원가입과 동일한 이메일·비밀번호 입력 규칙
const emailLocalPattern = /^(?=.*[a-z])[a-z0-9]{1,50}$/;
const emailDomainPattern = /^[A-Za-z]+(?:\.com|\.co\.kr|\.net)$/;
const passwordPattern =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])[\x21-\x7E]{10,100}$/;

let verificationRequestEmail = null;
let verifiedEmail = null;

// 직접 입력과 도메인 목록 선택을 전환
emailDomainSelect.addEventListener("change", function () {
    if (emailDomainSelect.value) {
        emailDomainInput.value = emailDomainSelect.value;
        emailDomainInput.readOnly = true;
        emailDomainInput.classList.add("input-readonly");
    } else {
        emailDomainInput.value = "";
        emailDomainInput.readOnly = false;
        emailDomainInput.classList.remove("input-readonly");
        emailDomainInput.focus();
    }

    resetVerification();
});

// 이메일 값이 바뀌면 이전 인증 결과와 토큰을 초기화
emailLocalInput.addEventListener("input", resetVerification);
emailDomainInput.addEventListener("input", resetVerification);

// 인증 버튼을 누르면 가입된 이메일의 개발용 코드를 생성
emailSendButton.addEventListener("click", async function () {
    if (!checkEmail()) {
        return;
    }

    const fullEmail = getFullEmail();
    emailSendButton.disabled = true;

    try {
        const response = await fetch(
            `${contextPath}/member/password-reset/email/send`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                    "X-Requested-With": "XMLHttpRequest"
                },
                body: new URLSearchParams({email: fullEmail})
            }
        );
        const result = await response.json();

        if (!result.success) {
            showResult(
                emailResult,
                result.message || "인증코드 생성에 실패했습니다.",
                false
            );
            return;
        }

        verificationRequestEmail = fullEmail;
        verifiedEmail = null;
        resetTokenInput.value = "";
        emailResult.textContent = `개발모드 인증코드: ${result.data}`;
        emailResult.className = "form-tip form-tip-info";
        emailCodeArea.hidden = false;
        passwordArea.hidden = true;
        emailCodeInput.value = "";
        emailCodeInput.readOnly = false;
        emailVerifyButton.disabled = false;
        emailCodeResult.textContent = "";
        emailCodeResult.className = "form-tip";
        emailCodeInput.focus();
    } catch (e) {
        showResult(emailResult, "인증코드 생성 중 오류가 발생했습니다.", false);
    } finally {
        emailSendButton.disabled = false;
    }
});

// 인증 코드에는 숫자 6자리까지만 입력
emailCodeInput.addEventListener("input", function () {
    emailCodeInput.value =
        emailCodeInput.value.replace(/[^0-9]/g, "").slice(0, 6);
    emailCodeResult.textContent = "";
    emailCodeResult.className = "form-tip";
});

// 인증 코드가 맞으면 일회성 토큰을 받고 새 비밀번호 영역을 표시
emailVerifyButton.addEventListener("click", async function () {
    const fullEmail = getFullEmail();
    const code = emailCodeInput.value.trim();

    if (verificationRequestEmail !== fullEmail || !/^[0-9]{6}$/.test(code)) {
        showResult(emailCodeResult, "코드를 다시 확인해주세요.", false);
        return;
    }

    emailVerifyButton.disabled = true;

    try {
        const response = await fetch(
            `${contextPath}/member/password-reset/email/verify`,
            {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                    "X-Requested-With": "XMLHttpRequest"
                },
                body: new URLSearchParams({email: fullEmail, code: code})
            }
        );
        const result = await response.json();

        if (!result.success || !result.data) {
            verifiedEmail = null;
            resetTokenInput.value = "";
            passwordArea.hidden = true;
            showResult(
                emailCodeResult,
                result.message || "코드를 다시 확인해주세요.",
                false
            );
            return;
        }

        verifiedEmail = fullEmail;
        resetTokenInput.value = result.data;
        emailCodeInput.readOnly = true;
        emailLocalInput.readOnly = true;
        emailDomainInput.readOnly = true;
        emailDomainSelect.disabled = true;
        emailSendButton.disabled = true;
        passwordArea.hidden = false;
        showResult(emailCodeResult, "인증되었습니다.", true);
        passwordInput.focus();
    } catch (e) {
        verifiedEmail = null;
        resetTokenInput.value = "";
        passwordArea.hidden = true;
        showResult(emailCodeResult, "코드를 다시 확인해주세요.", false);
    } finally {
        emailVerifyButton.disabled = verifiedEmail === fullEmail;
    }
});

// 비밀번호 입력이 바뀔 때마다 형식과 일치 여부를 표시
passwordInput.addEventListener("input", checkPassword);
passwordConfirmInput.addEventListener("input", checkPassword);

// 전송 전에 이메일 인증과 새 비밀번호를 다시 검사
resetForm.addEventListener("submit", function (e) {
    if (verifiedEmail !== getFullEmail() || !resetTokenInput.value) {
        e.preventDefault();
        alert("이메일 인증을 완료해주세요.");
        emailSendButton.focus();
        return;
    }

    if (!passwordPattern.test(passwordInput.value)) {
        e.preventDefault();
        alert("비밀번호 입력 규칙을 확인해주세요.");
        passwordInput.focus();
        return;
    }

    if (passwordInput.value !== passwordConfirmInput.value) {
        e.preventDefault();
        alert("새 비밀번호가 서로 일치하지 않습니다.");
        passwordConfirmInput.focus();
    }
});

// 화면에서 나누어 입력한 이메일 형식을 검사
function checkEmail() {
    const local = emailLocalInput.value.trim();
    const domain = emailDomainInput.value.trim().toLowerCase();
    const fullEmail = `${local}@${domain}`;

    if (!emailLocalPattern.test(local)) {
        showResult(emailResult, "이메일 아이디 형식을 확인해주세요.", false);
        emailLocalInput.focus();
        return false;
    }

    if (!emailDomainPattern.test(domain)) {
        showResult(emailResult, "이메일 도메인 형식을 확인해주세요.", false);
        emailDomainInput.focus();
        return false;
    }

    if (fullEmail.length > 100) {
        showResult(emailResult, "이메일은 전체 100자 이하로 입력해주세요.", false);
        return false;
    }

    return true;
}

// 나누어 입력한 이메일을 하나의 문자열로 합침
function getFullEmail() {
    const local = emailLocalInput.value.trim();
    const domain = emailDomainInput.value.trim().toLowerCase();
    return `${local}@${domain}`;
}

// 이메일 변경 시 이전 인증 코드와 일회성 토큰을 모두 제거
function resetVerification() {
    if (emailLocalInput.readOnly) {
        return;
    }

    verificationRequestEmail = null;
    verifiedEmail = null;
    resetTokenInput.value = "";
    emailResult.textContent = "";
    emailResult.className = "form-tip";
    emailCodeInput.value = "";
    emailCodeInput.readOnly = false;
    emailVerifyButton.disabled = false;
    emailCodeResult.textContent = "";
    emailCodeResult.className = "form-tip";
    emailCodeArea.hidden = true;
    passwordArea.hidden = true;
    passwordInput.value = "";
    passwordConfirmInput.value = "";
    passwordRuleResult.textContent = "";
    passwordConfirmResult.textContent = "";
}

// 새 비밀번호 규칙과 확인 값의 상태를 표시
function checkPassword() {
    const password = passwordInput.value;
    const passwordConfirm = passwordConfirmInput.value;
    const rulePassed = passwordPattern.test(password);

    passwordRuleResult.textContent = rulePassed
        ? "사용 가능한 비밀번호입니다."
        : "한글 없이 대문자·소문자·숫자·특수문자를 포함한 10자 이상이 필요합니다.";
    passwordRuleResult.className = rulePassed
        ? "form-tip form-tip-ok"
        : "form-tip form-tip-error";

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

// 성공과 실패 안내 문구의 색상을 함께 변경
function showResult(element, message, success) {
    element.textContent = message;
    element.className = success
        ? "form-tip form-tip-ok"
        : "form-tip form-tip-error";
}
