// 대시보드 전용: 접었다 펼치는 패널(계좌 연동/목표 설정), 기간별 손익 탭 전환,
// 보유 주식 컨트롤(주/종목 전환·정렬), 포트폴리오 분석(도넛·자산 추이 차트), 최근 활동 페이지네이션
document.addEventListener("DOMContentLoaded", () => {
    setupTogglePanel(".dashboard-link-account-toggle", "#link-account-panel");
    setupTogglePanel(".dashboard-goal-form-toggle", "#goal-form-panel");
    setupPeriodTabs();
    setupHoldingsCountToggle();
    setupHoldingsSort();
    drawPortfolioDonut();
    drawAssetTrendChart();
    setupAnalyticsDownload();
    setupTimelinePagination();
    setupBrokerageFilter();

    // 캔버스는 CSS가 아니라 직접 그린 픽셀이라, 테마 토글(header.js가 <html data-theme>를 바꿈)에
    // 맞춰 색을 다시 골라 새로 그려야 다크모드에서도 어색하지 않다
    new MutationObserver(() => {
        drawPortfolioDonut();
        drawAssetTrendChart();
    }).observe(document.documentElement, { attributes: true, attributeFilter: ["data-theme"] });
});

function isDarkTheme() {
    return document.documentElement.dataset.theme === "dark";
}

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

// 총 손익 카드의 기간 탭. 증권사 필터가 "전체"가 아닐 때는 기간별 수치가 없어(평가손익 하나뿐)
// 탭을 비활성화하는데, 그때도 재사용할 수 있도록 렌더링 로직을 밖에서도 부를 수 있게 만든다.
function setupPeriodTabs() {
    const tabs = document.querySelectorAll(".dashboard-period-tab");
    const valueEl = document.getElementById("dashboard-period-profit");
    const amountEl = document.getElementById("dashboard-period-profit-amount");
    if (tabs.length === 0 || !valueEl || !amountEl) {
        return;
    }

    tabs.forEach((tab) => {
        tab.addEventListener("click", () => {
            if (tab.disabled) {
                return;
            }
            tabs.forEach((t) => t.classList.remove("is-active"));
            tab.classList.add("is-active");
            renderActivePeriodTab();
        });
    });
}

const KRW_FORMATTER = new Intl.NumberFormat("ko-KR");

// 현재 is-active인 기간 탭 기준으로 총 손익 dd를 다시 그린다
function renderActivePeriodTab() {
    const valueEl = document.getElementById("dashboard-period-profit");
    const amountEl = document.getElementById("dashboard-period-profit-amount");
    const rateEl = document.getElementById("dashboard-period-profit-rate");
    const activeTab = document.querySelector(".dashboard-period-tab.is-active");
    if (!valueEl || !amountEl || !activeTab) {
        return;
    }

    const period = activeTab.dataset.period;
    const amount = Number(valueEl.dataset[period] || 0);
    const sign = amount > 0 ? "+" : "";
    amountEl.textContent = `${sign}${KRW_FORMATTER.format(amount)}원`;
    valueEl.classList.remove("value-positive", "value-negative");
    if (amount > 0) {
        valueEl.classList.add("value-positive");
    } else if (amount < 0) {
        valueEl.classList.add("value-negative");
    }

    if (rateEl) {
        const rate = Number(valueEl.dataset[period + "Rate"] || 0);
        const rateSign = rate > 0 ? "+" : "";
        rateEl.textContent = `(${rateSign}${rate.toFixed(2)}%)`;
        rateEl.hidden = false;
    }
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
        value: (a, b) => Number(b.dataset.value) - Number(a.dataset.value),
    };

    select.addEventListener("change", () => {
        const comparator = comparators[select.value];
        const rows = comparator ? [...originalOrder].sort(comparator) : originalOrder;
        rows.forEach((row) => tbody.appendChild(row));
    });
}

const PORTFOLIO_DONUT_COLORS = [
    "#0874f7", "#22c55e", "#f59e0b", "#e5484d", "#8b5cf6",
    "#06b6d4", "#ec4899", "#84cc16", "#f97316", "#64748b",
];

