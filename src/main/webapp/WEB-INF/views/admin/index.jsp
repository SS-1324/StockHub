<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- 관리자 전용 CSS를 공통 헤더의 head 안에서 불러오도록 전달 --%>
<c:url var="adminCssUrl" value="/css/admin.css">
    <c:param name="v" value="9" />
</c:url>
<c:set var="pageCssUrl" value="${adminCssUrl}" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />
<c:url var="adminJsUrl" value="/js/admin.js" />

<section class="admin-page" data-admin-root>
    <header class="admin-page-heading">
        <div>
            <h1 class="admin-title">관리자 페이지</h1>
            <p>회원과 콘텐츠, 문의, 용어사전을 한곳에서 관리합니다.</p>
        </div>
        <span class="admin-current-user">
            <strong><c:out value="${sessionScope.loginMember.nickname}" /></strong>
            관리자
        </span>
    </header>

    <c:if test="${not empty adminSuccess}">
        <p class="alert alert-success admin-alert">
            <c:out value="${adminSuccess}" />
        </p>
    </c:if>
    <c:if test="${not empty adminError}">
        <p class="alert alert-error admin-alert">
            <c:out value="${adminError}" />
        </p>
    </c:if>

    <%-- 일곱 개 관리 영역을 한 페이지 안에서 전환 --%>
    <nav class="admin-tabs" aria-label="관리 영역">
        <button type="button" class="admin-tab is-active"
                data-admin-tab="dashboard">대시보드</button>
        <button type="button" class="admin-tab"
                data-admin-tab="members">회원 관리</button>
        <button type="button" class="admin-tab"
                data-admin-tab="boards">게시글 관리</button>
        <button type="button" class="admin-tab"
                data-admin-tab="comments">댓글 관리</button>
        <button type="button" class="admin-tab"
                data-admin-tab="inquiries">문의 관리</button>
        <button type="button" class="admin-tab"
                data-admin-tab="glossary">용어사전</button>
        <button type="button" class="admin-tab"
                data-admin-tab="logs">관리자 기록</button>
    </nav>

    <%-- 대시보드 --%>
    <section class="admin-panel" data-admin-panel="dashboard">
        <div class="admin-panel-heading">
            <div>
                <h2>대시보드</h2>
                <p>현재 서비스의 핵심 현황입니다.</p>
            </div>
        </div>

        <div class="admin-stats">
            <a class="admin-stat-card"
               href="${pageContext.request.contextPath}/admin?tab=members">
                <div>
                    <small>전체 회원</small>
                    <strong><fmt:formatNumber value="${dashboard.memberCount}" pattern="#,##0" /></strong>
                    <span>명</span>
                </div>
            </a>
            <a class="admin-stat-card"
               href="${pageContext.request.contextPath}/admin?tab=boards">
                <div>
                    <small>전체 게시글</small>
                    <strong><fmt:formatNumber value="${dashboard.boardCount}" pattern="#,##0" /></strong>
                    <span>개</span>
                </div>
            </a>
            <a class="admin-stat-card"
               href="${pageContext.request.contextPath}/admin?tab=comments">
                <div>
                    <small>전체 댓글</small>
                    <strong><fmt:formatNumber value="${dashboard.commentCount}" pattern="#,##0" /></strong>
                    <span>개</span>
                </div>
            </a>
            <a class="admin-stat-card"
               href="${pageContext.request.contextPath}/admin?tab=glossary">
                <div>
                    <small>전체 용어</small>
                    <strong><fmt:formatNumber value="${dashboard.glossaryCount}" pattern="#,##0" /></strong>
                    <span>개</span>
                </div>
            </a>
            <a class="admin-stat-card is-warning"
               href="${pageContext.request.contextPath}/admin?tab=inquiries">
                <div>
                    <small>미답변 문의</small>
                    <strong><fmt:formatNumber value="${dashboard.pendingInquiryCount}" pattern="#,##0" /></strong>
                    <span>건</span>
                </div>
            </a>
        </div>
    </section>

    <%-- 회원 관리 --%>
    <section class="admin-panel" data-admin-panel="members" hidden>
        <div class="admin-panel-heading">
            <div><h2>회원 관리</h2><p>회원 정보, 이용 상태와 권한을 확인합니다.</p></div>
            <span class="admin-count"><c:out value="${fn:length(members)}" />명</span>
        </div>
        <div class="admin-table-card">
            <div class="admin-table-scroll">
                <table class="admin-table admin-action-table admin-member-table">
                    <thead><tr><th>아이디</th><th>닉네임</th><th>이메일</th><th>가입일</th><th>상태</th><th>권한</th><th>작업</th></tr></thead>
                    <tbody>
                    <c:forEach var="member" items="${members}">
                        <tr>
                            <td class="admin-key"><c:out value="${member.memberId}" /></td>
                            <td><c:out value="${member.nickname}" /></td>
                            <td><c:out value="${member.email}" /></td>
                            <td class="admin-nowrap"><c:out value="${member.createAtStr}" /></td>
                            <td>
                                <span class="admin-badge ${member.memberStatus eq 'RESTRICTED' ? 'is-danger' : 'is-success'}">
                                    ${member.memberStatus eq 'RESTRICTED' ? '이용 제한' : '정상'}
                                </span>
                            </td>
                            <td><span class="admin-badge is-blue"><c:out value="${member.memberRole}" /></span></td>
                            <td>
                                <c:choose>
                                    <c:when test="${member.memberId eq sessionScope.loginMember.memberId}">
                                        <span class="admin-self-label">현재 계정</span>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="admin-actions-inline admin-member-actions">
                                            <form action="${pageContext.request.contextPath}/admin/member/${member.memberId}/status" method="post">
                                                <input type="hidden" name="memberStatus"
                                                       value="${member.memberStatus eq 'RESTRICTED' ? 'ACTIVE' : 'RESTRICTED'}">
                                                <button type="submit" class="admin-btn admin-btn-muted">
                                                    ${member.memberStatus eq 'RESTRICTED' ? '제한 해제' : '이용 제한'}
                                                </button>
                                            </form>
                                            <form class="admin-role-form"
                                                  action="${pageContext.request.contextPath}/admin/member/${member.memberId}/role" method="post">
                                                <select name="memberRole" aria-label="회원 권한">
                                                    <option value="USER" ${member.memberRole eq 'USER' ? 'selected' : ''}>USER</option>
                                                    <option value="ADMIN" ${member.memberRole eq 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                                                </select>
                                                <button type="submit" class="admin-btn admin-btn-primary admin-role-submit">변경</button>
                                            </form>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty members}"><tr><td colspan="7" class="admin-empty-cell">회원이 없습니다.</td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </section>

    <%-- 게시글 관리 --%>
    <section class="admin-panel" data-admin-panel="boards" hidden>
        <div class="admin-panel-heading">
            <div><h2>게시글 관리</h2><p>작성자, 제목과 내용을 확인하고 숨김 또는 삭제합니다.</p></div>
            <span class="admin-count"><c:out value="${fn:length(boards)}" />개</span>
        </div>
        <div class="admin-table-card"><div class="admin-table-scroll">
            <table class="admin-table admin-content-table admin-action-table admin-board-table">
                <thead><tr><th>번호</th><th>작성자</th><th>제목</th><th>내용</th><th>작성일</th><th>상태</th><th>작업</th></tr></thead>
                <tbody>
                <c:forEach var="board" items="${boards}">
                    <tr>
                        <td class="admin-key">
                            <a class="admin-content-link"
                               href="${pageContext.request.contextPath}/community/${board.boardId}">#${board.boardId}</a>
                        </td>
                        <td><c:out value="${board.nickname}" /></td>
                        <td>
                            <a class="admin-content-link"
                               href="${pageContext.request.contextPath}/community/${board.boardId}">
                                <span class="admin-clamp"><c:out value="${board.title}" /></span>
                            </a>
                        </td>
                        <td>
                            <a class="admin-content-link"
                               href="${pageContext.request.contextPath}/community/${board.boardId}">
                                <span class="admin-clamp is-wide"><c:out value="${board.content}" /></span>
                            </a>
                        </td>
                        <td class="admin-nowrap"><c:out value="${board.createAtStr}" /></td>
                        <td><span class="admin-badge ${board.hidden ? 'is-danger' : 'is-success'}">${board.hidden ? '숨김' : '공개'}</span></td>
                        <td><div class="admin-actions-inline">
                            <form action="${pageContext.request.contextPath}/admin/board/${board.boardId}/visibility" method="post">
                                <input type="hidden" name="hidden" value="${not board.hidden}">
                                <button type="submit" class="admin-btn admin-btn-muted">${board.hidden ? '복원' : '숨김'}</button>
                            </form>
                            <form action="${pageContext.request.contextPath}/admin/board/${board.boardId}/delete" method="post"
                                  data-confirm-message="게시글과 연결된 댓글·이미지도 함께 삭제됩니다. 계속할까요?">
                                <button type="submit" class="admin-btn admin-btn-danger">삭제</button>
                            </form>
                        </div></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty boards}"><tr><td colspan="7" class="admin-empty-cell">게시글이 없습니다.</td></tr></c:if>
                </tbody>
            </table>
        </div></div>
    </section>

    <%-- 댓글 관리 --%>
    <section class="admin-panel" data-admin-panel="comments" hidden>
        <div class="admin-panel-heading">
            <div><h2>댓글 관리</h2><p>댓글 내용을 확인하고 숨김 또는 삭제합니다.</p></div>
            <span class="admin-count"><c:out value="${fn:length(comments)}" />개</span>
        </div>
        <div class="admin-table-card"><div class="admin-table-scroll">
            <table class="admin-table admin-content-table admin-action-table admin-comment-table">
                <thead><tr><th>번호</th><th>게시글</th><th>작성자</th><th>댓글 내용</th><th>작성일</th><th>상태</th><th>작업</th></tr></thead>
                <tbody>
                <c:forEach var="comment" items="${comments}">
                    <tr>
                        <td class="admin-key">#${comment.commentId}</td>
                        <td>#${comment.boardId}</td>
                        <td><c:out value="${comment.nickname}" /></td>
                        <td><div class="admin-clamp is-wide"><c:out value="${comment.content}" /></div></td>
                        <td class="admin-nowrap"><c:out value="${comment.createAtStr}" /></td>
                        <td><span class="admin-badge ${comment.hidden ? 'is-danger' : 'is-success'}">${comment.hidden ? '숨김' : '공개'}</span></td>
                        <td><div class="admin-actions-inline">
                            <form action="${pageContext.request.contextPath}/admin/comment/${comment.commentId}/visibility" method="post">
                                <input type="hidden" name="hidden" value="${not comment.hidden}">
                                <button type="submit" class="admin-btn admin-btn-muted">${comment.hidden ? '복원' : '숨김'}</button>
                            </form>
                            <form action="${pageContext.request.contextPath}/admin/comment/${comment.commentId}/delete" method="post"
                                  data-confirm-message="이 댓글을 완전히 삭제할까요?">
                                <input type="hidden" name="boardId" value="${comment.boardId}">
                                <button type="submit" class="admin-btn admin-btn-danger">삭제</button>
                            </form>
                        </div></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty comments}"><tr><td colspan="7" class="admin-empty-cell">댓글이 없습니다.</td></tr></c:if>
                </tbody>
            </table>
        </div></div>
    </section>

    <%-- 문의 관리 --%>
    <section class="admin-panel" data-admin-panel="inquiries" hidden>
        <div class="admin-panel-heading">
            <div><h2>문의 관리</h2><p>문의 내용을 확인하고 답변 또는 처리 완료합니다.</p></div>
            <span class="admin-count"><c:out value="${fn:length(inquiries)}" />건</span>
        </div>
        <div class="admin-table-card"><div class="admin-table-scroll">
            <table class="admin-table admin-content-table admin-action-table admin-inquiry-table">
                <thead><tr><th>번호</th><th>작성자</th><th>제목</th><th>문의 내용</th><th>접수일</th><th>상태</th><th>작업</th></tr></thead>
                <tbody>
                <c:forEach var="inquiry" items="${inquiries}">
                    <tr>
                        <td class="admin-key">#${inquiry.inquiryId}</td>
                        <td><c:out value="${empty inquiry.nickname ? inquiry.memberId : inquiry.nickname}" /></td>
                        <td><div class="admin-clamp"><c:out value="${inquiry.title}" /></div></td>
                        <td><div class="admin-clamp is-wide"><c:out value="${inquiry.content}" /></div></td>
                        <td class="admin-nowrap"><c:out value="${inquiry.createAtStr}" /></td>
                        <td>
                            <c:choose>
                                <c:when test="${inquiry.status eq 'PENDING'}"><span class="admin-badge is-warning">접수</span></c:when>
                                <c:when test="${inquiry.status eq 'ANSWERED'}"><span class="admin-badge is-blue">답변 완료</span></c:when>
                                <c:otherwise><span class="admin-badge is-success">처리 완료</span></c:otherwise>
                            </c:choose>
                        </td>
                        <td><button type="button" class="admin-btn admin-btn-primary"
                                    data-modal-target="admin-inquiry-${inquiry.inquiryId}">상세 보기</button></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty inquiries}"><tr><td colspan="7" class="admin-empty-cell">문의가 없습니다.</td></tr></c:if>
                </tbody>
            </table>
        </div></div>
    </section>

    <%-- 용어사전 관리 --%>
    <section class="admin-panel" data-admin-panel="glossary" hidden>
        <div class="admin-panel-heading">
            <div><h2>용어사전 관리</h2><p>용어, 설명과 분류를 추가·수정·삭제합니다.</p></div>
            <span class="admin-count"><c:out value="${fn:length(glossaryTerms)}" />개</span>
        </div>

        <form class="admin-create-form" action="${pageContext.request.contextPath}/admin/glossary/create" method="post">
            <label>용어<input name="term" maxlength="100" required></label>
            <label>분류<input name="category" maxlength="20" placeholder="예: trading" required></label>
            <label class="is-wide">설명<textarea name="definition" maxlength="5000" required></textarea></label>
            <button type="submit" class="admin-btn admin-btn-primary">용어 추가</button>
        </form>

        <div class="admin-table-card"><div class="admin-table-scroll">
            <table class="admin-table admin-edit-table glossary-table admin-action-table admin-glossary-table">
                <thead><tr><th>번호</th><th>용어</th><th>설명</th><th>분류</th><th>작업</th></tr></thead>
                <tbody>
                <c:forEach var="glossary" items="${glossaryTerms}">
                    <c:set var="glossaryFormId" value="glossary-form-${glossary.termId}" />
                    <tr>
                        <td class="admin-key">#${glossary.termId}</td>
                        <td><input form="${glossaryFormId}" name="term" maxlength="100" required value="<c:out value='${glossary.term}' />"></td>
                        <td><textarea form="${glossaryFormId}" name="definition" maxlength="5000" required><c:out value="${glossary.definition}" /></textarea></td>
                        <td><input form="${glossaryFormId}" name="category" maxlength="20" required value="<c:out value='${glossary.category}' />"></td>
                        <td><div class="admin-actions-inline">
                            <form id="${glossaryFormId}" action="${pageContext.request.contextPath}/admin/glossary/${glossary.termId}/update" method="post">
                                <button type="submit" class="admin-btn admin-btn-primary">수정</button>
                            </form>
                            <form action="${pageContext.request.contextPath}/admin/glossary/${glossary.termId}/delete" method="post"
                                  data-confirm-message="이 용어를 삭제할까요?">
                                <button type="submit" class="admin-btn admin-btn-danger">삭제</button>
                            </form>
                        </div></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty glossaryTerms}"><tr><td colspan="5" class="admin-empty-cell">등록된 용어가 없습니다.</td></tr></c:if>
                </tbody>
            </table>
        </div></div>
    </section>

    <%-- 관리자 작업 이력 --%>
    <section class="admin-panel" data-admin-panel="logs" hidden>
        <div class="admin-panel-heading">
            <div><h2>관리자 기록</h2><p>어떤 관리자가 무엇을 처리했는지 확인합니다.</p></div>
            <span class="admin-count">최근 <c:out value="${fn:length(adminLogs)}" />건</span>
        </div>
        <div class="admin-table-card"><div class="admin-table-scroll">
            <table class="admin-table admin-log-table">
                <thead><tr><th>일시</th><th>관리자</th><th>작업</th><th>대상</th><th>상세 내용</th></tr></thead>
                <tbody>
                <c:forEach var="log" items="${adminLogs}">
                    <tr>
                        <td class="admin-nowrap"><c:out value="${log.createAtStr}" /></td>
                        <td><strong><c:out value="${log.adminNickname}" /></strong><small class="admin-subtext">@<c:out value="${log.adminId}" /></small></td>
                        <td><span class="admin-badge is-blue"><c:out value="${log.actionType}" /></span></td>
                        <td><c:out value="${log.targetType}" /> · <c:out value="${log.targetId}" /></td>
                        <td><c:out value="${log.detail}" /></td>
                    </tr>
                </c:forEach>
                <c:if test="${empty adminLogs}"><tr><td colspan="5" class="admin-empty-cell">아직 기록된 관리 작업이 없습니다.</td></tr></c:if>
                </tbody>
            </table>
        </div></div>
    </section>
