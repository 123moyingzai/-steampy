package com.steampy.mapper;

/**
 * @author steampy
 * @date 2026-10-14
 */
/**
 * PaymentMethodMapper — PaymentMethod 实体的持久层
 * 继承 BaseMapper<PaymentMethod>，由 MyBatis-Plus 提供 CRUD
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.steampy.entity.PaymentMethod;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
public interface PaymentMethodMapper extends BaseMapper<PaymentMethod> {

    /**
     * 统计该渠道编码被订单引用的次数（删除前校验）。
     *
     * @param code 渠道编码
     * @return 引用条数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE payment_method = #{code}")
    long countOrderRefs(@Param("code") String code);

    /**
     * 统计该渠道编码被提现记录引用的次数（删除前校验）。
     *
     * @param code 渠道编码
     * @return 引用条数
     */
    @Select("SELECT COUNT(*) FROM withdraw_records WHERE pay_method = #{code}")
    long countWithdrawRefs(@Param("code") String code);
}
