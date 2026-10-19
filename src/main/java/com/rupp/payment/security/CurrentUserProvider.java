package com.rupp.payment.security;

// Who is calling: lets payment-security code (and its tests) avoid touching SecurityContextHolder directly
public interface CurrentUserProvider {

    Long getUserId();

    String getRole();

    boolean isAuthenticated();
}
