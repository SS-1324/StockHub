<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<h2 class="page-title">회원가입</h2>

<c:if test="${not empty error}">
    <p class="alert alert-error">${error}</p>
</c:if>

<form id="join-form" class="form form-flex"
      action="/member/join" method="post" enctype="multipart/form-data">

    <div class="form-row form-row-center">
        <div class="profile-preview-wrap">
            <div id="profile-preview-placeholder"
                 class="profile-preview profile-preview-placeholder">사진없음</div>
            <img id="profile-preview" class="profile-preview"
                 alt="프로필 미리보기" style="display:none;">
        </div>

        <label class="file-label">
            프로필 이미지 선택
            <input id="profile-image" name="profileImage"
                   type="file" accept="image/*">
        </label>
    </div>

    <div class="form-row">
        <label for="member-id">아이디</label>
        <div class="input-with-button">
            <input id="member-id" name="memberId"
                   type="text" maxlength="50" required autocomplete="off">
            <button id="check-id-btn" class="btn btn-outline"
                    type="button">중복확인</button>
        </div>
        <p id="check-id-result" class="form-tip"></p>
    </div>

    <div class="form-row">
        <label for="member-pwd">비밀번호</label>
        <input id="member-pwd" name="memberPwd"
               type="password" maxlength="100" required>
    </div>

    <div class="form-row">
        <label for="member-pwd-confirm">비밀번호 확인</label>
        <input id="member-pwd-confirm" type="password"
               maxlength="100" required>
        <p id="check-pwd-result" class="form-tip"></p>
    </div>

    <div class="form-row">
        <label for="member-name">이름</label>
        <input id="member-name" name="memberName"
               type="text" maxlength="50" required>
    </div>

    <div class="form-row">
        <label for="nickname">닉네임</label>
        <input id="nickname" name="nickname"
               type="text" maxlength="50" required>
    </div>

    <div class="form-row">
        <label for="email">이메일</label>
        <input id="email" name="email"
               type="text" maxlength="100" required autocomplete="off">
    </div>

    <div class="form-row">
        <button class="btn btn-primary" type="submit">가입하기</button>
    </div>
</form>

<script src="/js/member.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
