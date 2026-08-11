<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<!-- 글쓰기 화면과 동일한 Quill 에디터 -->
<link href="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.snow.css" rel="stylesheet">
<%-- Quill 기본 CSS보다 뒤에서 게시판용 편집기 모양을 적용한다. --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css?v=49">

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
        <%--
            작성 화면과 같은 board-editor-shell 구조를 사용한다.
            작성·수정 화면의 DOM이 같아야 하나의 CSS 규칙으로 툴바와 본문을 연결할 수 있고
            한쪽 화면만 테두리나 간격이 달라지는 회귀 오류도 막을 수 있다.
        --%>
        <div class="board-editor-shell">
            <div id="editor-container"></div>
        </div>
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
                <%-- 작성 화면과 동일한 밑줄과 서버 허용 색상 팔레트를 제공해 저장 결과를 일치시킨다. --%>
                ['bold', 'italic', 'underline', 'strike'],
                [{ 'size': ['small', false, 'large', 'huge'] }],
                [{ 'color': [false, '#e60000', '#ff9900', '#008a00', '#0066cc', '#9933ff'] }],
                <%-- [하이퍼링크-1] 작성 화면과 동일한 Quill 링크 도구를 표시한다. --%>
                ['link'],
                [{ 'list': 'ordered' }, { 'list': 'bullet' }]
            ]
        },
        placeholder: '내용을 입력하세요'
    });

    // hidden input에 미리 담아둔 기존 글 내용을 에디터에 채워 넣는다.
    const existingContent = document.getElementById('content').value;

    if (existingContent) {
   quill.clipboard.dangerouslyPasteHTML(
        existingContent,
        'silent'
    );
}

    // [하이퍼링크-2] 수정된 링크를 포함한 Quill HTML을 일반 POST 폼의 content로 옮긴다.
    // 글 수정은 board.js의 AJAX가 아니라 이 폼의 기존 POST 제출 방식을 사용한다.
    document.getElementById('board-edit-form').addEventListener('submit', function () {
        document.getElementById('content').value = quill.getSemanticHTML();
    });
</script>
<script src="${pageContext.request.contextPath}/js/board.js?v=12"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
