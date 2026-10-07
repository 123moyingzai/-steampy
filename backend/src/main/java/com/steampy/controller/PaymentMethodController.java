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

@RestController
@RequestMapping("/api/payment-methods")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodMapper paymentMethodMapper;

    // 用户端：读取指定用途下已启用的支付渠道（按 sort_order 排序）
    @GetMapping("/active")
    public Result<List<Map<String, Object>>> listActive(@RequestParam(defaultValue = "withdraw") String type) {
        QueryWrapper<PaymentMethod> qw = new QueryWrapper<>();
        qw.eq("type", type).eq("is_active", 1).orderByAsc("sort_order");
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
