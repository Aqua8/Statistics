(function () {
  var script = document.currentScript;
  if (!script) return;

  var trackingKey = script.getAttribute('data-key');
  if (!trackingKey) return;

  // data-api 속성으로 커스텀 엔드포인트 지정 가능, 기본값은 스크립트 origin
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

  function send(duration) {
    var payload = JSON.stringify({
      trackingKey: trackingKey,
      pageUrl: location.href,
      referrer: document.referrer || null,
      userAgent: navigator.userAgent,
      eventType: 'pageview',
      duration: duration,
      deviceType: getDeviceType(),
      browser: getBrowser(),
    });

    // sendBeacon: 탭 닫힘에도 안정적으로 전송
    if (navigator.sendBeacon) {
      navigator.sendBeacon(apiUrl, new Blob([payload], { type: 'application/json' }));
    } else {
      fetch(apiUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: payload,
        keepalive: true,
      }).catch(function () {});
    }
  }

  // 페이지 이탈 시 체류시간 포함해 전송 (중복 방지)
  var sent = false;
  function sendOnExit() {
    if (sent) return;
    sent = true;
    send(Date.now() - startTime);
  }

  // visibilitychange: 탭 전환/닫기 감지 (모던 브라우저)
  document.addEventListener('visibilitychange', function () {
    if (document.visibilityState === 'hidden') sendOnExit();
  });

  // pagehide: 모바일 Safari 대응
  window.addEventListener('pagehide', sendOnExit);

  // SPA에서 페이지 전환 시 즉시 전송이 필요한 경우 window.tracker.send() 호출 가능
  window.tracker = { send: sendOnExit };
})();
