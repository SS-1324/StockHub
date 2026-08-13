/**
 * 세션 타이머: 남은 시간을 실시간으로 표시하고 자동 로그아웃
 */
let timerInterval;  // 전역 변수로 선언 (연장할 때 필요)
let expiresAtMs = 0; // 전역 변수로 선언하여 연장 함수에서도 접근 가능하게 함

document.addEventListener('DOMContentLoaded', function() {
    const timerElement = document.querySelector('.header-session-timer');

    // 디버깅: 콘솔에 자동 출력
    console.log('=== 세션 타이머 디버깅 ===');
    console.log('타이머 요소:', timerElement);
    if (timerElement) {
        console.log('만료 시간:', timerElement.dataset.sessionExpiresAt);
    }

    if (!timerElement) {
        console.log('타이머 요소를 찾을 수 없습니다.');
        return;
    }

    // const 키워드를 제거하고 전역 변수 expiresAtMs에 값을 할당합니다.
    expiresAtMs = parseInt(timerElement.dataset.sessionExpiresAt, 10);

    // 헤더 타이머 또는 홈 타이머의 시간 요소
    let remainingTimeElement = document.getElementById('header-session-remaining-time');
    if (!remainingTimeElement) {
        remainingTimeElement = document.getElementById('home-session-remaining-time');
    }

    function updateTimer() {
        const now = Date.now();
        const remainingMs = expiresAtMs - now;

        if (remainingMs <= 0) {
            // 1. 타이머 멈춤
            clearInterval(timerInterval);
            remainingTimeElement.textContent = '00:00';

            // 2. [만료 시] alert 띄우고 로그아웃
            alert('세션이 만료되었습니다. 다시 로그인해주세요.');
            window.location.href = '/member/logout';
            return;
        }

        // 남은 시간을 분:초로 계산
        const remainingSeconds = Math.floor(remainingMs / 1000);
        const minutes = Math.floor(remainingSeconds / 60);
        const seconds = remainingSeconds % 60;

        // MM:SS 형식으로 표시
        remainingTimeElement.textContent =
            `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

        // 5분 이하일 때 경고 색상 (주황색)
        if (remainingSeconds <= 300) {
            timerElement.classList.add('warning');
        } else {
            timerElement.classList.remove('warning');
        }

        // 1분 이하일 때 긴급 색상 (빨간색 + 깜박임)
        if (remainingSeconds <= 60) {
            timerElement.classList.add('critical');
        } else {
            timerElement.classList.remove('critical');
        }
    }

    // 첫 로드 시 즉시 업데이트
    updateTimer();

    // 1초마다 타이머 업데이트
    timerInterval = setInterval(updateTimer, 1000);

    // 페이지 떠날 때 정리
    window.addEventListener('beforeunload', () => {
        clearInterval(timerInterval);
    });
});

/**
 * 세션 연장하기
 */
function extendSession(e) {
    if (e && e.preventDefault) {
        e.preventDefault();
    }

    const evt = e || window.event;
    const btn = evt ? (evt.currentTarget || evt.target) : document.querySelector('.session-extend-btn');
    const icon = btn ? btn.querySelector('i') : null;

    if (btn) btn.disabled = true;
    if (icon) icon.classList.add('fa-spin');

    fetch('/member/session/extend', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('연장 실패');
            }
            return response.json();
        })
        .then(data => {
            // data.data(서버가 넘겨준 새 만료 시각)가 있는지 확인하고 동적 반영!
            if (data.success && data.data) {
                // 1. 서버에서 새로 받아온 만료 시각으로 전역 변수 즉시 갱신
                expiresAtMs = data.data;

                // 2. 화면 dataset 값도 함께 업데이트
                const timerElement = document.querySelector('.header-session-timer');
                if (timerElement) {
                    timerElement.dataset.sessionExpiresAt = data.data;
                }

                // 3. 버튼 로딩 상태 해제 (새로고침 없이 바로 30:00으로 타이머 연장됨)
                if (btn) btn.disabled = false;
                if (icon) icon.classList.remove('fa-spin');
            } else {
                alert(data.message || '세션 연장에 실패했습니다.');
                if (btn) btn.disabled = false;
                if (icon) icon.classList.remove('fa-spin');
            }
        })
        .catch(error => {
            console.error('세션 연장 오류:', error);
            alert('연결 오류가 발생했습니다.');
            if (btn) btn.disabled = false;
            if (icon) icon.classList.remove('fa-spin');
        });
}

function disableExtendButton() {
    const btn = document.querySelector('.session-extend-btn');
    if (btn) {
        btn.disabled = true;
        btn.style.opacity = '0.4';
        btn.style.cursor = 'not-allowed';
    }
}