// 캔버스에 실제 도형을 그려야(=CSS가 아니어야) PNG 다운로드(canvas.toDataURL)가 가능하다
function drawPortfolioDonut() {
    const canvas = document.getElementById("portfolio-donut-canvas");
    const legend = document.getElementById("stock-portfolio-legend");
    if (!canvas || !legend) {
        return;
    }

    const rows = Array.from(document.querySelectorAll("#stock-holdings-tbody tr"));
    const holdings = rows
        .map((row) => ({ name: row.dataset.name, value: Number(row.dataset.value) || 0 }))
        .filter((h) => h.value > 0)
        .sort((a, b) => b.value - a.value);

    const total = holdings.reduce((sum, h) => sum + h.value, 0);
    if (total <= 0) {
        return;
    }

    const size = canvas.width;
    const ctx = prepareHiDpiCanvas(canvas, size, size);
    const cx = size / 2;
    const cy = size / 2;
    const outerR = size / 2 - 4;
    const innerR = outerR * 0.6;

    let startAngle = -Math.PI / 2;
    const formatter = new Intl.NumberFormat("ko-KR");
    legend.innerHTML = "";

    holdings.forEach((holding, i) => {
        const color = PORTFOLIO_DONUT_COLORS[i % PORTFOLIO_DONUT_COLORS.length];
        const percent = holding.value / total;
        const endAngle = startAngle + percent * Math.PI * 2;

        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.arc(cx, cy, outerR, startAngle, endAngle);
        ctx.closePath();
        ctx.fillStyle = color;
        ctx.fill();

        startAngle = endAngle;

        const li = document.createElement("li");
        const dot = document.createElement("span");
        dot.className = "dashboard-portfolio-legend-dot";
        dot.style.background = color;
        const name = document.createElement("span");
        name.className = "dashboard-portfolio-legend-name";
        name.textContent = holding.name;
        const percentEl = document.createElement("span");
        percentEl.className = "dashboard-portfolio-legend-percent";
        percentEl.textContent = `${(percent * 100).toFixed(1)}%`;
        li.append(dot, name, percentEl);
        legend.appendChild(li);
    });

    // 가운데를 뚫어 링(도넛) 모양으로 만들고, 종목 수/총액을 적는다 - 구멍 색은 카드 배경과 맞춘다
    const dark = isDarkTheme();
    ctx.beginPath();
    ctx.arc(cx, cy, innerR, 0, Math.PI * 2);
    ctx.fillStyle = dark ? "#1e293b" : "#ffffff";
    ctx.fill();

    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillStyle = dark ? "#f8fafc" : "#111827";
    ctx.font = "600 15px sans-serif";
    ctx.fillText(`${holdings.length}종목`, cx, cy - 9);
    ctx.fillStyle = dark ? "#94a3b8" : "#6b7280";
    ctx.font = "400 12px sans-serif";
    ctx.fillText(`${formatter.format(total)}원`, cx, cy + 11);
}

// 자산 성장 추이(주 단위 총자산 스냅샷)를 선 그래프로 그린다. asset-trend-canvas의
// data-trend 속성에 "날짜:금액,날짜:금액,..." 형태로 서버가 미리 심어둔 값을 읽어온다.
function drawAssetTrendChart() {
    const canvas = document.getElementById("asset-trend-canvas");
    if (!canvas || !canvas.dataset.trend) {
        return;
    }
    const points = canvas.dataset.trend.split(",")
        .map((pair) => {
            const [date, value] = pair.split(":");
            return { date, value: Number(value) };
        })
        .filter((p) => p.date && !Number.isNaN(p.value));
    if (points.length < 2) {
        return;
    }

    const width = canvas.width;
    const height = canvas.height;
    const ctx = prepareHiDpiCanvas(canvas, width, height);

    const padding = { top: 16, right: 16, bottom: 24, left: 68 };
    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    const values = points.map((p) => p.value);
    const minValue = Math.min(...values);
    const maxValue = Math.max(...values);
    const valueRange = maxValue - minValue || 1;
    const xOf = (i) => padding.left + (chartWidth * i) / (points.length - 1);
    const yOf = (v) => padding.top + chartHeight - ((v - minValue) / valueRange) * chartHeight;

    const compactFormatter = new Intl.NumberFormat("ko-KR", { notation: "compact", maximumFractionDigits: 1 });

    // 가로 눈금선 + 금액 라벨 (5단계)
    const dark = isDarkTheme();
    ctx.strokeStyle = dark ? "#334155" : "#e5e7eb";
    ctx.fillStyle = "#94a3b8";
    ctx.font = "11px sans-serif";
    ctx.textAlign = "right";
    ctx.textBaseline = "middle";
    ctx.lineWidth = 1;
    const gridSteps = 4;
    for (let i = 0; i <= gridSteps; i++) {
        const value = minValue + (valueRange * i) / gridSteps;
        const y = yOf(value);
        ctx.beginPath();
        ctx.moveTo(padding.left, y);
        ctx.lineTo(padding.left + chartWidth, y);
        ctx.stroke();
        ctx.fillText(`${compactFormatter.format(value)}원`, padding.left - 8, y);
    }

    // 선 아래 영역을 옅은 그라디언트로 채운다
    ctx.beginPath();
    points.forEach((p, i) => (i === 0 ? ctx.moveTo(xOf(i), yOf(p.value)) : ctx.lineTo(xOf(i), yOf(p.value))));
    ctx.lineTo(xOf(points.length - 1), padding.top + chartHeight);
    ctx.lineTo(xOf(0), padding.top + chartHeight);
    ctx.closePath();
    const gradient = ctx.createLinearGradient(0, padding.top, 0, padding.top + chartHeight);
    gradient.addColorStop(0, "rgba(8,116,247,0.18)");
    gradient.addColorStop(1, "rgba(8,116,247,0.02)");
    ctx.fillStyle = gradient;
    ctx.fill();

    // 선 자체
    ctx.beginPath();
    points.forEach((p, i) => (i === 0 ? ctx.moveTo(xOf(i), yOf(p.value)) : ctx.lineTo(xOf(i), yOf(p.value))));
    ctx.strokeStyle = "#0874f7";
    ctx.lineWidth = 2;
    ctx.stroke();

    // x축 라벨: 처음/중간/마지막 날짜만 (다 찍으면 겹쳐서 안 보인다)
    ctx.fillStyle = "#9ca3af";
    ctx.textAlign = "center";
    ctx.textBaseline = "top";
    [0, Math.floor((points.length - 1) / 2), points.length - 1].forEach((i) => {
        ctx.fillText(points[i].date, xOf(i), padding.top + chartHeight + 8);
    });
}

