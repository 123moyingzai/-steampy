# -steampy 双人协作 Git 操作指南

> **适用对象**：学号 719（仓库 owner）、学号 718（协作者）
> **GitHub 仓库**：https://github.com/123moyingzai/-steampy
> **建立时间**：2026-10-08
> **当前 HEAD**：539a086

---

## 一、分支命名

| 分支 | 负责人 | 用途 |
|---|---|---|
| `main` | 共同保护 | 主线，**不直接 push**，只通过合并 feature 分支进来。永远保持可用状态 |
| `719` | 学号 719 | 你的专属开发分支（moying） |
| `718` | 学号 718 | 朋友的专属开发分支 |

**分支关系**：`719` 和 `718` 都从 `main` 最新提交 539a086 分叉出去，起点完全一致。

```
main: ──●──...──539a086 ──────────────────── (合并你的改动) ───── (合并朋友的改动) ────
                  │                           │                      │
719:              └──●──●──●── (你的开发) ───┘                      │
                                                                     │
718:                  ──●──●── (朋友的开发) ─────────────────────────┘
```

---

## 二、第一次准备（只做 1 次）

### 719（你，已经帮你开好了，直接用）

你不需要做任何准备，分支已经存在于本地和远端。打开终端：

```bash
cd e:\实训学期\项目\-steampy
git checkout 719          # 切到你的分支
git pull origin 719        # 同步远端（可选，刚开的应该一致）
```

### 718（朋友，第一次 clone 后要做的）

```bash
# 1. clone 仓库（如果还没 clone）
git clone https://github.com/123moyingzai/-steampy.git
cd -steampy

# 2. 拉取远端所有分支信息
git fetch origin

# 3. 切到自己的分支（远端已存在 origin/718，会自动建立本地跟踪）
git checkout 718
```

朋友的本地现在就有了 `718` 分支，直接开始开发。

---

## 三、日常开发流程（每次开发都重复）

### 步骤 1：切到自己的分支

```bash
# 719 的操作
git checkout 719

# 718 的操作
git checkout 718
```

> **提示**：VS Code 左下角会显示当前分支名，确认是自己的（719 或 718），不是 main。

### 步骤 2：同步主线最新改动

```bash
git pull origin main
```

把 `main` 上最新的改动（比如对方已经合并进来的功能）拉到自己的分支上。**避免后期合并冲突**。

### 步骤 3：改代码

直接在 IDE 里改，和平时一样。

### 步骤 4：提交到自己的分支

```bash
# 719 提交
git add -A
git commit -m "feat: 改了XX功能的YY"
git push origin 719

# 718 提交（换分支名）
git add -A
git commit -m "feat: 改了XX功能的YY"
git push origin 718
```

**commit message 建议**：
```
feat:     新功能     例：feat: 加了游戏评价分页
fix:      修复 bug   例：fix: 修了订单金额显示错误
refactor: 重构       例：refactor: 订单控制器代码整理
docs:     文档       例：docs: 更新 README
```

---

## 四、合并到 main（功能做完/定期同步时）

**注意**：合并前互相打个招呼，一个一个来，不要同时合并。

### 719 合并自己的分支：

```bash
# 1. 确保你的分支是最新的
git checkout 719
git pull origin main
git push origin 719

# 2. 切到主线
git checkout main
git pull origin main         # 确保 main 是最新

# 3. 合并
git merge 719

# 4. 推到远端主线
git push origin main

# 5. 合并完切回自己的分支，同步主线最新状态
git checkout 719
git merge main
git push origin 719
```

### 718 合并自己的分支（完全一样，换分支名）：

```bash
git checkout 718
git pull origin main
git push origin 718

git checkout main
git pull origin main
git merge 718
git push origin main

git checkout 718
git merge main
git push origin 718
```

---

## 五、冲突处理（两个人改了同一文件同一行）

Git 会告诉你冲突的文件，打开文件会看到：

```
<<<<<<< HEAD
（main 上的版本，对方已经合进来的代码）
=======
（你写的代码）
>>>>>>> 719
```

**解决步骤**：

1. 打开冲突文件，手动删掉 `<<<<<<<`、`=======`、`>>>>>>>` 标记
2. 保留正确的代码（可能是你的、可能是对方的、也可能两部分都要）
3. 保存文件
4. 告诉对方你解决了什么（避免他那边同样冲突）
5. 继续：
   ```bash
   git add 冲突的文件
   git commit -m "merge: 解决冲突 - XX文件"
   git push origin main
   ```

---

## 六、常用命令速查表

| 我想... | 命令 |
|---|---|
| 看当前在哪个分支 | `git branch`（带 `*` 的就是当前） |
| 切到自己的分支 | `git checkout 719` 或 `git checkout 718` |
| 切到 main | `git checkout main` |
| 同步远端最新（包括新分支） | `git fetch origin` |
| 拉主线最新到自己的分支 | `git pull origin main` |
| 提交并推到自己的分支 | `git add -A && git commit -m "xxx" && git push origin 719` |
| 合并到主线 | `git checkout main && git pull origin main && git merge 719 && git push origin main` |
| 撤销所有未提交的改动 | `git checkout -- .`（危险，改了的代码会没） |
| 把不想提交的改动存起来 | `git stash`（存）→ `git stash pop`（取回来） |

---

## 七、重要规则（必须遵守）

| # | 规则 | 为什么 |
|---|---|---|
| 1 | **main 上不直接 push** | main 是两人看到的主线，必须通过 merge feature 分支进来，保证稳定 |
| 2 | **每次开发先 pull main** | 避免落队太多，最后合并冲突爆炸 |
| 3 | **合并前互相打个招呼** | 不要同时合并，一个一个来 |
| 4 | **push 到自己的分支（719 / 718），不要 push 到 main** | main 通过 merge 进，不直接改 |
| 5 | **分支长期保留** | 719 / 718 一直用到项目结束，不用每次新开 |

---

## 八、一句话流程（版本）

```
开 IDE → 切自己的分支 → pull main → 改代码 → commit → push 自己的分支 → 
做完了 → 切 main → pull main → merge 自己的分支 → push main → 切回自己的分支 → merge main → 继续
```
