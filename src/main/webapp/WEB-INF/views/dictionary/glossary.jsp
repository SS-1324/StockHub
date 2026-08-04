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
                 method="get">

               <div class="autocomplete-search">

                   <input type="text"
                          id="glossary-keyword"
                          name="keyword"
                          value="${fn:escapeXml(keyword)}"
                          placeholder="용어를 검색하세요"
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
    //카테고리 이미지 미리보여주기 기능
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

    const searchForm = document.getElementById("glossary-search-form");
    const keywordInput = document.getElementById("glossary-keyword");
    const autocompleteBox = document.getElementById("autocomplete");
    const contextPath = "${pageContext.request.contextPath}";

    //자동 검색에 입력 지연을 넣기 위한 변수
    let searchTimer;

    //자동 완성 목록 닫기
    function closeAutocomplete(){
        autocompleteBox.replaceChildren();
        autocompleteBox.classList.remove("show");
    }

   // 검색창에 글자를 입력할 때 실행
   keywordInput.addEventListener("input", function () {

       // 이전에 예약된 자동완성 요청 취소
       clearTimeout(searchTimer);

       const keyword = keywordInput.value.trim();

       // 입력값이 없으면 자동완성 목록 닫기
       if (keyword === "") {
           closeAutocomplete();
           return;
       }

       // 입력이 멈춘 뒤 200ms 후 서버 요청
       searchTimer = setTimeout(function () {

           fetch(
               contextPath
               + "/dictionary/autocomplete?keyword="
               + encodeURIComponent(keyword)
           )
               .then(function (response) {

                   if (!response.ok) {
                       throw new Error("자동완성 요청 실패");
                   }

                   return response.json();
               })
               .then(function (terms) {

                   // 기존 추천 목록 제거
                   closeAutocomplete();

                   // 결과가 없다면 종료
                   if (terms.length === 0) {
                       return;
                   }

                   // 서버에서 받은 용어마다 버튼 생성
                   terms.forEach(function (term) {

                       const item = document.createElement("button");

                       item.type = "button";
                       item.className = "autocomplete-item";
                       item.textContent = term;

                       // 자동완성 항목 클릭
                       item.addEventListener("click", function () {

                           keywordInput.value = term;

                           closeAutocomplete();

                           // 선택한 용어로 검색 실행
                           searchForm.requestSubmit();
                       });

                       // 자동완성 박스 안에 버튼 추가
                       autocompleteBox.appendChild(item);
                   });

                   // 자동완성 목록 표시
                   autocompleteBox.classList.add("show");
               })
               .catch(function (error) {

                   console.error(error);
                   closeAutocomplete();
               });

       }, 200);
   });
</script>

<%-- 공용 footer 불러오기 --%>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />