package com.iarecruiter.shared.security;

public class TenantContext {
    private static final ThreadLocal<String> currentCompanyId = new ThreadLocal<>();

    public static void set(String companyId) {
        currentCompanyId.set(companyId);
    }

    public static String get() {
        return currentCompanyId.get();
    }

    public static void clear() {
        currentCompanyId.remove();
    }
}
