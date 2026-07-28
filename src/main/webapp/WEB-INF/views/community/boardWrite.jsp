<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<h2 class="page-title">게시글 작성</h2>

<c:if test="${not empty error}">
    <p class="alert alert-error">${error}</p>
</c:if>

<form id="board-write-form" class="form form-flex" action="/community/board/write"
      method="post" enctype="multipart/form-data">

    <div class="form-row">
        <label for="category">카테고리</label>
        <select id="category" name="category">
            <c:forEach var="c" items="${allowedCategories}">
                <option value="${c}" ${c == defaultCategory ? 'selected' : ''}>${c}</option>
            </c:forEach>
        </select>
    </div>

    <div class="form-row">
        <label for="title">제목 (선택)</label>
        <input type="text" id="title" name="title" maxlength="200" autocomplete="off">
    </div>

    <div class="form-row">
        <label for="content">내용</label>
        <textarea id="content" name="content" maxlength="3000" required></textarea>
    </div>

    <div class="form-row">
        <label class="file-label">
            이미지 첨부 (최대 5장)
            <input type="file" id="image-input" name="images" accept="image/*" multiple>
        </label>
        <div id="image-preview-list" class="image-preview-list"></div>
    </div>

    <div class="form-row form-row-actions">
        <button type="submit" class="btn btn-primary">등록</button>
        <a class="btn btn-outline" href="/community/board">취소</a>
    </div>
</form>

<script src="/js/board.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
