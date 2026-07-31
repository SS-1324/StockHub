const DEFAULT_MAX_IMAGE_COUNT = 5; // 서버가 max-count를 못 내려준 경우에만 쓰는 최후 fallback
let selectedImages = []; // 지금까지 누적된 파일 별도 저장 배열 - {id, file}
let nextImageId = 0;

const imageInput = document.querySelector("#image-input");
const imagePreviewList = document.querySelector("#image-preview-list");
const existingImageList = document.querySelector("#existing-image-list"); // 수정 화면에만 존재
const deleteImageInputs = document.querySelector("#delete-image-inputs"); // 수정 화면에만 존재
const likeBtn = document.querySelector("#like-btn");
const bookmarkBtn = document.querySelector("#bookmark-btn");
const commentForm = document.querySelector("#comment-form");
const commentFormToggle = document.querySelector("#comment-form-toggle");
const commentList = document.querySelector("#comment-list");

/* "댓글 달기" 버튼 - 누르면 말풍선 모양 입력창이 펼쳐지고, 다시 누르면 접힌다 */
if (commentFormToggle && commentForm) {
    commentFormToggle.addEventListener("click", function(){
        const opened = commentForm.classList.toggle("hidden") === false;
        if (opened) {
            document.querySelector("#comment-input").focus();
        }
    });
}

/* 전체 첨부 가능 장수 - input의 data-max-count(컨트롤러가 내려준 값)를 그대로 씀, 하드코딩 금지 */
function getMaxImageCount(){
    if (imageInput && imageInput.dataset.maxCount) {
        return Number(imageInput.dataset.maxCount);
    }
    return DEFAULT_MAX_IMAGE_COUNT;
}

/* 수정 화면에서 "삭제 표시"되지 않고 아직 남아있는 기존 이미지 개수 */
function remainingExistingCount(){
    if (!existingImageList) {
        return 0;
    }
    return existingImageList.querySelectorAll(".image-preview-item:not(.removed)").length;
}

/* selectedImages 배열 내용을 실제 input.files와 동기화해서 파일 선택이 누적되게 만들기 위한 함수
 * 원래 input.files는 배열 할당을 못 해서 이를 우회하기 위함
 */
function syncInputFiles(){
    const dataTransfer = new DataTransfer();
    selectedImages.forEach(function(entry){
        dataTransfer.items.add(entry.file);
    });
    imageInput.files = dataTransfer.files;
}

/* 상태 -> 화면 렌더링 (파일을 전부 읽은 뒤 한 번에 그린다) */
async function renderImagePreviews(){
    const previews = await Promise.all(selectedImages.map(function(entry){
        return new Promise(function(resolve){
            const reader = new FileReader();
            reader.onload = function(ev){
                resolve({ id: entry.id, name: entry.file.name, dataUrl: ev.target.result });
            };
            reader.readAsDataURL(entry.file);
        });
    }));

    imagePreviewList.innerHTML = "";
    previews.forEach(function(preview){
        const item = document.createElement("div");
        item.className = "image-preview-item";
        item.dataset.imageId = preview.id;

        const img = document.createElement("img");
        img.src = preview.dataUrl;
        img.alt = preview.name;

        const removeBtn = document.createElement("button");
        removeBtn.type = "button";
        removeBtn.className = "image-remove-btn";
        removeBtn.setAttribute("aria-label", "이미지 삭제");
        removeBtn.textContent = "×";
        removeBtn.addEventListener("click", function(){
            removeSelectedImage(preview.id);
        });

        item.appendChild(img);
        item.appendChild(removeBtn);
        imagePreviewList.appendChild(item);
    });
}

/* 아직 서버에 올리기 전인, 방금 선택한 이미지를 목록에서 뺀다 */
function removeSelectedImage(id){
    selectedImages = selectedImages.filter(function(entry){
        return entry.id !== id;
    });
    syncInputFiles();
    renderImagePreviews();
}

/* 새로 선택된 파일을 상태에 누적 - 이미지가 아닌 파일(동영상 등)은 미리보기를 시도하기 전에 걸러낸다 */
function handleImageSelect(){
    const newFiles = Array.from(imageInput.files);
    const remainingSlots = getMaxImageCount() - remainingExistingCount() - selectedImages.length;

    let addedCount = 0;
    for (const file of newFiles) {
        if (addedCount >= remainingSlots) {
            alert(`이미지는 최대 ${getMaxImageCount()}장까지 첨부할 수 있습니다.`);
            break;
        }
        if (!file.type.startsWith("image/")) {
            alert(`이미지 파일만 첨부할 수 있습니다: ${file.name}`);
            continue;
        }
        selectedImages.push({ id: nextImageId++, file: file });
        addedCount++;
    }

    syncInputFiles();
    renderImagePreviews();
}

