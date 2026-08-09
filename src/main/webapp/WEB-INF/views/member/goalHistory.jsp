<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<c:url var="dashboardCssUrl" value="/css/dashboard.css" />
<c:set var="pageCssUrl" value="${dashboardCssUrl}" scope="request" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<section class="dashboard-page" aria-labelledby="goal-history-title">
    <div class="dashboard-section-heading dashboard-page-heading">
        <h1 id="goal-history-title">목표 히스토리</h1>
        <a class="dashboard-section-link" href="${pageContext.request.contextPath}/member/dashboard">← 대시보드로</a>
    </div>

    <section class="dashboard-holdings">
        <c:choose>
            <c:when test="${not empty goalHistory}">
                <div class="dashboard-table-wrap">
                    <table class="dashboard-table">
                        <thead>
                        <tr>
                            <th scope="col">목표 이름</th>
                            <th scope="col">목표치</th>
                            <th scope="col">기한</th>
                            <th scope="col">설정일</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="goal" items="${goalHistory}">
                            <tr>
                                <td data-label="목표 이름"><strong><c:out value="${goal.title}"/></strong></td>
                                <td data-label="목표치">
                                    <c:choose>
                                        <c:when test="${goal.goalType == 'RETURN_RATE'}">
                                            수익률 <fmt:formatNumber value="${goal.targetValue}" pattern="#,##0.##"/>%
                                        </c:when>
                                        <c:otherwise>
                                            수익금 <fmt:formatNumber value="${goal.targetValue}" pattern="#,##0"/>원
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td data-label="기한"><c:out value="${goal.targetDateText}"/></td>
                                <td data-label="설정일"><c:out value="${goal.createAtText}"/></td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:when>
            <c:otherwise>
                <div class="dashboard-empty">
                    <p>지난 목표가 없습니다.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
