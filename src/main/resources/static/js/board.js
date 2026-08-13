const DEFAULT_MAX_IMAGE_COUNT = 5; // 서버가 max-count를 못 내려준 경우에만 쓰는 최후 fallback
let selectedImages = []; // 지금까지 누적된 파일 별도 저장 배열 - {id, file}
let nextImageId = 0;

const imageInput = document.querySelector("#image-input");
const imagePreviewList = document.querySelector("#image-preview-list");
const existingImageList = document.querySelector("#existing-image-list"); // 수정 화면에만 존재
const deleteImageInputs = document.querySelector("#delete-image-inputs"); // 수정 화면에만 존재
const boardWriteForm = document.querySelector("#board-write-form");
const likeBtn = document.querySelector("#like-btn");
const bookmarkBtn = document.querySelector("#bookmark-btn");
const commentForm = document.querySelector("#comment-form");
const commentList = document.querySelector("#comment-list");

/*
 * 본문 편집기는 글자 서식만 저장한다. 클립보드/드래그 이미지를 Quill 본문에 넣으면
 * base64 데이터가 content HTML에 포함되므로, 이미지는 별도의 첨부 영역만 사용하게 한다.
 * 서버도 img 태그를 제거하지만 여기서 먼저 막아 사용자가 등록 직전에 실패하지 않도록 한다.
 */
function preventInlineEditorImages(editor){
    if (!editor || !editor.root || editor.root.dataset.imageGuardBound) {
        return;
    }
    editor.root.dataset.imageGuardBound = "true";

    const Delta = Quill.import("delta");
    editor.clipboard.addMatcher("img", function(){
        return new Delta();
    });

    function containsImage(dataTransfer){
        if (!dataTransfer) {
            return false;
        }

        const hasImageFile = Array.from(dataTransfer.items || []).some(function(item){
            return item.kind === "file" && item.type.startsWith("image/");
        });
        const html = dataTransfer.getData("text/html") || "";
        return hasImageFile || /<img\b/i.test(html);
    }

    editor.root.addEventListener("paste", function(ev){
        if (!containsImage(ev.clipboardData)) {
            return;
        }

        const hasImageFile = Array.from(ev.clipboardData.items || []).some(function(item){
            return item.kind === "file" && item.type.startsWith("image/");
        });
        if (hasImageFile) {
            ev.preventDefault();
        }
        alert("본문에는 이미지를 넣을 수 없습니다. 아래 이미지 첨부를 이용해 주세요.");
    }, true);

    editor.root.addEventListener("drop", function(ev){
        if (!containsImage(ev.dataTransfer)) {
            return;
        }
        ev.preventDefault();
        ev.stopPropagation();
        alert("본문에는 이미지를 넣을 수 없습니다. 아래 이미지 첨부를 이용해 주세요.");
    }, true);
}

/*
 * [글쓰기서식유지]
 * Quill은 Backspace로 문자를 지우거나 Enter로 새 문단을 만들 때 커서의 bold/italic 같은
 * 인라인 속성을 초기화할 수 있다. 키 입력 직전 서식을 저장하고 text-change 이후 복원해서
 * 사용자가 서식 버튼을 다시 누르지 않아도 다음 글자가 같은 모양으로 이어지게 한다.
 *
 * 목록(ol/ul)은 줄 자체의 블록 서식이므로 여기서 복원하지 않는다. 빈 목록을 지우는 것은
 * 사용자가 목록을 끝내려는 정상 동작일 수 있기 때문이다.
 */
