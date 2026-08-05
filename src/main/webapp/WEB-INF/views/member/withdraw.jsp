<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<section class="withdraw-card">
    <h2 class="page-title">회원 탈퇴</h2>

    <%-- 탈퇴 실패 메시지를 표시 --%>
    <c:if test="${not empty error}">
        <p class="alert alert-error withdraw-alert">${error}</p>
    </c:if>

    <%-- 탈퇴 시 삭제되거나 유지되는 정보를 안내 --%>
    <div class="withdraw-warning">
        <strong>탈퇴하기 전에 확인해주세요.</strong>
        <p>회원 정보는 삭제되며 복구할 수 없습니다. 증권사 계좌 연동은 해제되지만, 계좌·보유 종목·거래 내역 자체는 증권사에 남아있어 이후 재가입 시 다시 연동할 수 있습니다.</p>
        <p>작성한 게시글과 댓글은 작성자 정보가 제거된 상태로 남을 수 있습니다.</p>
    </div>

    <%-- 본인 확인 후 회원 탈퇴를 요청 --%>
    <form class="form form-flex withdraw-form"
          action="${pageContext.request.contextPath}/member/withdraw"
          method="post">

        <%-- 현재 비밀번호 입력 영역 --%>
        <div class="form-row">
            <label for="withdraw-password">현재 비밀번호</label>
            <input id="withdraw-password" name="memberPwd"
                   type="password" maxlength="100" required autocomplete="current-password">
        </div>

        <%-- 탈퇴 내용 최종 확인 영역 --%>
        <label class="withdraw-confirm-label">
            <input type="checkbox" name="confirmWithdraw" value="true" required>
            삭제되는 내용을 확인했으며 회원 탈퇴에 동의합니다.
        </label>

        <%-- 탈퇴 실행과 취소 버튼 --%>
        <div class="form-row form-row-actions">
            <button class="btn btn-withdraw" type="submit">회원 탈퇴</button>
            <a class="btn btn-outline"
               href="${pageContext.request.contextPath}/member/mypage">취소</a>
        </div>
    </form>
</section>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
