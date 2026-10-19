package com.rupp.payment.security;

import com.rupp.payment.exception.UnauthenticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Reads the user that JwtAuthenticationFilter put into the security context
@Component
public class SecurityContextCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getUserId() {
        return principal().map(CustomUserDetails::getId).orElseThrow(UnauthenticatedException::new);
    }

    @Override
    public String getRole() {
        return principal().map(user -> user.getRole().name()).orElseThrow(UnauthenticatedException::new);
    }

    @Override
    public boolean isAuthenticated() {
        return principal().isPresent();
    }

    private static Optional<CustomUserDetails> principal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
