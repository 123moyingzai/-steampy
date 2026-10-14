package com.steampy.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.constant.OrderStatus;
import com.steampy.entity.Listing;
import com.steampy.entity.Order;
import com.steampy.mapper.ListingMapper;
import com.steampy.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时自动关闭定时任务
 * 核心职责：定期扫描停留在 pending 状态超过超时阈值的订单，将其自动置为 closed，
 *          并释放这些订单占用的 listing（恢复 available 状态，与手动取消逻辑一致）
 *
 * @author steampy
 * @date 2026-10-14
 */
@Slf4j
@Component
public class OrderTimeoutScheduler {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ListingMapper listingMapper;

    @Value("${steampy.order.timeout-minutes:15}")
    private int timeoutMinutes;

    /**
     * 定时扫描 pending 状态超时订单并自动关闭，同时释放其占用的 listing
     * 每分钟执行一次，超时时间可通过配置项 steampy.order.timeout-minutes 调整，默认 15 分钟
     */
    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void closeTimeoutPendingOrders() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(timeoutMinutes);
        QueryWrapper<Order> qw = new QueryWrapper<>();
        qw.eq("status", OrderStatus.PENDING)
                .lt("created_at", deadline);
        List<Order> expired = orderMapper.selectList(qw);
        if (expired.isEmpty()) {
            return;
        }
        int released = 0;
        for (Order o : expired) {
            o.setStatus(OrderStatus.CLOSED);
            o.setUpdatedAt(LocalDateTime.now());
            orderMapper.updateById(o);
            if (o.getListingId() != null && !o.getListingId().isBlank()) {
                Listing l = listingMapper.selectById(o.getListingId());
                if (l != null && !"available".equals(l.getStatus())) {
                    l.setStatus("available");
                    l.setSoldAt(null);
                    l.setUpdatedAt(LocalDateTime.now());
                    listingMapper.updateById(l);
                    released++;
                }
            }
        }
        log.info("订单超时自动关闭 {} 笔（超时 {} 分钟），释放 listing {} 个", expired.size(), timeoutMinutes, released);
    }
}
