// 푸터의 모달 버튼과 입력 요소를 가져옴
const footerModalButtons = document.querySelectorAll("[data-modal-target]");
const footerModalOverlays = document.querySelectorAll(".footer-modal-overlay");
const footerModalCloseButtons = document.querySelectorAll(".footer-modal-close");
const inquiryForm = document.querySelector("#inquiry-form");
const inquiryTitle = document.querySelector("#inquiry-title");
const inquiryContent = document.querySelector("#inquiry-content");
const inquiryTitleCount = document.querySelector("#inquiry-title-count");
const inquiryContentCount = document.querySelector("#inquiry-content-count");
const myInquiriesModal = document.querySelector("#my-inquiries-modal");
const myInquiryList = document.querySelector("#my-inquiry-list");

let lastModalButton = null;

// 버튼에 지정된 ID의 모달을 화면에 표시
footerModalButtons.forEach(function (button) {
    button.addEventListener("click", function () {
        const modal = document.getElementById(button.dataset.modalTarget);
        if (!modal) {
            return;
        }

        lastModalButton = button;
        modal.hidden = false;
        document.body.classList.add("modal-open");

        // 모달이 열리면 닫기 버튼으로 키보드 초점을 이동
        const closeButton = modal.querySelector(".footer-modal-close");
        closeButton?.focus();

        // 내 문의는 모달을 열 때마다 DB의 최신 목록을 다시 조회
        if (button.dataset.loadMyInquiries === "true") {
            loadMyInquiries();
        }
    });
});

// 전달받은 모달을 숨기고 원래 버튼으로 초점을 돌림
function closeFooterModal(modal) {
    modal.hidden = true;

    const hasOpenModal = Array.from(footerModalOverlays)
        .some(function (overlay) {
            return !overlay.hidden;
        });

    if (!hasOpenModal) {
        document.body.classList.remove("modal-open");
    }

    lastModalButton?.focus();
}

// 각 모달의 X 버튼으로 현재 모달을 닫음
footerModalCloseButtons.forEach(function (button) {
    button.addEventListener("click", function () {
        closeFooterModal(button.closest(".footer-modal-overlay"));
    });
});

// 모달 바깥의 어두운 배경을 누르면 닫음
footerModalOverlays.forEach(function (overlay) {
    overlay.addEventListener("click", function (event) {
        if (event.target === overlay) {
            closeFooterModal(overlay);
        }
    });
});

// Esc 키를 누르면 현재 열려 있는 모달을 닫음
document.addEventListener("keydown", function (event) {
    if (event.key !== "Escape") {
        return;
    }

    footerModalOverlays.forEach(function (overlay) {
        if (!overlay.hidden) {
            closeFooterModal(overlay);
        }
    });
});

// 문의 제목과 내용의 현재 글자 수를 표시
function updateInquiryCount() {
    if (!inquiryTitle || !inquiryContent) {
        return;
    }

    inquiryTitleCount.textContent = inquiryTitle.value.length;
    inquiryContentCount.textContent = inquiryContent.value.length;
}

inquiryTitle?.addEventListener("input", updateInquiryCount);
inquiryContent?.addEventListener("input", updateInquiryCount);
updateInquiryCount();

// 문의 상태에 맞는 글씨와 색상 클래스를 만듦
function createInquiryStatus(status) {
    const statusElement = document.createElement("span");
    const isPending = status === "PENDING";

    statusElement.className = isPending
        ? "inquiry-status status-pending"
        : "inquiry-status status-answered";
    statusElement.textContent = isPending ? "접수됨." : "처리됨.";
    return statusElement;
}

// 문단에 줄바꿈을 유지하면서 안전하게 문자열을 넣음
function createInquiryParagraph(className, text) {
    const paragraph = document.createElement("p");
    paragraph.className = className;
    paragraph.textContent = text || "";
    return paragraph;
}

// 회원이 작성한 문의 한 건을 펼침 카드로 만듦
function createMyInquiryCard(inquiry) {
    const card = document.createElement("details");
    card.className = "my-inquiry-card";

    const summary = document.createElement("summary");
    summary.className = "my-inquiry-summary";

    const summaryText = document.createElement("span");
    summaryText.className = "my-inquiry-summary-text";

    const title = document.createElement("strong");
    title.textContent = inquiry.title;

    const date = document.createElement("small");
    date.textContent = inquiry.createAtStr || "";

    summaryText.append(title, date);
    summary.append(summaryText, createInquiryStatus(inquiry.status));

    const body = document.createElement("div");
    body.className = "my-inquiry-body";
    body.append(
        createInquiryParagraph("my-inquiry-label", "문의 내용"),
        createInquiryParagraph("my-inquiry-content", inquiry.content)
    );

    // 관리자가 처리한 문의에는 답변 내용을 문의 아래에 표시
    if (inquiry.status !== "PENDING" && inquiry.answer) {
        const answerBox = document.createElement("div");
        answerBox.className = "my-inquiry-answer";
        answerBox.append(
            createInquiryParagraph("my-inquiry-label", "관리자 답변"),
            createInquiryParagraph("my-inquiry-answer-content", inquiry.answer)
        );

        if (inquiry.answeredAtStr) {
            answerBox.append(
                createInquiryParagraph(
                    "my-inquiry-answer-date",
                    inquiry.answeredAtStr
                )
            );
        }
        body.append(answerBox);
    }

    card.append(summary, body);
    return card;
}

// 로그인 회원의 문의 목록을 JSON으로 받아 모달에 표시
async function loadMyInquiries() {
    if (!myInquiriesModal || !myInquiryList) {
        return;
    }

    myInquiryList.replaceChildren(
        createInquiryParagraph(
            "my-inquiry-message",
            "문의 내역을 불러오는 중입니다."
        )
    );

    try {
        const response = await fetch(myInquiriesModal.dataset.listUrl, {
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        });
        const result = await response.json();

        if (!response.ok || !result.success) {
            throw new Error(result.message || "문의 내역을 불러오지 못했습니다.");
        }

        myInquiryList.replaceChildren();
        if (!result.data || result.data.length === 0) {
            myInquiryList.append(
                createInquiryParagraph(
                    "my-inquiry-message",
                    "등록한 문의가 없습니다."
                )
            );
            return;
        }

        result.data.forEach(function (inquiry) {
            myInquiryList.append(createMyInquiryCard(inquiry));
        });
    } catch (error) {
        myInquiryList.replaceChildren(
            createInquiryParagraph(
                "my-inquiry-message my-inquiry-error",
                error.message || "문의 내역을 불러오지 못했습니다."
            )
        );
    }
}

// 문의 전송 전에 공백 입력과 최대 길이를 다시 검사
inquiryForm?.addEventListener("submit", function (event) {
    const title = inquiryTitle.value.trim();
    const content = inquiryContent.value.trim();

    if (!title || title.length > 20) {
        event.preventDefault();
        alert("문의 제목은 20자 이내로 입력해주세요.");
        inquiryTitle.focus();
        return;
    }

    if (!content || content.length > 200) {
        event.preventDefault();
        alert("문의 내용은 200자 이내로 입력해주세요.");
        inquiryContent.focus();
    }
});

// 서버 검사 오류가 있으면 문의 모달을 다시 열어 입력 내용을 보여줌
const initiallyOpenModal = Array.from(footerModalOverlays)
    .find(function (overlay) {
        return overlay.dataset.openOnLoad === "true";
    });

if (initiallyOpenModal) {
    initiallyOpenModal.hidden = false;
    document.body.classList.add("modal-open");
}