// 고해상도(레티나 등) 화면에서도 흐릿하지 않게, CSS 표시 크기는 그대로 두고 실제 캔버스 해상도만 키운다
function prepareHiDpiCanvas(canvas, cssWidth, cssHeight) {
    const dpr = window.devicePixelRatio || 1;
    canvas.style.width = cssWidth + "px";
    canvas.style.height = cssHeight + "px";
    canvas.width = cssWidth * dpr;
    canvas.height = cssHeight * dpr;
    const ctx = canvas.getContext("2d");
    ctx.scale(dpr, dpr);
    return ctx;
}

// 도넛 + 자산 추이 두 캔버스를 나란히 합쳐 PNG 한 장으로 내려받는다
function setupAnalyticsDownload() {
    const btn = document.getElementById("dashboard-analytics-download");
    if (!btn) {
        return;
    }
    btn.addEventListener("click", () => {
        const donut = document.getElementById("portfolio-donut-canvas");
        const trend = document.getElementById("asset-trend-canvas");
        if (!donut || !trend) {
            return;
        }
        const dpr = window.devicePixelRatio || 1;
        const donutW = donut.width / dpr;
        const donutH = donut.height / dpr;
        const trendW = trend.width / dpr;
        const trendH = trend.height / dpr;
        const gap = 24;
        const padding = 24;
        const width = donutW + trendW + gap + padding * 2;
        const height = Math.max(donutH, trendH) + padding * 2;

        const composite = document.createElement("canvas");
        composite.width = width;
        composite.height = height;
        const ctx = composite.getContext("2d");
        ctx.fillStyle = "#ffffff";
        ctx.fillRect(0, 0, width, height);
        ctx.drawImage(donut, padding, padding + (height - padding * 2 - donutH) / 2, donutW, donutH);
        ctx.drawImage(trend, padding + donutW + gap, padding + (height - padding * 2 - trendH) / 2, trendW, trendH);

        const link = document.createElement("a");
        link.download = "portfolio.png";
        link.href = composite.toDataURL("image/png");
        link.click();
    });
}

// 최근 활동을 15개씩 페이지로 나눠서 보여준다. 증권사 필터가 걸리면 그 증권사 몫만 골라서
// 다시 페이지를 나눠야 하므로, 바깥(증권사 필터)에서 다시 부를 수 있게 핸들을 남겨둔다.
let timelineFilterHandle = null;

