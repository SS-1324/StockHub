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
               class="category">
                <span class="category-box">매매와 투자 행동</span>
            </a>

            <a href="<c:url value='/dictionary/category/risk-management'/>"
               class="category">
                <span class="category-box">투자자·자금·손익 관리</span>
            </a>

            <a href="<c:url value='/dictionary/category/position'/>"
               class="category">
                <span class="category-box">상품과 포지션</span>
            </a>

            <a href="<c:url value='/dictionary/category/market'/>"
               class="category">
                <span class="category-box">시장·지수·주문·거래 제도</span>
            </a>

            <a href="<c:url value='/dictionary/category/fundamental'/>"
               class="category">
                <span class="category-box">종목 정보와 기업 분석</span>
            </a>

            <a href="<c:url value='/dictionary/category/chart'/>"
               class="category">
                <span class="category-box">차트와 기술적 분석</span>
            </a>

        </div>

    </section>
</main>
<%-- 공용 footer 불러오기 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />