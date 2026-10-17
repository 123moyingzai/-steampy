package com.steampy.service;

import com.steampy.entity.Game;
import com.steampy.entity.SteamAccount;
import com.steampy.entity.SteamLibrary;
import com.steampy.entity.User;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.SteamAccountMapper;
import com.steampy.mapper.SteamLibraryMapper;
import com.steampy.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * SteamService 单元测试（Service 层）
 * 使用 Mockito 隔离 Mapper 层，纯内存验证业务逻辑正确性
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SteamServiceTest {

    @Mock private SteamLibraryMapper steamLibraryMapper;
    @Mock private SteamAccountMapper steamAccountMapper;
    @Mock private UserMapper userMapper;
    @Mock private GameMapper gameMapper;

    @InjectMocks
    private SteamService steamService;

    private static final String USER_ID = "u-1001";
    private static final String ACCOUNT_ID = "acc-2002";

    @BeforeEach
    void setUp() {
        // 默认：用户已绑定 Steam 账号
        User u = new User();
        u.setId(USER_ID);
        u.setSteamAccountId(ACCOUNT_ID);
        when(userMapper.selectById(USER_ID)).thenReturn(u);
        SteamAccount acc = new SteamAccount();
        acc.setId(ACCOUNT_ID);
        when(steamAccountMapper.selectById(ACCOUNT_ID)).thenReturn(acc);
    }

    // ==================== hashUserId ====================

    @Test
    @DisplayName("hashUserId: 返回 32 位十六进制哈希")
    void hashUserId_returns32HexChars() {
        String h = SteamService.hashUserId(USER_ID);
        assertEquals(32, h.length());
        assertTrue(h.matches("[0-9a-f]{32}"));
    }

    @Test
    @DisplayName("hashUserId: 同一输入结果稳定，不同输入结果不同")
    void hashUserId_isDeterministicAndDistinct() {
        assertEquals(SteamService.hashUserId(USER_ID), SteamService.hashUserId(USER_ID));
        assertNotEquals(SteamService.hashUserId(USER_ID), SteamService.hashUserId("u-1002"));
        assertNotEquals(SteamService.hashUserId(USER_ID), SteamService.hashUserId("另一个用户"));
    }

    // ==================== findAccountByUserId ====================

    @Test
    @DisplayName("findAccountByUserId: 用户不存在返回 null")
    void findAccountByUserId_nullUser() {
        when(userMapper.selectById("ghost")).thenReturn(null);
        assertNull(steamService.findAccountByUserId("ghost"));
    }

    @Test
    @DisplayName("findAccountByUserId: 未绑定 Steam 返回 null")
    void findAccountByUserId_noBinding() {
        User u = new User();
        u.setId("u-2");
        u.setSteamAccountId(null);
        when(userMapper.selectById("u-2")).thenReturn(u);
        assertNull(steamService.findAccountByUserId("u-2"));
    }

    @Test
    @DisplayName("findAccountByUserId: 已绑定返回账号实体")
    void findAccountByUserId_returnsAccount() {
        SteamAccount acc = steamService.findAccountByUserId(USER_ID);
        assertNotNull(acc);
        assertEquals(ACCOUNT_ID, acc.getId());
    }

    // ==================== refreshUserSteamStats ====================

    @Test
    @DisplayName("refreshUserSteamStats: 用户不存在不触发更新")
    void refreshStats_userNull_noUpdate() {
        when(userMapper.selectById("ghost")).thenReturn(null);
        steamService.refreshUserSteamStats("ghost");
        verify(steamAccountMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("refreshUserSteamStats: 未绑定 Steam 不触发更新")
    void refreshStats_noBinding_noUpdate() {
        User u = new User();
        u.setId("u-2");
        u.setSteamAccountId(null);
        when(userMapper.selectById("u-2")).thenReturn(u);
        steamService.refreshUserSteamStats("u-2");
        verify(steamAccountMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("refreshUserSteamStats: 正确聚合游戏数/总价值/总时长并回写")
    void refreshStats_aggregatesCorrectly() {
        SteamLibrary l1 = lib(1, null, 120);           // 按 gameId 查价
        SteamLibrary l2 = lib(null, "CS2", 30);        // 按 gameName 查价
        when(steamLibraryMapper.selectList(any())).thenReturn(List.of(l1, l2));

        Game g1 = new Game();
        g1.setId("1");
        g1.setPrice(new BigDecimal("98.50"));
        when(gameMapper.selectById(1)).thenReturn(g1);
        Game g2 = new Game();
        g2.setId("2");
        g2.setPrice(new BigDecimal("199.00"));
        when(gameMapper.selectOne(any())).thenReturn(g2);

        steamService.refreshUserSteamStats(USER_ID);

        ArgumentCaptor<SteamAccount> cap = ArgumentCaptor.forClass(SteamAccount.class);
        verify(steamAccountMapper).updateById(cap.capture());
        SteamAccount updated = cap.getValue();
        assertEquals(2, updated.getGameCount());
        assertEquals(0, new BigDecimal("297.50").compareTo(updated.getAccountValue()));
        assertEquals(150, updated.getPlaytime());
    }

    @Test
    @DisplayName("refreshUserSteamStats: playtime 为 null 或游戏/价格缺失时安全跳过")
    void refreshStats_nullSafe() {
        SteamLibrary l1 = lib(1, null, null);    // playtime null
        SteamLibrary l2 = lib(null, "幽灵游戏", 10); // 游戏查不到
        SteamLibrary l3 = lib(3, null, 5);       // 游戏无价格
        when(steamLibraryMapper.selectList(any())).thenReturn(List.of(l1, l2, l3));
        when(gameMapper.selectById(1)).thenReturn(null);
        Game noPrice = new Game();
        noPrice.setId("3");
        noPrice.setPrice(null);
        when(gameMapper.selectById(3)).thenReturn(noPrice);

        steamService.refreshUserSteamStats(USER_ID);

        ArgumentCaptor<SteamAccount> cap = ArgumentCaptor.forClass(SteamAccount.class);
        verify(steamAccountMapper).updateById(cap.capture());
        assertEquals(3, cap.getValue().getGameCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(cap.getValue().getAccountValue()));
        assertEquals(15, cap.getValue().getPlaytime());
    }

    @Test
    @DisplayName("refreshUserSteamStats: 空库存聚合为零")
    void refreshStats_emptyLibrary() {
        when(steamLibraryMapper.selectList(any())).thenReturn(Collections.emptyList());
        steamService.refreshUserSteamStats(USER_ID);
        ArgumentCaptor<SteamAccount> cap = ArgumentCaptor.forClass(SteamAccount.class);
        verify(steamAccountMapper).updateById(cap.capture());
        assertEquals(0, cap.getValue().getGameCount());
        assertEquals(0, BigDecimal.ZERO.compareTo(cap.getValue().getAccountValue()));
        assertEquals(0, cap.getValue().getPlaytime());
    }

    // ==================== isGameOwned / isGameOwnedByUser ====================

    @Test
    @DisplayName("isGameOwned: accountId 为 null 直接返回 false")
    void isGameOwned_nullAccount() {
        assertFalse(steamService.isGameOwned(null, 1L, "CS2"));
    }

    @Test
    @DisplayName("isGameOwned: 已拥有返回 true")
    void isGameOwned_owned() {
        when(steamLibraryMapper.selectCount(any())).thenReturn(1L);
        assertTrue(steamService.isGameOwned(ACCOUNT_ID, 1L, "CS2"));
    }

    @Test
    @DisplayName("isGameOwned: 未拥有返回 false")
    void isGameOwned_notOwned() {
        when(steamLibraryMapper.selectCount(any())).thenReturn(0L);
        assertFalse(steamService.isGameOwned(ACCOUNT_ID, 1L, "CS2"));
    }

    @Test
    @DisplayName("isGameOwnedByUser: 用户不存在返回 false")
    void isGameOwnedByUser_nullUser() {
        when(userMapper.selectById("ghost")).thenReturn(null);
        assertFalse(steamService.isGameOwnedByUser("ghost", 1L, "CS2"));
    }

    @Test
    @DisplayName("isGameOwnedByUser: 已绑定用户委托 isGameOwned 判断")
    void isGameOwnedByUser_delegates() {
        when(steamLibraryMapper.selectCount(any())).thenReturn(2L);
        assertTrue(steamService.isGameOwnedByUser(USER_ID, 1L, "CS2 豪华版"));
    }

    // ==================== 辅助方法 ====================

    /** 构造一条 SteamLibrary 库存记录（gameId 为 Integer 类型主键引用） */
    private SteamLibrary lib(Integer gameId, String gameName, Integer playtime) {
        SteamLibrary sl = new SteamLibrary();
        sl.setSteamAccountId(ACCOUNT_ID);
        sl.setGameId(gameId);
        sl.setGameName(gameName);
        sl.setPlaytime(playtime);
        return sl;
    }
}
