/* 커뮤니티 게시판 화면 스크립트 (member.js와 동일하게 순수 fetch/async 스타일, 공용 헬퍼 모듈 없이 이 파일 안에서만 완결) */

const MAX_IMAGE_COUNT = 5;

const imageInput = document.querySelector("#image-input");
const imagePreviewList = document.querySelector("#image-preview-list");
const likeBtn = document.querySelector("#like-btn");
const bookmarkBtn = document.querySelector("#bookmark-btn");
const commentForm = document.querySelector("#comment-form");
const commentList = document.querySelector("#comment-list");

/* 이미지 첨부 미리보기 (글쓰기 화면) */
function previewSelectedImages(){
    imagePreviewList.innerHTML = "";

    const files = Array.from(imageInput.files).slice(0, MAX_IMAGE_COUNT);
    files.forEach(function(file){
        const reader = new FileReader();
        reader.onload = function(ev){
            const item = document.createElement("div");
            item.className = "image-preview-item";

            const img = document.createElement("img");
            img.src = ev.target.result;
            img.alt = file.name;

            item.appendChild(img);
            imagePreviewList.appendChild(item);
        };
        reader.readAsDataURL(file);
    });
}

if (imageInput) {
    imageInput.addEventListener("change", previewSelectedImages);
}

/* 서버에 상태를 바꾸는 요청을 보내는 공용 헬퍼 (좋아요/북마크/댓글좋아요 토글에서만 사용) */
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
        const result = await postAction(`/community/board/like/${boardId}`);
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
        const bookmarked = await postAction(`/community/board/bookmark/${boardId}`);
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

/* 댓글/답글 작성 - 성공하면 새로고침해서 서버가 만든 하이라이트/멘션 마크업을 그대로 받는다 */
async function submitComment(boardId, content, parentCommentId){
    const response = await fetch(`/community/board/${boardId}/comment`, {
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
}

/* 댓글 삭제 - 성공하면 새로고침 */
async function deleteComment(boardId, commentId){
    if (!confirm("댓글을 삭제하시겠습니까?")) {
        return;
    }
    const response = await fetch(`/community/board/${boardId}/comment/${commentId}/delete`, {
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
