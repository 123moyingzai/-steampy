package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.PaymentMethod;
import com.steampy.mapper.PaymentMethodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PaymentMethodController
 * 负责支付方式（渠道配置）的 HTTP 接口层
 * 提供的核心接口：
 *   - GET /api/payment-methods/active?type=withdraw — 查询指定用途下已启用的支付渠道列表
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodMapper paymentMethodMapper;

    /**
     * 查询指定用途下已启用的支付渠道列表（按 sort_order 升序排序）
     * <p>
     * 仅返回 is_active = 1 的渠道，用于前端下拉菜单展示。
     * 返回字段经过精简，不包含内部管理字段。
     * </p>
     *
     * @param type 用途类型，默认 withdraw（提现）；也可传 recharge（充值）等
     * @return {@link Result} 包含渠道配置列表，每项含 id、method_code、method_name、fee_rate、min_fee、max_fee
     */
    @GetMapping("/active")
    public Result<List<Map<String, Object>>> listActive(@RequestParam(defaultValue = "withdraw") String type) {
        QueryWrapper<PaymentMethod> qw = new QueryWrapper<>();
        qw.in("type", List.of(type, "both"))
          .eq("is_active", 1)
          .orderByAsc("sort_order");
        List<PaymentMethod> list = paymentMethodMapper.selectList(qw);
        List<Map<String, Object>> result = new ArrayList<>();
        if (list != null) {
            for (PaymentMethod pm : list) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", pm.getId());
                m.put("method_code", pm.getMethodCode());
                m.put("method_name", pm.getMethodName());
                m.put("fee_rate", pm.getFeeRate());
                m.put("min_fee", pm.getMinFee());
                m.put("max_fee", pm.getMaxFee());
                result.add(m);
            }
        }
        return Result.success(result);
    }
}
