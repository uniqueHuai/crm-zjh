package com.crm.mall.mp;

public class MpContext {

    private static final ThreadLocal<Long> CURRENT_CUSTOMER_ID = new ThreadLocal<>();

    public static void setCustomerId(Long customerId) {
        CURRENT_CUSTOMER_ID.set(customerId);
    }

    public static Long getCustomerId() {
        return CURRENT_CUSTOMER_ID.get();
    }

    public static void clear() {
        CURRENT_CUSTOMER_ID.remove();
    }
}
