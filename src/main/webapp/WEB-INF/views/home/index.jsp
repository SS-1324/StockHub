<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%-- 홈 전용 CSS를 공통 헤더의 head 안에서 불러오도록 전달 --%>
<c:url var="homeCssUrl" value="/css/home.css">
    <c:param name="v" value="15" />
</c:url>
<c:set var="pageCssUrl" value="${homeCssUrl}" scope="request" />

<%-- 홈에서 사용하는 페이지 주소 --%>
<c:url var="dictionaryUrl" value="/dictionary" />
<c:url var="rankingUrl" value="/ranking" />
<c:url var="communityUrl" value="/community" />
<c:url var="defaultProfileUrl" value="/images/common_member.png" />

<%-- 공통 헤더를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<%-- 회원 탈퇴 완료 메시지를 표시 --%>
<c:if test="${not empty withdrawSuccess}">
    <p class="alert alert-success home-alert">회원 탈퇴가 완료되었습니다.</p>
</c:if>

<%-- 문의 등록 성공 메시지를 표시 --%>
<c:if test="${not empty inquirySuccess}">
    <p class="alert alert-success home-alert">문의가 관리자에게 전달되었습니다.</p>
</c:if>

<div class="home-main">
    <%-- 메인 소개 문구 --%>
    <section class="home-hero" aria-labelledby="home-headline">
        <p class="home-live-status">
            <span class="home-live-status-dot" aria-hidden="true"></span>
            실시간 시세 연동 중
        </p>

        <h1 id="home-headline" class="home-headline">
            <span class="home-headline-main">주식, 혼자 보면 숫자지만</span>
            <span class="home-headline-accent">같이 보면 이야기니까.</span>
        </h1>

        <p class="home-hero-description">
            <span class="home-hero-description-line">여러 증권사 시세를 비교하고, 커뮤니티에서 팁을 나누고,</span>
            <span class="home-hero-description-line">검증된 랭커의 거래 내역을 확인하세요.</span>
        </p>
    </section>

    <%-- 거래 허브와 동일한 TradingView 실시간 차트 --%>
    <section class="home-section home-chart-section" aria-labelledby="home-chart-title">
        <div class="home-section-heading">
            <div>
                <h2 id="home-chart-title">주식 거래 허브</h2>
                <p>관심 종목의 실시간 차트를 확인하세요.</p>
            </div>
        </div>

        <div class="home-chart-wrapper">
            <div id="tv-chart-container"></div>
        </div>
    </section>

    <%-- 최신 커뮤니티 게시글 5개를 처음 사용한 3개/2개 방식으로 배치 --%>
    <section class="home-section" aria-labelledby="home-community-title">
        <div class="home-section-heading">
            <div>
                <h2 id="home-community-title">커뮤니티 게시판</h2>
                <p>회원이 되어 여러 회원들과 소통해보세요.</p>
            </div>
            <a href="${communityUrl}">전체 보기 →</a>
        </div>

        <c:choose>
            <c:when test="${not empty latestBoards}">
                <div class="home-community-grid">
                    <c:forEach var="board" items="${latestBoards}">

                        <%-- 홈 게시글 작성자의 현재 수익률 순위에 따라 금·은·동 클래스를 계산 --%>
                        <c:set var="boardRankClass" value="" />
                        <c:choose>
                            <c:when test="${board.rankPosition eq 1}">
                                <c:set var="boardRankClass" value="rank-first" />
                            </c:when>
                            <c:when test="${board.rankPosition eq 2}">
                                <c:set var="boardRankClass" value="rank-second" />
                            </c:when>
                            <c:when test="${board.rankPosition eq 3}">
                                <c:set var="boardRankClass" value="rank-third" />
                            </c:when>
                        </c:choose>

                        <c:set var="hasBoardTitle"
                               value="${not empty board.title and not empty fn:trim(board.title)}" />
                        <c:set var="hasBoardImage" value="${not empty board.imageList}" />
                        <a class="home-community-card ${hasBoardTitle ? '' : 'has-no-title'} ${hasBoardImage ? 'has-image' : 'has-no-image'}"
                           href="${communityUrl}/${board.boardId}">
                            <div class="home-community-author ${boardRankClass}">
                                <c:choose>
                                    <%-- 수익률 1~3위는 홈 랭킹의 프로필 프레임을 재사용 --%>
                                    <c:when test="${not empty boardRankClass}">
                                        <span class="home-ranking-avatar-frame home-community-rank-frame">
                                            <c:choose>
                                                <c:when test="${not empty board.profile}">
                                                    <img class="home-ranking-avatar"
                                                         src="${pageContext.request.contextPath}${board.profile}"
                                                         onerror="this.onerror=null; this.src='${defaultProfileUrl}';"
                                                         alt="${board.nickname} 프로필 이미지">
                                                </c:when>
                                                <c:otherwise>
                                                    <img class="home-ranking-avatar"
                                                         src="${defaultProfileUrl}"
                                                         alt="기본 프로필 이미지">
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                    </c:when>
                                    <%-- 1~3위가 아닌 회원은 기존 프로필 모양을 유지 --%>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${not empty board.profile}">
                                                <img src="${pageContext.request.contextPath}${board.profile}"
                                                     onerror="this.onerror=null; this.src='${defaultProfileUrl}';"
                                                     alt="${board.nickname} 프로필 이미지">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${defaultProfileUrl}"
                                                     alt="기본 프로필 이미지">
                                            </c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                                <span>
                                    <strong>
                                        <c:choose>
                                            <c:when test="${not empty board.nickname}">
                                                <c:out value="${board.nickname}" />
                                            </c:when>
                                            <c:otherwise>탈퇴한 회원</c:otherwise>
                                        </c:choose>
                                    </strong>
                                    <small><c:out value="${board.createAtStr}" /></small>
                                </span>
                            </div>

                            <c:if test="${hasBoardTitle}">
                                <h3 class="home-community-title">
                                    <c:out value="${board.title}" />
                                </h3>
                            </c:if>

                            <div class="home-community-content">
                                <c:choose>
                                    <c:when test="${not empty board.formattedPreviewContent}">
                                        <%-- 서버에서 허용한 태그만 남긴 HTML이므로 굵게·기울임·크기 서식을 유지해 출력 --%><c:out
                                            value="${board.formattedPreviewContent}" escapeXml="false" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:choose>
                                            <c:when test="${not empty board.content}">
                                                <c:out value="${board.content}" />
                                            </c:when>
                                            <c:otherwise>
                                                <span class="home-community-empty-content">내용이 없는 게시글입니다.</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <%-- 긴 본문일 때 stockhub.js가 별도의 마지막 줄로 표시 --%>
                            <span class="home-community-ellipsis" aria-hidden="true">...</span>

                            <c:if test="${hasBoardImage}">
                                <div class="home-community-image">
                                    <img src="${pageContext.request.contextPath}${board.imageList[0].imgPath}"
                                         alt="게시글 첨부 이미지"
                                         onerror="this.onerror=null; this.closest('.home-community-image').remove();"
                                         loading="lazy">
                                    <c:if test="${fn:length(board.imageList) gt 1}">
                                        <span>+${fn:length(board.imageList) - 1}</span>
                                    </c:if>
                                </div>
                            </c:if>

                            <div class="home-community-meta">
                                <span>
                                    <c:choose>
                                        <c:when test="${not empty allowedCategories[board.category]}">
                                            <c:out value="${allowedCategories[board.category]}" />
                                        </c:when>
                                        <c:otherwise><c:out value="${board.category}" /></c:otherwise>
                                    </c:choose>
                                </span>
                                <span>좋아요 <fmt:formatNumber value="${board.likeCount}" pattern="#,##0" /></span>
                                <span>댓글 <fmt:formatNumber value="${board.commentCount}" pattern="#,##0" /></span>
                                <span>조회 <fmt:formatNumber value="${board.count}" pattern="#,##0" /></span>
                            </div>
                        </a>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <p class="home-empty-message">아직 등록된 게시글이 없습니다.</p>
            </c:otherwise>
        </c:choose>
    </section>

    <%-- 전체 투자 랭킹 중 상위 5명 --%>
    <section class="home-section" aria-labelledby="home-ranking-title">
        <div class="home-section-heading">
            <div>
                <h2 id="home-ranking-title">투자 랭킹</h2>
                <p>회원들의 투자 수익률 순위를 확인해보세요.</p>
            </div>
            <a href="${rankingUrl}">전체 보기 →</a>
        </div>

        <div class="home-ranking-card">
            <c:choose>
                <c:when test="${not empty topRankings}">
                    <ol class="home-ranking-list">
                        <c:forEach var="ranking" items="${topRankings}">
                            <%-- 랭킹 페이지와 같은 금·은·동 프로필 테두리를 적용 --%>
                            <c:set var="rankClass" value="" />
                            <c:choose>
                                <c:when test="${ranking.rankPosition eq 1}">
                                    <c:set var="rankClass" value="rank-first" />
                                </c:when>
                                <c:when test="${ranking.rankPosition eq 2}">
                                    <c:set var="rankClass" value="rank-second" />
                                </c:when>
                                <c:when test="${ranking.rankPosition eq 3}">
                                    <c:set var="rankClass" value="rank-third" />
                                </c:when>
                            </c:choose>

                            <li class="${rankClass}">
                                <a class="home-ranking-row" href="${rankingUrl}">
                                    <span class="home-ranking-position"
                                          aria-label="${ranking.rankPosition}위">
                                        <span class="home-ranking-number" aria-hidden="true">
                                            ${ranking.rankPosition}
                                        </span>
                                    </span>

                                    <span class="home-ranking-member">
                                        <span class="home-ranking-avatar-frame">
                                            <c:choose>
                                                <c:when test="${not empty ranking.profile}">
                                                    <img class="home-ranking-avatar"
                                                         src="${pageContext.request.contextPath}${ranking.profile}"
                                                         onerror="this.onerror=null; this.src='${defaultProfileUrl}';"
                                                         alt="프로필 이미지">
                                                </c:when>
                                                <c:otherwise>
                                                    <img class="home-ranking-avatar"
                                                         src="${defaultProfileUrl}"
                                                         alt="기본 프로필 이미지">
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                        <span class="home-ranking-copy">
                                            <strong>
                                                <c:choose>
                                                    <c:when test="${not empty ranking.nickname}">
                                                        <c:out value="${ranking.nickname}" />
                                                    </c:when>
                                                    <c:otherwise>탈퇴한 회원</c:otherwise>
                                                </c:choose>
                                            </strong>
                                            <small>@<c:out value="${ranking.memberId}" /></small>
                                        </span>
                                    </span>

                                    <span class="home-ranking-result ${ranking.returnRate lt 0 ? 'is-negative' : 'is-positive'}">
                                        <strong>
                                            <c:if test="${ranking.returnRate gt 0}">+</c:if><fmt:formatNumber value="${ranking.returnRate}" pattern="#0.00"/>%
                                        </strong>
                                        <small>
                                            <c:if test="${ranking.profit gt 0}">+</c:if><fmt:formatNumber value="${ranking.profit}" pattern="#,##0"/>원
                                        </small>
                                    </span>
                                </a>
                            </li>
                        </c:forEach>
                    </ol>
                </c:when>
                <c:otherwise>
                    <p class="home-empty-message">아직 표시할 투자 랭킹이 없습니다.</p>
                </c:otherwise>
            </c:choose>
        </div>
    </section>

    <%-- 용어사전으로 이동하는 안내 박스 --%>
    <a class="home-dictionary-card" href="${dictionaryUrl}">
        <span class="home-dictionary-text">
            <strong>주식 용어 사전</strong>
            <span>직관적인 설명으로 구성된 알짜배기 주식 용어 사전입니다.</span>
        </span>
        <span class="home-card-arrow" aria-hidden="true">→</span>
    </a>
</div>

<%-- 거래 허브와 동일한 기본 종목·일봉 차트를 메인 화면에 마운트 --%>
<script src="https://s3.tradingview.com/tv.js"></script>
<script>
    const resolvedCode = "AAPL";
    const resolvedPeriod = "day";
</script>
<script src="${pageContext.request.contextPath}/js/tradingview-chart.js"></script>
<script src="${pageContext.request.contextPath}/js/stockhub.js?v=4"></script>

<%-- 공통 푸터를 현재 페이지에 포함 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