function preserveInlineFormatsAcrossEditingKeys(editor){
    if (!editor || !editor.root || editor.root.dataset.formatMemoryBound) {
        return;
    }
    editor.root.dataset.formatMemoryBound = "true";

    /* 밑줄과 글자색도 Backspace·Enter 이후 이어져야 하는 문자 단위 서식이다. */
    const inlineFormatNames = ["bold", "italic", "underline", "strike", "size", "color"];
    let rememberedFormats = {};
    let formatsBeforeBackspace = null;
    let formatsBeforeEnter = null;
    let isComposing = false;
    let compositionRevision = 0;
    let editRevision = 0;

    function pickInlineFormats(formats){
        const picked = {};
        inlineFormatNames.forEach(function(name){
            if (formats && formats[name]) picked[name] = formats[name];
        });
        return picked;
    }

    function rememberCurrentFormats(){
        const range = editor.getSelection();
        if (!range) return;

        rememberedFormats = pickInlineFormats(editor.getFormat(range));
    }

    function readFormatsAtOrBeforeCursor(range){
        let formats = editor.getFormat(range);
        /* 커서 자체에 서식 정보가 없으면 바로 앞 문자의 인라인 서식을 기준으로 삼는다. */
        if (range.length === 0 && range.index > 0 && Object.keys(pickInlineFormats(formats)).length === 0) {
            formats = editor.getFormat(range.index - 1, 1);
        }
        return pickInlineFormats(formats);
    }

    function restoreFormatsAtCursor(formatsToRestore){
        /*
         * Quill의 키보드 처리가 선택 영역을 갱신한 다음 적용해야 하므로 다음 화면 프레임까지 기다린다.
         * source를 silent로 지정해 복원 자체가 새로운 text-change를 발생시키는 재귀 호출을 막는다.
         * 예약 이후 새 입력이 생기거나 한글 조합이 시작되면 오래된 복원 작업을 취소한다.
         * 늦게 실행된 작업이 IME 커서에 개입하면 글자의 입력 순서가 뒤섞일 수 있기 때문이다.
         */
        const scheduledRevision = editRevision;
        queueMicrotask(function(){
            if (isComposing || scheduledRevision !== editRevision) return;

            const range = editor.getSelection();
            if (!range || range.length !== 0) return;

            inlineFormatNames.forEach(function(name){
                editor.format(name, formatsToRestore[name] || false, "silent");
            });
            rememberedFormats = formatsToRestore;
        });
    }

    /*
     * 한글 IME는 한 글자를 조합하는 동안 insert/delete를 반복해서 발생시킨다.
     * 이 기간에는 일반 Backspace·Enter 처리로 오인하지 않도록 서식 복원을 멈추고,
     * compositionend 다음 프레임에 현재 커서의 서식만 다시 기억한다.
     */
    editor.root.addEventListener("compositionstart", function(){
        isComposing = true;
        compositionRevision += 1;
        formatsBeforeBackspace = null;
        formatsBeforeEnter = null;
    });

    editor.root.addEventListener("compositionend", function(){
        const endingCompositionRevision = compositionRevision;
        /* 조합 종료 상태는 즉시 반영해 뒤이어 누른 Space·Enter를 일반 키로 처리한다. */
        isComposing = false;

        requestAnimationFrame(function(){
            /* 다음 한글 조합이 이미 시작됐다면 이전 compositionend의 예약 작업은 무시한다. */
            if (isComposing || endingCompositionRevision !== compositionRevision) return;

            rememberCurrentFormats();
        });
    });

    /*
     * Backspace가 실행되기 전에 커서 서식을 저장한다.
     * text-change 시점에는 Quill이 이미 앞 문자를 지우고 일반 서식으로 바꿨을 수 있으므로
     * 삭제 후의 getFormat()만 읽으면 사용자가 켜 둔 굵게·기울임 상태를 잃게 된다.
     */
    editor.root.addEventListener("keydown", function(event){
        if (isComposing || event.isComposing || event.keyCode === 229) return;
        if (event.key !== "Backspace" && event.key !== "Enter") return;

        const range = editor.getSelection();
        if (!range) return;

        const formatsBeforeKey = readFormatsAtOrBeforeCursor(range);
        if (event.key === "Backspace") {
            formatsBeforeBackspace = formatsBeforeKey;
        } else {
            /* Enter가 새 문단을 만들기 전에 현재 굵게·기울임 등의 인라인 서식을 저장한다. */
            formatsBeforeEnter = formatsBeforeKey;
        }
        rememberedFormats = formatsBeforeKey;
    }, true);

    /* 커서가 문서 맨 앞이라 실제 삭제가 없었던 Backspace 기록은 다음 입력에 넘기지 않는다. */
    editor.root.addEventListener("keyup", function(event){
        if (event.key === "Backspace") formatsBeforeBackspace = null;
        if (event.key === "Enter") formatsBeforeEnter = null;
    }, true);

    editor.on("selection-change", function(range){
        if (range && !isComposing) rememberCurrentFormats();
    });

    editor.on("text-change", function(delta, oldDelta, source){
        /* API로 기존 글을 불러오는 경우가 아니라 사용자가 직접 편집한 경우에만 개입한다. */
        if (source !== "user") return;

        editRevision += 1;

        /* 한글 조합 과정의 삽입·삭제는 일반 편집 키 결과가 아니므로 건드리지 않는다. */
        if (isComposing) return;

        /* Delta는 Quill이 이번 입력에서 추가·삭제한 내용만 담으므로 키 결과를 정확히 구분할 수 있다. */
        const containsDeletion = delta.ops.some(function(operation){
            return Object.prototype.hasOwnProperty.call(operation, "delete");
        });
        const containsLineBreak = delta.ops.some(function(operation){
            return typeof operation.insert === "string" && operation.insert.includes("\n");
        });

        /*
         * Backspace로 문자가 실제 삭제된 경우에는 삭제 직전 서식을 현재 커서에 복구한다.
         * 따라서 한 글자만 지웠든 본문 전체를 지웠든 다음 입력도 같은 서식으로 이어진다.
         */
        if (containsDeletion && formatsBeforeBackspace) {
            const formatsToRestore = formatsBeforeBackspace;
            formatsBeforeBackspace = null;
            restoreFormatsAtCursor(formatsToRestore);
            return;
        }

        /*
         * Enter로 새 줄이 추가되면 Quill이 커서의 문자 서식을 초기화할 수 있다.
         * Enter 직전 서식을 새 줄의 빈 커서에 되돌려 다음 입력이 같은 모양으로 이어지게 한다.
         */
        if (containsLineBreak && formatsBeforeEnter) {
            const formatsToRestore = formatsBeforeEnter;
            formatsBeforeEnter = null;
            restoreFormatsAtCursor(formatsToRestore);
            return;
        }

        if (editor.getLength() > 1) {
            rememberCurrentFormats();
        }
        /*
         * 편집기가 비었더라도 setSelection(0, 0)으로 커서를 강제로 옮기지 않는다.
         * 한글 조합 직후 Quill의 길이 갱신보다 이 예약 작업이 늦게 실행되면,
         * 방금 입력한 글자 앞으로 커서가 되돌아가는 원인이 되기 때문이다.
         */
    });

    /*
     * 툴바 이벤트보다 먼저 값을 읽으면 변경 전 서식이 저장될 수 있다.
     * click과 select(change) 처리가 끝난 다음 프레임에서 실제 활성 서식을 기억한다.
     */
    const toolbar = editor.getModule("toolbar");
    if (toolbar && toolbar.container) {
        toolbar.container.addEventListener("click", function(){
            requestAnimationFrame(rememberCurrentFormats);
        });
        toolbar.container.addEventListener("change", function(){
            requestAnimationFrame(rememberCurrentFormats);
        });
    }
}


