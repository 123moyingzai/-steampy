package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.steampy.dto.Result;
import com.steampy.entity.UserGame;
import com.steampy.mapper.UserGameMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UserGameController
 * 负责用户游戏库（已购买/已拥有的游戏记录）的 HTTP 接口层
 * 提供的核心接口：
 *   - GET    /api/user-games/user/{userId}  — 查询用户的全部游戏库记录
 *   - GET    /api/user-games/{id}           — 根据记录 ID 查询游戏库详情
 *   - POST   /api/user-games                — 新增一条用户游戏库记录
 *   - PUT    /api/user-games/{id}/activate  — 将待激活的游戏记录标记为已激活
 *   - DELETE /api/user-games/{id}           — 删除一条游戏库记录
 *
 * 注：用户游戏库的 CDKey 购买入库实际由 OrderController 创建订单时自动完成，
 * 本接口主要提供手动增删及手动激活能力。
 *
 * @author steampy
 * @date 2026-10-14
 */
@RestController
@RequestMapping("/api/user-games")
public class UserGameController {

    @Autowired
    private UserGameMapper userGameMapper;

    /**
     * 查询指定用户的游戏库全部记录（按购买时间倒序）
     *
     * @param userId 用户 ID
     * @return {@link Result} 包含游戏库记录列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<UserGame>> getUserGames(@PathVariable String userId) {
        QueryWrapper<UserGame> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).orderByDesc("purchase_date");
        return Result.success(userGameMapper.selectList(qw));
    }

    /**
     * 根据记录 ID 查询单条游戏库详情
     *
     * @param id 游戏库记录 ID
     * @return {@link Result} 包含记录对象（若不存在则 data 为 null）
     */
    @GetMapping("/{id}")
    public Result<UserGame> getById(@PathVariable String id) {
        return Result.success(userGameMapper.selectById(id));
    }

    /**
     * 手动新增一条用户游戏库记录
     * <p>
     * 若 purchaseDate 未传则默认为当前时间。
     * 注：正常购买流程由 OrderController 自动写入，此接口用于补发或手动入库场景。
     * </p>
     *
     * @param ug 用户游戏库对象（JSON 传入，需包含 userId、gameName 等必要字段）
     * @return {@link Result} 成功返回创建好的记录对象
     */
    @PostMapping
    public Result<UserGame> createUserGame(@RequestBody UserGame ug) {
        if (ug.getPurchaseDate() == null) ug.setPurchaseDate(LocalDateTime.now());
        userGameMapper.insert(ug);
        return Result.success(ug);
    }

    /**
     * 将一条游戏库记录的状态从 pending（待激活）更新为 activated（已激活）
     * <p>
     * 同时自动填充 activationDate 为当前时间。
     * </p>
     *
     * @param id 游戏库记录 ID
     * @return {@link Result} 成功返回 null；失败返回错误信息（记录不存在）
     */
    @PutMapping("/{id}/activate")
    public Result<Void> activate(@PathVariable String id) {
        UserGame ug = userGameMapper.selectById(id);
        if (ug == null) return Result.error("游戏记录不存在");
        ug.setStatus("activated");
        ug.setActivationDate(LocalDateTime.now());
        userGameMapper.updateById(ug);
        return Result.success(null);
    }

    /**
     * 删除一条游戏库记录（物理删除）
     *
     * @param id 游戏库记录 ID
     * @return {@link Result} 成功返回 null
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        userGameMapper.deleteById(id);
        return Result.success(null);
    }
}
