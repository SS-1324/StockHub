<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/css/dictionary.css">
<section class="glossary-category">

    <h2 class = "category-name">${categoryName}</h2>

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

<jsp:include page="/WEB-INF/views/common/footer.jsp" />