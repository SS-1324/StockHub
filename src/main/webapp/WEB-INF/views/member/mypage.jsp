<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<h2 class="page-title">프로필 수정</h2>

<%-- 프로필 수정 실패 메시지를 표시 --%>
<c:if test="${not empty error}">
    <p class="alert alert-error profile-alert">${error}</p>
</c:if>

<%-- 프로필 수정 성공 메시지를 표시 --%>
<c:if test="${not empty profileSuccess}">
    <p class="alert alert-success profile-alert">프로필이 수정되었습니다.</p>
</c:if>

<%-- 변경할 프로필 정보를 서버로 전송 --%>
<form id="profile-form" class="form form-flex profile-form"
      action="${pageContext.request.contextPath}/member/mypage"
      method="post" enctype="multipart/form-data">

    <%-- 현재 프로필 이미지와 새 이미지 선택 영역 --%>
    <div class="form-row form-row-center">
        <div class="profile-preview-wrap">
            <c:choose>
                <c:when test="${not empty member.profile}">
                    <img id="profile-preview" class="profile-preview"
                         src="${pageContext.request.contextPath}${member.profile}"
                         onerror="this.onerror=null; this.src='${pageContext.request.contextPath}/images/default-profile.svg';"
                         alt="현재 프로필 이미지">
                    <div id="profile-preview-placeholder"
                         class="profile-preview profile-preview-placeholder"
                         style="display:none;">사진없음</div>
                </c:when>
                <c:otherwise>
                    <img id="profile-preview" class="profile-preview"
                         alt="프로필 미리보기" style="display:none;">
                    <div id="profile-preview-placeholder"
                         class="profile-preview profile-preview-placeholder">사진없음</div>
                </c:otherwise>
            </c:choose>
        </div>

        <label class="file-label">
            프로필 이미지 변경
            <input id="profile-image" name="profileImage"
                   type="file" accept=".jpg,.jpeg,.png,.gif,.webp,image/*">
        </label>
        <p class="form-tip">선택하지 않으면 현재 이미지가 유지됩니다.</p>
    </div>

    <%-- 로그인 아이디는 확인용으로만 표시 --%>
    <div class="form-row">
        <label for="member-id">아이디</label>
        <input id="member-id" class="input-readonly" type="text"
               value="<c:out value="${member.memberId}"/>" readonly>
    </div>

    <%-- 새 닉네임 입력 영역 --%>
    <div class="form-row">
        <label for="nickname">닉네임</label>
        <input id="nickname" name="nickname" type="text"
               minlength="2" maxlength="10" pattern="[가-힣A-Za-z0-9]{2,10}"
               value="<c:out value="${member.nickname}"/>" required>
        <p id="profile-nickname-result" class="form-tip"></p>
        <p class="form-tip">한글·영문·숫자로 2자 이상 10자 이하로 입력해주세요. 특수문자는 사용할 수 없습니다.</p>
    </div>

    <%-- 새 비밀번호 입력 영역 --%>
    <div class="form-row">
        <label for="new-password">변경할 비밀번호</label>
        <input id="new-password" name="newPassword"
               type="password" maxlength="100" autocomplete="new-password">
        <p id="password-rule-result" class="form-tip">
            비워두면 기존 비밀번호가 유지됩니다.
        </p>
        <p class="form-tip">
            변경 시 한글 없이 대문자·소문자·숫자·특수문자를 각각 1자 이상 포함하여 10자 이상 입력해주세요.
        </p>
    </div>

    <%-- 새 비밀번호 일치 확인 영역 --%>
    <div class="form-row">
        <label for="new-password-confirm">변경할 비밀번호 확인</label>
        <input id="new-password-confirm" name="newPasswordConfirm"
               type="password" maxlength="100" autocomplete="new-password">
        <p id="password-confirm-result" class="form-tip"></p>
    </div>

    <%-- 프로필 공개 여부 선택 영역 --%>
    <fieldset class="form-row choice-group">
        <legend>프로필 공개 여부</legend>
        <div class="radio-row">
            <label class="radio-label">
                <input type="radio" name="profilePublic" value="true"
                       <c:if test="${member.profilePublic}">checked</c:if> required>
                Y
            </label>
            <label class="radio-label">
                <input type="radio" name="profilePublic" value="false"
                       <c:if test="${not member.profilePublic}">checked</c:if>>
                N
            </label>
        </div>
    </fieldset>

    <%-- 주식 용어 툴팁 사용 여부 선택 영역 --%>
    <fieldset class="form-row choice-group">
        <legend>주식 용어 툴팁 활성화 여부</legend>
        <p class="form-tip">Y를 선택하면 주식 용어에 마우스를 올렸을 때 설명을 표시합니다.</p>
        <div class="radio-row">
            <label class="radio-label">
                <input type="radio" name="wordTooltip" value="true"
                       <c:if test="${member.wordTooltip}">checked</c:if> required>
                Y
            </label>
            <label class="radio-label">
                <input type="radio" name="wordTooltip" value="false"
                       <c:if test="${not member.wordTooltip}">checked</c:if>>
                N
            </label>
        </div>
    </fieldset>

    <%-- 회원 정보에 저장할 증권사를 선택 --%>
    <div class="form-row">
        <label for="brokerage">증권사</label>
        <select id="brokerage" name="brokerage">
            <option value="">증권사를 선택해주세요.</option>
            <option value="스톡증권"
                    <c:if test="${member.brokerage eq '스톡증권'}">selected</c:if>>스톡증권</option>
            <option value="허브증권"
                    <c:if test="${member.brokerage eq '허브증권'}">selected</c:if>>허브증권</option>
            <option value="KH투자증권"
                    <c:if test="${member.brokerage eq 'KH투자증권'}">selected</c:if>>KH투자증권</option>
        </select>
        <p class="form-tip">계좌를 등록할 때만 증권사를 선택해주세요.</p>
    </div>

    <%-- 하이픈 없는 숫자 계좌번호 입력 영역 --%>
    <div class="form-row">
        <label for="account-no">계좌번호</label>
        <input id="account-no" name="accountNo" type="text"
               inputmode="numeric" pattern="[0-9]{1,15}" maxlength="15"
               value="<c:out value="${member.accountNo}"/>"
               placeholder="- 없이 숫자만 입력">
        <p id="account-result" class="form-tip"></p>
    </div>

    <%-- 프로필 수정 내용을 저장 --%>
    <div class="form-row form-row-actions">
        <button class="btn btn-primary" type="submit">수정하기</button>
        <a class="btn btn-outline" href="${pageContext.request.contextPath}/">취소</a>
    </div>
</form>

<%-- 프로필 수정 폼 아래에 버튼으로 표시하는 회원 탈퇴 링크 --%>
<div class="withdraw-link-wrap">
    <a class="btn btn-withdraw withdraw-link"
       href="${pageContext.request.contextPath}/member/withdraw">회원 탈퇴</a>
</div>

<%-- 프로필 이미지 미리보기와 입력 검사를 불러옴 --%>
<script src="${pageContext.request.contextPath}/js/profile.js"></script>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
