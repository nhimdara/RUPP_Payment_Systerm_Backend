package com.rupp.payment.security;

import com.rupp.payment.entity.Role;
import com.rupp.payment.entity.User;
import com.rupp.payment.exception.UnauthenticatedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityContextCurrentUserProviderTest {

    private final SecurityContextCurrentUserProvider provider = new SecurityContextCurrentUserProvider();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsIdAndRoleFromJwtPrincipal() {
        CustomUserDetails principal = new CustomUserDetails(
                new User(7L, "student1", "hash", "student1@rupp.edu.kh", Role.ROLE_STUDENT));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        assertThat(provider.isAuthenticated()).isTrue();
        assertThat(provider.getUserId()).isEqualTo(7L);
        assertThat(provider.getRole()).isEqualTo("ROLE_STUDENT");
    }

    @Test
    void anonymousCallerIsUnauthenticated() {
        assertThat(provider.isAuthenticated()).isFalse();
        assertThatThrownBy(provider::getUserId).isInstanceOf(UnauthenticatedException.class);
    }
}
