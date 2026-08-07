<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css">

<!-- 글쓰기 화면에 굵게/폰트크기/링크 기능을 위한 Quill 에디터 -->
<link href="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.snow.css" rel="stylesheet">

<h2 class="page-title board-form-title">게시글 작성</h2>

<c:if test="${not empty error}">
    <p class="alert alert-error">${error}</p>
</c:if>

<form id="board-write-form" class="form form-flex" action="${communityUrl}/write"
      method="post" enctype="multipart/form-data">

    <div class="form-row">
        <label for="category">카테고리</label>
        <select id="category" name="category">
            <c:forEach var="entry" items="${allowedCategories}">
                <option value="${entry.key}" ${entry.key == defaultCategory ? 'selected' : ''}>${entry.value}</option>
            </c:forEach>
        </select>
    </div>

    <div class="form-row">
        <label for="title">제목 (선택)</label>
        <input type="text" id="title" name="title" maxlength="200" autocomplete="off">
    </div>

    <div class="form-row">
        <label for="editor-container">내용</label>
        <div id="editor-container"></div>
        <!-- 실제로 서버에 전송되는 값은 이 hidden input. Quill 내용이 여기로 복사됨 -->
        <input type="hidden" id="content" name="content" required>
    </div>

    <div class="form-row">
        <label class="file-label">
            이미지 첨부 (최대 <c:out value="${maxImageCount}" />장)
            <input type="file" id="image-input" name="images" accept="image/*" multiple data-max-count="${maxImageCount}">
        </label>
        <div id="image-preview-list" class="image-preview-list"></div>
    </div>

    <div class="form-row form-row-actions board-form-actions">
        <a class="btn btn-outline" href="${communityUrl}">취소</a>
        <button type="submit" class="btn btn-primary btn-board-submit">등록</button>
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

    // 폼이 제출되기 직전에 Quill 안의 HTML을 hidden input(content)으로 옮겨준다.
    // 이 hidden input이 없으면 Controller의 BoardDto.content가 계속 빈 값으로 넘어간다.
    document.getElementById('board-write-form').addEventListener('submit', function () {
        document.getElementById('content').value = quill.getSemanticHTML();
    });
</script>
<script src="/js/board.js"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
