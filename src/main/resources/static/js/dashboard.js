// 대시보드 전용: 접었다 펼치는 패널(계좌 연동/목표 설정), 기간별 손익 탭 전환,
// 보유 주식 컨트롤(주/종목 전환·정렬·목록-차트 전환), 최근 활동 페이지네이션
document.addEventListener("DOMContentLoaded", () => {
    setupTogglePanel(".dashboard-link-account-toggle", "#link-account-panel");
    setupTogglePanel(".dashboard-goal-form-toggle", "#goal-form-panel");
    setupPeriodTabs();
    setupHoldingsCountToggle();
    setupHoldingsSort();
    setupHoldingsView();
    setupTimelinePagination();
});

function setupTogglePanel(toggleSelector, panelSelector) {
    const toggle = document.querySelector(toggleSelector);
    const panel = document.querySelector(panelSelector);
    if (!toggle || !panel) {
        return;
    }
    toggle.addEventListener("click", () => {
        const willOpen = panel.hidden;
        panel.hidden = !willOpen;
        toggle.setAttribute("aria-expanded", String(willOpen));
    });
}

function setupPeriodTabs() {
    const tabs = document.querySelectorAll(".dashboard-period-tab");
    const valueEl = document.getElementById("dashboard-period-profit");
    const amountEl = document.getElementById("dashboard-period-profit-amount");
    const rateEl = document.getElementById("dashboard-period-profit-rate");
    if (tabs.length === 0 || !valueEl || !amountEl) {
        return;
    }
    const formatter = new Intl.NumberFormat("ko-KR");

    tabs.forEach((tab) => {
        tab.addEventListener("click", () => {
            tabs.forEach((t) => t.classList.remove("is-active"));
            tab.classList.add("is-active");

            const period = tab.dataset.period;
            const amount = Number(valueEl.dataset[period] || 0);
            const sign = amount > 0 ? "+" : "";
            amountEl.textContent = `${sign}${formatter.format(amount)}원`;
            valueEl.classList.remove("value-positive", "value-negative");
            if (amount > 0) {
                valueEl.classList.add("value-positive");
            } else if (amount < 0) {
                valueEl.classList.add("value-negative");
            }

            // 수익률(%)은 "전체" 기준일 때만 의미가 있어서 그때만 보여준다
            if (rateEl) {
                rateEl.hidden = period !== "all";
            }
        });
    });
}

// "1주 · 1종목"을 한 줄에 다 보여주는 대신, 버튼 하나로 둘을 번갈아 보여준다
function setupHoldingsCountToggle() {
    const toggle = document.getElementById("stock-holdings-count-toggle");
    if (!toggle) {
        return;
    }
    toggle.addEventListener("click", () => {
        const nextMode = toggle.dataset.mode === "quantity" ? "stock" : "quantity";
        toggle.dataset.mode = nextMode;
        toggle.textContent = nextMode === "quantity" ? toggle.dataset.quantityText : toggle.dataset.stockText;
    });
}

// 보유 주식 표를 정렬 기준에 맞춰 다시 그린다 (서버 왕복 없이 이미 받아온 행을 그대로 재정렬)
function setupHoldingsSort() {
    const select = document.getElementById("stock-holdings-sort");
    const tbody = document.getElementById("stock-holdings-tbody");
    if (!select || !tbody) {
        return;
    }
    const originalOrder = Array.from(tbody.querySelectorAll("tr"));

    const comparators = {
        default: null,
        returnRate: (a, b) => Number(b.dataset.returnRate) - Number(a.dataset.returnRate),
        quantity: (a, b) => Number(b.dataset.quantity) - Number(a.dataset.quantity),
        name: (a, b) => a.dataset.name.localeCompare(b.dataset.name, "ko"),
        price: (a, b) => Number(b.dataset.price) - Number(a.dataset.price),
    };

    select.addEventListener("change", () => {
        const comparator = comparators[select.value];
        const rows = comparator ? [...originalOrder].sort(comparator) : originalOrder;
        rows.forEach((row) => tbody.appendChild(row));
    });
}

