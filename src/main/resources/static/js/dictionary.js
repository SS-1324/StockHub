//카테고리 이미지 미리보여주기 기능
const categories = document.querySelectorAll(".category");
const previewImage = document.getElementById("category-preview-image");

if (previewImage) {
    categories.forEach(category => {
        category.addEventListener("mouseenter", function () {
            previewImage.src = this.dataset.image;
            previewImage.alt = this.dataset.alt;
            previewImage.classList.add("show");
        });

        category.addEventListener("mouseleave", function () {
            previewImage.classList.remove("show");
        });
    });
}

const searchForm = document.getElementById("glossary-search-form");
const keywordInput = document.getElementById("glossary-keyword");
const autocompleteBox = document.getElementById("autocomplete");
const autocompleteSearch = document.querySelector(".autocomplete-search");
const autocompleteUrl = searchForm.dataset.autocompleteUrl;

//자동 검색에 입력 지연을 넣기 위한 변수
let searchTimer;

//자동 완성 목록 닫기
function closeAutocomplete(){
    autocompleteBox.replaceChildren();
    autocompleteBox.classList.remove("show");
}

// 검색창에 글자를 입력할 때 실행
keywordInput.addEventListener("input", function () {

    // 이전에 예약된 자동완성 요청 취소
    clearTimeout(searchTimer);

    const keyword = keywordInput.value.trim();

    // 입력값이 없으면 자동완성 목록 닫기
    if (keyword === "") {
        closeAutocomplete();
        return;
    }

    // 입력이 멈춘 뒤 200ms 후 서버 요청
    searchTimer = setTimeout(function () {

        fetch(
            autocompleteUrl
            + "?keyword="
            + encodeURIComponent(keyword)
        )
            .then(function (response) {

                if (!response.ok) {
                    throw new Error("자동완성 요청 실패");
                }

                return response.json();
            })
            .then(function (terms) {

                // 기존 추천 목록 제거
                closeAutocomplete();

                // 결과가 없다면 종료
                if (terms.length === 0) {
                    return;
                }

                // 서버에서 받은 용어마다 버튼 생성
                terms.forEach(function (term) {

                    const item = document.createElement("button");

                    item.type = "button";
                    item.className = "autocomplete-item";
                    item.textContent = term;

                    // 자동완성 항목 클릭
                    item.addEventListener("click", function () {

                        keywordInput.value = term;

                        closeAutocomplete();

                        // 선택한 용어로 검색 실행
                        searchForm.requestSubmit();
                    });

                    // 자동완성 박스 안에 버튼 추가
                    autocompleteBox.appendChild(item);
                });

                // 자동완성 목록 표시
                autocompleteBox.classList.add("show");
            })
            .catch(function (error) {

                console.error(error);
                closeAutocomplete();
            });
    }, 200);
});


// 자동완성 검색 영역 바깥을 클릭하면 목록 닫기
document.addEventListener("click", function (event) {

    if (!autocompleteSearch.contains(event.target)) {
        clearTimeout(searchTimer);
        closeAutocomplete();
    }
});


// ESC 키를 누르면 자동완성 목록 닫기
keywordInput.addEventListener("keydown", function (event) {

    if (event.key === "Escape") {
        clearTimeout(searchTimer);
        closeAutocomplete();
        keywordInput.blur();
    }
});
// 용어 카드 확대
const cards = document.querySelectorAll(".glossary-item");
const overlay = document.querySelector(".glossary-overlay");

let enlargedCard = null;


// 용어 카드 클릭
cards.forEach(function (card) {

    card.addEventListener("click", function () {

        // 이미 확대된 카드가 있으면 중복 실행 방지
        if (enlargedCard) {
            return;
        }


        // 클릭한 용어 카드를 그대로 복제
        enlargedCard = card.cloneNode(true);

        // 확대본 전용 클래스 추가
        enlargedCard.classList.add("glossary-enlarged");

        // body에 추가
        document.body.appendChild(enlargedCard);

        // 배경 어둡게
        overlay.classList.add("active");

    });

});


// 어두운 배경 클릭하면 닫기
overlay.addEventListener("click", function () {

    if (enlargedCard) {

        enlargedCard.remove();
        enlargedCard = null;

    }

    overlay.classList.remove("active");

});


// ESC로도 닫기
document.addEventListener("keydown", function (event) {

    if (event.key === "Escape" && enlargedCard) {

        enlargedCard.remove();
        enlargedCard = null;

        overlay.classList.remove("active");

    }

});