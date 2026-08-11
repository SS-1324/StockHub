<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<%-- 공용 header 불러오기 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/dictionary.css">

<main class="glossary-page">
    <section class="glossary">

       <header class="glossary-header">

           <div class="glossary-title-area">
               <h2>주식 용어 사전</h2>

               <p class="glossary-info">
                   직관적인 설명으로 구성된 알짜배기 주식 용어 사전입니다.
               </p>
           </div>

           <%-- 메인 용어 검색창 --%>
              <form class="glossary-search"
              id="glossary-search-form"
              action="${pageContext.request.contextPath}/dictionary"
              data-autocomplete-url="${pageContext.request.contextPath}/dictionary/autocomplete"
              method="get">

               <div class="autocomplete-search">

                   <input type="text"
                          id="glossary-keyword"
                          name="keyword"
                          value="${fn:escapeXml(keyword)}"
                          placeholder="검색"
                          autocomplete="off">

                   <%-- 돋보기 검색 버튼 --%>
                   <button type="submit"
                           class="glossary-search-icon-btn"
                           aria-label="검색">

                       <svg width="16"
                            height="16"
                            viewBox="0 0 24 24"
                            fill="none"
                            xmlns="http://www.w3.org/2000/svg">

                           <circle cx="11"
                                   cy="11"
                                   r="7"
                                   stroke="currentColor"
                                   stroke-width="2"/>

                           <line x1="21"
                                 y1="21"
                                 x2="16.65"
                                 y2="16.65"
                                 stroke="currentColor"
                                 stroke-width="2"
                                 stroke-linecap="round"/>
                       </svg>
                   </button>

                   <div id="autocomplete"
                        class="autocomplete-list">
                   </div>

               </div>

           </form>

       </header>

        <c:choose>

            <%-- 검색어가 없을 때: 기존 카테고리 화면 --%>
            <c:when test="${empty keyword}">

                <div class="category-grid">

                    <%-- 매매와 투자 행동 --%>
                    <c:if test="${visible['trading']}">
                        <c:url var="tradingUrl" value="/dictionary/category/trading"/>

                        <a href="${tradingUrl}"
                           class="category"
                           data-image="<c:url value='/images/dictionary/trading.png'/>"
                           data-alt="trading-image">
                            <span class="category-box">매매와 투자 행동</span>
                        </a>
                    </c:if>

                    <%-- 투자자·자금·손익 관리 --%>
                    <c:if test="${visible['risk-management']}">
                        <c:url var="riskManagementUrl" value="/dictionary/category/risk-management"/>

                        <a href="${riskManagementUrl}"
                           class="category"
                           data-image="<c:url value='/images/dictionary/risk-management.png'/>"
                           data-alt="risk-management-image">
                            <span class="category-box">투자자·자금·손익 관리</span>
                        </a>
                    </c:if>

                    <%-- 상품과 포지션 --%>
                    <c:if test="${visible['position']}">
                        <c:url var="positionUrl" value="/dictionary/category/position"/>

                        <a href="${positionUrl}"
                           class="category"
                           data-image="<c:url value='/images/dictionary/position.png'/>"
                           data-alt="position-image">
                            <span class="category-box">상품과 포지션</span>
                        </a>
                    </c:if>

                    <%-- 시장·지수·주문·거래 제도 --%>
                    <c:if test="${visible['market']}">
                        <c:url var="marketUrl" value="/dictionary/category/market"/>

                        <a href="${marketUrl}"
                           class="category"
                           data-image="<c:url value='/images/dictionary/market.png'/>"
                           data-alt="market-image">
                            <span class="category-box">시장·지수·주문·거래 제도</span>
                        </a>
                    </c:if>

                    <%-- 종목 정보와 기업 분석 --%>
                    <c:if test="${visible['fundamental']}">
                        <c:url var="fundamentalUrl" value="/dictionary/category/fundamental"/>

                        <a href="${fundamentalUrl}"
                           class="category"
                           data-image="<c:url value='/images/dictionary/fundamental.png'/>"
                           data-alt="fundamental-image">
                            <span class="category-box">종목 정보와 기업 분석</span>
                        </a>
                    </c:if>

                    <%-- 차트와 기술적 분석 --%>
                    <c:if test="${visible['chart']}">
                        <c:url var="chartUrl" value="/dictionary/category/chart"/>

                        <a href="${chartUrl}"
                           class="category"
                           data-image="<c:url value='/images/dictionary/chart.png'/>"
                           data-alt="chart-image">
                            <span class="category-box">차트와 기술적 분석</span>
                        </a>
                    </c:if>

                </div>

                <%-- 카테고리 이미지 띄울 공간 --%>
                <div class="category-preview">
                    <img id="category-preview-image" src="" alt="">
                </div>

            </c:when>

            <%-- 검색어가 있을 때: 카테고리를 거치지 않고 실제 용어 표시 --%>
            <c:otherwise>

                <c:choose>

                    <c:when test="${hasResult}">

                        <div class="search-result-info">
                            <strong>‘<c:out value="${keyword}"/>’</strong>
                            검색 결과
                            <c:out value="${fn:length(glossaryList)}"/>개입니다.
                        </div>

                        <div class="glossary-list">
                            <c:forEach var="glossary" items="${glossaryList}">

                                <div class="glossary-item">
                                    <h3 class="glossary-term">
                                        <c:out value="${glossary.term}"/>
                                    </h3>

                                    <p class="glossary-definition">
                                        <c:out value="${glossary.definition}"/>
                                    </p>
                                </div>

                            </c:forEach>
                        </div>

                    </c:when>

                    <c:otherwise>
                        <div class="search-empty">
                            <strong>‘<c:out value="${keyword}"/>’</strong>에 해당하는 용어를 찾지 못했습니다.
                        </div>
                    </c:otherwise>

                </c:choose>

            </c:otherwise>

        </c:choose>

    </section>
</main>

<script src="${pageContext.request.contextPath}/js/dictionary.js"></script>

<%-- 공용 footer 불러오기 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />