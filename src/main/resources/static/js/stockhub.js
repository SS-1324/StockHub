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

    // 최신글 5개를 왼쪽 열부터 채우고, 나머지를 오른쪽 열에 배치한다.
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
    tvChart.mount(code, period);

    // 사이트 테마 토글(header.js가 <html data-theme>를 바꿈)에 맞춰 위젯도 재생성
    // TradingView 공개 위젯 API는 생성 후 테마를 즉시 바꾸는 기능이 없어 재마운트로 처리함
    new MutationObserver(() => tvChart.applyCurrentTheme())
        .observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] });

    const chat = setupChatPanel(code);
    const voteWidget = setupVoteWidget(code);
    setupChartHeader(tvChart, code, chat, voteWidget);
    setupFullscreenToggle();
});

// 헤더 왼쪽 종목명 표시 + 오른쪽 빠른 종목 전환 버튼 5개를 구성
function setupChartHeader(tvChart, initialCode, chat, voteWidget) {
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

            // 채팅과 투표 카드도 지금 보고 있는 종목으로 갈아탐
            if (chat) {
                chat.switchStock(quickCode);
            }
            if (voteWidget) {
                voteWidget.switchStock(quickCode);
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
        // 실시간으로 첫 메시지가 들어오는 경우, "아직 채팅이 없습니다" 등
        // showPlaceholder()가 남겨둔 안내 문구가 그대로 남아있으므로 먼저 지워줌
        const existingPlaceholder = messagesEl.querySelector('.chat-panel-placeholder');
        if (existingPlaceholder) existingPlaceholder.remove();

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

    // 도배 방지 기준: 0.1초보다 짧은 간격은 그냥 무시, 1초 안에 3개 이상 보내면 3초간 잠금
    // (서버의 StockChatServiceImpl과 동일한 기준으로 맞춰둠 — 여기는 즉각적인 화면 반응용이고,
    // 실제 강제는 서버가 함)
    const MIN_SEND_INTERVAL_MS = 100;
    const BURST_WINDOW_MS = 1000;
    const BURST_LIMIT = 3;
    const LOCKOUT_MS = 3000;

    let lastSentAt = 0;
    let recentSentTimestamps = [];
    let lockedUntil = 0;

    function applyLockout(durationMs) {
        lockedUntil = Date.now() + durationMs;
        const placeholderBeforeLockout = inputEl.placeholder;
        inputEl.disabled = true;
        sendBtn.disabled = true;
        inputEl.placeholder = '메시지를 너무 많이 보냈어요. 잠시 후 다시 시도해주세요';

        setTimeout(() => {
            // 그 사이 또 도배로 lockedUntil이 늘어났으면 아직 풀어주면 안 됨
            if (Date.now() < lockedUntil) return;
            if (stompClient && stompClient.connected
                && typeof isLoggedIn !== 'undefined' && isLoggedIn) {
                inputEl.disabled = false;
                sendBtn.disabled = false;
                inputEl.placeholder = placeholderBeforeLockout;
                inputEl.focus();
            }
        }, durationMs);
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
    const REFRESH_INTERVAL_MS = 20000;

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
        upBtn.disabled = true;
        downBtn.disabled = true;
        fetch(`/api/hub/vote/${encodeURIComponent(currentStockCode)}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ voteType }),
        })
            .then((res) => res.json())
            .then((res) => {
                if (!res.success) {
                    alert(res.message || '투표에 실패했습니다.');
                    return;
                }
                render(res.data);
            })
            .catch(() => alert('투표에 실패했습니다.'))
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

