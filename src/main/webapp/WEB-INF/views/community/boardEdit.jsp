<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<h2 class="page-title">게시글 수정</h2>

<c:if test="${not empty error}">
    <p class="alert alert-error">${error}</p>
</c:if>

<form id="board-edit-form" class="form form-flex" action="/community/board/edit/${board.boardId}" method="post">

    <div class="form-row">
        <label for="category">카테고리</label>
        <select id="category" name="category">
            <c:forEach var="c" items="${allowedCategories}">
                <option value="${c}" ${c == board.category ? 'selected' : ''}>${c}</option>
            </c:forEach>
        </select>
    </div>

    <div class="form-row">
        <label for="title">제목 (선택)</label>
        <input type="text" id="title" name="title" maxlength="200" autocomplete="off" value="${board.title}">
    </div>

    <div class="form-row">
        <label for="content">내용</label>
        <textarea id="content" name="content" maxlength="3000" required>${board.content}</textarea>
    </div>

    <c:if test="${not empty images}">
        <div class="form-row">
            <label>첨부된 이미지 (수정 화면에서는 읽기 전용입니다)</label>
            <div class="board-existing-images">
                <c:forEach var="image" items="${images}">
                    <img src="${image.imgPath}" alt="${image.originalName}">
                </c:forEach>
            </div>
        </div>
    </c:if>

    <div class="form-row form-row-actions">
        <button type="submit" class="btn btn-primary">수정 완료</button>
        <a class="btn btn-outline" href="/community/board/${board.boardId}">취소</a>
    </div>
</form>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
