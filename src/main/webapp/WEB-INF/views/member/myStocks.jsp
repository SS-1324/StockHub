<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<c:url var="myStocksCssUrl" value="/css/my-stocks.css" />
<c:set var="pageCssUrl" value="${myStocksCssUrl}" scope="request" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />
<c:url var="myPostsUrl" value="/member/stocks/posts" />
<c:url var="profileEditUrl" value="/member/mypage/password-check" />
<c:url var="followersUrl" value="/member/stocks/followers" />
<c:url var="followingUrl" value="/member/stocks/following" />

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<section class="my-info-page" aria-labelledby="my-info-title">
    <h1 id="my-info-title" class="my-info-title">내 정보</h1>

    <div class="my-info-card">
        <div class="my-info-profile-row">
            <c:choose>
                <c:when test="${not empty member.profile}">
                    <img class="my-info-profile-image"
                         src="${pageContext.request.contextPath}${member.profile}"
                         onerror="this.onerror=null; this.src='${defaultProfileUrl}';"
                         alt="프로필 이미지">
                </c:when>
                <c:otherwise>
                    <img class="my-info-profile-image"
                         src="${defaultProfileUrl}"
                         alt="기본 프로필 이미지">
                </c:otherwise>
            </c:choose>

            <div class="my-info-identity">
                <h2><c:out value="${member.nickname}"/></h2>
                <p class="my-info-member-id"><c:out value="${member.memberId}"/></p>
            </div>

            <div class="my-info-profile-actions">
                <span class="my-info-role"><c:out value="${member.memberRole}"/></span>
                <a class="my-info-edit" href="${profileEditUrl}">프로필 수정</a>
            </div>
        </div>

        <nav class="my-info-actions" aria-label="내 활동 메뉴">
            <a class="my-info-action" href="${myPostsUrl}">
                <span>내가 쓴 글</span>
                <strong><c:out value="${myPostCount}"/></strong>
            </a>
            <button class="my-info-action" type="button"
                    data-modal-target="followers-modal"
                    data-load-follow-list="true">
                <span>팔로우</span>
                <strong><c:out value="${followerCount}"/></strong>
            </button>
            <button class="my-info-action" type="button"
                    data-modal-target="following-modal"
                    data-load-follow-list="true">
                <span>팔로잉</span>
                <strong><c:out value="${followingCount}"/></strong>
            </button>
            <button class="my-info-action" type="button"
                    data-modal-target="my-inquiries-modal"
                    data-load-my-inquiries="true">
                <span>내 문의</span>
                <strong><c:out value="${inquiryCount}"/></strong>
            </button>
        </nav>
    </div>
</section>

<%-- 나를 팔로우하는 회원을 페이지 이동 없이 표시 --%>
<div id="followers-modal"
     class="footer-modal-overlay"
     data-list-url="${followersUrl}"
     data-context-path="${pageContext.request.contextPath}"
     hidden>
    <section class="footer-modal follow-list-modal"
             role="dialog" aria-modal="true"
             aria-labelledby="followers-modal-title">
        <div class="footer-modal-header">
            <h2 id="followers-modal-title">팔로우</h2>
            <button type="button" class="footer-modal-close"
                    aria-label="팔로우 목록 닫기">×</button>
        </div>
        <div class="footer-modal-content follow-member-list"
             data-follow-list>
            <p class="follow-list-message">목록을 불러오는 중입니다.</p>
        </div>
    </section>
</div>

<%-- 내가 팔로우하는 회원을 페이지 이동 없이 표시 --%>
<div id="following-modal"
     class="footer-modal-overlay"
     data-list-url="${followingUrl}"
     data-context-path="${pageContext.request.contextPath}"
     hidden>
    <section class="footer-modal follow-list-modal"
             role="dialog" aria-modal="true"
             aria-labelledby="following-modal-title">
        <div class="footer-modal-header">
            <h2 id="following-modal-title">팔로잉</h2>
            <button type="button" class="footer-modal-close"
                    aria-label="팔로잉 목록 닫기">×</button>
        </div>
        <div class="footer-modal-content follow-member-list"
             data-follow-list>
            <p class="follow-list-message">목록을 불러오는 중입니다.</p>
        </div>
    </section>
</div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
