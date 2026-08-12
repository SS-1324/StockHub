// hub/chart.jsp의 화면 동작(위젯 마운트, 테마 동기화)을 담당.
// 실제 차트 렌더링은 tradingview-chart.js의 StockHubTradingViewChart가 처리함.

// URL 쿼리 파라미터(?code=AAPL)로 표시할 종목 코드를 받되,
// 없으면 서버가 정해준 기본 종목(resolvedCode)을 사용
const code = new URLSearchParams(window.location.search).get('code')
    || (typeof resolvedCode !== 'undefined' ? resolvedCode : null);
// 서버(StockController)가 code에 대해 조회한 거래소 정보. 국내 종목이거나 존재하지 않는
// 코드면 빈 문자열로 내려오는데, 이 경우 위젯이 내부적으로 기본 종목으로 대체해서 보여주므로
// 왜 다른 종목이 보이는지 헷갈리지 않도록 콘솔에 남겨둠
const exchange = typeof resolvedExchange !== 'undefined' && resolvedExchange ? resolvedExchange : null;
if (code && !exchange) {
    console.warn(`'${code}'는 지원하지 않는 종목이라 기본 종목으로 대체됩니다.`);
}
// 캔들 주기(minute/day/week/month)도 같은 방식으로 결정
const period = new URLSearchParams(window.location.search).get('period')
    || (typeof resolvedPeriod !== 'undefined' ? resolvedPeriod : 'day');

// 티커테이프 위젯(tv-ticker-tape)도 advanced chart와 마찬가지로 사이트 테마에 맞춰
// color-theme 속성을 갱신한다. 이 위젯은 속성 변경만으로 즉시 반영되는 커스텀 엘리먼트라
// advanced chart처럼 재마운트할 필요는 없음
function syncTickerTapeTheme() {
    const tickerTape = document.querySelector('tv-ticker-tape');
    if (!tickerTape) return;
    const theme = document.documentElement.dataset.theme === 'light' ? 'light' : 'dark';
    tickerTape.setAttribute('color-theme', theme);
}

document.addEventListener('DOMContentLoaded', function () {
    syncTickerTapeTheme();
    new MutationObserver(syncTickerTapeTheme)
        .observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });

    // 최신글 6개를 왼쪽 3개, 오른쪽 3개 순서로 배치한다.
    const communityGrid = document.querySelector('.home-community-grid');
    if (communityGrid && !communityGrid.querySelector('.home-community-column')) {
        const cards = Array.from(
            communityGrid.querySelectorAll(':scope > .home-community-card')
        );

        if (cards.length > 0) {
            cards.forEach(prepareCommunityCardPreview);

            const leftColumn = document.createElement('div');
            const rightColumn = document.createElement('div');
            leftColumn.className = 'home-community-column';
            rightColumn.className = 'home-community-column';

            const leftColumnCount = Math.ceil(cards.length / 2);
            cards.forEach(function (card, index) {
                const targetColumn = index < leftColumnCount
                    ? leftColumn
                    : rightColumn;
                targetColumn.appendChild(card);
            });

            communityGrid.replaceChildren(leftColumn);
            if (rightColumn.children.length > 0) {
                communityGrid.appendChild(rightColumn);
            } else {
                communityGrid.classList.add('is-single-column');
            }
        }
    }

    const container = document.getElementById('tv-chart-container');
    if (!container) return;
    if (!code) {
        console.error('종목 코드가 없습니다. URL에 ?code=AAPL 형태로 접속하세요.');
        return;
    }

    const tvChart = new StockHubTradingViewChart('tv-chart-container');
    tvChart.mount(code, period, exchange);

    // 사이트 테마 토글(header.js가 <html data-theme>를 바꿈)에 맞춰 위젯도 재생성
    // TradingView 공개 위젯 API는 생성 후 테마를 즉시 바꾸는 기능이 없어 재마운트로 처리함
    new MutationObserver(() => tvChart.applyCurrentTheme())
        .observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });

    const chat = setupChatPanel(code);
    const voteWidget = setupVoteWidget(code);
    const quickSwitchExchangesData = typeof quickSwitchExchanges !== 'undefined' ? quickSwitchExchanges : {};
    setupChartHeader(tvChart, code, chat, voteWidget, quickSwitchExchangesData);
    setupFullscreenToggle();
});

