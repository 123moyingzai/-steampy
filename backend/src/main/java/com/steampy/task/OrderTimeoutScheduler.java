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
 * 订单超时自动关闭：
 * 停留在 pending（待支付/待处理）超过 timeout-minutes 的订单自动置为 closed，
 * 并释放其占用的 listing（恢复 available，与手动取消一致）。
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
