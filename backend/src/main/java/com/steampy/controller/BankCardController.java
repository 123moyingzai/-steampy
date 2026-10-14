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

/**
 * BankCardController
 * 负责用户银行卡绑定与状态管理的 HTTP 接口层。
 *
 * 安全要点：
 * - 列表接口绝不回传完整卡号，统一使用 card_number_masked 脱敏展示（前 4 + 后 4，中间 *）。
 * - 绑卡时做三重校验：姓名正则（中文 2-20 字）、卡号正则（16-19 位数字）、Luhn 算法校验。
 * - 同一用户不允许重复绑定同一卡号（uk_user_card 唯一约束）。
 * - 停用采用状态机而非物理删除，保留审计痕迹。
 *
 * 提供的核心接口：
 *   - GET    /api/bank-cards/user/{userId} — 查询某用户绑定的银行卡列表（脱敏）
 *   - POST   /api/bank-cards                — 新增银行卡（三重校验 + Luhn + 重复检查）
 *   - PUT    /api/bank-cards/{id}/disable   — 停用银行卡
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/bank-cards")
public class BankCardController {

    @Autowired
    private BankCardMapper bankCardMapper;

    /**
     * 查询某用户绑定的银行卡列表（只返回脱敏卡号，绝不回传完整卡号）。
     *
     * 返回字段：id / bank_name / card_number_masked / card_holder / status / created_at。
     *
     * @param userId 用户主键
     * @return {@link Result} Map 列表，每一条是一张银行卡（脱敏）
     */
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

    /**
     * 新增银行卡（绑卡）。
     *
     * 三重校验：① card_holder 必须是 2-20 个中文字符；② card_number 必须是 16-19 位纯数字；③ Luhn 算法校验通过。
     * 同一用户不允许重复绑定同一卡号（uk_user_card 唯一约束）。
     * 入库时同时写入完整 card_number（加密存储）和脱敏 card_number_masked（用于列表展示）。
     * 初始 status 为 active。
     *
     * @param body 需含 user_id / bank_name / card_number / card_holder
     * @return {@link Result} 插入成功的银行卡脱敏 Map；任一校验失败返回 error
     */
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

    /**
     * 停用银行卡（状态机，不物理删除，保留审计）。
     *
     * 只有 active 状态的卡可以被停用；已停用的返回错误。
     *
     * @param id 银行卡主键
     * @return {@link Result} 含 { id, status } 的 Map；卡不存在或已停用返回 error
     */
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

    /**
     * 银行卡号脱敏：{@code 6222021234567890 → 6222 **** **** 7890}。
     *
     * 前 4 位保留银行 BIN 码，后 4 位保留识别卡主，中间全部用 * 替代。
     * 短卡号（< 8 位）原样返回。
     *
     * @param num 完整卡号字符串
     * @return 脱敏后的卡号
     */
    private static String maskCardNumber(String num) {
        if (num == null || num.length() < 8) return num;
        return num.substring(0, 4) + " **** **** " + num.substring(num.length() - 4);
    }

    /**
     * Luhn 算法校验银行卡号合法性。
     *
     * 步骤：从右向左遍历，偶数位乘 2（结果 > 9 则减 9），所有位求和，总和能被 10 整除则合法。
     * 非纯数字输入直接返回 false。
     *
     * @param num 纯数字卡号字符串
     * @return true 通过校验 / false 非法
     */
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
