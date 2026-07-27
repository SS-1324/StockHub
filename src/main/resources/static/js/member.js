// 회원가입 화면에서 사용하는 요소
const profileInput = document.querySelector("#profile-image");
const idButton = document.querySelector("#check-id-btn");
const idInput = document.querySelector("#member-id");
const idResult = document.querySelector("#check-id-result");
const nicknameButton = document.querySelector("#check-nickname-btn");
const nicknameInput = document.querySelector("#nickname");
const nicknameResult = document.querySelector("#check-nickname-result");
const passwordInput = document.querySelector("#member-pwd");
const passwordConfirmInput = document.querySelector("#member-pwd-confirm");
const passwordRuleResult = document.querySelector("#password-rule-result");
const passwordConfirmResult = document.querySelector("#check-pwd-result");
const emailInput = document.querySelector("#email");
const emailLocalInput = document.querySelector("#email-local");
const emailDomainInput = document.querySelector("#email-domain");
const emailDomainSelect = document.querySelector("#email-domain-select");
const emailResult = document.querySelector("#email-result");
const emailSendButton = document.querySelector("#send-email-code-btn");
const emailCodeArea = document.querySelector("#email-code-area");
const emailCodeInput = document.querySelector("#email-code");
const emailVerifyButton = document.querySelector("#verify-email-code-btn");
const emailCodeResult = document.querySelector("#email-code-result");
const joinForm = document.querySelector("#join-form");
const contextPath = joinForm.dataset.contextPath || "";

// 아이디·닉네임·비밀번호·이메일 입력 규칙
const idPattern = /^[A-Za-z0-9]{6,50}$/;
const nicknamePattern = /^[가-힣A-Za-z0-9]{2,10}$/;
const passwordPattern =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])[\x21-\x7E]{10,100}$/;
const emailLocalPattern = /^(?=.*[a-z])[a-z0-9]{1,50}$/;
const emailDomainPattern = /^[A-Za-z]+(?:\.com|\.co\.kr|\.net)$/;

// 중복확인과 비밀번호 검사 상태
let checkedId = null;
let checkedNickname = null;
let passwordRulePassed = false;
let passwordMatched = false;
let verificationRequestEmail = null;
let verifiedEmail = null;

