package com.steampy.constant;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 订单状态机定义
 * 核心职责：集中管理订单所有合法状态值及其流转规则，提供状态合法性校验与流转判定
 *
 * 主流程：下单即完成（即时发货），无待支付中间态；
 * pending 为保留的待处理态，供超时自动关闭等扩展使用。
 *
 * 流转规则：
 *   pending  -> paid / shipped / completed / cancelled / closed / refunded
 *   paid     -> shipped / completed / cancelled / refunded
 *   shipped  -> completed / refunded
 *   completed-> refunded（仅售后退款）
 *   cancelled / closed / refunded -> 终态
 *
 * @author steampy
 * @date 2026-10-14
 */
public class OrderStatus {

    /** 待处理（保留态，供超时自动关闭扩展使用） */
    public static final String PENDING = "pending";
    /** 已支付 */
    public static final String PAID = "paid";
    /** 已发货 */
    public static final String SHIPPED = "shipped";
    /** 已完成 */
    public static final String COMPLETED = "completed";
    /** 已取消（用户主动取消） */
    public static final String CANCELLED = "cancelled";
    /** 已关闭（系统超时自动关闭） */
    public static final String CLOSED = "closed";
    /** 已退款（售后终态） */
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

    /**
     * 判断给定状态值是否属于合法的订单状态
     *
     * @param status 待校验的状态字符串
     * @return true 表示是合法状态，false 表示 null 或不在已定义的状态集合中
     */
    public static boolean isValid(String status) {
        return status != null && ALL.contains(status);
    }

    /**
     * 判断是否允许从 from 流转到 to
     * from 为 null 视为初始态，可以流转到任意合法状态；
     * 终态（cancelled / closed / refunded）不允许再转出
     *
     * @param from 当前状态（可为 null 代表初始态）
     * @param to  目标状态（必须非 null 且为合法值）
     * @return true 允许流转，false 禁止流转
     */
    public static boolean canTransition(String from, String to) {
        if (to == null || !ALL.contains(to)) return false;
        if (from == null || from.isBlank()) return true;
        Set<String> allowed = TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }
}
