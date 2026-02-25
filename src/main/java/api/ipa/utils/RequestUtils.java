package api.ipa.utils;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {
    private RequestUtils() {}

    public static String extractRealIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip != null ? ip : "unknown-ip";
    }
}
