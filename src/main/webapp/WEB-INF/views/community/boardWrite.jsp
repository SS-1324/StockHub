<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp" />

<!-- 글쓰기 화면에 굵게/폰트크기/링크 기능을 위한 Quill 에디터 -->
<link href="https://cdn.jsdelivr.net/npm/quill@2.0.2/dist/quill.snow.css" rel="stylesheet">
<%-- Quill 기본 CSS 다음에 게시판 CSS를 불러야 편집기 테두리와 모서리 설정이 덮어써지지 않는다. --%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/board.css?v=50">

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
        <%--
            Quill은 #editor-container 바로 앞에 툴바를 자동으로 만든다.
            둘을 같은 껍데기 안에 넣어야 form-row의 간격이 툴바와 본문 사이에 생기지 않고
            하나의 편집기처럼 연결된 테두리와 둥근 모서리를 적용할 수 있다.
        --%>
        <div class="board-editor-shell">
            <div id="editor-container"></div>
        </div>
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
                <%--
                    underline은 밑줄, color는 서버 허용 목록과 동일한 다섯 색만 제공한다.
                    임의 색상 전체를 열지 않아 다크모드 가독성과 저장 HTML의 안전성을 함께 지킨다.
                --%>
                ['bold', 'italic', 'underline', 'strike'],
                [{ 'size': ['small', false, 'large', 'huge'] }],
                [{ 'color': [false, '#e60000', '#ff9900', '#008a00', '#0066cc', '#9933ff'] }],
                <%-- [하이퍼링크-1] 선택한 글자에 URL을 연결하는 Quill 링크 도구를 표시한다. --%>
                ['link'],
                [{ 'list': 'ordered' }, { 'list': 'bullet' }]
            ]
        },
        placeholder: '내용을 입력하세요'
    });

    // [하이퍼링크-2] Quill이 만든 <a href="..."> HTML을 content에 담아 AJAX FormData로 전송한다.
    // 폼이 제출되기 직전에 Quill 안의 HTML을 hidden input(content)으로 옮겨준다.
    // 이 hidden input이 없으면 Controller의 BoardDto.content가 계속 빈 값으로 넘어간다.
    document.getElementById('board-write-form').addEventListener('submit', function () {
        document.getElementById('content').value = quill.getSemanticHTML();
    });
</script>
<script src="${pageContext.request.contextPath}/js/board.js?v=13"></script>
<jsp:include page="/WEB-INF/views/common/footer.jsp" />
