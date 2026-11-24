package com.onedao.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Resolves a client IP to a country code (ip-api.com) and checks it against the blocked list. */
@Service
public class GeoService {
    private static final Logger log = LoggerFactory.getLogger(GeoService.class);
    private final RestTemplate rest;
    private final List<String> blocked;

    public GeoService(RestTemplate rest, @Value("${app.blocked-countries}") String blocked) {
        this.rest = rest;
        this.blocked = Arrays.stream(blocked.split(",")).map(String::trim).map(String::toUpperCase).toList();
    }

    /** Returns ISO country code, or null if lookup failed. */
    @SuppressWarnings("unchecked")
    public String resolveCountry(String ip) {
        boolean local = ip == null || ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")
                || ip.startsWith("192.168.") || ip.startsWith("10.");
        // for local requests, ip-api uses the caller's public IP when none is given
        String url = "http://ip-api.com/json/" + (local ? "" : ip) + "?fields=status,countryCode";
        try {
            Map<String, Object> body = rest.getForObject(url, Map.class);
            if (body != null && "success".equals(body.get("status"))) {
                return (String) body.get("countryCode");
            }
        } catch (Exception e) {
            log.warn("Geo lookup failed: {}", e.getMessage());
        }
        return null;
    }

    public boolean isBlocked(String countryCode) {
        return countryCode != null && blocked.contains(countryCode.toUpperCase());
    }
}
