package com.dashboard.backend.util;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class GeoIpService {

    // null은 ConcurrentHashMap에 저장 불가 — 조회 실패한 IP는 sentinel로 캐싱
    private static final String UNKNOWN = "__unknown__";

    private final RestClient restClient = RestClient.create();
    private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public String getCountry(String ip) {
        if (ip == null || ip.isBlank() || isPrivateIp(ip)) return null;
        String result = cache.computeIfAbsent(ip, this::resolve);
        return UNKNOWN.equals(result) ? null : result;
    }

    private String resolve(String ip) {
        try {
            IpApiResponse body = restClient.get()
                    .uri("http://ip-api.com/json/{ip}?fields=status,countryCode", ip)
                    .retrieve()
                    .body(IpApiResponse.class);
            if (body != null && "success".equals(body.status())) {
                return body.countryCode();
            }
        } catch (Exception ignored) {}
        return UNKNOWN;
    }

    // RFC 1918 사설망 및 루프백 IP — API 호출 없이 바로 null 반환
    private boolean isPrivateIp(String ip) {
        if (ip.equals("127.0.0.1") || ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1")) return true;
        if (ip.startsWith("10.") || ip.startsWith("192.168.")) return true;
        // RFC 1918: 172.16.0.0/12 → 172.16 ~ 172.31
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length >= 2) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) return true;
                } catch (NumberFormatException ignored) {}
            }
        }
        return false;
    }

    private record IpApiResponse(String status, String countryCode) {}
}
