package com.example.applicationservice.domain.util;

import com.example.applicationservice.domain.dto.ApplicationInfo;

import java.util.List;

/**
 * Простая страница с cursor-ом (domain-level)
 */
public class ApplicationPage {
    private final List<ApplicationInfo> items;
    private final String nextCursor;

    public ApplicationPage(List<ApplicationInfo> items, String nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public List<ApplicationInfo> getItems() { return items; }
    public String getNextCursor() { return nextCursor; }
}
