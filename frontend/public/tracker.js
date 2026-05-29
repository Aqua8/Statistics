(function () {
  var script = document.currentScript || document.querySelector('script[data-key]');
  if (!script) return;

  var trackingKey = script.getAttribute('data-key');
  if (!trackingKey) return;

  var origin = script.getAttribute('data-api') ||
    (script.src ? new URL(script.src).origin : '');
  var apiUrl = origin + '/api/collect';

  var startTime = Date.now();

  function getDeviceType() {
    var ua = navigator.userAgent;
    if (/Mobi|Android/i.test(ua)) return 'mobile';
    if (/iPad|Tablet/i.test(ua)) return 'tablet';
    return 'desktop';
  }

  function getBrowser() {
    var ua = navigator.userAgent;
    if (/Edg\//i.test(ua)) return 'Edge';
    if (/Chrome\//i.test(ua)) return 'Chrome';
    if (/Firefox\//i.test(ua)) return 'Firefox';
    if (/Safari\//i.test(ua)) return 'Safari';
    if (/MSIE|Trident/i.test(ua)) return 'IE';
    return 'Other';
  }

  function buildPayload(eventType, duration) {
    return JSON.stringify({
      trackingKey: trackingKey,
      pageUrl: location.href,
      referrer: document.referrer || null,
      userAgent: navigator.userAgent,
      eventType: eventType,
      duration: duration,
      deviceType: getDeviceType(),
      browser: getBrowser(),
    });
  }

  // 일반 요청에는 fetch 사용 (응답 대기 가능)
  function sendFetch(eventType, duration) {
    fetch(apiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: buildPayload(eventType, duration),
    }).catch(function () {});
  }

  // 탭 닫힘 직전에는 fetch가 취소될 수 있어 sendBeacon 사용; 미지원 브라우저는 keepalive fetch로 폴백
  function sendBeaconSafe(eventType, duration) {
    var payload = buildPayload(eventType, duration);
    if (navigator.sendBeacon) {
      navigator.sendBeacon(apiUrl, new Blob([payload], { type: 'application/json' }));
    } else {
      fetch(apiUrl, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: payload, keepalive: true }).catch(function () {});
    }
  }

  // 페이지 진입 즉시 pageview 전송 — 일별 통계 집계 및 실시간 방문자 카운트에 반영
  sendFetch('pageview', 0);

  // 이탈 시 체류시간을 pageleave 이벤트로 전송 — 서버 집계에서는 제외하고 duration 기록용으로만 사용
  var left = false;
  function sendLeave() {
    if (left) return; // visibilitychange + pagehide 중복 전송 방지
    left = true;
    sendBeaconSafe('pageleave', Date.now() - startTime);
  }

  document.addEventListener('visibilitychange', function () {
    if (document.visibilityState === 'hidden') sendLeave();
  });
  window.addEventListener('pagehide', sendLeave);

  // SPA에서 라우터 전환 후 window.tracker.pageview() 를 수동 호출하면 새 pageview로 기록
  window.tracker = { pageview: function () { startTime = Date.now(); sendFetch('pageview', 0); } };
})();
