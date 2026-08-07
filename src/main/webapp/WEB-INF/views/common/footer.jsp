<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 헤더와 같은 StockHub 로고 파일을 푸터에서도 재사용 --%>
<c:url var="footerLogoUrl" value="/images/StockHub_logo_blue.png" />
<c:url var="footerJsUrl" value="/js/footer.js" />
<c:url var="inquiryUrl" value="/inquiry" />
<c:url var="myInquiriesUrl" value="/inquiry/my" />

<%-- header.jsp에서 연 본문 영역을 닫음 --%>
</main>

<%-- 모든 페이지 아래쪽에 공통으로 표시하는 푸터 --%>
<footer class="site-footer">
    <div class="site-footer-inner">
        <div class="site-footer-main">
            <%-- 푸터 왼쪽에 StockHub 로고를 표시 --%>
            <a class="footer-logo" href="${pageContext.request.contextPath}/"
               aria-label="StockHub 홈으로 이동">
                <img src="${footerLogoUrl}" alt="StockHub">
            </a>

            <%-- 푸터 오른쪽에서 각 모달을 여는 버튼 --%>
            <div class="footer-links">
                <button type="button" class="footer-link-button"
                        data-modal-target="inquiry-modal">문의하기</button>
                <button type="button" class="footer-link-button"
                        data-modal-target="terms-modal">이용약관</button>
                <button type="button" class="footer-link-button"
                        data-modal-target="privacy-modal">개인정보처리방침</button>
            </div>
        </div>

        <%-- 위쪽 구분선을 가진 저작권과 투자 주의 문구 --%>
        <p class="footer-copyright">
            © 2026 StockHub. All rights reserved.
            본 서비스는 투자 권유를 목적으로 하지 않습니다.
        </p>
    </div>
</footer>

<%-- 문의 제목과 내용을 입력하는 모달 --%>
<div id="inquiry-modal"
     class="footer-modal-overlay"
     data-open-on-load="${not empty inquiryError}"
     hidden>
    <section class="footer-modal" role="dialog" aria-modal="true"
             aria-labelledby="inquiry-modal-title">
        <div class="footer-modal-header">
            <h2 id="inquiry-modal-title">문의하기</h2>
            <button type="button" class="footer-modal-close"
                    aria-label="문의하기 창 닫기">×</button>
        </div>

        <form id="inquiry-form" class="inquiry-form"
              action="${inquiryUrl}" method="post">
            <div class="footer-modal-content">
                <%-- 서버 검사에 실패한 문의 메시지를 모달 안에 표시 --%>
                <c:if test="${not empty inquiryError}">
                    <p class="alert alert-error inquiry-alert">
                        <c:out value="${inquiryError}"/>
                    </p>
                </c:if>

                <div class="inquiry-field">
                    <label for="inquiry-title">제목</label>
                    <input id="inquiry-title" name="title" type="text"
                           maxlength="20" required
                           value="<c:out value="${inquiryTitle}"/>">
                    <p class="inquiry-count">
                        <span id="inquiry-title-count">0</span>/20
                    </p>
                </div>

                <div class="inquiry-field">
                    <label for="inquiry-content">내용</label>
                    <textarea id="inquiry-content" name="content"
                              maxlength="200" required><c:out value="${inquiryContent}"/></textarea>
                    <p class="inquiry-count">
                        <span id="inquiry-content-count">0</span>/200
                    </p>
                </div>
            </div>

            <%-- 문의 등록 버튼을 모달 오른쪽 아래에 배치 --%>
            <div class="inquiry-actions">
                <button class="btn btn-primary"
                        type="submit">문의하기</button>
            </div>
        </form>
    </section>
</div>

<%-- 로그인 회원 본인의 문의와 관리자 답변을 보여주는 모달 --%>
<c:if test="${not empty sessionScope.loginMember}">
    <div id="my-inquiries-modal"
         class="footer-modal-overlay"
         data-list-url="${myInquiriesUrl}"
         hidden>
        <section class="footer-modal my-inquiries-modal"
                 role="dialog" aria-modal="true"
                 aria-labelledby="my-inquiries-modal-title">
            <div class="footer-modal-header">
                <h2 id="my-inquiries-modal-title">내 문의</h2>
                <button type="button" class="footer-modal-close"
                        aria-label="내 문의 창 닫기">×</button>
            </div>

            <%-- 목록은 모달을 열 때 최신 내용으로 불러옴 --%>
            <div id="my-inquiry-list"
                 class="footer-modal-content my-inquiry-list">
                <p class="my-inquiry-message">
                    문의 내역을 불러오는 중입니다.
                </p>
            </div>
        </section>
    </div>
</c:if>

<%-- 이용약관 내용을 보여주는 모달 --%>
<div id="terms-modal" class="footer-modal-overlay" hidden>
    <section class="footer-modal" role="dialog" aria-modal="true"
             aria-labelledby="terms-modal-title">
        <div class="footer-modal-header">
            <h2 id="terms-modal-title">이용약관</h2>
            <button type="button" class="footer-modal-close"
                    aria-label="이용약관 창 닫기">×</button>
        </div>
        <div class="footer-modal-content footer-policy-content">
            이용약관 넣기
        </div>
    </section>
</div>

<%-- 개인정보처리방침 내용을 보여주는 모달 --%>
<div id="privacy-modal" class="footer-modal-overlay" hidden>
    <section class="footer-modal" role="dialog" aria-modal="true"
             aria-labelledby="privacy-modal-title">
        <div class="footer-modal-header">
            <h2 id="privacy-modal-title">개인정보처리방침</h2>
            <button type="button" class="footer-modal-close"
                    aria-label="개인정보처리방침 창 닫기">×</button>
        </div>
        <div class="footer-modal-content footer-policy-content">
            개인정보처리방침 넣기
        </div>
    </section>
</div>

<%-- 푸터 모달과 문의 글자 수 검사 기능을 불러옴 --%>
<script src="${footerJsUrl}?v=3"></script>
</body>
</html>
