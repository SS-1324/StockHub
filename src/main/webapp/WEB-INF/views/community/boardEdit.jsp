<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<h2 class="page-title">게시글 수정</h2>

<c:if test="${not empty error}">
    <p class="alert alert-error">${error}</p>
</c:if>

<form id="board-edit-form" class="form form-flex" action="${communityUrl}/edit/${board.boardId}"
      method="post" enctype="multipart/form-data">

    <div class="form-row">
        <label for="category">카테고리</label>
        <select id="category" name="category">
            <c:forEach var="entry" items="${allowedCategories}">
                <option value="${entry.key}" ${entry.key == board.category ? 'selected' : ''}>${entry.value}</option>
            </c:forEach>
        </select>
    </div>

    <div class="form-row">
        <label for="title">제목 (선택)</label>
        <input type="text" id="title" name="title" maxlength="200" autocomplete="off" value="${fn:escapeXml(board.title)}">
    </div>

    <div class="form-row">
        <label for="content">내용</label>
        <textarea id="content" name="content" maxlength="3000" required><c:out value="${board.content}" /></textarea>
    </div>

    <c:if test="${not empty images}">
        <div class="form-row">
            <label>기존 이미지 (X를 누르면 삭제 대상으로 표시됩니다)</label>
            <div id="existing-image-list" class="image-preview-list">
                <c:forEach var="image" items="${images}">
                    <div class="image-preview-item" data-existing-id="${image.imgId}">
                        <img src="${image.imgPath}" alt="${image.originalName}">
                        <button type="button" class="image-remove-btn" data-existing-id="${image.imgId}" aria-label="이미지 삭제">×</button>
                    </div>
                </c:forEach>
            </div>
            <%-- 삭제로 표시된 기존 이미지의 id가 board.js에서 hidden input으로 여기에 채워진다 --%>
            <div id="delete-image-inputs"></div>
        </div>
    </c:if>

    <div class="form-row">
        <label class="file-label">
            이미지 추가 (전체 최대 <c:out value="${maxImageCount}" />장)
            <input type="file" id="image-input" name="images" accept="image/*" multiple data-max-count="${maxImageCount}">
        </label>
        <div id="image-preview-list" class="image-preview-list"></div>
    </div>

    <div class="form-row form-row-actions">
        <button type="submit" class="btn btn-primary">수정 완료</button>
        <a class="btn btn-outline" href="${communityUrl}/${board.boardId}">취소</a>
    </div>
</form>

<script src="/js/board.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