function setupTimelinePagination() {
    const list = document.getElementById("dashboard-timeline-list");
    const pagination = document.getElementById("dashboard-timeline-pagination");
    if (!list || !pagination) {
        return;
    }
    const PAGE_SIZE = 15;
    const allItems = Array.from(list.children);
    let currentPage = 1;
    let brokerageFilter = "__ALL__";

    function eligibleItems() {
        return allItems.filter((item) => brokerageFilter === "__ALL__" || item.dataset.brokerage === brokerageFilter);
    }

    function render() {
        const items = eligibleItems();
        const pageCount = Math.max(1, Math.ceil(items.length / PAGE_SIZE));
        currentPage = Math.min(currentPage, pageCount);

        allItems.forEach((item) => {
            const position = items.indexOf(item);
            if (position === -1) {
                item.hidden = true;
                return;
            }
            const page = Math.floor(position / PAGE_SIZE) + 1;
            item.hidden = page !== currentPage;
        });

        pagination.innerHTML = "";
        if (pageCount <= 1) {
            return;
        }

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

    timelineFilterHandle = (brokerage) => {
        brokerageFilter = brokerage;
        currentPage = 1;
        render();
    };
}

// ==================== 증권사 필터 ====================
// "여러 증권사를 연동해도 전부 다 합쳐 보여준다"는 이 사이트의 원래 목적을, 합친 숫자 뒤에
// 숨겨두지 않고 "전체/증권사별"로 직접 걸러볼 수 있게 하는 기능. 페이지 맨 위에 필터 하나만 두고,
// 값이 바뀌면 아래 화면 전체(총자산~최근활동)가 서버 왕복 없이 다시 그려진다.
//
// 총 손익만 예외: "전체"일 때는 실현손익까지 포함한 정교한 계산(주/달/년 탭)을 그대로 쓰지만,
// 특정 증권사로 좁히면 계좌 단위 자산 스냅샷 이력까지 새로 쌓아야 해서 범위를 넘어가므로,
// 그 증권사의 보유 주식·상품 "평가손익" 하나로 근사하고 기간 탭은 잠근다.
function setupBrokerageFilter() {
    const select = document.getElementById("dashboard-brokerage-filter");
    if (!select) {
        return;
    }

    const dataScript = document.getElementById("dashboard-brokerage-data");
    let brokerageData = {};
    if (dataScript) {
        try {
            brokerageData = JSON.parse(dataScript.textContent);
        } catch (e) {
            brokerageData = {};
        }
    }

    select.addEventListener("change", () => {
        applyBrokerageFilter(select.value, brokerageData);
    });
}

function applyBrokerageFilter(brokerage, brokerageData) {
    const isAll = brokerage === "__ALL__";

    updateSummaryFigure("dashboard-total-asset", isAll, brokerageData.totalAsset, brokerage);
    updateSummaryFigure("dashboard-cash-balance", isAll, brokerageData.currentBalance, brokerage);
    applyProfitFilter(isAll, brokerage, brokerageData);
    applyStockBrokerageFilter(brokerage);
    applyProductBrokerageFilter(brokerage);

    if (timelineFilterHandle) {
        timelineFilterHandle(brokerage);
    }
}

function updateSummaryFigure(elementId, isAll, brokerageMap, brokerage) {
    const el = document.getElementById(elementId);
    if (!el) {
        return;
    }
    const value = isAll ? Number(el.dataset.all || 0) : Number((brokerageMap && brokerageMap[brokerage]) || 0);
    el.textContent = `${KRW_FORMATTER.format(value)}원`;
}

function applyProfitFilter(isAll, brokerage, brokerageData) {
    const periodTabs = document.querySelectorAll(".dashboard-period-tab");
    const profitEl = document.getElementById("dashboard-period-profit");
    const amountEl = document.getElementById("dashboard-period-profit-amount");
    const rateEl = document.getElementById("dashboard-period-profit-rate");
    if (!profitEl || !amountEl) {
        return;
    }

    if (isAll) {
        periodTabs.forEach((t) => {
            t.disabled = false;
        });
        renderActivePeriodTab();
        return;
    }

    // 특정 증권사로 좁힌 화면 - 기간 탭은 의미가 없어 잠그고, 평가손익 하나만 보여준다
    periodTabs.forEach((t) => {
        t.disabled = true;
    });
    const amount = (brokerageData.totalProfit && brokerageData.totalProfit[brokerage]) || 0;
    const rate = (brokerageData.totalReturnRate && brokerageData.totalReturnRate[brokerage]) || 0;
    const sign = amount > 0 ? "+" : "";
    amountEl.textContent = `${sign}${KRW_FORMATTER.format(amount)}원`;
    profitEl.classList.remove("value-positive", "value-negative");
    if (amount > 0) {
        profitEl.classList.add("value-positive");
    } else if (amount < 0) {
        profitEl.classList.add("value-negative");
    }
    if (rateEl) {
        const rateSign = rate > 0 ? "+" : "";
        rateEl.textContent = `(${rateSign}${Number(rate).toFixed(2)}%, 평가손익 기준)`;
        rateEl.hidden = false;
    }
}

// 보유 주식 표를 증권사 기준으로 다시 그린다 - 그 증권사에 없는 종목은 행을 숨기고,
// 있는 종목은 셀 내용을 그 증권사 몫(수량/평균매입가/평가금액/수익률)으로 바꿔치기한다.
function applyStockBrokerageFilter(brokerage) {
    const tbody = document.getElementById("stock-holdings-tbody");
    const toggle = document.getElementById("stock-holdings-count-toggle");
    if (!tbody) {
        return;
    }
    const isAll = brokerage === "__ALL__";
    let totalQuantity = 0;
    let visibleCount = 0;

    Array.from(tbody.querySelectorAll("tr")).forEach((row) => {
        let match = null;
        if (!isAll) {
            const accounts = JSON.parse(row.dataset.accounts || "[]");
            match = accounts.find((a) => a.brokerage === brokerage) || null;
            if (!match) {
                row.hidden = true;
                return;
            }
        }
        row.hidden = false;
        visibleCount += 1;

        const quantity = match ? match.quantity : Number(row.dataset.quantity);
        const avgPrice = match ? match.avgPrice : Number(row.dataset.avgPrice);
        const value = match ? match.currentValue : Number(row.dataset.value);
        const returnRate = match ? match.returnRate : Number(row.dataset.returnRate);
        totalQuantity += quantity;

        const cells = row.querySelectorAll("td");
        cells[1].textContent = `${KRW_FORMATTER.format(Math.round(quantity))}주`;
        cells[2].textContent = `${KRW_FORMATTER.format(Math.round(avgPrice))}원`;
        cells[4].textContent = `${KRW_FORMATTER.format(Math.round(value))}원`;
        setReturnRateCell(cells[5], returnRate);
    });

    if (toggle) {
        toggle.dataset.quantityText = `${KRW_FORMATTER.format(totalQuantity)}주`;
        toggle.dataset.stockText = `${visibleCount}종목`;
        toggle.textContent = toggle.dataset.mode === "quantity" ? toggle.dataset.quantityText : toggle.dataset.stockText;
    }
}

// 보유 상품 표도 같은 방식으로 증권사별 몫을 반영한다
function applyProductBrokerageFilter(brokerage) {
    const tbody = document.getElementById("product-holdings-tbody");
    const countEl = document.getElementById("product-holdings-count");
    if (!tbody) {
        return;
    }
    const isAll = brokerage === "__ALL__";
    let visibleCount = 0;

    Array.from(tbody.querySelectorAll("tr")).forEach((row) => {
        let match = null;
        if (!isAll) {
            const accounts = JSON.parse(row.dataset.accounts || "[]");
            match = accounts.find((a) => a.brokerage === brokerage) || null;
            if (!match) {
                row.hidden = true;
                return;
            }
        }
        row.hidden = false;
        visibleCount += 1;

        const quantity = match ? match.quantity : Number(row.dataset.quantity);
        const avgNav = match ? match.avgNav : Number(row.dataset.avgNav);
        const value = match ? match.currentValue : Number(row.dataset.value);
        const returnRate = match ? match.returnRate : Number(row.dataset.returnRate);

        const cells = row.querySelectorAll("td");
        cells[1].textContent = `${quantity.toLocaleString("ko-KR", { maximumFractionDigits: 4 })}좌`;
        cells[2].textContent = `${avgNav.toLocaleString("ko-KR", { minimumFractionDigits: 2, maximumFractionDigits: 2 })}원`;
        cells[4].textContent = `${KRW_FORMATTER.format(Math.round(value))}원`;
        setReturnRateCell(cells[5], returnRate);
    });

    if (countEl) {
        countEl.textContent = `${visibleCount}개`;
    }
}

function setReturnRateCell(cell, returnRate) {
    const sign = returnRate > 0 ? "+" : "";
    cell.textContent = `${sign}${Number(returnRate).toFixed(2)}%`;
    cell.classList.remove("value-positive", "value-negative");
    if (returnRate > 0) {
        cell.classList.add("value-positive");
    } else if (returnRate < 0) {
        cell.classList.add("value-negative");
    }
}
