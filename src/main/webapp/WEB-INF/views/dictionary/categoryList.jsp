<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<section class="glossary-category">

    <h2>${categoryName}</h2>

    <c:choose>
        <c:when test="${not empty glossaryList}">
            <c:forEach var="glossary" items="${glossaryList}">
                <div class="glossary-item">
                    <h3>${glossary.term}</h3>
                    <p>${glossary.definition}</p>
                </div>
            </c:forEach>
        </c:when>

        <c:otherwise>
            <p>해당 카테고리에 등록된 용어가 없습니다.</p>
        </c:otherwise>
    </c:choose>

</section>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />