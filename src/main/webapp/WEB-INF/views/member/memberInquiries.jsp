<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="myInfoCssUrl" value="/css/my-stocks.css">
    <c:param name="v" value="3" />
</c:url>
<c:set var="pageCssUrl" value="${myInfoCssUrl}" scope="request" />
<c:url var="communityUrl" value="/community" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<c:set var="inquiryPageTitle" value="내 문의글" />
<c:if test="${not ownProfile}">
    <c:set var="inquiryPageTitle" value="${profileOwner.nickname}님의 문의글" />
</c:if>

<section class="my-posts-page" aria-labelledby="member-inquiries-title">
    <header class="my-posts-header">
        <h1 id="member-inquiries-title"><c:out value="${inquiryPageTitle}"/></h1>
        <span><c:out value="${totalCount}"/>개</span>
    </header>

    <c:choose>
        <c:when test="${not empty inquiryList}">
            <div class="public-inquiry-list">
                <c:forEach var="inquiry" items="${inquiryList}">
                    <article class="public-inquiry-card">
                        <header>
                            <span class="public-inquiry-status ${inquiry.status eq 'ANSWERED' ? 'is-answered' : ''}">
                                ${inquiry.status eq 'ANSWERED' ? '답변 완료' : '접수'}
                            </span>
                            <time><c:out value="${inquiry.createAtStr}"/></time>
                        </header>
                        <h2><c:out value="${inquiry.title}"/></h2>
                        <p class="public-inquiry-content"><c:out value="${inquiry.content}"/></p>

                        <c:choose>
                            <c:when test="${not empty inquiry.answer}">
                                <section class="public-inquiry-answer">
                                    <strong>관리자 답변</strong>
                                    <p><c:out value="${inquiry.answer}"/></p>
                                    <c:if test="${not empty inquiry.answeredAtStr}">
                                        <time><c:out value="${inquiry.answeredAtStr}"/></time>
                                    </c:if>
                                </section>
                            </c:when>
                            <c:otherwise>
                                <p class="public-inquiry-waiting">아직 등록된 답변이 없습니다.</p>
                            </c:otherwise>
                        </c:choose>
                    </article>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <div class="my-posts-empty">
                <p>아직 작성한 문의글이 없습니다.</p>
            </div>
        </c:otherwise>
    </c:choose>

    <a class="my-posts-back" href="${communityUrl}">커뮤니티로 돌아가기</a>
</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