/* [AJAX-1: 게시글 등록] 글쓰기 폼 제출 - 일반 폼 네비게이션 대신 fetch로 보내고, 성공하면 location.replace()로 상세 이동.
 * location.href가 아니라 replace()를 쓰는 이유: href는 히스토리에 새 항목을 쌓지만 replace는 현재 항목(글쓰기 폼)을
 * 대체한다. 그래야 글 등록 후 상세페이지에서 "뒤로가기"를 눌렀을 때, 이미 제출한 글쓰기 폼이 아니라
 * 그 이전 페이지(커뮤니티 목록)로 곧장 돌아간다.
 */
if (boardWriteForm) {
    boardWriteForm.addEventListener("submit", function(ev){
        ev.preventDefault();
        // 이미지와 Quill 링크 HTML을 함께 보내야 하므로 multipart FormData를 그대로 AJAX 요청 본문으로 사용한다.
        fetch(boardWriteForm.getAttribute("action"), {
            method: "POST",
            headers: {"X-Requested-With": "XMLHttpRequest"},
            body: new FormData(boardWriteForm)
        })
            .then(function(response){ return response.json().then(function(result){ return {response, result}; }); })
            .then(function({response, result}){
                if (!response.ok || !result.success) {
                    throw new Error(result.message || "게시글 등록에 실패했습니다.");
                }
                location.replace(`/community/${result.data}`);
            })
            .catch(function(err){
                alert(err.message);
            });
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

/* 대시보드 "포트폴리오 공유" 버튼에서 넘어온 경우: PNG를 sessionStorage에 잠깐 담아뒀다가
   여기서 이어받아 이미지 첨부칸에 자동으로 넣어준다 (dashboard.js의 setupAnalyticsShare 참고) */
function attachSharedPortfolioImage(){
    if (!imageInput) {
        return;
    }
    const raw = sessionStorage.getItem("stockhub:sharedPortfolioImage");
    if (!raw) {
        return;
    }
    sessionStorage.removeItem("stockhub:sharedPortfolioImage");

    let payload;
    try {
        payload = JSON.parse(raw);
    } catch (e) {
        return;
    }
    if (!payload || !payload.dataUrl) {
        return;
    }

    fetch(payload.dataUrl)
        .then(function(res){ return res.blob(); })
        .then(function(blob){
            const file = new File([blob], payload.fileName || "portfolio.png", { type: "image/png" });
            selectedImages.push({ id: nextImageId++, file: file });
            syncInputFiles();
            renderImagePreviews();
        });
}

attachSharedPortfolioImage();

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

/* [AJAX-2: 토글 동작] 서버에 상태를 바꾸는 요청을 보내는 공용 헬퍼 (좋아요/북마크/댓글좋아요에서 사용) */
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
        likeBtn.setAttribute("aria-pressed", String(result.active));
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
        bookmarkBtn.setAttribute("aria-pressed", String(bookmarked));
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

// [AJAX-3: 댓글·답글 등록] JSON으로 작성 요청을 보내고, 성공하면 서버의 최신 표시 결과를 받기 위해 새로고침한다.
// 실패 사유(글자수 초과, 제어문자 포함 등)는 호출부의 .catch(err => alert(err.message))가 그대로
// 보여줘야 하므로, 여기서 뭉뚱그린 메시지로 삼키지 않고 그대로 위로 던진다.
async function submitComment(boardId, content, parentCommentId) {
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
}

/* [AJAX-4: 댓글 삭제] 별도 페이지 이동 없이 삭제 요청을 보내고, 성공하면 현재 상세 화면을 새로고침한다. */
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

/* [AJAX-5: 관리자 댓글 수정] JSON으로 수정 요청을 보내고, 성공하면 하이라이트와 수정일시를 다시 받도록 새로고침한다. */
async function updateComment(boardId, commentId, content){
    const response = await fetch(`/community/${boardId}/comment/${commentId}/edit`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "X-Requested-With": "XMLHttpRequest"
        },
        body: JSON.stringify({content: content})
    });
    const result = await response.json();
    if (!response.ok || !result.success) {
        throw new Error(result.message || "댓글 수정에 실패했습니다.");
    }
    location.reload();
}

/* 답글 입력창 펼치기/접기 */
function toggleReplyForm(commentId){
    const commentItem = commentList.querySelector(`.comment-item[data-comment-id="${commentId}"]`);
    const replyForm = commentItem.querySelector(".reply-form");
    replyForm.classList.toggle("hidden");
}

/* 관리자 댓글 수정 입력창 펼치기/접기 */
function toggleCommentEditForm(commentId, open){
    const commentItem = commentList.querySelector(`.comment-item[data-comment-id="${commentId}"]`);
    const editForm = commentItem?.querySelector(".comment-edit-form");
    if (!editForm) {
        return;
    }

    editForm.classList.toggle("hidden", !open);
    commentItem.querySelector(".comment-body")?.classList.toggle("hidden", open);
    if (open) {
        editForm.querySelector("textarea").focus();
    }
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

        const editBtnEl = ev.target.closest(".comment-edit-btn");
        if (editBtnEl) {
            toggleCommentEditForm(editBtnEl.dataset.commentId, true);
            return;
        }

        const editCancelEl = ev.target.closest(".comment-edit-cancel");
        if (editCancelEl) {
            const commentItem = editCancelEl.closest(".comment-item");
            toggleCommentEditForm(commentItem.dataset.commentId, false);
            return;
        }

        const deleteBtnEl = ev.target.closest(".comment-delete-btn");
        if (deleteBtnEl) {
            const commentItem = deleteBtnEl.closest(".comment-item");
            deleteComment(commentList.dataset.boardId, commentItem.dataset.commentId);
        }
    });

    commentList.addEventListener("submit", function(ev){
        const editForm = ev.target.closest(".comment-edit-form");
        if (editForm) {
            ev.preventDefault();
            const textarea = editForm.querySelector("textarea");
            const content = textarea.value.trim();
            if (!content) {
                alert("댓글 내용을 입력해주세요.");
                return;
            }

            updateComment(editForm.dataset.boardId, editForm.dataset.commentId, content)
                .catch(function(err){ alert(err.message); });
            return;
        }

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

/* 목록 카드 - 고정된 미리보기 줄 수를 넘는 본문에만 "더보기" 버튼을 동적으로 붙인다. */
function enhanceCardSnippets(container){
    container.querySelectorAll(".board-card-snippet").forEach(function(snippet){
        if (snippet.dataset.enhanced) {
            return;
        }
        snippet.dataset.enhanced = "true";

        const card = snippet.closest(".board-card");
        if (card && card.classList.contains("has-no-image")) {
            /* [카드본문-1]
             * 이미지 없는 글의 연속 빈 줄은 CSS의 6줄 제한을 불필요하게 차지하므로 <br>만 정리한다.
             * 주의: snippet.textContent를 다시 대입하면 strong/em/span 같은 Quill 서식 태그가 전부 삭제된다.
             * 따라서 DOM 노드를 유지한 채 앞·뒤·연속 <br>만 제거해 굵기·색상·크기 서식을 보존한다.
             */
            Array.from(snippet.childNodes).forEach(function(node){
                if (node.nodeType === Node.TEXT_NODE && !node.textContent.trim()) {
                    node.remove();
                }
            });

            let previousWasBreak = true;
            Array.from(snippet.childNodes).forEach(function(node){
                const isBreak = node.nodeType === Node.ELEMENT_NODE && node.tagName === "BR";
                if (isBreak && previousWasBreak) {
                    node.remove();
                    return;
                }
                previousWasBreak = isBreak;
            });

            const lastNode = snippet.lastChild;
            if (lastNode && lastNode.nodeType === Node.ELEMENT_NODE && lastNode.tagName === "BR") {
                lastNode.remove();
            }
        }

        if (snippet.scrollHeight <= snippet.clientHeight + 1) {
            return;
        }

        const moreBtn = document.createElement("button");
        moreBtn.type = "button";
        moreBtn.className = "board-card-more-btn";
        moreBtn.textContent = "더보기";
        moreBtn.addEventListener("click", function(ev){
            /* [카드본문펼치기-2] 카드 이동을 막고 현재 카드의 본문만 더보기/접기 상태로 전환한다. */
            ev.preventDefault();
            ev.stopPropagation();
            const expanded = snippet.classList.toggle("expanded");
            card.classList.toggle("content-expanded", expanded);
            moreBtn.textContent = expanded ? "접기" : "더보기";
            moreBtn.setAttribute("aria-expanded", String(expanded));
        });
        moreBtn.setAttribute("aria-expanded", "false");
        snippet.insertAdjacentElement("afterend", moreBtn);
    });
}

const boardListEl = document.querySelector("#board-list");

/*
 * [커뮤니티새로고침위치]
 * 브라우저는 일반적으로 새로고침에도 이전 스크롤 위치를 복원한다. 긴 피드에서는 사용자가
 * 중간 글부터 보게 되므로, 커뮤니티 목록을 '새로고침(reload)'한 경우에만 최상단으로 이동한다.
 * 게시글을 읽고 뒤로가기로 목록에 돌아오는 back_forward 탐색은 제외해 읽던 위치를 보존한다.
 */
function scrollCommunityListToTopOnReload(){
    if (!boardListEl) return;

    const navigationEntry = typeof performance.getEntriesByType === "function"
        ? performance.getEntriesByType("navigation")[0]
        : null;
    const isReload = navigationEntry
        ? navigationEntry.type === "reload"
        : Boolean(performance.navigation && performance.navigation.type === 1);

    if (!isReload) return;

    const canControlRestoration = "scrollRestoration" in history;
    const previousRestoration = canControlRestoration ? history.scrollRestoration : "auto";
    if (canControlRestoration) history.scrollRestoration = "manual";

    window.addEventListener("pageshow", function(){
        /* 브라우저의 자체 위치 복원이 끝난 다음 프레임에서 즉시 최상단을 확정한다. */
        window.scrollTo(0, 0);
        requestAnimationFrame(function(){
            window.scrollTo(0, 0);
            if (canControlRestoration) history.scrollRestoration = previousRestoration;
        });
    }, {once: true});
}

scrollCommunityListToTopOnReload();
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

/* [AJAX-6: 무한스크롤] 화면 바닥의 sentinel이 보이면 다음 페이지 HTML 조각을 가져와 목록 뒤에 이어붙인다. */
function initInfiniteFeed(){
    const sentinel = document.querySelector("#feed-sentinel");
    if (!sentinel || !boardListEl) {
        return;
    }

    let nextPage = Number(sentinel.dataset.nextPage || "2");
    const category = sentinel.dataset.category || "";
    const keyword = sentinel.dataset.keyword || "";
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
        const query = `page=${nextPage}`
            + (category ? `&category=${encodeURIComponent(category)}` : "")
            + (keyword ? `&keyword=${encodeURIComponent(keyword)}` : "");
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

/*
 * [게시글메뉴-3] 상세 게시글의 ⋯ 버튼만 수정·삭제 메뉴를 열고 닫는다.
 * 메뉴 밖 클릭 또는 Esc 입력 시 닫아서 작은 메뉴가 본문 위에 계속 남지 않게 한다.
 */
const boardPostMenus = document.querySelectorAll("[data-board-post-menu]");

function closeBoardPostMenus(exceptMenu){
    boardPostMenus.forEach(function(menu){
        if (menu === exceptMenu) return;
        const toggle = menu.querySelector("[data-board-post-menu-toggle]");
        const panel = menu.querySelector("[data-board-post-menu-panel]");
        if (toggle) toggle.setAttribute("aria-expanded", "false");
        if (panel) panel.hidden = true;
    });
}

boardPostMenus.forEach(function(menu){
    const toggle = menu.querySelector("[data-board-post-menu-toggle]");
    const panel = menu.querySelector("[data-board-post-menu-panel]");
    if (!toggle || !panel) return;

    toggle.addEventListener("click", function(event){
        event.stopPropagation();
        const willOpen = panel.hidden;
        closeBoardPostMenus(menu);
        panel.hidden = !willOpen;
        toggle.setAttribute("aria-expanded", String(willOpen));
    });

    panel.addEventListener("click", function(event){
        event.stopPropagation();
    });
});

document.addEventListener("click", function(){
    closeBoardPostMenus();
});

document.addEventListener("keydown", function(event){
    if (event.key !== "Escape") return;
    closeBoardPostMenus();
});
