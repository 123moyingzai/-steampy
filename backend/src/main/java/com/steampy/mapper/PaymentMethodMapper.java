package com.steampy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.PaymentMethod;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PaymentMethodMapper extends BaseMapper<PaymentMethod> {

    /**
     * 统计引用该渠道编码的订单数量（删除渠道前的引用校验）。
     *
     * @param code 渠道编码 method_code
     * @return 订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE payment_method = #{code}")
    long countOrderRefs(@Param("code") String code);

    /**
     * 统计引用该渠道编码的提现记录数量（删除渠道前的引用校验）。
     *
     * @param code 渠道编码 method_code
     * @return 提现记录数
     */
    @Select("SELECT COUNT(*) FROM withdraw_records WHERE pay_method = #{code}")
    long countWithdrawRefs(@Param("code") String code);
}
