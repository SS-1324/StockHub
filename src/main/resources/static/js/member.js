/* 회원가입 화면 스크립트 */
const profileImageInput = document.querySelector("#profile-image"); //프로필 이미지 파일 태그
const checkIdReult = document.querySelector("#check-id-btn"); //아이디 중복체크 버튼
const memberIdInput = document.querySelector("#member-id"); //아이디 입력창
const checkIdResult = document.querySelector("#check-id-result"); //아이디 상태

//서버에 마지막으로 중복이 아님을 확인받은 아이디값
let checkedMemberId = null;
let checkedPwd = false;

/* 프로필 이미지 미리보기 */
profileImageInput.addEventListener("change", function(ev){
    //업로드한 파일중 첫번째 요소를 가져옴
    const file = ev.target.files[0];
    if(!file){
        return;
    }

    // FileReader - 아직 서버에 업로드하지 않은, 사용자 PC에 있는 파일을
    // 브라우저 메모리에 올리기위해 base64라는 문자열로 만들어주는 js객체
    // base64로 변경해야 img태그의 src속성에 넣어 사용이 가능
    const reader = new FileReader();
    reader.onload = function(ev){
        const profilePreview = document.querySelector("#profile-preview");
        profilePreview.src = ev.target.result;
        profilePreview.style.display = "block";

        const profilePlaceholder = document.querySelector("#profile-preview-placeholder");
        profilePlaceholder.style.display = "none";
    }

    // 업로드한 파일을 base64방식의 데이터URL로 변경.
    reader.readAsDataURL(file);

})

/* 아이디 중복확인 */
checkIdReult.addEventListener("click",async function(){
    const memberId = memberIdInput.value.trim();
    if(memberId.length === 0) {
        checkIdResult.textContent = "아이디를 입력해주세요";
        checkIdResult.className = "form-tip form-tip-error";
        checkedMemberId = null;
        return;
    }
    
    try {
        // encodeURIComponent감싸주는 이유: 아이디에 &, =와같은 요청 url에 영향을 주는 것들을 제거해주는 용도
        const response = await fetch(`/member/checkId?memberId=${encodeURIComponent(memberId)}`,{
                                    method: "GET",
                                    headers: {"X-Request-With": "XMLHtttpRequest"}
                                });

        // response.json() : json응답을 자바스크립트 객체로 변경
        const result = await response.json();
        const isDuplicate = result.data;
        
        checkIdResult.textContent = result.message;
        checkIdResult.className = isDuplicate ? "form-tip form-tip-error" : "form-tip form-tip-ok";

        checkedMemberId = isDuplicate ? null : memberId;
    } catch(err){
        checkIdResult.textContent = "중복확인 중 오류가 발생했습니다.";
        checkIdResult.className = "form-tip form-tip-error";
    }
})

memberIdInput.addEventListener("input", function(){
    checkedMemberId = null;
    checkIdResult.textContent = "";
})

/* 비밀번호 확인 */
const pwInput = document.querySelector("#member-pwd"); //비밀번호 입력창
const pwConfirmInput = document.querySelector("#member-pwd-confirm"); //비밀번호 입력창

function validatePwdConfirm(){
    const pwConfirmResult = document.querySelector("#check-pwd-result");

    //비밀번호 확인창이 비어있다면 검사x
    if(!pwConfirmInput.value.trim()){
        pwConfirmResult.textContent = "";
        checkedPwd = false;
        return;
    }

    checkedPwd = pwInput.value === pwConfirmInput.value;

    pwConfirmResult.textContent = checkedPwd ? "비밀번호가 일치합니다" : "비밀번호가 일치하지 않습니다";
    pwConfirmResult.className = checkedPwd ? "form-tip form-tip-ok" : "form-tip form-tip-error";
}

pwInput.addEventListener("input", validatePwdConfirm);
pwConfirmInput.addEventListener("input", validatePwdConfirm);

/* 회원가입 폼 제출 */
const joinForm = document.querySelector("#join-form");
joinForm.addEventListener("submit", function (ev){
    if(!checkedMemberId){
        ev.preventDefault();
        alert("아이디 중복확인을 진행해주세요");
        return;
    }

    if(!checkedPwd){
        ev.preventDefault();
        alert("비밀번호가 일치하지 않습니다.");
        return;
    }
    // js에서의 검증은 UX관점일 뿐.
    // 우회가 얼마든지 가능하기 때문에 서버에서 재 검증이 필요하다.
    // (아이디 중복o, 비밀번호확인x)
})
