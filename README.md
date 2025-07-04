# Simple EVM

<br>

✅（已完成）Stack + Arithmetic Opcode 模擬

✅ 支援 Memory 操作（MLOAD, MSTORE）

✅ 支援 Gas 模型

✅ 支援跳轉指令（JUMP, JUMPI）

✅ 支援 Storage 模擬

🔜 支援 Function Call（CALL）或簡化合約模型

🔜  CALL / RETURN 模型：支援呼叫內部 function / 合約

🔜  LOG 模擬：支援類似 Solidity 的 event 紀錄

🔜  寫 parser：把人類可讀 op 編譯成 bytecode

<br>

---

### 在真實的 EVM 裡：

JUMP: 無條件跳轉到某個 offset（跳轉目的地）
➜ Stack: [dest] → JUMP → pc = dest

JUMPI: 條件跳轉，當條件不為 0 時才跳轉
➜ Stack: [dest, condition] → JUMPI → if (condition != 0) pc = dest else pc++

✅ 跳轉目的地必須是 JUMPDEST，否則會 Revert。

<br>

### 📘 EVM 中的 Gas 是什麼？
在 Ethereum 中，每條指令都會消耗 Gas。當合約執行時，會累計使用的 Gas。若 Gas 用盡，EVM 就會中止執行並 Revert 所有變更。

* 每條指令設定基本的固定 Gas 消耗
* 實作 Gas 計數器：初始化 → 每執行一條指令扣除對應 Gas
* 加入 Gas 不足時拋出錯誤（模擬 out of gas）
* Revert 時恢復 Gas 狀態 暫時未實現 （太複雜了）

<br>

### 簡化 CALL 支援

🎯 功能設計目標：
支援使用 CALL opcode 呼叫「另一段 bytecode」作為目標函式執行

✅ 每個 CALL 執行都使用新的 SimpleEVM 執行子程式

✅ 模擬 CALL 的 Gas forwarding

✅ 子合約執行成功則回傳值到 stack

✅ 子合約失敗（OOG, exception）→ stack 推 0 表示失敗