if (imageInput) {
    imageInput.addEventListener("change", handleImageSelect);
}

/* 수정 화면 - 기존 이미지의 X 클릭 시 화면에서 숨기고, 삭제 대상 id를 hidden input으로 폼에 실어 보낸다 */
if (existingImageList) {
    existingImageList.addEventListener("click", function(ev){
        const btn = ev.target.closest(".image-remove-btn");
        if (!btn) {
            return;
        }
        const item = btn.closest(".image-preview-item");
        item.classList.add("removed");
        item.style.display = "none";

        const hidden = document.createElement("input");
        hidden.type = "hidden";
        hidden.name = "deleteImageIds";
        hidden.value = btn.dataset.existingId;
        deleteImageInputs.appendChild(hidden);
    });
}

/* 서버에 상태를 바꾸는 요청을 보내는 공용 헬퍼 (좋아요/북마크/댓글좋아요 토글에서 사용) */
async function postAction(url){
    const response = await fetch(url, {
        method: "POST",
        headers: {"X-Requested-With": "XMLHttpRequest"}
    });
    const result = await response.json();
    if (!response.ok || !result.success) {
        throw new Error(result.message || "요청 처리에 실패했습니다.");
    }
    return result.data;
}

/* 게시글 좋아요 토글 */
async function toggleBoardLike(){
    const boardId = likeBtn.dataset.boardId;
    try {
        const result = await postAction(`/community/like/${boardId}`);
        likeBtn.classList.toggle("active", result.active);
        likeBtn.querySelector("#like-count").textContent = result.count;
    } catch (err) {
        alert(err.message);
    }
}

if (likeBtn) {
    likeBtn.addEventListener("click", toggleBoardLike);
}

/* 게시글 북마크 토글 */
async function toggleBoardBookmark(){
    const boardId = bookmarkBtn.dataset.boardId;
    try {
        const bookmarked = await postAction(`/community/bookmark/${boardId}`);
        bookmarkBtn.classList.toggle("active", bookmarked);
    } catch (err) {
        alert(err.message);
    }
}

if (bookmarkBtn) {
    bookmarkBtn.addEventListener("click", toggleBoardBookmark);
}

/* 댓글 좋아요 토글 */
async function toggleCommentLike(commentId, button){
    try {
        const result = await postAction(`/community/comment/like/${commentId}`);
        button.classList.toggle("active", result.active);
        button.querySelector(".like-count").textContent = result.count;
    } catch (err) {
        alert(err.message);
    }
}

// 댓글/답글 작성 - 성공하면 새로고침해서 서버가 만든 하이라이트/멘션 마크업을 그대로 받는다
async function submitComment(boardId, content, parentCommentId) {
    try {
        const response = await fetch(`/community/${boardId}/comment`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: JSON.stringify({content: content, parentCommentId: parentCommentId})
        });
        const result = await response.json();
        if (!response.ok || !result.success) {
            throw new Error(result.message || "댓글 등록에 실패했습니다.");
        }
        location.reload();
    } catch (err) {
        alert("에러가 발생했습니다.");
    }
}

/* 댓글 삭제 - 성공하면 새로고침 */
async function deleteComment(boardId, commentId){
    if (!confirm("댓글을 삭제하시겠습니까?")) {
        return;
    }
    const response = await fetch(`/community/${boardId}/comment/${commentId}/delete`, {
        method: "POST",
        headers: {"X-Requested-With": "XMLHttpRequest"}
    });
    const result = await response.json();
    if (!response.ok || !result.success) {
        alert(result.message || "댓글 삭제에 실패했습니다.");
        return;
    }
    location.reload();
}

/* 답글 입력창 펼치기/접기 */
function toggleReplyForm(commentId){
    const commentItem = commentList.querySelector(`.comment-item[data-comment-id="${commentId}"]`);
    const replyForm = commentItem.querySelector(".reply-form");
    replyForm.classList.toggle("hidden");
}

if (commentForm) {
    commentForm.addEventListener("submit", function(ev){
        ev.preventDefault();
        const boardId = commentForm.dataset.boardId;
        const input = document.querySelector("#comment-input");
        const content = input.value.trim();
        if (!content) {
            alert("댓글 내용을 입력해주세요.");
            return;
        }
        submitComment(boardId, content, null).catch(function(err){
            alert(err.message);
        });
    });
}