// 헤더 왼쪽 종목명 표시 + 오른쪽 빠른 종목 전환 버튼 5개 + 종목 검색을 구성
function setupChartHeader(tvChart, initialCode, chat, voteWidget, quickSwitchExchanges) {
    const nameEl = document.getElementById('chart-symbol-name');
    const buttonsWrap = document.getElementById('chart-symbol-buttons');
    if (!nameEl || !buttonsWrap || typeof StockHubTradingViewChart === 'undefined') return;

    const quickSwitchCodes = StockHubTradingViewChart.QUICK_SWITCH_CODES || [];

    function setActiveCode(activeCode) {
        nameEl.textContent = activeCode;
        buttonsWrap.querySelectorAll('.chart-symbol-btn').forEach((btn) => {
            btn.classList.toggle('active', btn.dataset.code === activeCode);
        });
    }

    // 빠른전환 버튼과 검색 결과 클릭이 공통으로 쓰는 "종목 전환" 절차:
    // 차트 재마운트 -> 헤더 활성 표시 -> 채팅/투표 위젯 전환 -> URL 갱신(새로고침/공유 시 유지)
    function switchTo(newCode, newExchange) {
        tvChart.setSymbol(newCode, tvChart.getCurrentPeriod(), newExchange);
        setActiveCode(newCode);

        if (chat) {
            chat.switchStock(newCode);
        }
        if (voteWidget) {
            voteWidget.switchStock(newCode);
        }

        const url = new URL(window.location.href);
        url.searchParams.set('code', newCode);
        window.history.replaceState(null, '', url);
    }

    quickSwitchCodes.forEach((quickCode) => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'chart-symbol-btn';
        btn.textContent = quickCode;
        btn.dataset.code = quickCode;

        btn.addEventListener('click', () => {
            switchTo(quickCode, quickSwitchExchanges[quickCode]);
        });

        buttonsWrap.appendChild(btn);
    });

    setActiveCode(initialCode);
    setupSymbolSearch(switchTo);
}

// 해외 종목 검색 자동완성. 입력마다 /api/hub/stock-search를 200ms 디바운스로 호출해
// 드롭다운에 후보를 보여주고, 클릭하면 switchTo로 그 종목으로 전환한다.
// (dictionary.js의 용어사전 자동완성과 동일한 디바운스/드롭다운 패턴)
function setupSymbolSearch(switchTo) {
    const wrap = document.getElementById('chart-symbol-search');
    const input = document.getElementById('chart-symbol-search-input');
    const list = document.getElementById('chart-symbol-search-list');
    if (!wrap || !input || !list) return;

    let debounceTimer = null;

    function closeList() {
        list.classList.remove('show');
        list.replaceChildren();
    }

    function renderResults(stocks) {
        list.replaceChildren();
        if (!stocks || stocks.length === 0) {
            closeList();
            return;
        }

        stocks.forEach((stock) => {
            const item = document.createElement('button');
            item.type = 'button';
            item.className = 'chart-symbol-search-item';
            item.textContent = `${stock.stockCode} · ${stock.stockName} (${stock.exchange})`;
            item.addEventListener('click', () => {
                switchTo(stock.stockCode, stock.exchange);
                input.value = '';
                closeList();
            });
            list.appendChild(item);
        });

        list.classList.add('show');
    }

    input.addEventListener('input', () => {
        clearTimeout(debounceTimer);
        const keyword = input.value.trim();
        if (!keyword) {
            closeList();
            return;
        }

        debounceTimer = setTimeout(() => {
            fetch(`/api/hub/stock-search?keyword=${encodeURIComponent(keyword)}`)
                .then((res) => res.json())
                .then((res) => {
                    if (!res.success) return;
                    renderResults(res.data);
                })
                .catch(() => closeList());
        }, 3000);
    });

    input.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeList();
            input.blur();
        }
    });

    document.addEventListener('click', (e) => {
        if (!wrap.contains(e.target)) {
            closeList();
        }
    });
}