// 보유 주식을 표 대신 원그래프(도넛 차트)로도 볼 수 있게 전환한다
function setupHoldingsView() {
    const tabs = document.querySelectorAll(".dashboard-view-tab");
    const listView = document.getElementById("stock-holdings-list-view");
    const chartView = document.getElementById("stock-holdings-chart-view");
    if (tabs.length === 0 || !listView || !chartView) {
        return;
    }

    let chartBuilt = false;

    tabs.forEach((tab) => {
        tab.addEventListener("click", () => {
            tabs.forEach((t) => t.classList.remove("is-active"));
            tab.classList.add("is-active");

            const view = tab.dataset.view;
            listView.hidden = view !== "list";
            chartView.hidden = view !== "chart";

            if (view === "chart" && !chartBuilt) {
                buildPortfolioDonut();
                chartBuilt = true;
            }
        });
    });
}

const PORTFOLIO_DONUT_COLORS = [
    "#0874f7", "#22c55e", "#f59e0b", "#e5484d", "#8b5cf6",
    "#06b6d4", "#ec4899", "#84cc16", "#f97316", "#64748b",
];

function buildPortfolioDonut() {
    const rows = Array.from(document.querySelectorAll("#stock-holdings-tbody tr"));
    const donut = document.getElementById("stock-portfolio-donut");
    const legend = document.getElementById("stock-portfolio-legend");
    if (!donut || !legend) {
        return;
    }

    const holdings = rows
        .map((row) => ({ name: row.dataset.name, value: Number(row.dataset.value) || 0 }))
        .filter((h) => h.value > 0)
        .sort((a, b) => b.value - a.value);

    const total = holdings.reduce((sum, h) => sum + h.value, 0);
    if (total <= 0) {
        return;
    }

    const formatter = new Intl.NumberFormat("ko-KR");
    let cursor = 0;
    const stops = [];
    legend.innerHTML = "";

    holdings.forEach((holding, i) => {
        const color = PORTFOLIO_DONUT_COLORS[i % PORTFOLIO_DONUT_COLORS.length];
        const percent = (holding.value / total) * 100;
        const start = cursor;
        const end = cursor + percent;
        stops.push(`${color} ${start}% ${end}%`);
        cursor = end;

        const li = document.createElement("li");
        const dot = document.createElement("span");
        dot.className = "dashboard-portfolio-legend-dot";
        dot.style.background = color;
        const name = document.createElement("span");
        name.className = "dashboard-portfolio-legend-name";
        name.textContent = holding.name;
        const percentEl = document.createElement("span");
        percentEl.className = "dashboard-portfolio-legend-percent";
        percentEl.textContent = `${percent.toFixed(1)}%`;
        li.append(dot, name, percentEl);
        legend.appendChild(li);
    });

    donut.style.background = `conic-gradient(${stops.join(", ")})`;
    donut.setAttribute("data-center-label", `${holdings.length}종목\n${formatter.format(total)}원`);
}

// 최근 활동을 15개씩 페이지로 나눠서 보여준다
function setupTimelinePagination() {
    const list = document.getElementById("dashboard-timeline-list");
    const pagination = document.getElementById("dashboard-timeline-pagination");
    if (!list || !pagination) {
        return;
    }
    const PAGE_SIZE = 15;
    const items = Array.from(list.children);
    const pageCount = Math.ceil(items.length / PAGE_SIZE);
    if (pageCount <= 1) {
        return;
    }

    let currentPage = 1;

    function render() {
        items.forEach((item, i) => {
            const page = Math.floor(i / PAGE_SIZE) + 1;
            item.hidden = page !== currentPage;
        });

        pagination.innerHTML = "";

        const prevBtn = document.createElement("button");
        prevBtn.type = "button";
        prevBtn.className = "dashboard-timeline-page-btn";
        prevBtn.textContent = "이전";
        prevBtn.disabled = currentPage === 1;
        prevBtn.addEventListener("click", () => {
            currentPage -= 1;
            render();
        });
        pagination.appendChild(prevBtn);

        for (let page = 1; page <= pageCount; page += 1) {
            const btn = document.createElement("button");
            btn.type = "button";
            btn.className = "dashboard-timeline-page-btn" + (page === currentPage ? " is-active" : "");
            btn.textContent = String(page);
            btn.addEventListener("click", () => {
                currentPage = page;
                render();
            });
            pagination.appendChild(btn);
        }

        const nextBtn = document.createElement("button");
        nextBtn.type = "button";
        nextBtn.className = "dashboard-timeline-page-btn";
        nextBtn.textContent = "다음";
        nextBtn.disabled = currentPage === pageCount;
        nextBtn.addEventListener("click", () => {
            currentPage += 1;
            render();
        });
        pagination.appendChild(nextBtn);
    }

    render();
}
