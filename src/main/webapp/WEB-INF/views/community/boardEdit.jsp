<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<!-- 글쓰기 화면과 동일한 Quill 에디터 -->
<link href="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.snow.css" rel="stylesheet">

<h2 class="page-title board-form-title">게시글 수정</h2>

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
        <label for="editor-container">내용</label>
        <div id="editor-container"></div>
        <!-- 서버로 전송되는 값이자, 기존 글 내용을 처음에 담아두는 곳. board.content는 write.jsp에서
             Quill로 저장한 HTML이라, value에 그대로 넣었다가 JS가 읽어서 에디터에 채워 넣는다. -->
        <input type="hidden" id="content" name="content" value="${fn:escapeXml(board.content)}" required>
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

    <div class="form-row form-row-actions board-form-actions">
        <a class="btn btn-outline" href="${communityUrl}/${board.boardId}">취소</a>
        <button type="submit" class="btn btn-primary btn-board-submit">수정 완료</button>
    </div>
</form>

<script src="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.js"></script>
<script>
    const quill = new Quill('#editor-container', {
        theme: 'snow',
        modules: {
            toolbar: [
                ['bold', 'italic', 'strike'],
                [{ 'size': ['small', false, 'large', 'huge'] }],
                ['link'],
                [{ 'list': 'ordered' }, { 'list': 'bullet' }]
            ]
        },
        placeholder: '내용을 입력하세요'
    });

    // hidden input에 미리 담아둔 기존 글 내용을 에디터에 채워 넣는다.
    const existingContent = document.getElementById('content').value;
    if (existingContent) {
        quill.root.innerHTML = existingContent;
    }

    // 폼이 제출되기 직전에 Quill 안의 최신 HTML을 hidden input(content)으로 다시 옮긴다.
    document.getElementById('board-edit-form').addEventListener('submit', function () {
        document.getElementById('content').value = quill.root.innerHTML;
    });
</script>
<script src="/js/board.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
