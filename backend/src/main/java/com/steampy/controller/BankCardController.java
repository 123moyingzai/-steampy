package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.BankCard;
import com.steampy.mapper.BankCardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/bank-cards")
public class BankCardController {

    @Autowired
    private BankCardMapper bankCardMapper;

    // 银行卡列表（只返回脱敏卡号，绝不回传完整卡号）
    @GetMapping("/user/{userId}")
    public Result<List<Map<String, Object>>> listByUser(@PathVariable String userId) {
        QueryWrapper<BankCard> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("created_at");
        List<BankCard> cards = bankCardMapper.selectList(qw);
        List<Map<String, Object>> list = new ArrayList<>();
        if (cards != null) {
            for (BankCard c : cards) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", c.getId());
                m.put("bank_name", c.getBankName());
                m.put("card_number_masked", c.getCardNumberMasked());
                m.put("card_holder", c.getCardHolder());
                m.put("status", c.getStatus());
                m.put("created_at", c.getCreatedAt());
                list.add(m);
            }
        }
        return Result.success(list);
    }

    // 新增银行卡
    @PostMapping
    public Result<Map<String, Object>> add(@RequestBody Map<String, Object> body) {
        String userId = (String) body.getOrDefault("user_id", "");
        String bankName = (String) body.getOrDefault("bank_name", "");
        String cardNumber = (String) body.getOrDefault("card_number", "");
        String cardHolder = (String) body.getOrDefault("card_holder", "");

        if (userId == null || userId.isBlank()) return Result.error("用户不存在");
        if (bankName == null || bankName.isBlank()) return Result.error("请填写开户银行");
        if (cardHolder == null || !cardHolder.matches("^[\\u4e00-\\u9fa5·]{2,20}$"))
            return Result.error("持卡人姓名必须为 2-20 个中文字符");
        if (cardNumber == null || !cardNumber.matches("^\\d{16,19}$"))
            return Result.error("银行卡号必须为 16-19 位纯数字");
        if (!luhnCheck(cardNumber)) return Result.error("银行卡号校验失败，请检查是否输入正确");

        // 同一用户不允许重复绑定同一卡号（uk_user_card）
        QueryWrapper<BankCard> dup = new QueryWrapper<>();
        dup.eq("user_id", userId).eq("card_number", cardNumber);
        if (bankCardMapper.selectCount(dup) > 0)
            return Result.error("该银行卡已绑定，请勿重复添加");

        BankCard c = new BankCard();
        c.setId(UUID.randomUUID().toString());
        c.setUserId(userId);
        c.setBankName(bankName.trim());
        c.setCardNumber(cardNumber);
        c.setCardNumberMasked(maskCardNumber(cardNumber));
        c.setCardHolder(cardHolder.trim());
        c.setStatus("active");
        bankCardMapper.insert(c);

        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("bank_name", c.getBankName());
        m.put("card_number_masked", c.getCardNumberMasked());
        m.put("card_holder", c.getCardHolder());
        m.put("status", c.getStatus());
        return Result.success(m);
    }

    // 停用银行卡（状态机，不物理删除，保留审计）
    @PutMapping("/{id}/disable")
    public Result<Map<String, Object>> disable(@PathVariable String id) {
        BankCard c = bankCardMapper.selectById(id);
        if (c == null) return Result.error("银行卡不存在");
        if (!"active".equals(c.getStatus())) return Result.error("该卡已停用");
        c.setStatus("disabled");
        bankCardMapper.updateById(c);

        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId());
        m.put("status", c.getStatus());
        return Result.success(m);
    }

    // 卡号脱敏：6222021234567890 → 6222 **** **** 7890
    private static String maskCardNumber(String num) {
        if (num == null || num.length() < 8) return num;
        return num.substring(0, 4) + " **** **** " + num.substring(num.length() - 4);
    }

    // Luhn 算法校验银行卡号
    private static boolean luhnCheck(String num) {
        if (num == null || !num.matches("^\\d+$")) return false;
        int sum = 0;
        boolean even = false;
        for (int i = num.length() - 1; i >= 0; i--) {
            int d = num.charAt(i) - '0';
            if (even) { d *= 2; if (d > 9) d -= 9; }
            sum += d;
            even = !even;
        }
        return sum % 10 == 0;
    }
}
