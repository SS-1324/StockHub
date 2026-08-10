<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />
<c:url var="currentProfileUrl"
       value="${empty member.profile ? '/images/common_member.png' : member.profile}" />

<%-- [전체프로필프레임-4] 프로필 수정 화면의 미리보기에도 현재 순위 프레임을 유지한다. --%>
<c:set var="profileEditRankClass" value="" />
<c:choose>
    <c:when test="${headerRankPosition eq 1}"><c:set var="profileEditRankClass" value="rank-first" /></c:when>
    <c:when test="${headerRankPosition eq 2}"><c:set var="profileEditRankClass" value="rank-second" /></c:when>
    <c:when test="${headerRankPosition eq 3}"><c:set var="profileEditRankClass" value="rank-third" /></c:when>
</c:choose>

<h2 class="page-title profile-page-title">프로필 수정</h2>

<%-- 프로필 수정 실패 메시지를 표시 --%>
<c:if test="${not empty error}">
    <p class="alert alert-error profile-alert">${error}</p>
</c:if>

<%-- 프로필 수정 성공 메시지를 표시 --%>
<c:if test="${not empty profileSuccess}">
    <p class="alert alert-success profile-alert">프로필이 수정되었습니다.</p>
</c:if>

<%-- 프로필 이미지 삭제 버튼이 요청할 별도 폼 --%>
<form id="delete-profile-image-form"
      action="${pageContext.request.contextPath}/member/mypage/profile-image/delete"
      method="post"></form>

<c:if test="${not empty profileImageDeleted}">
    <p class="alert alert-success profile-alert">
        프로필 이미지가 삭제되어 기본 이미지로 변경되었습니다.
    </p>
</c:if>

<%-- 변경할 프로필 정보를 서버로 전송 --%>
<form id="profile-form" class="form form-flex profile-form" novalidate
      action="${pageContext.request.contextPath}/member/mypage"
      data-current-profile-url="${currentProfileUrl}"
      data-context-path="${pageContext.request.contextPath}"
      method="post" enctype="multipart/form-data">

    <%-- 현재 프로필 이미지와 새 이미지 선택 영역 --%>
    <div class="form-row form-row-center">
        <div class="profile-preview-wrap account-profile-rank-frame profile-edit-rank-frame ${profileEditRankClass}">
            <c:choose>
                <c:when test="${not empty member.profile}">
                    <img id="profile-preview" class="profile-preview"
                         src="${pageContext.request.contextPath}${member.profile}"
                         onerror="this.onerror=null; this.src='${defaultProfileUrl}';"
                         alt="현재 프로필 이미지">
                    <div id="profile-preview-placeholder"
                         class="profile-preview profile-preview-placeholder"
                         style="display:none;">사진없음</div>
                </c:when>
                <c:otherwise>
                    <img id="profile-preview" class="profile-preview"
                         src="${defaultProfileUrl}"
                         alt="기본 프로필 이미지">
                    <div id="profile-preview-placeholder"
                         class="profile-preview profile-preview-placeholder"
                         style="display:none;">사진없음</div>
                </c:otherwise>
            </c:choose>
        </div>

        <div class="profile-image-actions">
            <label class="file-label">
                프로필 이미지 변경
                <input id="profile-image" name="profileImage"
                       type="file"
                       accept=".jpg,.jpeg,.png,.webp,image/jpeg,image/png,image/webp">
            </label>
            <button id="delete-profile-image-button"
                    class="btn btn-danger btn-profile-image-delete"
                    type="submit"
                    form="delete-profile-image-form">프로필 이미지 삭제</button>
        </div>
        <p class="form-tip">선택하지 않으면 현재 이미지가 유지됩니다.</p>
        <p class="form-tip">3MB 이하의 JPG, PNG, WEBP 파일만 선택할 수 있습니다. GIF 파일은 업로드할 수 없습니다.</p>
    </div>

    <%-- 로그인 아이디는 확인용으로만 표시 --%>
    <div class="form-row">
        <label for="member-id">아이디</label>
        <input id="member-id" class="input-readonly" type="text"
               value="<c:out value="${member.memberId}"/>" readonly>
    </div>

    <%-- 이름은 숫자·공백·특수문자 없이 한글과 영문만 입력 --%>
    <div class="form-row">
        <label for="member-name">이름</label>
        <input id="member-name" name="memberName" type="text"
               maxlength="50" autocomplete="name"
               value="<c:out value="${member.memberName}"/>" required>
        <p id="profile-name-result" class="form-tip"></p>
        <p class="form-tip">숫자·띄어쓰기·특수문자 없이 한글 또는 영문으로 입력해주세요.</p>
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

    <%-- 이메일 형식 오류를 팝업 대신 입력칸 아래에 바로 표시 -->
    <div class="form-row">
        <label for="email">이메일</label>
        <input id="email" name="email" type="text"
               inputmode="email" maxlength="100" autocomplete="email"
               value="<c:out value="${member.email}"/>" required>
        <p id="profile-email-result" class="form-tip"></p>
        <p class="form-tip">
            영문 소문자·숫자를 사용하고 .com, .co.kr, .net으로 끝나도록 입력해주세요.
        </p>
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

    <%-- 내 주식 정보 공개 여부 선택 영역 --%>
    <fieldset class="form-row choice-group">
        <legend>내 주식 정보 공개</legend>
        <p class="form-tip">
            N을 선택하면 다른 사람이 프로필 아이콘을 눌렀을 때 주식 정보가 표시되지 않습니다. 또한, 랭킹보드에도 올라가지 않습니다.
        </p>
        <div class="radio-row">
            <label class="radio-label">
                <input type="radio" name="stockPublic" value="true"
                       <c:if test="${member.stockPublic}">checked</c:if> required>
                Y
            </label>
            <label class="radio-label">
                <input type="radio" name="stockPublic" value="false"
                       <c:if test="${not member.stockPublic}">checked</c:if>>
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
<script src="${pageContext.request.contextPath}/js/profile.js?v=6"></script>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