/* 댓글 목록 영역 - 좋아요/답글펼치기/삭제/답글제출을 이벤트 위임으로 한 번에 처리 */
if (commentList) {
    commentList.addEventListener("click", function(ev){
        const likeBtnEl = ev.target.closest(".comment-like-btn");
        if (likeBtnEl) {
            toggleCommentLike(likeBtnEl.dataset.commentId, likeBtnEl);
            return;
        }

        const replyBtnEl = ev.target.closest(".comment-reply-btn");
        if (replyBtnEl) {
            toggleReplyForm(replyBtnEl.dataset.commentId);
            return;
        }

        const deleteBtnEl = ev.target.closest(".comment-delete-btn");
        if (deleteBtnEl) {
            const commentItem = deleteBtnEl.closest(".comment-item");
            deleteComment(commentList.dataset.boardId, commentItem.dataset.commentId);
        }
    });

    commentList.addEventListener("submit", function(ev){
        const replyForm = ev.target.closest(".reply-form");
        if (!replyForm) {
            return;
        }
        ev.preventDefault();

        const textarea = replyForm.querySelector("textarea");
        const content = textarea.value.trim();
        if (!content) {
            alert("답글 내용을 입력해주세요.");
            return;
        }

        submitComment(replyForm.dataset.boardId, content, replyForm.dataset.parentId).catch(function(err){
            alert(err.message);
        });
    });
}

/* 전문용어 하이라이트 툴팁 - span 하나마다 만들지 않고 재사용 가능한 툴팁 엘리먼트 하나만 둔다 */
let termTooltipEl = null;

function getTermTooltipEl(){
    if (!termTooltipEl) {
        termTooltipEl = document.createElement("div");
        termTooltipEl.className = "term-tooltip hidden";
        document.body.appendChild(termTooltipEl);
    }
    return termTooltipEl;
}

function showTermTooltip(target){
    const tooltip = getTermTooltipEl();
    tooltip.textContent = target.dataset.definition;
    tooltip.classList.remove("hidden");

    const rect = target.getBoundingClientRect();
    tooltip.style.left = `${rect.left}px`;
    tooltip.style.top = `${rect.bottom + 6}px`;
}

function hideTermTooltip(){
    if (termTooltipEl) {
        termTooltipEl.classList.add("hidden");
    }
}

function initTermTooltips(){
    document.addEventListener("mouseover", function(ev){
        const term = ev.target.closest(".term-highlight");
        if (term) {
            showTermTooltip(term);
        }
    });

    document.addEventListener("mouseout", function(ev){
        if (ev.target.closest(".term-highlight")) {
            hideTermTooltip();
        }
    });
}

initTermTooltips();

/* 이미지 스트립(.board-image-strip / .board-card-image-strip) 가로 스크롤 보강.
 * overflow-x:auto만으로는 트랙패드/터치 스와이프는 되지만, 스크롤바를 숨겨놔서(디자인상)
 * 일반 마우스 휠이나 드래그로는 옆으로 넘길 방법이 없었음 - 그래서 둘 다 직접 붙여준다.
 */
function enableHorizontalScroll(strip){
    if (strip.dataset.hscrollBound) {
        return;
    }
    strip.dataset.hscrollBound = "true";

    // 마우스 휠(세로 스크롤)을 가로 스크롤로 변환
    strip.addEventListener("wheel", function(ev){
        if (ev.deltaY === 0) {
            return;
        }
        strip.scrollLeft += ev.deltaY;
        ev.preventDefault();
    });

    // 마우스로 눌러서 드래그하면 옆으로 넘어가게 함
    let dragging = false;
    let dragged = false;
    let startX = 0;
    let startScrollLeft = 0;

    strip.addEventListener("mousedown", function(ev){
        // 기본 동작(이미지 드래그/텍스트 선택)을 막지 않으면 브라우저가 끌어보려다 버벅거림
        ev.preventDefault();
        dragging = true;
        dragged = false;
        strip.classList.add("dragging");
        startX = ev.pageX;
        startScrollLeft = strip.scrollLeft;
    });

    window.addEventListener("mousemove", function(ev){
        if (!dragging) {
            return;
        }
        const delta = ev.pageX - startX;
        if (Math.abs(delta) > 5) {
            dragged = true;
        }
        strip.scrollLeft = startScrollLeft - delta;
    });

    window.addEventListener("mouseup", function(){
        dragging = false;
        strip.classList.remove("dragging");
    });

    // 카드 전체가 <a>인 경우, 드래그였다면 클릭으로 이어져 상세페이지로 튀지 않게 막는다
    strip.addEventListener("click", function(ev){
        if (dragged) {
            ev.preventDefault();
            ev.stopPropagation();
            dragged = false;
        }
    });
}

function initImageStrips(container){
    container.querySelectorAll(".board-image-strip, .board-card-image-strip").forEach(enableHorizontalScroll);
}

initImageStrips(document);

/* maxlength가 걸린 입력창(제목/본문/댓글/답글) 아래에 "N/최대" 남은 글자수를 실시간으로 보여준다.
 * 필드마다 따로 만들지 않고 maxlength 속성이 있는 곳을 전부 찾아서 공통으로 붙인다.
 */
