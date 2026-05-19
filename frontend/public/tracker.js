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

  // 즉시 전송용 (fetch - 로드 시)
  function sendFetch(eventType, duration) {
    fetch(apiUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: buildPayload(eventType, duration),
    }).catch(function () {});
  }

  // 이탈 전송용 (sendBeacon - 탭 닫힘에도 안정적)
  function sendBeaconSafe(eventType, duration) {
    var payload = buildPayload(eventType, duration);
    if (navigator.sendBeacon) {
      navigator.sendBeacon(apiUrl, new Blob([payload], { type: 'application/json' }));
    } else {
      fetch(apiUrl, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: payload, keepalive: true }).catch(function () {});
    }
  }

  // 페이지 로드 즉시 pageview 전송 (일별 통계 + 실시간 카운트)
  sendFetch('pageview', 0);

  // 페이지 이탈 시 체류시간 기록 (pageleave는 일별 통계에서 제외)
  var left = false;
  function sendLeave() {
    if (left) return;
    left = true;
    sendBeaconSafe('pageleave', Date.now() - startTime);
  }

  document.addEventListener('visibilitychange', function () {
    if (document.visibilityState === 'hidden') sendLeave();
  });
  window.addEventListener('pagehide', sendLeave);

  // SPA 라우트 전환 시 수동 호출용
  window.tracker = { pageview: function () { startTime = Date.now(); sendFetch('pageview', 0); } };
})();
