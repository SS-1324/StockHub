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
    setupAnalyticsShare();
    setupAnalyticsDetailModal();
    setupTimelinePagination();
    setupTimelineModal();
    setupBrokerageFilter();

    // 캔버스는 CSS가 아니라 직접 그린 픽셀이라, 테마 토글(header.js가 <html data-theme>를 바꿈)에
    // 맞춰 색을 다시 골라 새로 그려야 다크모드에서도 어색하지 않다
    new MutationObserver(() => {
        drawPortfolioDonut();
        drawAssetTrendChart();
    }).observe(document.documentElement, { attributes: true, attributeFilter: ["data-theme"] });

    // 창 크기가 바뀌면(사이드바 레이아웃이 1050px에서 접히는 것도 포함) 자산 추이 차트가
    // 실제 폭에 맞춰 다시 그려지도록 한다. 리사이즈 중 매 프레임 다시 그리진 않게 디바운스한다.
    let resizeRedrawTimer = null;
    window.addEventListener("resize", () => {
        clearTimeout(resizeRedrawTimer);
        resizeRedrawTimer = setTimeout(drawAssetTrendChart, 150);
    });
});

function isDarkTheme() {
    return document.documentElement.dataset.theme === "dark";
}

function addDays(date, days) {
    const result = new Date(date);
    result.setDate(result.getDate() + days);
    return result;
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

// 파스텔톤으로 통일 - 진한 원색은 종목이 많아질수록 화면이 번잡해 보여서 톤을 낮췄다
const PORTFOLIO_DONUT_COLORS = [
    "#93c5fd", "#86efac", "#fde68a", "#fca5a5", "#c4b5fd",
    "#67e8f9", "#f9a8d4", "#bef264", "#fdba74", "#cbd5e1",
];

const LEGEND_TWO_COL_THRESHOLD = 6;

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

    // 보유 종목은 개수 제한 없이 전부 보여준다 - 대신 범례 영역 자체가 오른쪽(추이+통계) 높이에
    // 맞춰 고정된 최대 높이를 갖고, 그 안에서 넘치는 만큼만 스크롤된다(CSS 쪽 처리).
    // 그래야 종목이 몇 개든 왼쪽 카드와 오른쪽 카드 높이가 항상 맞는다.
    const displayHoldings = holdings;

    const size = canvas.width;
    const ctx = prepareHiDpiCanvas(canvas, size, size);
    const cx = size / 2;
    const cy = size / 2;
    const outerR = size / 2 - 4;
    const innerR = outerR * 0.6;

    let startAngle = -Math.PI / 2;
    const formatter = new Intl.NumberFormat("ko-KR");
    legend.innerHTML = "";

    displayHoldings.forEach((holding, i) => {
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

    // 보유 종목이 많을 때(예: 12개) 세로로 계속 늘어지지 않도록 6개|6개 식 2열로 접는다
    legend.classList.toggle("is-two-col", displayHoldings.length > LEGEND_TWO_COL_THRESHOLD);

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
    const allPoints = canvas.dataset.trend.split(",")
        .map((pair) => {
            const [date, value] = pair.split(":");
            return { date, value: Number(value) };
        })
        .filter((p) => p.date && !Number.isNaN(p.value));

    // 최대 1년까지만 보여준다. 서버는 계좌 생성 이후 실제 이력 전체를 보내주므로, 시작한 지
    // 3개월 된 사람은 이 필터를 거쳐도 자연스럽게 3개월치 그대로 남는다("사람에 맞는" 그래프).
    const oneYearAgo = allPoints.length > 0 ? addDays(new Date(allPoints[allPoints.length - 1].date), -365) : null;
    const points = oneYearAgo ? allPoints.filter((p) => new Date(p.date) >= oneYearAgo) : allPoints;
    if (points.length < 2) {
        return;
    }

    // 캔버스 attribute(900)를 그대로 쓰면 실제로는 CSS가 눌러 줄인 폭(사이드바가 생기면서
    // 더 좁아졌다)과 안 맞아 안의 글자까지 통째로 쪼그라들었다. 인라인 스타일을 비워 CSS의
    // width:100%가 다시 적용되게 한 뒤 실제 렌더링 폭을 재서, 그 폭에 맞는 해상도로 새로 그린다.
    canvas.style.width = "";
    const width = Math.max(260, Math.round(canvas.getBoundingClientRect().width) || 900);
    const height = 220;
    const ctx = prepareHiDpiCanvas(canvas, width, height);

    // 폭이 좁으면 y축 금액 라벨도 자리를 덜 차지하게 줄인다
    const padding = { top: 16, right: 16, bottom: 28, left: width < 420 ? 56 : 74 };
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
    gradient.addColorStop(0, "rgba(147,197,253,0.35)");
    gradient.addColorStop(1, "rgba(147,197,253,0.03)");
    ctx.fillStyle = gradient;
    ctx.fill();

    // 선 자체 (파스텔톤 - 원색 파랑보다 톤을 낮췄다)
    ctx.beginPath();
    points.forEach((p, i) => (i === 0 ? ctx.moveTo(xOf(i), yOf(p.value)) : ctx.lineTo(xOf(i), yOf(p.value))));
    ctx.strokeStyle = "#60a5fa";
    ctx.lineWidth = 2;
    ctx.stroke();

    // x축 라벨: 처음/중간/마지막 날짜만 (다 찍으면 겹쳐서 안 보인다). 처음 라벨은 왼쪽 정렬,
    // 마지막 라벨은 오른쪽 정렬로 바깥쪽을 향하게 해서 왼쪽 y축의 "0원" 라벨과 겹치지 않게 한다
    // (가운데 정렬로 두면 좌하단 모서리에서 두 라벨이 바짝 붙어 읽기 어려웠다)
    ctx.fillStyle = "#9ca3af";
    ctx.textBaseline = "top";
    const labelIndexes = [0, Math.floor((points.length - 1) / 2), points.length - 1];
    labelIndexes.forEach((i, idx) => {
        ctx.textAlign = idx === 0 ? "left" : idx === labelIndexes.length - 1 ? "right" : "center";
        ctx.fillText(points[i].date, xOf(i), padding.top + chartHeight + 10);
    });
}

// ==================== 통계 카드 상세정보 모달 ====================
// 매매 승률/평균 보유기간/최고·최악의 매매/집중도/국내해외 비중 카드를 눌렀을 때, 그 요약 숫자
// 뒤에 깔린 실제 데이터(전체 매매 내역 또는 종목별 비중)를 표/막대 목록으로 보여준다.

function readRealizedProfits() {
    const script = document.getElementById("dashboard-realized-profits");
    if (!script) {
        return [];
    }
    try {
        return JSON.parse(script.textContent) || [];
    } catch (e) {
        return [];
    }
}

function readHoldingsFromTable() {
    return Array.from(document.querySelectorAll("#stock-holdings-tbody tr"))
        .map((row) => ({
            name: row.dataset.name,
            value: Number(row.dataset.value) || 0,
            isForeign: row.dataset.foreign === "true",
        }))
        .filter((h) => h.value > 0);
}

// ==================== 모달 공통 처리 ====================
// 포트폴리오 상세정보 모달, 최근 활동 더보기 모달 등 이 페이지에 뜨는 모든 모달이 공유하는
// 열기/닫기 + 배경 스크롤 잠금. 열려있는 모달 개수를 세어, 하나라도 남아있으면 계속 잠가둔다
// (동시에 두 개가 뜨진 않지만, 방어적으로 카운트 기반으로 짰다).
let openModalCount = 0;

function setBodyScrollLocked(locked) {
    document.body.classList.toggle("dashboard-scroll-locked", locked);
}

function registerModal(modal) {
    if (!modal) {
        return null;
    }
    function close() {
        if (modal.hidden) {
            return;
        }
        modal.hidden = true;
        openModalCount = Math.max(0, openModalCount - 1);
        setBodyScrollLocked(openModalCount > 0);
    }
    function open() {
        if (!modal.hidden) {
            return;
        }
        modal.hidden = false;
        openModalCount += 1;
        setBodyScrollLocked(true);
    }
    modal.querySelectorAll("[data-modal-close]").forEach((el) => el.addEventListener("click", close));
    return { open, close, modal };
}

// Esc는 열려있는 모달이면 뭐든 닫는다(모달이 하나여도, 나중에 여러 개가 생겨도 그대로 동작)
document.addEventListener("keydown", (e) => {
    if (e.key !== "Escape") {
        return;
    }
    document.querySelectorAll(".dashboard-modal:not([hidden])").forEach((modal) => {
        modal.hidden = true;
    });
    openModalCount = 0;
    setBodyScrollLocked(false);
});

// 사이드바 "최근 활동" 위젯의 더보기 버튼 - 전체 목록 + 페이지네이션이 들어있는 모달을 연다
function setupTimelineModal() {
    const btn = document.getElementById("dashboard-timeline-more");
    const modalCtl = registerModal(document.getElementById("dashboard-timeline-modal"));
    if (!btn || !modalCtl) {
        return;
    }
    btn.addEventListener("click", () => modalCtl.open());
}

function setupAnalyticsDetailModal() {
    const modal = document.getElementById("dashboard-analytics-modal");
    const titleEl = document.getElementById("dashboard-modal-title");
    const bodyEl = document.getElementById("dashboard-modal-body");
    const buttons = document.querySelectorAll(".dashboard-analytics-stat-clickable");
    const modalCtl = registerModal(modal);
    if (!modalCtl || !titleEl || !bodyEl || buttons.length === 0) {
        return;
    }

    const DETAIL_BUILDERS = {
        winRate: () => buildWinRateDetailHtml(),
        holdingDays: () => buildHoldingDaysDetailHtml(),
        pinpoint: (btn) => buildPinpointDetailHtml(btn.dataset.highlight),
        concentration: () => buildConcentrationDetailHtml(),
        region: () => buildRegionDetailHtml(),
    };

    buttons.forEach((btn) => {
        btn.addEventListener("click", () => {
            const builder = DETAIL_BUILDERS[btn.dataset.detail];
            if (!builder) {
                return;
            }
            titleEl.textContent = btn.dataset.detailTitle || "상세정보";
            bodyEl.innerHTML = builder(btn);
            modalCtl.open();
        });
    });
}

// 매매 승률 상세: "총 n건 중 이득/손해 몇 건"으로 요약 + 승/패를 한 막대 안에 비율대로 나눠 보여준다
// (승/패 막대를 따로 두 줄로 그리면 각 줄의 남는 회색 여백이 뭘 뜻하는지 애매해서, 하나의 막대를
//  승/패 비율대로 나눠 채우는 방식으로 바꿨다 - 나머지 회색은 "본전"인 경우만 남는다)
function buildWinRateDetailHtml() {
    const trades = readRealizedProfits();
    if (trades.length === 0) {
        return `<p class="dashboard-modal-empty">아직 마감된(전량 매도한) 매매 내역이 없습니다.</p>`;
    }

    const total = trades.length;
    const wins = trades.filter((t) => t.profitAmount > 0).length;
    const losses = trades.filter((t) => t.profitAmount < 0).length;
    const evens = total - wins - losses;
    const winPct = (wins / total) * 100;
    const lossPct = (losses / total) * 100;

    const evenText = evens > 0 ? `, 본전 ${evens}건` : "";
    return `<p class="dashboard-modal-summary">총 ${total}건의 매매 중 <strong>이득 ${wins}건</strong>, <strong>손해 ${losses}건</strong>${evenText}이에요. (승률 ${winPct.toFixed(1)}%)</p>
        <div class="dashboard-modal-split-bar">
            <span class="dashboard-modal-split-segment" style="width:${winPct.toFixed(1)}%;background:#e5484d;"></span>
            <span class="dashboard-modal-split-segment" style="width:${lossPct.toFixed(1)}%;background:#0874f7;"></span>
        </div>
        <div class="dashboard-modal-split-legend">
            <span><i style="background:#e5484d;"></i>이득 ${wins}건 (${winPct.toFixed(1)}%)</span>
            <span><i style="background:#0874f7;"></i>손해 ${losses}건 (${lossPct.toFixed(1)}%)</span>
        </div>`;
}

// 평균 보유기간 상세: 최장/최단 보유 종목, 평균, 중앙값 4칸
function buildHoldingDaysDetailHtml() {
    const trades = readRealizedProfits();
    if (trades.length === 0) {
        return `<p class="dashboard-modal-empty">아직 마감된(전량 매도한) 매매 내역이 없습니다.</p>`;
    }

    const longest = trades.reduce((a, b) => (b.holdingDays > a.holdingDays ? b : a));
    const shortest = trades.reduce((a, b) => (b.holdingDays < a.holdingDays ? b : a));
    const days = trades.map((t) => t.holdingDays).sort((a, b) => a - b);
    const avg = days.reduce((sum, d) => sum + d, 0) / days.length;
    const mid = Math.floor(days.length / 2);
    const median = days.length % 2 === 0 ? (days[mid - 1] + days[mid]) / 2 : days[mid];

    function stat(label, value, sub, range) {
        return `<div class="dashboard-modal-mini-stat">
            <p class="dashboard-modal-mini-stat-label">${label}</p>
            <p class="dashboard-modal-mini-stat-value">${value}</p>
            ${sub ? `<p class="dashboard-modal-mini-stat-sub">${escapeHtml(sub)}</p>` : ""}
            ${range ? `<p class="dashboard-modal-mini-stat-range">${escapeHtml(range)}</p>` : ""}
        </div>`;
    }

    return `<div class="dashboard-modal-stat-grid">
        ${stat("최장 보유", `${longest.holdingDays}일`, longest.itemName, `${longest.buyAt} ~ ${longest.sellAt}`)}
        ${stat("최단 보유", `${shortest.holdingDays}일`, shortest.itemName, `${shortest.buyAt} ~ ${shortest.sellAt}`)}
        ${stat("평균", `${avg.toFixed(1)}일`)}
        ${stat("중앙값", `${median}일`)}
    </div>`;
}

// 최고·최악의 매매 상세: "언제 얼마에 사서 얼마에 팔았다"를 핀포인트로 보여준다
function buildPinpointDetailHtml(highlight) {
    const trades = readRealizedProfits();
    if (trades.length === 0) {
        return `<p class="dashboard-modal-empty">아직 마감된(전량 매도한) 매매 내역이 없습니다.</p>`;
    }

    const target = trades.reduce((best, t) => {
        if (!best) return t;
        const better = highlight === "worst" ? t.profitAmount < best.profitAmount : t.profitAmount > best.profitAmount;
        return better ? t : best;
    }, null);

    const sign = target.profitAmount > 0 ? "+" : "";
    const profitClass = target.profitAmount > 0 ? "value-positive" : target.profitAmount < 0 ? "value-negative" : "";

    function row(label, value) {
        return `<div class="dashboard-modal-pinpoint-row">
            <span class="dashboard-modal-pinpoint-label">${label}</span>
            <span class="dashboard-modal-pinpoint-value">${value}</span>
        </div>`;
    }

    return `<p class="dashboard-modal-summary"><strong>${escapeHtml(target.itemName)}</strong></p>
        ${row("매수", `${escapeHtml(target.buyAt)} · ${KRW_FORMATTER.format(target.buyPrice)}원 × ${target.quantity}`)}
        ${row("매도", `${escapeHtml(target.sellAt)} · ${KRW_FORMATTER.format(target.sellPrice)}원 × ${target.quantity}`)}
        ${row("보유기간", `${target.holdingDays}일`)}
        ${row("손익", `<span class="${profitClass}">${sign}${KRW_FORMATTER.format(target.profitAmount)}원 (${sign}${Number(target.returnRate).toFixed(2)}%)</span>`)}`;
}

function buildConcentrationDetailHtml() {
    const holdings = readHoldingsFromTable().sort((a, b) => b.value - a.value);
    if (holdings.length === 0) {
        return `<p class="dashboard-modal-empty">보유 중인 주식이 없습니다.</p>`;
    }
    const total = holdings.reduce((sum, h) => sum + h.value, 0);
    const rows = holdings.map((h, i) => {
        const percent = (h.value / total) * 100;
        const color = PORTFOLIO_DONUT_COLORS[i % PORTFOLIO_DONUT_COLORS.length];
        return `<div class="dashboard-modal-bar-row">
            <span class="dashboard-modal-bar-name">${escapeHtml(h.name)}</span>
            <span class="dashboard-modal-bar-track"><span class="dashboard-modal-bar-fill" style="width:${percent.toFixed(1)}%;background:${color};"></span></span>
            <span class="dashboard-modal-bar-percent">${percent.toFixed(1)}%</span>
        </div>`;
    }).join("");
    return `<p class="dashboard-modal-summary">보유 종목 ${holdings.length}개, 총 평가금액 ${KRW_FORMATTER.format(total)}원 기준 비중입니다.</p>${rows}`;
}

function buildRegionDetailHtml() {
    const holdings = readHoldingsFromTable();
    if (holdings.length === 0) {
        return `<p class="dashboard-modal-empty">보유 중인 주식이 없습니다.</p>`;
    }
    const domestic = holdings.filter((h) => !h.isForeign).sort((a, b) => b.value - a.value);
    const foreign = holdings.filter((h) => h.isForeign).sort((a, b) => b.value - a.value);
    const domesticTotal = domestic.reduce((sum, h) => sum + h.value, 0);
    const foreignTotal = foreign.reduce((sum, h) => sum + h.value, 0);
    const total = domesticTotal + foreignTotal;

    function group(label, list, listTotal, color) {
        if (list.length === 0) {
            return "";
        }
        const percent = total > 0 ? (listTotal / total) * 100 : 0;
        const names = list.map((h) => escapeHtml(h.name)).join(", ");
        return `<div class="dashboard-modal-bar-row">
            <span class="dashboard-modal-bar-name">${label} (${list.length}종목)</span>
            <span class="dashboard-modal-bar-track"><span class="dashboard-modal-bar-fill" style="width:${percent.toFixed(1)}%;background:${color};"></span></span>
            <span class="dashboard-modal-bar-percent">${percent.toFixed(1)}%</span>
        </div>
        <p class="dashboard-modal-summary">${escapeHtml(names)}</p>`;
    }

    return group("국내", domestic, domesticTotal, "#93c5fd") + group("해외", foreign, foreignTotal, "#fde68a");
}

function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str == null ? "" : String(str);
    return div.innerHTML;
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

// ==================== 포트폴리오 분석 영역 PNG 저장 ====================
// 예전엔 도넛/추이 캔버스 두 개만 나란히 합성해서, 종목 이름·범례·통계 텍스트가 전부 빠진 채로
// 저장됐다. 이제는 #dashboard-analytics-capture DOM 전체를 "스캔"하듯 그대로 이미지로 옮긴다.
// 외부 라이브러리(html2canvas 등) 없이, DOM을 복제해 계산된 스타일을 인라인으로 박아 넣고
// SVG foreignObject에 담아 이미지로 그리는 방식(표준 브라우저 API만 사용)을 쓴다.

// <canvas>는 outerHTML로 복제해도 그려진 픽셀이 안 딸려오므로, 복제본에서는 <img>로 바꿔치기한다
function inlineStylesRecursive(sourceEl, cloneEl) {
    if (sourceEl.tagName === "CANVAS") {
        const img = document.createElement("img");
        img.setAttribute("style", computedStyleText(sourceEl));
        img.setAttribute("width", sourceEl.clientWidth);
        img.setAttribute("height", sourceEl.clientHeight);
        img.src = sourceEl.toDataURL("image/png");
        cloneEl.replaceWith(img);
        return;
    }

    cloneEl.setAttribute("style", computedStyleText(sourceEl));

    const sourceChildren = Array.from(sourceEl.children);
    const cloneChildren = Array.from(cloneEl.children);
    sourceChildren.forEach((child, i) => inlineStylesRecursive(child, cloneChildren[i]));
}

function computedStyleText(el) {
    const computed = getComputedStyle(el);
    let text = "";
    for (let i = 0; i < computed.length; i++) {
        const prop = computed[i];
        text += `${prop}:${computed.getPropertyValue(prop)};`;
    }
    return text;
}

// #dashboard-analytics-capture를 통째로 PNG용 <canvas>로 그려 반환한다 (다운로드/공유가 공유해서 쓴다)
function captureAnalyticsCanvas() {
    const source = document.getElementById("dashboard-analytics-capture");
    if (!source) {
        return Promise.reject(new Error("no capture target"));
    }

    return new Promise((resolve, reject) => {
        const rect = source.getBoundingClientRect();
        const width = Math.ceil(rect.width);
        const height = Math.ceil(rect.height);

        const clone = source.cloneNode(true);
        inlineStylesRecursive(source, clone);
        clone.setAttribute("xmlns", "http://www.w3.org/1999/xhtml");

        const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="${width}" height="${height}">`
            + `<foreignObject width="100%" height="100%">${new XMLSerializer().serializeToString(clone)}</foreignObject>`
            + `</svg>`;

        const img = new Image();
        img.onload = () => {
            const dpr = window.devicePixelRatio || 1;
            const canvas = document.createElement("canvas");
            const pad = 24;
            canvas.width = (width + pad * 2) * dpr;
            canvas.height = (height + pad * 2) * dpr;
            const ctx = canvas.getContext("2d");
            ctx.scale(dpr, dpr);
            ctx.fillStyle = isDarkTheme() ? "#172033" : "#ffffff";
            ctx.fillRect(0, 0, width + pad * 2, height + pad * 2);
            ctx.drawImage(img, pad, pad, width, height);
            resolve(canvas);
        };
        img.onerror = () => reject(new Error("svg image load failed"));
        img.src = "data:image/svg+xml;charset=utf-8," + encodeURIComponent(svg);
    });
}

function setupAnalyticsDownload() {
    const btn = document.getElementById("dashboard-analytics-download");
    if (!btn) {
        return;
    }
    btn.addEventListener("click", () => {
        captureAnalyticsCanvas().then((canvas) => {
            const link = document.createElement("a");
            link.download = "portfolio.png";
            link.href = canvas.toDataURL("image/png");
            link.click();
        });
    });
}

// PNG를 저장하면서 동시에, 방금 만든 이미지를 커뮤니티 글쓰기 화면에 자동 첨부한 채로 넘겨준다.
// 페이지 이동을 거치면 JS 변수(캔버스/Blob)는 그대로 못 들고 가므로, sessionStorage에 base64로
// 잠깐 담아뒀다가 글쓰기 페이지 쪽 스크립트가 이어받는다(board.js에 최소한의 코드만 추가했다).
function setupAnalyticsShare() {
    const btn = document.getElementById("dashboard-analytics-share");
    if (!btn) {
        return;
    }
    btn.addEventListener("click", () => {
        btn.disabled = true;
        captureAnalyticsCanvas().then((canvas) => {
            const dataUrl = canvas.toDataURL("image/png");
            const link = document.createElement("a");
            link.download = "portfolio.png";
            link.href = dataUrl;
            link.click();

            try {
                sessionStorage.setItem("stockhub:sharedPortfolioImage", JSON.stringify({
                    dataUrl,
                    fileName: `portfolio-${new Date().toISOString().slice(0, 10)}.png`,
                }));
            } catch (e) {
                // sessionStorage 용량 초과 등은 첨부만 못 할 뿐, 위에서 이미 PNG 저장은 끝났으니 조용히 넘어간다
            }
            location.href = "/community/write";
        }).finally(() => {
            btn.disabled = false;
        });
    });
}

// 최근 활동을 15개씩 페이지로 나눠서 보여준다. 증권사 필터가 걸리면 그 증권사 몫만 골라서
// 다시 페이지를 나눠야 하므로, 바깥(증권사 필터)에서 다시 부를 수 있게 핸들을 남겨둔다.
let timelineFilterHandle = null;

function todayIso() {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
}

function setupTimelinePagination() {
    const list = document.getElementById("dashboard-timeline-list");
    const pagination = document.getElementById("dashboard-timeline-pagination");
    const todayList = document.getElementById("dashboard-timeline-today-list");
    const todayEmpty = document.getElementById("dashboard-timeline-today-empty");
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

    // 사이드바 위젯: 오늘 날짜인 항목만 추려 별도 목록에 복제해 보여준다(전체 목록/페이지네이션은
    // 그대로 "더보기" 모달 쪽에 둔다). 증권사 필터가 바뀌면 이것도 같이 다시 그려야 하므로 render() 안에서 처리한다.
    function renderTodayWidget(items) {
        if (!todayList) {
            return;
        }
        const today = todayIso();
        const todays = items.filter((item) => item.dataset.date === today);
        todayList.innerHTML = "";
        todays.forEach((item) => {
            const clone = item.cloneNode(true);
            clone.hidden = false;
            todayList.appendChild(clone);
        });
        if (todayEmpty) {
            todayEmpty.hidden = todays.length > 0;
        }
    }

    function render() {
        const items = eligibleItems();
        renderTodayWidget(items);
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