// 선택한 프로필 이미지를 화면에 미리 표시
profileInput.addEventListener("change", function (e) {
    // 선택한 첫 번째 파일을 가져옴
    const file = e.target.files[0];
    if (!file) {
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

// 중복확인 버튼을 누르면 서버에 아이디를 확인
idButton.addEventListener("click", async function () {
    // 입력값의 앞뒤 공백을 제거
    const memberId = idInput.value.trim();

    // 입력 규칙이 맞지 않으면 서버에 요청하지 않음
    if (!idPattern.test(memberId)) {
        showResult(
            idResult,
            "영문·숫자로 6자 이상 입력해주세요.",
            false
        );
        checkedId = null;
        return;
    }

    try {
        // 서버의 아이디 중복확인 주소를 호출
        const response = await fetch(
            `${contextPath}/member/checkId?memberId=${encodeURIComponent(memberId)}`,
            {headers: {"X-Requested-With": "XMLHttpRequest"}}
        );
        const result = await response.json();
        const duplicate = result.data;

        // 결과 메시지와 확인된 아이디를 저장
        showResult(idResult, result.message, !duplicate);
        checkedId = duplicate ? null : memberId;
    } catch (e) {
        // 통신 실패 메시지를 표시
        showResult(idResult, "중복확인 중 오류가 발생했습니다.", false);
        checkedId = null;
    }
});

// 중복확인 버튼을 누르면 서버에 닉네임을 확인
nicknameButton.addEventListener("click", async function () {
    // 입력값의 앞뒤 공백을 제거
    const nickname = nicknameInput.value.trim();

    // 입력 규칙이 맞지 않으면 서버에 요청하지 않음
    if (!nicknamePattern.test(nickname)) {
        showResult(
            nicknameResult,
            "특수문자 없이 2자 이상 10자 이하로 입력해주세요.",
            false
        );
        checkedNickname = null;
        return;
    }

    try {
        // 서버의 닉네임 중복확인 주소를 호출
        const response = await fetch(
            `${contextPath}/member/checkNickname?nickname=${encodeURIComponent(nickname)}`,
            {headers: {"X-Requested-With": "XMLHttpRequest"}}
        );
        const result = await response.json();
        const duplicate = result.data;

        // 결과 메시지와 확인된 닉네임을 저장
        showResult(nicknameResult, result.message, !duplicate);
        checkedNickname = duplicate ? null : nickname;
    } catch (e) {
        // 통신 실패 메시지를 표시
        showResult(nicknameResult, "중복확인 중 오류가 발생했습니다.", false);
        checkedNickname = null;
    }
});

// 아이디가 바뀌면 기존 중복확인을 취소
idInput.addEventListener("input", function () {
    checkedId = null;
    idResult.textContent = "";
});

// 닉네임이 바뀌면 기존 중복확인을 취소
nicknameInput.addEventListener("input", function () {
    checkedNickname = null;
    nicknameResult.textContent = "";
});

// 비밀번호 입력이 바뀔 때마다 규칙과 일치 여부를 확인
passwordInput.addEventListener("input", checkPassword);
passwordConfirmInput.addEventListener("input", checkPassword);

// 이메일 값이 바뀌면 이전 발송·인증 상태를 취소
emailLocalInput.addEventListener("input", resetEmailVerification);
emailDomainInput.addEventListener("input", resetEmailVerification);

// 목록에서 도메인을 선택하면 직접 입력란에 반영하고 수정 여부를 전환
emailDomainSelect.addEventListener("change", function () {
    const selectedDomain = emailDomainSelect.value;

    if (selectedDomain) {
        emailDomainInput.value = selectedDomain;
        emailDomainInput.readOnly = true;
        emailDomainInput.classList.add("input-readonly");
    } else {
        emailDomainInput.value = "";
        emailDomainInput.readOnly = false;
        emailDomainInput.classList.remove("input-readonly");
        emailDomainInput.focus();
    }

    resetEmailVerification();
});

// 인증 버튼을 누르면 외부 메일 없이 개발용 코드를 생성
emailSendButton.addEventListener("click", async function () {
    if (!checkEmail()) {
        return;
    }

    const fullEmail = getFullEmail();
    const wasVerified = verifiedEmail === fullEmail;
    emailSendButton.disabled = true;

    try {
        const response = await fetch(`${contextPath}/member/email/send`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: new URLSearchParams({email: fullEmail})
        });
        const result = await response.json();

        if (!result.success) {
            showResult(
                emailResult,
                result.message || "개발용 인증코드 생성에 실패했습니다.",
                false
            );
            return;
        }

        // 새 코드를 받으면 이전 인증 완료 상태를 취소하고 다시 입력할 수 있게 초기화
        verificationRequestEmail = fullEmail;
        verifiedEmail = null;
        emailInput.value = fullEmail;
        emailResult.textContent = `개발모드 인증코드: ${result.data}`;
        emailResult.className = "form-tip form-tip-info";
        emailCodeArea.hidden = false;
        emailCodeInput.value = "";
        emailCodeInput.readOnly = false;
        emailVerifyButton.disabled = false;

        // 인증 완료 후 재발급한 경우 새 코드를 다시 확인해야 함을 표시
        if (wasVerified) {
            showResult(emailCodeResult, "코드를 다시 확인해주세요.", false);
        } else {
            emailCodeResult.textContent = "";
            emailCodeResult.className = "form-tip";
        }

        emailCodeInput.focus();
    } catch (e) {
        showResult(emailResult, "개발용 인증코드 생성 중 오류가 발생했습니다.", false);
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

// 확인 버튼을 누르면 서버에서 인증 코드 일치 여부를 검사
emailVerifyButton.addEventListener("click", async function () {
    const fullEmail = getFullEmail();
    const code = emailCodeInput.value.trim();

    if (verificationRequestEmail !== fullEmail || !/^[0-9]{6}$/.test(code)) {
        showResult(emailCodeResult, "코드를 다시 확인해주세요.", false);
        return;
    }

    emailVerifyButton.disabled = true;

    try {
        const response = await fetch(`${contextPath}/member/email/verify`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: new URLSearchParams({email: fullEmail, code: code})
        });
        const result = await response.json();

        if (result.success && result.data === true) {
            verifiedEmail = fullEmail;
            emailInput.value = fullEmail;
            emailCodeInput.readOnly = true;
            showResult(emailCodeResult, "인증되었습니다.", true);
        } else {
            verifiedEmail = null;
            showResult(emailCodeResult, "코드를 다시 확인해주세요.", false);
        }
    } catch (e) {
        verifiedEmail = null;
        showResult(emailCodeResult, "코드를 다시 확인해주세요.", false);
    } finally {
        emailVerifyButton.disabled = verifiedEmail === fullEmail;
    }
});

// 가입 전 모든 입력 규칙과 중복확인 상태를 검사
joinForm.addEventListener("submit", function (e) {
    // 아이디 형식이 올바르지 않으면 전송 중단
    if (!idPattern.test(idInput.value.trim())) {
        e.preventDefault();
        alert("아이디 입력 규칙을 확인해주세요.");
        idInput.focus();
        return;
    }

    // 현재 아이디가 중복확인되지 않았으면 전송 중단
    if (checkedId !== idInput.value.trim()) {
        e.preventDefault();
        alert("아이디 중복확인을 진행해주세요.");
        return;
    }

    // 닉네임 형식이 올바르지 않으면 전송 중단
    if (!nicknamePattern.test(nicknameInput.value.trim())) {
        e.preventDefault();
        alert("닉네임 입력 규칙을 확인해주세요.");
        nicknameInput.focus();
        return;
    }

    // 현재 닉네임이 중복확인되지 않았으면 전송 중단
    if (checkedNickname !== nicknameInput.value.trim()) {
        e.preventDefault();
        alert("닉네임 중복확인을 진행해주세요.");
        return;
    }

    // 이메일 앞부분과 도메인이 지정한 형식에 맞는지 검사
    if (!checkEmail()) {
        e.preventDefault();
        alert("이메일 입력 형식을 확인해주세요.");

        if (!emailLocalPattern.test(emailLocalInput.value.trim())) {
            emailLocalInput.focus();
        } else {
            emailDomainInput.focus();
        }
        return;
    }

    // 나누어 입력한 이메일을 서버로 보낼 하나의 값으로 합침
    emailInput.value = getFullEmail();

    // 현재 입력한 이메일의 인증이 끝나지 않았으면 전송 중단
    if (verifiedEmail !== emailInput.value) {
        e.preventDefault();
        alert("이메일 인증을 완료해주세요.");
        emailSendButton.focus();
        return;
    }

    // 비밀번호가 필수 조합에 맞지 않으면 전송 중단
    if (!passwordRulePassed) {
        e.preventDefault();
        alert("비밀번호 입력 규칙을 확인해주세요.");
        passwordInput.focus();
        return;
    }

    // 비밀번호가 서로 다르면 전송 중단
    if (!passwordMatched) {
        e.preventDefault();
        alert("비밀번호가 일치하지 않습니다.");
        passwordConfirmInput.focus();
    }
});

// 이메일 앞부분과 도메인의 형식을 검사하고 결과를 표시
function checkEmail() {
    const local = emailLocalInput.value.trim();
    const domain = emailDomainInput.value.trim().toLowerCase();
    const fullEmail = `${local}@${domain}`;
    const localPassed = emailLocalPattern.test(local);
    const domainPassed = emailDomainPattern.test(domain);

    if (!local && !domain) {
        emailResult.textContent = "";
        emailResult.className = "form-tip";
        return false;
    }

    if (!localPassed) {
        showResult(
            emailResult,
            "이메일 앞부분은 소문자 또는 소문자와 숫자로 입력해주세요.",
            false
        );
        return false;
    }

    if (!domainPassed) {
        showResult(
            emailResult,
            "도메인은 영문 + .com, .co.kr 또는 .net 형식으로 입력해주세요.",
            false
        );
        return false;
    }

    if (fullEmail.length > 100) {
        showResult(emailResult, "이메일은 전체 100자 이하로 입력해주세요.", false);
        return false;
    }

    if (verificationRequestEmail !== fullEmail) {
        emailResult.textContent = "";
        emailResult.className = "form-tip";
    }
    return true;
}

// 화면에서 나누어 입력한 이메일을 하나의 문자열로 합침
function getFullEmail() {
    const local = emailLocalInput.value.trim();
    const domain = emailDomainInput.value.trim().toLowerCase();
    return `${local}@${domain}`;
}

// 이메일이 수정되면 이전 코드와 인증 결과를 모두 초기화
function resetEmailVerification() {
    verificationRequestEmail = null;
    verifiedEmail = null;
    emailInput.value = "";
    emailResult.textContent = "";
    emailResult.className = "form-tip";
    emailCodeInput.value = "";
    emailCodeInput.readOnly = false;
    emailVerifyButton.disabled = false;
    emailCodeResult.textContent = "";
    emailCodeResult.className = "form-tip";
    emailCodeArea.hidden = true;
}

// 비밀번호 규칙과 확인 값의 상태를 표시
function checkPassword() {
    const password = passwordInput.value;
    const passwordConfirm = passwordConfirmInput.value;

    // 비밀번호 필수 조합을 검사
    passwordRulePassed = passwordPattern.test(password);
    passwordRuleResult.textContent = passwordRulePassed
        ? "사용 가능한 비밀번호입니다."
        : "한글 없이 대문자·소문자·숫자·특수문자를 포함한 10자 이상이 필요합니다.";
    passwordRuleResult.className = passwordRulePassed
        ? "form-tip form-tip-ok"
        : "form-tip form-tip-error";

    // 확인 값이 없으면 일치 메시지를 비움
    if (!passwordConfirm) {
        passwordMatched = false;
        passwordConfirmResult.textContent = "";
        return;
    }

    // 두 비밀번호가 같은지 검사
    passwordMatched = password === passwordConfirm;
    passwordConfirmResult.textContent = passwordMatched
        ? "비밀번호가 일치합니다."
        : "비밀번호가 일치하지 않습니다.";
    passwordConfirmResult.className = passwordMatched
        ? "form-tip form-tip-ok"
        : "form-tip form-tip-error";
}

// 검사 메시지의 내용과 색상을 변경
function showResult(element, message, ok) {
    element.textContent = message;
    element.className = ok
        ? "form-tip form-tip-ok"
        : "form-tip form-tip-error";
}
