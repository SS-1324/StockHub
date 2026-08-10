<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/dictionary.css">
<section class="glossary-category">

    <div class="category-header glossary-header">
        <div class="glossary-title-area">
            <h2 class = "category-name">
                <c:out value="${categoryName}"/>
            </h2>
        </div>

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

                <button type="submit"
                        class="glossary-search-icon-btn"
                        aria-label="검색">

                     <svg width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          fill="none"
                          xmlns="http://www.w3.org/2000/svg">

                          <circle cx ="11"
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

        </div>

    <c:choose>
        <c:when test="${not empty glossaryList}">
        <div class = "glossary-list">
            <c:forEach var="glossary" items="${glossaryList}">

                <div class="glossary-item">

                    <h3 class="glossary-term">
                        <c:out value="${glossary.term}" />
                    </h3>

                    <p class="glossary-definition">
                        <c:out value="${glossary.definition}" />
                    </p>

                </div>

                </c:forEach>
        </div>
        </c:when>

        <c:otherwise>
            <p>해당 카테고리에 등록된 용어가 없습니다.</p>
        </c:otherwise>
    </c:choose>

</section>

<script src="${pageContext.request.contextPath}/js/dictionary.js"></script>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />