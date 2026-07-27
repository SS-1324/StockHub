<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8" />
<meta name="viewport" content="width=device-width, initial-scale=1.0" />
<title>StockHub - 랭킹보드</title>
<script src="https://cdn.tailwindcss.com"></script>
<script>
  tailwind.config = { darkMode: "class" };
</script>
</head>
<body class="bg-[#f0f4ff] dark:bg-[#0d1117]" style="font-family: Inter, system-ui, sans-serif;">

<div class="max-w-3xl mx-auto px-6 py-10 space-y-6">

  <div>
    <h2 class="text-xl font-bold text-slate-900 dark:text-white mb-1">랭킹보드</h2>
    <p class="text-sm text-slate-500 dark:text-slate-400">검증된 거래 히스토리 기반 수익률 랭킹입니다.</p>
  </div>

  <!-- 기간 필터: 지금은 일간(오늘)만 실제 조회 가능, 나머지는 추후 백엔드 확장 필요 -->
  <div id="period-buttons" class="flex gap-2"></div>

  <div id="ranking-list" class="bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-2xl overflow-hidden shadow-sm">
    <p id="loading-msg" class="px-6 py-8 text-center text-sm text-slate-400">불러오는 중...</p>
  </div>

</div>

<script>
  const PERIODS = ["일간", "주간", "월간", "연간"];
  let currentPeriod = "일간";

  function renderPeriodButtons() {
    const container = document.getElementById("period-buttons");
    container.innerHTML = "";
    PERIODS.forEach((p) => {
      const btn = document.createElement("button");
      btn.textContent = p;
      const isActive = currentPeriod === p;
      btn.className = "px-4 py-1.5 rounded-full text-xs font-bold transition-colors " +
        (isActive
          ? "bg-blue-600 text-white"
          : "bg-white dark:bg-slate-800 text-slate-500 dark:text-slate-400 border border-slate-200 dark:border-slate-600 hover:border-blue-300");
      btn.addEventListener("click", () => {
        currentPeriod = p;
        renderPeriodButtons();
        // 참고: 현재 /ranking API는 "오늘 날짜(rank_date = CURDATE())" 기준으로만 조회함.
        // 주간/월간/연간을 실제로 지원하려면 백엔드에 기간별 조회 API가 추가로 필요함.
      });
      container.appendChild(btn);
    });
  }

  function badgeFor(rank) {
    if (rank === 1) return "🥇";
    if (rank === 2) return "🥈";
    if (rank === 3) return "🥉";
    return "";
  }

  function formatReturnRate(rate) {
    const num = Number(rate);
    const sign = num >= 0 ? "+" : "";
    return `${sign}${num.toFixed(2)}%`;
  }

  // 프로필 사진이 DB에 없으므로, 닉네임 첫 글자로 아바타를 대체 생성
  function initialAvatar(nickname) {
    const ch = (nickname || "?").slice(0, 1);
    const colors = ["bg-blue-500", "bg-violet-500", "bg-emerald-500", "bg-amber-500", "bg-rose-500"];
    const color = colors[ch.charCodeAt(0) % colors.length];
    return `<div class="w-11 h-11 rounded-full ${color} flex items-center justify-center text-white font-bold text-sm shrink-0">${ch}</div>`;
  }

  async function loadRanking() {
    const listEl = document.getElementById("ranking-list");
    try {
      const res = await fetch("/ranking");
      if (!res.ok) throw new Error("서버 응답 오류: " + res.status);
      const data = await res.json();

      if (!data || data.length === 0) {
        listEl.innerHTML = '<p class="px-6 py-8 text-center text-sm text-slate-400">오늘 날짜의 랭킹 데이터가 아직 없습니다.</p>';
        return;
      }

      listEl.innerHTML = "";
      data.forEach((r, i) => {
        const isPos = Number(r.returnRate) >= 0;
        const isLast = i === data.length - 1;
        const badge = badgeFor(r.rankPosition);

        const row = document.createElement("div");
        row.className = "flex items-center gap-4 px-6 py-5 hover:bg-slate-50 dark:hover:bg-slate-700/50 transition-colors group" +
          (isLast ? "" : " border-b border-slate-100 dark:border-slate-700");

        row.innerHTML = `
          <div class="w-8 text-center shrink-0">
            ${badge
              ? `<span class="text-2xl">${badge}</span>`
              : `<span class="text-sm font-bold text-slate-400 dark:text-slate-500 font-mono">${r.rankPosition}</span>`}
          </div>
          ${initialAvatar(r.nickname)}
          <div class="flex-1 min-w-0">
            <p class="font-bold text-slate-900 dark:text-white group-hover:text-blue-700 dark:group-hover:text-blue-400 transition-colors">${r.nickname ?? r.memberId}</p>
            <p class="text-xs text-slate-400 dark:text-slate-500">@${r.memberId}</p>
          </div>
          <div class="text-right">
            <p class="text-lg font-bold font-mono ${isPos ? "text-blue-600" : "text-red-500"}">${formatReturnRate(r.returnRate)}</p>
            ${r.benefitReceived ? `<p class="text-xs font-mono mt-0.5 text-emerald-500">🎁 ${r.benefitReceived}</p>` : ""}
          </div>
        `;
        listEl.appendChild(row);
      });
    } catch (err) {
      listEl.innerHTML = `<p class="px-6 py-8 text-center text-sm text-red-500">불러오기 실패: ${err.message}</p>`;
      console.error(err);
    }
  }

  renderPeriodButtons();
  loadRanking();

  if (window.matchMedia("(prefers-color-scheme: dark)").matches) {
    document.documentElement.classList.add("dark");
  }
</script>

</body>
</html>
