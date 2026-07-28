<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%-- 관리자 전용 CSS를 공통 헤더의 head 안에서 불러오도록 전달 --%>
<c:url var="adminCssUrl" value="/css/admin.css" />
<c:set var="pageCssUrl" value="${adminCssUrl}" scope="request" />

<%-- 공통 헤더와 관리자 전용 스타일을 불러옴 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<c:url var="adminJsUrl" value="/js/admin.js" />

<section class="admin-page">
    <h1 class="admin-title">문의 내역</h1>

    <%-- 관리자 답변과 삭제 결과를 안내 --%>
    <c:if test="${not empty adminSuccess}">
        <p class="alert alert-success admin-alert">
            <c:out value="${adminSuccess}"/>
        </p>
    </c:if>
    <c:if test="${not empty adminError}">
        <p class="alert alert-error admin-alert">
            <c:out value="${adminError}"/>
        </p>
    </c:if>

    <%-- 등록된 문의가 없을 때 안내 문구를 표시 --%>
    <c:if test="${empty inquiries}">
        <p class="admin-empty">등록된 문의가 없습니다.</p>
    </c:if>

    <%-- 회원이 보낸 문의를 작고 간단한 버튼 목록으로 표시 --%>
    <div class="admin-inquiry-list">
        <c:forEach var="inquiry" items="${inquiries}">
            <button type="button"
                    class="admin-inquiry-card"
                    data-modal-target="admin-inquiry-${inquiry.inquiryId}">
                <div class="admin-inquiry-summary">
                    <span class="admin-inquiry-number">
                        문의 #<c:out value="${inquiry.inquiryId}"/>
                    </span>
                    <strong><c:out value="${inquiry.title}"/></strong>
                    <small>
                        <c:out value="${empty inquiry.nickname ? inquiry.memberId : inquiry.nickname}"/>
                        · <c:out value="${inquiry.createAtStr}"/>
                    </small>
                </div>

                <c:choose>
                    <c:when test="${inquiry.status eq 'PENDING'}">
                        <span class="inquiry-status status-pending">접수됨.</span>
                    </c:when>
                    <c:otherwise>
                        <span class="inquiry-status status-answered">처리됨.</span>
                    </c:otherwise>
                </c:choose>
            </button>
        </c:forEach>
    </div>
</section>

<%-- 문의 목록을 누르면 답변과 삭제 기능을 모달로 표시 --%>
<c:forEach var="inquiry" items="${inquiries}">
    <div id="admin-inquiry-${inquiry.inquiryId}"
         class="footer-modal-overlay"
         hidden>
        <section class="footer-modal admin-inquiry-modal"
                 role="dialog" aria-modal="true"
                 aria-labelledby="admin-inquiry-title-${inquiry.inquiryId}">
            <div class="footer-modal-header">
                <h2 id="admin-inquiry-title-${inquiry.inquiryId}">
                    문의 #<c:out value="${inquiry.inquiryId}"/>
                </h2>
                <button type="button" class="footer-modal-close"
                        aria-label="문의 상세 창 닫기">×</button>
            </div>

            <div class="footer-modal-content admin-modal-content">
                <div class="admin-modal-title-row">
                    <h3><c:out value="${inquiry.title}"/></h3>
                    <c:choose>
                        <c:when test="${inquiry.status eq 'PENDING'}">
                            <span class="inquiry-status status-pending">접수됨.</span>
                        </c:when>
                        <c:otherwise>
                            <span class="inquiry-status status-answered">처리됨.</span>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="admin-modal-meta">
                    <span>
                        아이디:
                        <c:out value="${empty inquiry.memberId ? '탈퇴 회원' : inquiry.memberId}"/>
                    </span>
                    <span>
                        닉네임:
                        <c:out value="${empty inquiry.nickname ? '-' : inquiry.nickname}"/>
                    </span>
                    <span><c:out value="${inquiry.createAtStr}"/></span>
                </div>

                <div class="admin-question-box">
                    <strong>문의 내용</strong>
                    <p><c:out value="${inquiry.content}"/></p>
                </div>

                <c:choose>
                    <c:when test="${inquiry.status eq 'PENDING'}">
                        <%-- 처리 전 문의에만 답장 입력칸을 표시 --%>
                        <form id="admin-reply-form-${inquiry.inquiryId}"
                              class="admin-reply-form"
                              action="${pageContext.request.contextPath}/admin/inquiry/${inquiry.inquiryId}/reply"
                              method="post">
                            <label for="admin-answer-${inquiry.inquiryId}">
                                답장
                            </label>
                            <textarea id="admin-answer-${inquiry.inquiryId}"
                                      name="answer"
                                      maxlength="500"
                                      data-reply-input
                                      required></textarea>
                            <p class="admin-reply-count">
                                <span data-reply-count>0</span>/500
                            </p>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <%-- 처리된 문의에는 저장된 관리자 답변을 표시 --%>
                        <div class="admin-answer-box">
                            <strong>관리자 답변</strong>
                            <p><c:out value="${inquiry.answer}"/></p>
                            <small>
                                <c:out value="${empty inquiry.answeredByNickname ? inquiry.answeredBy : inquiry.answeredByNickname}"/>
                                · <c:out value="${inquiry.answeredAtStr}"/>
                            </small>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>

            <div class="admin-modal-actions">
                <%-- 삭제하면 해당 문의 행이 DB에서 완전히 제거됨 --%>
                <form action="${pageContext.request.contextPath}/admin/inquiry/${inquiry.inquiryId}/delete"
                      method="post"
                      data-inquiry-delete-form>
                    <button class="btn btn-danger"
                            type="submit">문의 삭제</button>
                </form>

                <c:if test="${inquiry.status eq 'PENDING'}">
                    <button class="btn btn-primary"
                            type="submit"
                            form="admin-reply-form-${inquiry.inquiryId}">
                        답장 보내기
                    </button>
                </c:if>
            </div>
        </section>
    </div>
</c:forEach>

<%-- 관리자 답변 글자 수와 문의 삭제 확인 기능을 불러옴 --%>
<script src="${adminJsUrl}"></script>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