</section>

<%-- 문의 상세·답변·처리 완료 모달 --%>
<c:forEach var="inquiry" items="${inquiries}">
    <div id="admin-inquiry-${inquiry.inquiryId}" class="footer-modal-overlay" hidden>
        <section class="footer-modal admin-inquiry-modal" role="dialog" aria-modal="true"
                 aria-labelledby="admin-inquiry-title-${inquiry.inquiryId}">
            <div class="footer-modal-header">
                <h2 id="admin-inquiry-title-${inquiry.inquiryId}">문의 #${inquiry.inquiryId}</h2>
                <button type="button" class="footer-modal-close" aria-label="문의 상세 창 닫기">×</button>
            </div>
            <div class="footer-modal-content admin-modal-content">
                <div class="admin-modal-title-row">
                    <h3><c:out value="${inquiry.title}" /></h3>
                    <c:choose>
                        <c:when test="${inquiry.status eq 'PENDING'}"><span class="admin-badge is-warning">접수</span></c:when>
                        <c:when test="${inquiry.status eq 'ANSWERED'}"><span class="admin-badge is-blue">답변 완료</span></c:when>
                        <c:otherwise><span class="admin-badge is-success">처리 완료</span></c:otherwise>
                    </c:choose>
                </div>
                <div class="admin-modal-meta">
                    <span>아이디: <c:out value="${empty inquiry.memberId ? '탈퇴 회원' : inquiry.memberId}" /></span>
                    <span>닉네임: <c:out value="${empty inquiry.nickname ? '-' : inquiry.nickname}" /></span>
                    <span><c:out value="${inquiry.createAtStr}" /></span>
                </div>
                <div class="admin-question-box"><strong>문의 내용</strong><p><c:out value="${inquiry.content}" /></p></div>

                <c:choose>
                    <c:when test="${inquiry.status eq 'PENDING'}">
                        <form id="admin-reply-form-${inquiry.inquiryId}" class="admin-reply-form"
                              action="${pageContext.request.contextPath}/admin/inquiry/${inquiry.inquiryId}/reply" method="post">
                            <label for="admin-answer-${inquiry.inquiryId}">답변</label>
                            <textarea id="admin-answer-${inquiry.inquiryId}" name="answer" maxlength="500" data-reply-input required></textarea>
                            <p class="admin-reply-count"><span data-reply-count>0</span>/500</p>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <div class="admin-answer-box"><strong>관리자 답변</strong><p><c:out value="${inquiry.answer}" /></p>
                            <small><c:out value="${empty inquiry.answeredByNickname ? inquiry.answeredBy : inquiry.answeredByNickname}" /> · <c:out value="${inquiry.answeredAtStr}" /></small>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="admin-modal-actions">
                <form action="${pageContext.request.contextPath}/admin/inquiry/${inquiry.inquiryId}/delete" method="post"
                      data-confirm-message="이 문의를 완전히 삭제할까요?">
                    <button class="admin-btn admin-btn-danger" type="submit">문의 삭제</button>
                </form>
                <c:if test="${inquiry.status eq 'PENDING'}">
                    <button class="admin-btn admin-btn-primary" type="submit" form="admin-reply-form-${inquiry.inquiryId}">답변 저장</button>
                </c:if>
                <c:if test="${inquiry.status eq 'ANSWERED'}">
                    <form action="${pageContext.request.contextPath}/admin/inquiry/${inquiry.inquiryId}/complete" method="post">
                        <button class="admin-btn admin-btn-primary" type="submit">처리 완료</button>
                    </form>
                </c:if>
            </div>
        </section>
    </div>
</c:forEach>

<script src="${adminJsUrl}?v=2"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
