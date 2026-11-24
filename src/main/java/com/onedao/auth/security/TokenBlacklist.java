package com.onedao.auth.security;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** In-memory store of logged-out tokens (by jti). */
@Component
public class TokenBlacklist {
    private final Set<String> revoked = ConcurrentHashMap.newKeySet();
    public void revoke(String jti) { revoked.add(jti); }
    public boolean isRevoked(String jti) { return revoked.contains(jti); }
}
