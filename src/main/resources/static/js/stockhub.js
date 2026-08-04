// hub/chart.jsp의 화면 동작(위젯 마운트, 테마 동기화)을 담당.
// 실제 차트 렌더링은 tradingview-chart.js의 StockHubTradingViewChart가 처리함.

// URL 쿼리 파라미터(?code=AAPL)로 표시할 종목 코드를 받되,
// 없으면 서버가 정해준 기본 종목(resolvedCode)을 사용
const code = new URLSearchParams(window.location.search).get('code')
    || (typeof resolvedCode !== 'undefined' ? resolvedCode : null);
// 트레이딩뷰 위젯이 지원하지 않는 코드(예: 국내 종목 코드)면 위젯이 내부적으로
// 기본 종목으로 대체해서 보여주므로, 왜 다른 종목이 보이는지 헷갈리지 않도록 콘솔에 남겨둠
if (code && typeof isSupportedCode === 'function' && !isSupportedCode(code)) {
    console.warn(`'${code}'는 트레이딩뷰 위젯이 지원하지 않는 종목이라 기본 종목으로 대체됩니다.`);
}
// 캔들 주기(minute/day/week/month)도 같은 방식으로 결정
const period = new URLSearchParams(window.location.search).get('period')
    || (typeof resolvedPeriod !== 'undefined' ? resolvedPeriod : 'day');

document.addEventListener('DOMContentLoaded', function () {
    const container = document.getElementById('tv-chart-container');
    if (!container) return;
    if (!code) {
        console.error('종목 코드가 없습니다. URL에 ?code=AAPL 형태로 접속하세요.');
        return;
    }

    const tvChart = new StockHubTradingViewChart('tv-chart-container');
    tvChart.mount(code, period);

    // 사이트 테마 토글(header.js가 <html data-theme>를 바꿈)에 맞춰 위젯도 재생성
    // TradingView 공개 위젯 API는 생성 후 테마를 즉시 바꾸는 기능이 없어 재마운트로 처리함
    new MutationObserver(() => tvChart.applyCurrentTheme())
        .observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });

    const chat = setupChatPanel(code);
    setupChartHeader(tvChart, code, chat);
    setupFullscreenToggle();
});

// 헤더 왼쪽 종목명 표시 + 오른쪽 빠른 종목 전환 버튼 5개를 구성
function setupChartHeader(tvChart, initialCode, chat) {
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

    quickSwitchCodes.forEach((quickCode) => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'chart-symbol-btn';
        btn.textContent = quickCode;
        btn.dataset.code = quickCode;

        // 버튼을 누르면 해당 종목으로 차트를 다시 불러오고, URL도 같이 갱신해 새로고침/공유 시 유지되게 함
        btn.addEventListener('click', () => {
            tvChart.setSymbol(quickCode, tvChart.getCurrentPeriod());
            setActiveCode(quickCode);

            // 채팅도 지금 보고 있는 종목 방으로 갈아탐
            if (chat) {
                chat.switchStock(quickCode);
            }

            const url = new URL(window.location.href);
            url.searchParams.set('code', quickCode);
            window.history.replaceState(null, '', url);
        });

        buttonsWrap.appendChild(btn);
    });

    setActiveCode(initialCode);
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

    function setOpen(isOpen) {
        panel.classList.toggle('open', isOpen);
        panel.setAttribute('aria-hidden', String(!isOpen));
        toggleBtn.setAttribute('aria-label', isOpen ? '채팅 닫기' : '채팅 열기');
    }

    // 메시지 목록을 안내 문구 한 줄로 교체 (연결 중/빈 채팅방/에러 상태 표시용)
    function showPlaceholder(text) {
        messagesEl.innerHTML = '';
        const placeholder = document.createElement('p');
        placeholder.className = 'chat-panel-placeholder';
        placeholder.textContent = text;
        messagesEl.appendChild(placeholder);
    }

    // 채팅 메시지 하나를 그려 넣음. 다른 사용자가 입력한 내용이 들어오는 자리라
    // innerHTML은 절대 쓰지 않고 textContent로만 채움 (XSS 방지)
    function appendMessage(chatMessage) {
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
        messagesEl.appendChild(item);
        messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    // 채팅 패널을 열거나 종목을 바꿀 때 과거 메시지를 REST로 먼저 불러옴
    function loadRecentMessages(stockCode) {
        showPlaceholder('채팅 불러오는 중...');
        fetch(`/api/hub/chat/${encodeURIComponent(stockCode)}/recent`)
            .then((res) => res.json())
            .then((res) => {
                if (!res.success || !res.data || res.data.length === 0) {
                    showPlaceholder('아직 채팅이 없습니다. 첫 메시지를 남겨보세요.');
                    return;
                }
                messagesEl.innerHTML = '';
                res.data.forEach(appendMessage);
            })
            .catch(() => showPlaceholder('채팅을 불러오지 못했습니다.'));
    }

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

    function sendMessage() {
        const content = inputEl.value.trim();
        if (!content || !stompClient || !stompClient.connected) return;

        stompClient.publish({
            destination: `/app/chat/${currentStockCode}`,
            body: JSON.stringify({ content }),
        });
        inputEl.value = '';
    }

    sendBtn.addEventListener('click', sendMessage);
    inputEl.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') sendMessage();
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
