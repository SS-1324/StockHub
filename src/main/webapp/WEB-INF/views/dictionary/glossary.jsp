<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- 공용 header 불러오기 --%>
<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@100..900&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/common.css">
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/dictionary.css">
<main class = "glossary-page">
<section class="glossary">

        <header class="glossary-header">
            <h1>주식 용어 사전</h1>

            <p class="glossary-info">
                주식이 낯설게만 느껴지는 여러분을 위해 준비한
                직관적이고 쉬운 설명으로 구성된 알짜배기 주식 용어 사전입니다.
            </p>
        </header>

        <div class="category-grid">

            <a href="<c:url value='/dictionary/category/trading'/>"
               class="category"
               data-image="<c:url value='/images/dictionary/trading.png'/>"
               data-alt="trading-image">
                <span class="category-box">매매와 투자 행동</span>
            </a>

            <a href="<c:url value='/dictionary/category/risk-management'/>"
               class="category"
               data-image="<c:url value='/images/dictionary/risk-management.png'/>"
               data-alt="risk-management-image">
                <span class="category-box">투자자·자금·손익 관리</span>
            </a>

            <a href="<c:url value='/dictionary/category/position'/>"
               class="category"
               data-image="<c:url value='/images/dictionary/position.png'/>"
               data-alt="position-image">
                <span class="category-box">상품과 포지션</span>
            </a>

            <a href="<c:url value='/dictionary/category/market'/>"
               class="category"
               data-image="<c:url value='/images/dictionary/market.png'/>"
               data-alt="market-image">
                <span class="category-box">시장·지수·주문·거래 제도</span>
            </a>

            <a href="<c:url value='/dictionary/category/fundamental'/>"
               class="category"
               data-image="<c:url value='/images/dictionary/fundamental.png'/>"
               data-alt="fundamental-image">
                <span class="category-box">종목 정보와 기업 분석</span>
            </a>

            <a href="<c:url value='/dictionary/category/chart'/>"
               class="category"
               data-image="<c:url value='/images/dictionary/chart.png'/>"
               data-alt="chart">
                <span class="category-box">차트와 기술적 분석</span>
            </a>

        </div>

        <%-- 카테고리 이미지 띄울 공간 만들기 --%>
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