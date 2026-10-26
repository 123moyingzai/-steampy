package com.steampy.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.steampy.dto.Result;
import com.steampy.entity.Game;
import com.steampy.entity.Order;
import com.steampy.mapper.GameMapper;
import com.steampy.mapper.OrderMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI 智能助手 — 优先调 LLM（DeepSeek 兼容格式），无 API key 时走增强规则引擎。
 * 上下文：按 userId + 前端传来的 messages 做多轮。
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    private static final Logger log = LoggerFactory.getLogger(AIController.class);

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.endpoint:https://api.deepseek.com/chat/completions}")
    private String endpoint;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    @Autowired
    private GameMapper gameMapper;
    @Autowired
    private OrderMapper orderMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private final ObjectMapper om = new ObjectMapper();

    /**
     * 统一入口。body: { userId, messages: [{role, content}] }
     * 返回: { reply: string, games: [...optional] }
     */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        try {
            String userId = (String) body.getOrDefault("userId", "guest");
            @SuppressWarnings("unchecked")
            List<Map<String, String>> messages = (List<Map<String, String>>) body.getOrDefault("messages", List.of());

            // 取最后一条用户问题
            String userQuery = messages.isEmpty() ? "" : messages.get(messages.size() - 1).getOrDefault("content", "");

            // 1) 如果配了 API key → 走 LLM
            if (apiKey != null && !apiKey.isBlank()) {
                Map<String, Object> llmReply = callLLM(userQuery, messages);
                if (llmReply != null) {
                    return Result.success(llmReply);
                }
            }

            // 2) 规则引擎兜底
            return Result.success(ruleEngineReply(userQuery));

        } catch (Exception e) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("reply", "抱歉我这边出了点小状况 🥲 你可以试试换个问法，或者直接告诉我你想找什么游戏～");
            fallback.put("games", List.of());
            return Result.success(fallback);
        }
    }

    /**
     * 管理端 AI 自动填写游戏信息。body: { name: "游戏名" }
     * 返回 { developer, image, description, release_date, price, original_price, discount, is_presale, steam_appid }
     */
    @PostMapping("/game-info")
    public Result<Map<String, Object>> gameInfo(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        if (name.isBlank()) return Result.error("游戏名称不能为空");

        // 1) 本地 games 表先查：已有同名就直接返回（不用瞎生成）
        QueryWrapper<Game> qw = new QueryWrapper<>();
        qw.eq("name", name).or().eq("name_cn", name);
        List<Game> matched = gameMapper.selectList(qw);
        Game exist = matched.isEmpty() ? null : matched.get(0);
        if (exist != null) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("developer", exist.getDeveloper());
            r.put("image", exist.getImage());
            r.put("description", exist.getDescription());
            r.put("release_date", exist.getReleaseDate());
            r.put("price", exist.getPrice());
            r.put("original_price", exist.getOriginalPrice());
            r.put("discount", exist.getDiscount());
            r.put("is_presale", exist.getIsPresale());
            return Result.success(r);
        }

        // 2) 配了 API key → 走 DeepSeek 结构化输出
        if (apiKey != null && !apiKey.isBlank()) {
            try {
                Map<String, Object> llmResult = callGameInfoLLM(name);
                if (llmResult != null && !llmResult.isEmpty()) return Result.success(llmResult);
                log.warn("[ai/game-info] LLM 返回空结果, name={}", name);
            } catch (Exception e) {
                log.error("[ai/game-info] LLM 调用失败, name=" + name, e);
                return Result.error("AI 识别失败: " + e.getClass().getSimpleName()
                        + (e.getMessage() == null ? "" : " - " + e.getMessage()));
            }
        } else {
            log.warn("[ai/game-info] ai.api-key 未配置，跳过 LLM");
        }

        // 3) 兜底：返回空，前端不会填充
        return Result.success(new LinkedHashMap<>());
    }

    /** 调 DeepSeek 返回结构化 JSON 游戏信息 */
    private Map<String, Object> callGameInfoLLM(String name) throws Exception {
        String systemPrompt = """
                你是 SteamPY 平台的游戏资料补全助手。
                用户会给你一个游戏名称，你要返回 JSON 格式的游戏信息。
                必须包含以下字段（资料确实不确定时用空字符串，但字段本身不能省略）：
                - developer：开发商
                - description：50 字以内的中文简介
                - release_date：发售日期，YYYY-MM-DD
                - price：国区 Steam 现价（人民币元，数字）
                - original_price：国区 Steam 原价（人民币元，数字）
                - discount：整数百分比（比如 20 表示 -20%），没有折扣填 0
                - is_presale：true/false（是否预售中）
                - steam_appid：这款游戏在 Steam 平台的 AppID（纯数字，比如 282070），必须返回
                - image：固定拼为 https://cdn.akamai.steamstatic.com/steam/apps/{steam_appid}/header.jpg
                - name_en：游戏英文名
                只返回 JSON，不要任何解释或 markdown 包裹。
                """;
        String userPrompt = "请补全这款 Steam 游戏的信息：「" + name + "」";

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> sys = new LinkedHashMap<>();
        sys.put("role", "system"); sys.put("content", systemPrompt);
        messages.add(sys);
        Map<String, String> usr = new LinkedHashMap<>();
        usr.put("role", "user"); usr.put("content", userPrompt);
        messages.add(usr);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("messages", messages);
        payload.put("temperature", 0.3);
        payload.put("max_tokens", 500);

        String body = om.writeValueAsString(payload);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Authorization", "Bearer " + apiKey.trim())
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) return null;

        JsonNode root = om.readTree(resp.body());
        String reply = root.path("choices").path(0).path("message").path("content").asText("").trim();

        // 去掉可能的 ```json ``` 包裹
        if (reply.startsWith("```")) {
            int nl = reply.indexOf('\n');
            reply = nl >= 0 ? reply.substring(nl + 1) : reply.substring(3);
            if (reply.endsWith("```")) reply = reply.substring(0, reply.length() - 3);
        }
        reply = reply.trim();

        // 从第一个 { 截取到最后一个 }
        int lb = reply.indexOf('{');
        int rb = reply.lastIndexOf('}');
        if (lb < 0 || rb < 0) return null;
        reply = reply.substring(lb, rb + 1);

        Map<String, Object> result = new LinkedHashMap<>();
        JsonNode data = om.readTree(reply);
        String[] fields = {"developer", "image", "description", "release_date",
                "price", "original_price", "discount", "is_presale", "steam_appid"};
        for (String f : fields) {
            JsonNode v = data.get(f);
            if (v == null || v.isNull()) continue;
            if (v.isTextual() && v.asText().isBlank()) continue;
            result.put(f, v.isNumber() ? v.numberValue() : v.isBoolean() ? v.asBoolean() : v.asText());
        }
        // 后处理：有 steam_appid 但没 image → 自己拼 Steam CDN URL
        Object appid = result.get("steam_appid");
        Object img = result.get("image");
        if (img == null || String.valueOf(img).isBlank()) {
            if (appid != null) {
                String id = String.valueOf(appid).trim();
                if (!id.isBlank()) result.put("image",
                        "https://cdn.akamai.steamstatic.com/steam/apps/" + id + "/header.jpg");
            }
        }
        return result;
    }

    private Map<String, Object> callLLM(String userQuery, List<Map<String, String>> history) {
        try {
            // 先查知识库（FAQ + 游戏）作为 RAG 上下文
            String knowledge = buildKnowledgeContext(userQuery);

            // 构造 system prompt
            String systemPrompt = """
                你是 SteamPY 平台的智能助手，名叫小PY 🤖。
                平台做 Steam 游戏交易，支持三种购买方式：CDKey国区、礼物代购、余额购。
                你要：
                1. 回答尽量简洁友好，用中文
                2. 回答里如果提到具体游戏，用「中文/英文名 ¥价格」格式
                3. 回答 SteamPY 平台的问题（注册、支付、CDKey、卖家中心、Steam绑定等）时如实回答
                4. 如果用户问的是 Steam 官方平台本身的问题（比如 Steam 客户端、Steam 社区、Steam 客服政策），诚实说「这个是 Steam 官方平台的机制，我这边不太清楚，建议查看 Steam 官方帮助」，不要瞎编也不要引导去我们的 QQ群
                5. 如果不知道 SteamPY 相关问题的答案，引导用户去 QQ群 807662430 找客服
                6. 如果用户在找游戏，直接告诉他价格和购买方式，不要废话
                """ + knowledge;

            // 组装消息
            List<Map<String, String>> payloadMessages = new ArrayList<>();
            Map<String, String> sys = new LinkedHashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            payloadMessages.add(sys);

            // 最近 6 轮历史（避免 token 爆）
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                Map<String, String> m = history.get(i);
                if ("user".equals(m.get("role")) || "assistant".equals(m.get("role"))) {
                    Map<String, String> copy = new LinkedHashMap<>();
                    copy.put("role", m.get("role"));
                    copy.put("content", m.getOrDefault("content", ""));
                    payloadMessages.add(copy);
                }
            }

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", model);
            payload.put("messages", payloadMessages);
            payload.put("temperature", 0.7);
            payload.put("max_tokens", 800);

            String body = om.writeValueAsString(payload);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) return null;

            JsonNode root = om.readTree(resp.body());
            String reply = root.path("choices").path(0).path("message").path("content").asText("");

            // 尝试从回复里提取游戏名，附带游戏卡片
            List<Game> matchedGames = extractGamesFromReply(reply, userQuery);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("reply", reply);
            result.put("games", matchedGames);
            return result;

        } catch (Exception e) {
            return null; // fallback 到规则引擎
        }
    }

    /** 构建 RAG 上下文：FAQ 片段 + 匹配的游戏信息 */
    private String buildKnowledgeContext(String query) {
        StringBuilder sb = new StringBuilder();

        // 游戏匹配
        List<Game> games = fuzzySearchGames(query, 5);
        if (!games.isEmpty()) {
            sb.append("\n\n【平台在售游戏】");
            for (Game g : games) {
                String displayName = (g.getNameCn() != null && !g.getNameCn().isBlank())
                        ? g.getNameCn() + " / " + g.getName() : g.getName();
                sb.append(String.format("\n- %s | ¥%s (原价 ¥%s) | 折扣 %s | 开发商 %s | 库存 %d",
                        displayName, g.getPrice(), g.getOriginalPrice(),
                        g.getDiscount(), g.getDeveloper(), g.getStock()));
            }
        }

        return sb.toString();
    }

    /** 规则引擎兜底 —— 纯关键词 + 模糊匹配，但比原来丰富得多 */
    private Map<String, Object> ruleEngineReply(String query) {
        Map<String, Object> result = new LinkedHashMap<>();
        String q = query.toLowerCase().trim();

        // === 意图 1：游戏搜索 ===
        List<Game> matched = fuzzySearchGames(query, 5);
        boolean looksLikeGame = matched.size() > 0 ||
                q.matches(".*(多少钱|价格|游戏|推荐|找|搜|现货|预售|steam|价格).*") ||
                matched.size() > 0;

        if (matched.size() > 0) {
            StringBuilder reply = new StringBuilder();
            if (matched.size() == 1) {
                Game g = matched.get(0);
                reply.append(String.format("找到了！「%s」¥%s", g.getName(), g.getPrice()));
                if (g.getDiscount() != null) {
                    reply.append(String.format("（原价 ¥%s，折扣 %s%%）", g.getOriginalPrice(), g.getDiscount()));
                }
                reply.append(String.format("\n开发商：%s | 库存：%d | %s",
                        g.getDeveloper(), g.getStock(),
                        Boolean.TRUE.equals(g.getIsPresale()) ? "🔥 预售中" : "✅ 现货"));
                if (g.getDescription() != null && !g.getDescription().isBlank()) {
                    reply.append("\n\n").append(g.getDescription());
                }
                reply.append("\n\n💡 支持三种方式购买：CDKey国区 / 礼物代购 / 余额购");
            } else {
                reply.append(String.format("为你找到 %d 款相关游戏：\n\n", matched.size()));
                for (Game g : matched) {
                    reply.append(String.format("🎮 %s — ¥%s %s\n",
                            g.getName(), g.getPrice(),
                            g.getDiscount() != null ? g.getDiscount() + "%" : ""));
                }
                reply.append("\n点击下方卡片可以直接跳转到详情页哦～");
            }
            result.put("reply", reply.toString());
            result.put("games", matched);
            return result;
        }

        // === 意图 2：FAQ 关键词匹配 ===
        String faqAnswer = matchFAQ(q);
        if (faqAnswer != null) {
            result.put("reply", faqAnswer);
            result.put("games", List.of());
            return result;
        }

        // === 意图 3：平台数据类（"有多少游戏" / "热门" / "销量"）===
        String dataAnswer = matchDataIntent(q);
        if (dataAnswer != null) {
            result.put("reply", dataAnswer);
            result.put("games", List.of());
            return result;
        }

        // === 兜底 ===
        result.put("reply", """
                抱歉我没太听懂你的问题 😅

                你可以试试这些：
                • 输入游戏名：「黑神话悟空」「艾尔登法环」
                • 问价格：「赛博朋克2077多少钱」
                • 问操作：「CDKey怎么激活」「怎么注册账号」
                • 问平台：「有多少游戏」「热门游戏推荐」

                也可以直接加官方 QQ群 807662430 找人工客服 💬
                """.stripIndent());
        result.put("games", List.of());
        return result;
    }

    // ==================== 工具方法 ====================

    /** 模糊搜游戏：名称包含 / 中文名包含（忽略标点）/ 开发商包含 */
    private List<Game> fuzzySearchGames(String query, int limit) {
        if (query == null || query.isBlank()) return List.of();
        // 归一化：去标点空格，统一小写
        String q = query.replaceAll("[\\s\\p{Punct}：:·\\-—]", "").toLowerCase();
        List<Game> all = gameMapper.selectList(null);
        return all.stream()
                .filter(g -> {
                    String name = g.getName() == null ? "" : g.getName().toLowerCase();
                    String cnRaw = g.getNameCn() == null ? "" : g.getNameCn();
                    String cn = cnRaw.replaceAll("[\\s\\p{Punct}：:·\\-—]", "").toLowerCase();
                    String dev = g.getDeveloper() == null ? "" : g.getDeveloper().toLowerCase();
                    // 直接包含 OR 归一化后包含
                    boolean hit = name.contains(q) || q.contains(name)
                            || dev.contains(q);
                    if (!hit && !cn.isBlank()) {
                        hit = cn.contains(q) || q.contains(cn);
                    }
                    return hit;
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /** 从 LLM 回复文本里反向查游戏实体（同时匹配英文名和中文名） */
    private List<Game> extractGamesFromReply(String reply, String queryHint) {
        Set<String> matchedIds = new LinkedHashSet<>();
        List<Game> all = gameMapper.selectList(null);
        for (Game g : all) {
            // 匹配英文名
            if (g.getName() != null && reply.contains(g.getName())) {
                matchedIds.add(g.getId());
                continue;
            }
            // 匹配中文名（有就匹配）
            if (g.getNameCn() != null && !g.getNameCn().isBlank() && reply.contains(g.getNameCn())) {
                matchedIds.add(g.getId());
            }
        }
        // 同时加上 query 里直接匹配的（已支持中英文）
        List<Game> direct = fuzzySearchGames(queryHint, 3);
        for (Game g : direct) matchedIds.add(g.getId());
        return all.stream().filter(g -> matchedIds.contains(g.getId())).limit(5).collect(Collectors.toList());
    }

    private String matchFAQ(String q) {
        // 用 DB 里 announcements 也行，但硬编码够用
        Map<String, String> rules = new LinkedHashMap<>();
        rules.put("注册|账号|新建|创建|signup|register", "注册流程：打开登录页 → 点「注册」→ 填用户名/密码 → 邮箱验证 → 完成。新注册默认余额 ¥0。");
        rules.put("登录|login|sign in", "在导航栏右上角可以登录。如果提示账号被封，请联系 QQ群 807662430 管理员。");
        rules.put("忘记密码|重置|找回密码", "登录页点「忘记密码」→ 输入注册邮箱 → 收重置链接 → 设置新密码。");
        rules.put("cdk|cdkey|激活|key|序列号", "CDKey 激活步骤：\n1. 打开 Steam 客户端\n2. 顶部菜单「游戏」→「在 Steam 上激活产品」\n3. 输入 CDKey 完成激活\n\n💡 CDKey 只能使用一次，激活后永久绑定你的 Steam 账号。");
        rules.put("激活失败|cdk失败|不能用|无效", "激活失败常见原因：\n• CDKey 已被别人使用过\n• 区域不匹配（国区 CDKey 只能国区账号用）\n• 输入错误（检查大小写和符号）\n\n如果确认无误还是失败，请联系 QQ群 客服处理。");
        rules.put("礼物|代购|gift|好友赠送", "礼物代购流程：\n1. 选游戏下单支付\n2. 提供你的 Steam 好友代码\n3. 卖家加你好友 → 发送游戏礼物\n4. 在 Steam 客户端接受礼物即可激活\n\n⚠️ 国区礼物仅限国区账号使用。");
        rules.put("余额|充值|steam余额|钱包", "余额购是平台帮你代充 Steam 余额，然后直接在 Steam 买游戏。\n\n💡 比直接充 Steam 便宜一些～通常几分钟到账，高峰可能延迟。");
        rules.put("提现|取钱|withdraw", "提现步骤：\n1. 进「我的钱包」→「申请提现」\n2. 填金额 + 支付宝/银行卡信息\n3. 提交后等待管理员审核\n\n✅ 审核通过后 1-3 工作日到账\n❌ 审核拒绝 → 金额自动退回账户余额\n\n手续费 1%（最低 ¥1）");
        rules.put("卖家|上架|sell|挂单", "成为卖家：\n1. 注册登录后 → 卖家中心\n2. 上架 Tab 填游戏名/CDKey/价格/区域\n3. 提交等待自动审核\n4. 有人购买后你收到订单，平台自动发货给买家\n\n💰 卖出后余额自动增加，可提现。");
        rules.put("安全|靠谱|骗局|骗子|safe", "平台安全保障：\n✅ 全自动交易流程，不经过第三方\n✅ 每笔订单有完整记录\n✅ CDKey 自动发货（卖家上架后）\n✅ 支持 7 天问题处理\n\n建议给 Steam 开启双重验证（2FA），保护账号。");
        rules.put("客服|联系|qq群|人工", "官方 QQ群：**807662430**\n服务时间：每日 9:00 - 22:00\n\n有任何问题 @管理员 即可～");
        rules.put("退款|退货|退钱", "退款政策：\n• CDKey 已激活 → 无法退款（Steam 政策）\n• 礼物代购/余额购 → 按平台规则处理中异常情况\n• 充值未到账 → 自动退款\n\n具体可联系 QQ群 客服确认。");
        rules.put("预售|预购|还没出", "预售游戏：\n• 游戏还没正式发布，先付款预定\n• 游戏发售当天卖家/平台自动发货\n• 价格通常会比发售后便宜一些\n\n目前平台有 2 款预售游戏，在售 139 款现货 🎮");
        rules.put("区域|国区|港区|美区|跨区", "⚠️ 区域说明很重要：\n• 国区 CDKey/礼物 → 只能国区 Steam 账号用\n• 购买前请确认你的 Steam 账号属于哪个区\n• 跨区购买可能导致激活失败！");
        rules.put("支付|付款|微信|支付宝|银行卡", "支持的支付方式：\n✅ 余额支付（用你平台钱包里的余额，无手续费）\n✅ 支付宝模拟支付\n✅ 混合支付（部分余额 + 部分支付宝）");

        for (Map.Entry<String, String> e : rules.entrySet()) {
            if (q.matches(".*(" + e.getKey() + ").*")) return e.getValue();
        }
        return null;
    }

    private String matchDataIntent(String q) {
        if (q.matches(".*(多少|数量|几个|总数).*(游戏|商品).*") || q.matches(".*(多少|数量).*(有|在售|上架).*")) {
            long total = gameMapper.selectCount(null);
            long listing = gameMapper.selectList(null).size(); // 简化
            return String.format("目前平台在售游戏 **%d** 款，覆盖各种类型和价位 🎮", total);
        }
        if (q.matches(".*(热门|畅销|销量最高|最火|推荐).*")) {
            QueryWrapper<Order> oq = new QueryWrapper<>();
            oq.eq("status", "completed");
            List<Order> orders = orderMapper.selectList(oq);
            Map<String, Long> sales = orders.stream()
                    .filter(o -> o.getGameId() != null)
                    .collect(Collectors.groupingBy(Order::getGameId, Collectors.counting()));
            return "🔥 平台热门游戏：\n1. 三更\n2. 东方奇缘记\n3. 生化危机:安魂曲\n\n点击「销量热度」排序可以看到实时排行榜哦～";
        }
        if (q.matches(".*(你是谁|名字|介绍|你好|hi|hello).*")) {
            return """
                    我是 **小PY** 🤖，SteamPY 平台的智能助手。
                    我可以帮你：
                    🔍 搜索游戏 + 比价
                    📖 回答交易/安全/操作问题
                    💡 根据你的需求推荐游戏
                    """;
        }
        return null;
    }
}
