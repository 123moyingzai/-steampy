package com.steampy.constant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 订单状态机定义
 * 主流程：下单即完成（即时发货），无待支付中间态；
 * pending 为保留的待处理态，供超时自动关闭等扩展使用。
 *
 * 流转规则：
 *   pending  -> paid / shipped / completed / cancelled / closed / refunded
 *   paid     -> shipped / completed / cancelled / refunded
 *   shipped  -> completed / refunded
 *   completed-> refunded（仅售后退款）
 *   cancelled / closed / refunded -> 终态
 */
public class OrderStatus {

    public static final String PENDING = "pending";
    public static final String PAID = "paid";
    public static final String SHIPPED = "shipped";
    public static final String COMPLETED = "completed";
    public static final String CANCELLED = "cancelled";
    public static final String CLOSED = "closed";
    public static final String REFUNDED = "refunded";

    private static final Set<String> ALL = new HashSet<>();
    private static final Map<String, Set<String>> TRANSITIONS = new HashMap<>();

    static {
        ALL.add(PENDING);
        ALL.add(PAID);
        ALL.add(SHIPPED);
        ALL.add(COMPLETED);
        ALL.add(CANCELLED);
        ALL.add(CLOSED);
        ALL.add(REFUNDED);

        TRANSITIONS.put(PENDING, Set.of(PAID, SHIPPED, COMPLETED, CANCELLED, CLOSED, REFUNDED));
        TRANSITIONS.put(PAID, Set.of(SHIPPED, COMPLETED, CANCELLED, REFUNDED));
        TRANSITIONS.put(SHIPPED, Set.of(COMPLETED, REFUNDED));
        TRANSITIONS.put(COMPLETED, Set.of(REFUNDED));
        TRANSITIONS.put(CANCELLED, Set.of());
        TRANSITIONS.put(CLOSED, Set.of());
        TRANSITIONS.put(REFUNDED, Set.of());
    }

    public static boolean isValid(String status) {
        return status != null && ALL.contains(status);
    }

    /** 是否允许 from -> to 的状态流转（from 为 null 视为初始态，任意合法状态均可） */
    public static boolean canTransition(String from, String to) {
        if (to == null || !ALL.contains(to)) return false;
        if (from == null || from.isBlank()) return true;
        Set<String> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