// 말풍선 아이콘으로 여닫는 종목 채팅 패널. WebSocket(STOMP)으로 실시간 송수신하고,
// 패널을 열 때 REST로 과거 메시지를 먼저 불러옴. 종목이 바뀌면 switchStock()으로 방을 갈아탐.
// 반환값의 switchStock은 setupChartHeader의 종목 전환 버튼에서 호출됨
function setupChatPanel(initialStockCode) {
    const toggleBtn = document.getElementById('chat-toggle-btn');
    const panel = document.getElementById('chat-panel');
    const closeBtn = document.getElementById('chat-panel-close-btn');
    const messagesEl = document.getElementById('chat-messages');
    const inputEl = document.getElementById('chat-input');
    const sendBtn = document.getElementById('chat-send-btn');
    if (!toggleBtn || !panel || !closeBtn || !messagesEl || !inputEl || !sendBtn) return null;

    let currentStockCode = initialStockCode;
    let stompClient = null;
    let subscription = null;

    // 무한스크롤(위로 스크롤 = 과거 메시지 추가 로드) 상태.
    // 종목을 열거나 전환할 때마다 초기화해야 다른 종목의 커서가 섞이지 않음
    let oldestLoadedChatId = null;
    let hasMoreHistory = true;
    let isLoadingHistory = false;
    // 서버 StockChatController의 RECENT_MESSAGE_LIMIT / OLDER_MESSAGE_PAGE_SIZE와 동일하게 맞춤
    const RECENT_MESSAGES_LIMIT = 50;
    const OLDER_MESSAGES_PAGE_SIZE = 25;
    const LOAD_MORE_SCROLL_THRESHOLD = 40;

    function setOpen(isOpen) {
        panel.classList.toggle('open', isOpen);
        panel.setAttribute('aria-hidden', String(!isOpen));
        toggleBtn.setAttribute('aria-label', isOpen ? '채팅 닫기' : '채팅 열기');
        if (isOpen) {
            // 패널이 display:none인 동안에는 scrollHeight가 0이라 최초 메시지 로드 때
            // 맨 아래로 스크롤한 게 반영되지 않으므로, 열리는 시점에 다시 맞춰줌
            // (그래야 패널을 열었을 때 가장 오래된 메시지가 아니라 최신 메시지부터 보임)
            messagesEl.scrollTop = messagesEl.scrollHeight;
        }
    }

    // 메시지 목록을 안내 문구 한 줄로 교체 (연결 중/빈 채팅방/에러 상태 표시용)
    function showPlaceholder(text) {
        messagesEl.innerHTML = '';
        const placeholder = document.createElement('p');
        placeholder.className = 'chat-panel-placeholder';
        placeholder.textContent = text;
        messagesEl.appendChild(placeholder);
    }

    // 채팅 메시지 하나의 DOM 엘리먼트를 만듦. 다른 사용자가 입력한 내용이 들어오는 자리라
    // innerHTML은 절대 쓰지 않고 textContent로만 채움 (XSS 방지)
    function buildMessageElement(chatMessage) {
        const item = document.createElement('div');
        item.className = 'chat-message';

        const meta = document.createElement('div');
        meta.className = 'chat-message-meta';

        const nickname = document.createElement('span');
        nickname.className = 'chat-message-nickname';
        nickname.textContent = chatMessage.nickname || '알 수 없음';

        const time = document.createElement('span');
        time.className = 'chat-message-time';
        time.textContent = chatMessage.createAtStr || '';

        meta.appendChild(nickname);
        meta.appendChild(time);

        const content = document.createElement('div');
        content.className = 'chat-message-content';
        content.textContent = chatMessage.content || '';

        item.appendChild(meta);
        item.appendChild(content);
        return item;
    }

    // 실시간 수신/최초 로드 시 맨 아래에 메시지를 붙이고 바닥으로 스크롤
    function appendMessage(chatMessage) {
        // 실시간으로 첫 메시지가 들어오는 경우, "아직 채팅이 없습니다" 등
        // showPlaceholder()가 남겨둔 안내 문구가 그대로 남아있으므로 먼저 지워줌
        const existingPlaceholder = messagesEl.querySelector('.chat-panel-placeholder');
        if (existingPlaceholder) existingPlaceholder.remove();

        messagesEl.appendChild(buildMessageElement(chatMessage));
        messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    // 무한스크롤로 불러온 과거 메시지들을 맨 위에 붙임. appendMessage와 달리 바닥으로
    // 스크롤하면 안 되고, 사용자가 보고 있던 위치를 그대로 유지해야 함
    function prependMessages(chatMessages) {
        const prevScrollHeight = messagesEl.scrollHeight;
        const prevScrollTop = messagesEl.scrollTop;

        const fragment = document.createDocumentFragment();
        chatMessages.forEach((chatMessage) => fragment.appendChild(buildMessageElement(chatMessage)));
        messagesEl.insertBefore(fragment, messagesEl.firstChild);

        messagesEl.scrollTop = messagesEl.scrollHeight - prevScrollHeight + prevScrollTop;
    }

    // 채팅 패널을 열거나 종목을 바꿀 때 과거 메시지를 REST로 먼저 불러옴
    function loadRecentMessages(stockCode) {
        oldestLoadedChatId = null;
        hasMoreHistory = true;
        isLoadingHistory = false;

        showPlaceholder('채팅 불러오는 중...');
        fetch(`/api/hub/chat/${encodeURIComponent(stockCode)}/recent`)
            .then((res) => res.json())
            .then((res) => {
                if (!res.success || !res.data || res.data.length === 0) {
                    showPlaceholder('아직 채팅이 없습니다. 첫 메시지를 남겨보세요.');
                    hasMoreHistory = false;
                    return;
                }
                messagesEl.innerHTML = '';
                res.data.forEach(appendMessage);
                oldestLoadedChatId = res.data[0].chatId;
                hasMoreHistory = res.data.length >= RECENT_MESSAGES_LIMIT;
            })
            .catch(() => showPlaceholder('채팅을 불러오지 못했습니다.'));
    }

    // 채팅 목록 맨 위로 스크롤하면 beforeChatId보다 이전 메시지를 25개씩 추가로 불러옴
    function loadOlderMessages(stockCode) {
        if (isLoadingHistory || !hasMoreHistory || oldestLoadedChatId == null) return;
        isLoadingHistory = true;

        fetch(`/api/hub/chat/${encodeURIComponent(stockCode)}/older?beforeChatId=${oldestLoadedChatId}`)
            .then((res) => res.json())
            .then((res) => {
                if (!res.success || !res.data || res.data.length === 0) {
                    hasMoreHistory = false;
                    return;
                }
                prependMessages(res.data);
                oldestLoadedChatId = res.data[0].chatId;
                hasMoreHistory = res.data.length >= OLDER_MESSAGES_PAGE_SIZE;
            })
            .catch(() => {
                // 실패해도 hasMoreHistory는 유지 - 다음 스크롤에서 재시도
            })
            .finally(() => {
                isLoadingHistory = false;
            });
    }

    messagesEl.addEventListener('scroll', () => {
        if (messagesEl.scrollTop <= LOAD_MORE_SCROLL_THRESHOLD) {
            loadOlderMessages(currentStockCode);
        }
    });

    // 종목 방 구독을 stockCode로 갈아끼움 (기존 구독은 해제)
    function subscribeStock(stockCode) {
        if (subscription) {
            subscription.unsubscribe();
            subscription = null;
        }
        if (!stompClient || !stompClient.connected) return;
        subscription = stompClient.subscribe(`/topic/chat/${stockCode}`, (frame) => {
            appendMessage(JSON.parse(frame.body));
        });
    }

    function connect() {
        if (typeof SockJS === 'undefined' || typeof StompJs === 'undefined') {
            showPlaceholder('채팅을 불러올 수 없습니다.');
            return;
        }

        stompClient = new StompJs.Client({
            webSocketFactory: () => new SockJS('/ws-chat'),
            reconnectDelay: 3000,
        });

        stompClient.onConnect = () => {
            subscribeStock(currentStockCode);
            loadRecentMessages(currentStockCode);

            // 서버가 검증 실패(비로그인/빈 메시지/지원하지 않는 종목 등)를 보낸 사람에게만 돌려주는 채널
            stompClient.subscribe('/user/queue/errors', (frame) => {
                alert(frame.body);
            });

            if (typeof isLoggedIn !== 'undefined' && isLoggedIn) {
                inputEl.disabled = false;
                sendBtn.disabled = false;
            } else {
                inputEl.placeholder = '로그인 후 이용해주세요';
            }
        };

        stompClient.onWebSocketClose = () => {
            inputEl.disabled = true;
            sendBtn.disabled = true;
        };

        stompClient.activate();
    }

    // 도배 방지 기준: 0.1초보다 짧은 간격은 그냥 무시, 5초 안에 5개 이상 보내면 30초간 잠금
    // (서버의 StockChatServiceImpl과 동일한 기준으로 맞춰둠 — 여기는 즉각적인 화면 반응용이고,
    // 실제 강제는 서버가 함)
    const MIN_SEND_INTERVAL_MS = 100;
    const BURST_WINDOW_MS = 5000;
    const BURST_LIMIT = 5;
    const LOCKOUT_MS = 30000;

    let lastSentAt = 0;
    let recentSentTimestamps = [];
    let lockedUntil = 0;
    let lockoutIntervalId = null;
    // 잠금이 여러 번 연장돼도(도배를 계속 시도) 원래 placeholder를 잃지 않도록 잠금이 처음 걸릴 때만 저장해둠
    let placeholderBeforeLockout = null;

    // 잠금 상태를 1초마다 다시 그림 - 남은 시간을 placeholder에 표시하고, 다 지나면 원상복구.
    // lockedUntil을 매번 다시 읽으므로 잠금 도중 도배를 더 시도해 lockedUntil이 늘어나도 자연스럽게 반영됨
    function tickLockout() {
        const remainingMs = lockedUntil - Date.now();
        if (remainingMs <= 0) {
            clearInterval(lockoutIntervalId);
            lockoutIntervalId = null;
            if (stompClient && stompClient.connected
                && typeof isLoggedIn !== 'undefined' && isLoggedIn) {
                inputEl.disabled = false;
                sendBtn.disabled = false;
                inputEl.placeholder = placeholderBeforeLockout;
                inputEl.focus();
            }
            placeholderBeforeLockout = null;
            return;
        }
        const remainingSec = Math.ceil(remainingMs / 1000);
        inputEl.placeholder = `${remainingSec}초 후 전송 가능`;
    }

    function applyLockout(durationMs) {
        lockedUntil = Date.now() + durationMs;
        if (placeholderBeforeLockout === null) {
            placeholderBeforeLockout = inputEl.placeholder;
        }
        inputEl.disabled = true;
        sendBtn.disabled = true;
        tickLockout();

        if (lockoutIntervalId === null) {
            lockoutIntervalId = setInterval(tickLockout, 1000);
        }
    }

    function sendMessage() {
        const content = inputEl.value.trim();
        if (!content || !stompClient || !stompClient.connected) return;

        const now = Date.now();
        if (now - lastSentAt < MIN_SEND_INTERVAL_MS) return;
        lastSentAt = now;

        recentSentTimestamps.push(now);
        recentSentTimestamps = recentSentTimestamps.filter((t) => now - t <= BURST_WINDOW_MS);

        stompClient.publish({
            destination: `/app/chat/${currentStockCode}`,
            body: JSON.stringify({ content }),
        });
        inputEl.value = '';

        if (recentSentTimestamps.length >= BURST_LIMIT) {
            applyLockout(LOCKOUT_MS);
        }
    }

    sendBtn.addEventListener('click', sendMessage);
    inputEl.addEventListener('keydown', (e) => {
        // e.isComposing이 true면 한글(IME) 입력이 아직 조합 중이라는 뜻.
        // 이걸 걸러주지 않으면 마지막 글자를 조합 중에 Enter가 눌려 메시지가 먼저 전송되고,
        // 그 직후 조합이 끝난 마지막 글자가 빈 입력창에 남아 두 번째 메시지로 또 전송돼버림
        if (e.key === 'Enter' && !e.isComposing) sendMessage();
    });

    toggleBtn.addEventListener('click', () => setOpen(!panel.classList.contains('open')));
    closeBtn.addEventListener('click', () => setOpen(false));

    connect();

    return {
        // setupChartHeader의 종목 전환 버튼에서 호출: 채팅방을 새 종목으로 갈아탐
        switchStock(newStockCode) {
            if (newStockCode === currentStockCode) return;
            currentStockCode = newStockCode;
            loadRecentMessages(newStockCode);
            subscribeStock(newStockCode);
        },
    };
}

// 종목별 "살까?팔까?" 실시간 투표 카드. 현황을 REST로 불러오고, 버튼을 누르면 투표를 보냄.
// 종목이 바뀌면 switchStock()으로 다시 불러옴 (반환값은 setupChartHeader의 종목 전환 버튼에서 호출됨)
function setupVoteWidget(initialStockCode) {
    const titleEl = document.getElementById('stock-vote-title');
    const participantsEl = document.getElementById('stock-vote-participants');
    const barUpEl = document.getElementById('stock-vote-bar-up');
    const barDownEl = document.getElementById('stock-vote-bar-down');
    const upLabelEl = document.getElementById('stock-vote-up-label');
    const downLabelEl = document.getElementById('stock-vote-down-label');
    const upBtn = document.getElementById('stock-vote-up-btn');
    const downBtn = document.getElementById('stock-vote-down-btn');
    if (!titleEl || !participantsEl || !barUpEl || !barDownEl || !upBtn || !downBtn) return null;

    // 다른 사람이 새로 남긴 투표도 자연스럽게 반영되도록 일정 주기로 다시 불러옴
    const REFRESH_INTERVAL_MS = 5000;

    let currentStockCode = initialStockCode;
    const numberFormatter = new Intl.NumberFormat('ko-KR');

    function render(result) {
        titleEl.textContent = `${result.stockName}, 지금 팔까?`;
        participantsEl.textContent = `${numberFormatter.format(result.totalCount)}명 참여`;

        barUpEl.style.width = `${result.upPercent}%`;
        barDownEl.style.width = `${result.downPercent}%`;
        upLabelEl.textContent = `▲ 상승 ${result.upPercent}%`;
        downLabelEl.textContent = `하락 ${result.downPercent}% ▼`;

        upBtn.classList.toggle('selected', result.myVote === 'UP');
        downBtn.classList.toggle('selected', result.myVote === 'DOWN');
    }

    function loadResult(stockCode) {
        fetch(`/api/hub/vote/${encodeURIComponent(stockCode)}`)
            .then((res) => res.json())
            .then((res) => {
                // 응답이 오는 사이 다른 종목으로 전환됐으면 화면에 반영하지 않음
                if (!res.success || stockCode !== currentStockCode) return;
                render(res.data);
            })
            .catch(() => {});
    }

    function sendVote(voteType) {
        if (typeof isLoggedIn !== 'undefined' && !isLoggedIn) {
            alert('로그인이 필요합니다.');
            return;
        }
        // 이미 선택된 의견을 다시 누르면 투표를 취소함
        const selectedBtn = voteType === 'UP' ? upBtn : downBtn;
        const isCancel = selectedBtn.classList.contains('selected');

        upBtn.disabled = true;
        downBtn.disabled = true;
        const request = isCancel
            ? fetch(`/api/hub/vote/${encodeURIComponent(currentStockCode)}`, { method: 'DELETE' })
            : fetch(`/api/hub/vote/${encodeURIComponent(currentStockCode)}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ voteType }),
            });

        request
            .then((res) => res.json())
            .then((res) => {
                if (!res.success) {
                    alert(res.message || '투표 처리에 실패했습니다.');
                    return;
                }
                render(res.data);
            })
            .catch(() => alert('투표 처리에 실패했습니다.'))
            .finally(() => {
                upBtn.disabled = false;
                downBtn.disabled = false;
            });
    }

    upBtn.addEventListener('click', () => sendVote('UP'));
    downBtn.addEventListener('click', () => sendVote('DOWN'));

    loadResult(currentStockCode);
    setInterval(() => loadResult(currentStockCode), REFRESH_INTERVAL_MS);

    return {
        switchStock(newStockCode) {
            if (newStockCode === currentStockCode) return;
            currentStockCode = newStockCode;
            loadResult(newStockCode);
        },
    };
}

// 차트 래퍼를 브라우저 전체화면 API로 확대/축소하는 버튼 동작
function setupFullscreenToggle() {
    const wrapper = document.querySelector('.chart-wrapper');
    const btn = document.getElementById('fullscreen-toggle-btn');
    if (!wrapper || !btn) return;

    btn.addEventListener('click', () => {
        if (document.fullscreenElement) {
            document.exitFullscreen();
        } else {
            wrapper.requestFullscreen();
        }
    });

    document.addEventListener('fullscreenchange', () => {
        const isFullscreen = document.fullscreenElement === wrapper;
        btn.classList.toggle('is-fullscreen', isFullscreen);
        btn.setAttribute('aria-label', isFullscreen ? '전체화면 종료' : '전체화면');
    });
}
// 이미지 글의 본문은 1줄, 이미지 없는 글의 본문은 5줄까지만 남긴다.
// 넘친 글은 글자 서식을 보존한 채 줄 경계 안으로 줄이고 다음 줄에 ...을 표시한다.
function prepareCommunityCardPreview(card) {
    const content = card.querySelector('.home-community-content');
    if (!content) return;

    const hasImage = card.classList.contains('has-image');
    const bodyLineLimit = hasImage ? 1 : 5;
    const originalHtml = content.innerHTML;
    const computedStyle = window.getComputedStyle(content);
    const lineHeight = parseFloat(computedStyle.lineHeight)
        || parseFloat(computedStyle.fontSize) * 1.7;
    const maxBodyHeight = lineHeight * bodyLineLimit;

    // CSS의 초기 안전 높이를 잠시 해제해 실제 전체 줄 수를 계산한다.
    content.style.maxHeight = 'none';
    content.style.overflow = 'visible';
    const fullHeight = content.scrollHeight;
    const isTruncated = fullHeight > maxBodyHeight + 1;

    if (isTruncated) {
        content.innerHTML = findFittingPreviewHtml(
            content,
            originalHtml,
            maxBodyHeight
        );
    }

    content.style.maxHeight = `${maxBodyHeight}px`;
    content.style.overflow = 'hidden';
    card.classList.toggle('is-content-truncated', isTruncated);

}

function findFittingPreviewHtml(content, originalHtml, maxHeight) {
    const totalCharacters = content.textContent.length;
    let low = 0;
    let high = totalCharacters;
    let bestHtml = '';

    while (low <= high) {
        const middle = Math.floor((low + high) / 2);
        content.innerHTML = originalHtml;
        truncateRichContent(content, middle);

        if (content.scrollHeight <= maxHeight + 1) {
            bestHtml = content.innerHTML;
            low = middle + 1;
        } else {
            high = middle - 1;
        }
    }

    return bestHtml;
}

// 텍스트 노드만 줄이고 뒤쪽 노드는 제거하므로 strong/em/s/span 서식은 그대로 남는다.
function truncateRichContent(root, characterLimit) {
    let usedCharacters = 0;
    let reachedLimit = false;

    function trimNode(parent) {
        Array.from(parent.childNodes).forEach(function (node) {
            if (reachedLimit) {
                node.remove();
                return;
            }

            if (node.nodeType === Node.TEXT_NODE) {
                const textLength = node.nodeValue.length;
                const remaining = characterLimit - usedCharacters;

                if (textLength > remaining) {
                    node.nodeValue = node.nodeValue.slice(0, Math.max(0, remaining));
                    reachedLimit = true;
                } else {
                    usedCharacters += textLength;
                }
                return;
            }

            if (node.nodeType === Node.ELEMENT_NODE) {
                trimNode(node);
            }
        });
    }

    trimNode(root);

    // 잘린 지점 뒤에 남은 공백과 줄바꿈 태그를 정리한다.
    const textNodes = [];
    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
    while (walker.nextNode()) textNodes.push(walker.currentNode);
    const lastTextNode = textNodes[textNodes.length - 1];
    if (lastTextNode) {
        lastTextNode.nodeValue = lastTextNode.nodeValue.replace(/\s+$/, '');
    }

    // 글자가 제거되며 비게 된 인라인 서식 태그도 정리한다.
    root.querySelectorAll('span, strong, em, s').forEach(function (element) {
        if (!element.textContent.trim() && !element.querySelector('br')) {
            element.remove();
        }
    });

    // 말줄임 줄 앞에 불필요한 빈 줄이 생기지 않도록 마지막 공백과 br을 제거한다.
    while (root.lastChild) {
        const lastNode = root.lastChild;
        const isWhitespaceText = lastNode.nodeType === Node.TEXT_NODE
            && !lastNode.nodeValue.trim();
        const isTrailingBreak = lastNode.nodeType === Node.ELEMENT_NODE
            && lastNode.tagName === 'BR';

        if (!isWhitespaceText && !isTrailingBreak) break;
        lastNode.remove();
    }
}

