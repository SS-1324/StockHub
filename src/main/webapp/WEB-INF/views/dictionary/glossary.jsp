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
                <h1>주식 용어 사전</h1>

                <p class="glossary-info">
                    주식이 낯설게만 느껴지는 여러분을 위해 준비한
                    직관적이고 쉬운 설명으로 구성된 알짜배기 주식 용어 사전입니다.
                </p>
            </div>

            <%-- 메인 용어 검색창 --%>
            <form class="glossary-search"
                  action="${pageContext.request.contextPath}/dictionary"
                  method="get">

                <input type="search"
                       name="keyword"
                       value="${fn:escapeXml(keyword)}"
                       placeholder="용어를 검색하세요"
                       aria-label="주식 용어 검색">

                <button type="submit">검색</button>

                <c:if test="${not empty keyword}">
                    <a href="${pageContext.request.contextPath}/dictionary"
                       class="search-reset">
                        초기화
                    </a>
                </c:if>
            </form>
        </header>

        <%-- 검색 중인 키워드 안내 --%>
        <c:if test="${not empty keyword and hasResult}">
            <div class="search-result-info">
                <strong>‘<c:out value="${keyword}"/>’</strong>
                용어가 포함된 카테고리입니다.
            </div>
        </c:if>

        <div class="category-grid">

            <%-- 매매와 투자 행동 --%>
            <c:if test="${visible['trading']}">
                <c:url var="tradingUrl" value="/dictionary/category/trading">
                    <c:param name="keyword" value="${keyword}"/>
                </c:url>

                <a href="${tradingUrl}"
                   class="category"
                   data-image="<c:url value='/images/dictionary/trading.png'/>"
                   data-alt="trading-image">
                    <span class="category-box">매매와 투자 행동</span>
                </a>
            </c:if>

            <%-- 투자자·자금·손익 관리 --%>
            <c:if test="${visible['risk-management']}">
                <c:url var="riskManagementUrl" value="/dictionary/category/risk-management">
                    <c:param name="keyword" value="${keyword}"/>
                </c:url>

                <a href="${riskManagementUrl}"
                   class="category"
                   data-image="<c:url value='/images/dictionary/risk-management.png'/>"
                   data-alt="risk-management-image">
                    <span class="category-box">투자자·자금·손익 관리</span>
                </a>
            </c:if>

            <%-- 상품과 포지션 --%>
            <c:if test="${visible['position']}">
                <c:url var="positionUrl" value="/dictionary/category/position">
                    <c:param name="keyword" value="${keyword}"/>
                </c:url>

                <a href="${positionUrl}"
                   class="category"
                   data-image="<c:url value='/images/dictionary/position.png'/>"
                   data-alt="position-image">
                    <span class="category-box">상품과 포지션</span>
                </a>
            </c:if>

            <%-- 시장·지수·주문·거래 제도 --%>
            <c:if test="${visible['market']}">
                <c:url var="marketUrl" value="/dictionary/category/market">
                    <c:param name="keyword" value="${keyword}"/>
                </c:url>

                <a href="${marketUrl}"
                   class="category"
                   data-image="<c:url value='/images/dictionary/market.png'/>"
                   data-alt="market-image">
                    <span class="category-box">시장·지수·주문·거래 제도</span>
                </a>
            </c:if>

            <%-- 종목 정보와 기업 분석 --%>
            <c:if test="${visible['fundamental']}">
                <c:url var="fundamentalUrl" value="/dictionary/category/fundamental">
                    <c:param name="keyword" value="${keyword}"/>
                </c:url>

                <a href="${fundamentalUrl}"
                   class="category"
                   data-image="<c:url value='/images/dictionary/fundamental.png'/>"
                   data-alt="fundamental-image">
                    <span class="category-box">종목 정보와 기업 분석</span>
                </a>
            </c:if>

            <%-- 차트와 기술적 분석 --%>
            <c:if test="${visible['chart']}">
                <c:url var="chartUrl" value="/dictionary/category/chart">
                    <c:param name="keyword" value="${keyword}"/>
                </c:url>

                <a href="${chartUrl}"
                   class="category"
                   data-image="<c:url value='/images/dictionary/chart.png'/>"
                   data-alt="chart-image">
                    <span class="category-box">차트와 기술적 분석</span>
                </a>
            </c:if>

            <%-- 검색 결과가 없을 때 --%>
            <c:if test="${not empty keyword and not hasResult}">
                <div class="search-empty">
                    <strong>‘<c:out value="${keyword}"/>’</strong>에 해당하는 용어를 찾지 못했습니다.
                </div>
            </c:if>

        </div>

        <%-- 카테고리 이미지 띄울 공간 --%>
        <div class="category-preview">
            <img id="category-preview-image" src="" alt="">
        </div>

    </section>
</main>

<script>
    const categories = document.querySelectorAll(".category");
    const previewImage = document.getElementById("category-preview-image");

    categories.forEach(category => {
        category.addEventListener("mouseenter", function () {
            previewImage.src = this.dataset.image;
            previewImage.alt = this.dataset.alt;
            previewImage.classList.add("show");
        });

        category.addEventListener("mouseleave", function () {
            previewImage.classList.remove("show");
        });
    });
</script>

<%-- 공용 footer 불러오기 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />