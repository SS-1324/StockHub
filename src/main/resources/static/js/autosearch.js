const searchForm = document.getElementById("glossary-search-form");
const keywordInput = document.getElementById("glossary-keyword");
const autocompleteBox = document.getElementById("autocomplete");
const autocompleteSearch = document.querySelector(".autocomplete-search");
const contextPath = window.contextPath || "";

//자동 검색에 입력 지연을 넣기 위한 변수
if (
    searchForm &&
    keywordInput &&
    autocompleteBox &&
    autocompleteSearch
) {
    let searchTimer;

//자동 완성 목록 닫기
    function closeAutocomplete() {
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
                contextPath
                + "/dictionary/autocomplete?keyword="
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
}