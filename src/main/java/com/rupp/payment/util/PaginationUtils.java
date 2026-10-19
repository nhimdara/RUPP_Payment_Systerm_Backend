package com.rupp.payment.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class PaginationUtils {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    // Whitelist allowable sort fields for Payment to prevent SQL injection or Hibernate crashes
    private static final Map<String, String> PAYMENT_SORT_FIELDS = Map.of(
            "id", "id",
            "paymentdate", "paymentDate",
            "payment_date", "paymentDate",
            "totalpaid", "totalPaid",
            "total_paid", "totalPaid",
            "status", "status"
    );

    private PaginationUtils() {
    }

    public static Pageable createPageable(Integer page, Integer size, String sortBy, String sortDir) {
        int pageIndex = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int pageSize = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        String normalizedSortBy = normalizeSortProperty(sortBy, PAYMENT_SORT_FIELDS, "paymentDate");
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return PageRequest.of(pageIndex, pageSize, Sort.by(direction, normalizedSortBy));
    }

    public static String normalizeSortProperty(String requestedProperty, Map<String, String> allowedFields, String defaultProperty) {
        if (requestedProperty == null || requestedProperty.trim().isEmpty()) {
            return defaultProperty;
        }
        String cleanProperty = requestedProperty.trim().toLowerCase();
        return allowedFields.getOrDefault(cleanProperty, defaultProperty);
    }
}
