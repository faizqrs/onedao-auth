package com.onedao.auth;

import com.onedao.auth.service.GeoService;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

class GeoServiceTest {

    @Test
    void shouldBlockRestrictedCountries() {
        GeoService geoService =
                new GeoService(new RestTemplate(), "SY,AF,IR,KP");

        assertTrue(geoService.isBlocked("IR"));
        assertTrue(geoService.isBlocked("AF"));
        assertTrue(geoService.isBlocked("KP"));
        assertTrue(geoService.isBlocked("SY"));
    }

    @Test
    void shouldAllowNonRestrictedCountries() {
        GeoService geoService =
                new GeoService(new RestTemplate(), "SY,AF,IR,KP");

        assertFalse(geoService.isBlocked("IN"));
        assertFalse(geoService.isBlocked("US"));
    }

    @Test
    void shouldHandleNullAndLowercaseCodes() {
        GeoService geoService = new GeoService(new RestTemplate(), "SY,AF,IR,KP");
        assertFalse(geoService.isBlocked(null));
        assertTrue(geoService.isBlocked("ir"));
    }
}