function initCharCounters(container){
    container.querySelectorAll("[maxlength]").forEach(function(field){
        if (field.dataset.counterBound) {
            return;
        }
        field.dataset.counterBound = "true";

        const max = Number(field.getAttribute("maxlength"));
        const counter = document.createElement("div");
        counter.className = "char-counter";
        field.insertAdjacentElement("afterend", counter);

        function updateCounter(){
            counter.textContent = `${field.value.length}/${max}`;
        }
        field.addEventListener("input", updateCounter);
        updateCounter();
    });
}

initCharCounters(document);

/* 목록 카드 - 5줄 넘게 넘치는 본문에만 "더보기" 버튼을 동적으로 붙인다.
 * 실제로 몇 줄인지는 화면 폭/폰트에 따라 달라서 서버가 미리 알 수 없으므로, 렌더링된 뒤 scrollHeight로 판단한다.
 */
function enhanceCardSnippets(container){
    container.querySelectorAll(".board-card-snippet").forEach(function(snippet){
        if (snippet.dataset.enhanced) {
            return;
        }
        snippet.dataset.enhanced = "true";

        if (snippet.scrollHeight <= snippet.clientHeight + 1) {
            return;
        }

        const moreBtn = document.createElement("button");
        moreBtn.type = "button";
        moreBtn.className = "board-card-more-btn";
        moreBtn.textContent = "더보기";
        moreBtn.addEventListener("click", function(ev){
            // 카드 전체가 <a>라서 클릭이 그대로 새면 상세 페이지로 이동해버리므로 막는다
            ev.preventDefault();
            ev.stopPropagation();
            const expanded = snippet.classList.toggle("expanded");
            moreBtn.textContent = expanded ? "접기" : "더보기";
        });
        snippet.insertAdjacentElement("afterend", moreBtn);
    });
}

const boardListEl = document.querySelector("#board-list");
if (boardListEl) {
    enhanceCardSnippets(boardListEl);
}

/* 목록 카드의 좋아요/북마크/댓글 버튼 - 이벤트 위임으로 처리해서 무한스크롤로 나중에 추가되는 카드에도 그대로 적용됨.
 * 카드 전체가 <a>라서, 버튼 클릭이 카드 클릭(상세 이동)으로 새지 않게 매번 막아준다.
 */
if (boardListEl) {
    boardListEl.addEventListener("click", function(ev){
        const likeBtnEl = ev.target.closest(".board-card-like-btn");
        if (likeBtnEl) {
            ev.preventDefault();
            ev.stopPropagation();
            postAction(`/community/like/${likeBtnEl.dataset.boardId}`)
                .then(function(result){
                    likeBtnEl.classList.toggle("active", result.active);
                    likeBtnEl.querySelector(".like-count").textContent = result.count;
                })
                .catch(function(err){ alert(err.message); });
            return;
        }

        const bookmarkBtnEl = ev.target.closest(".board-card-bookmark-btn");
        if (bookmarkBtnEl) {
            ev.preventDefault();
            ev.stopPropagation();
            postAction(`/community/bookmark/${bookmarkBtnEl.dataset.boardId}`)
                .then(function(bookmarked){
                    bookmarkBtnEl.classList.toggle("active", bookmarked);
                })
                .catch(function(err){ alert(err.message); });
            return;
        }

        const commentLinkEl = ev.target.closest(".board-card-comment-link");
        if (commentLinkEl) {
            ev.preventDefault();
            ev.stopPropagation();
            window.location.href = commentLinkEl.dataset.href;
        }
    });
}

/* 게시판 목록 무한스크롤 - 화면 바닥의 sentinel이 보이면 다음 페이지를 가져와 이어붙인다 */
function initInfiniteFeed(){
    const sentinel = document.querySelector("#feed-sentinel");
    if (!sentinel || !boardListEl) {
        return;
    }

    let nextPage = Number(sentinel.dataset.nextPage || "2");
    const category = sentinel.dataset.category || "";
    let loading = false;

    const observer = new IntersectionObserver(function(entries){
        const isVisible = entries.some(function(entry){ return entry.isIntersecting; });
        if (!isVisible || loading) {
            return;
        }
        loading = true;
        loadNextPage().finally(function(){ loading = false; });
    });

    async function loadNextPage(){
        const query = `page=${nextPage}` + (category ? `&category=${encodeURIComponent(category)}` : "");
        const response = await fetch(`/community/feed?${query}`, {
            headers: {"X-Requested-With": "XMLHttpRequest"}
        });
        const html = await response.text();

        if (!html.trim()) {
            observer.disconnect(); // 더 가져올 게시글이 없음
            return;
        }

        boardListEl.insertAdjacentHTML("beforeend", html);
        enhanceCardSnippets(boardListEl);
        initImageStrips(boardListEl);
        nextPage++;
    }

    observer.observe(sentinel);
}

initInfiniteFeed();
