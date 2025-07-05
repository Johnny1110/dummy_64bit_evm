# Simple EVM

<br>

✅ Stack(32 bit) + Arithmetic Opcode 模擬

✅ 支援 Memory 操作（MLOAD, MSTORE）

✅ 支援 Gas 模型

✅ 支援跳轉指令（JUMP, JUMPI）

✅ PUSHx 系列自動化

✅ 支援 DUP/SWAP

✅ 支援 Storage 模擬

✅  CALL / RETURN 模型：支援呼叫內部 function / 合約

🔜  LOG 模擬：支援類似 Solidity 的 event 紀錄

🔜  寫 bytecode 編譯器（高階語言轉 bytecode）




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

### DUPx / SWAPx

🔁 DUPx / SWAPx 概念簡介
✅ DUPx (0x80 ~ 0x8f)
DUP1 把 stack 的頂部複製一份，推到上面

DUP2 複製的是第 2 個（從頂部數下來）…

DUP16 是複製第 16 個元素

範例：
```text
stack = [5, 10, 15]
DUP2 → 複製 10，stack = [5, 10, 15, 10]
```

🔄 SWAPx (0x90 ~ 0x9f)
SWAP1 把 top 與第 2 個交換

SWAP16 把 top 與第 17 個交換

範例：
```text
stack = [5, 10, 15]
SWAP2 → stack = [15, 10, 5]
```

<br>

### 簡化 CALL 支援

🎯 功能設計目標：
支援使用 CALL opcode 呼叫「另一段 bytecode」作為目標函式執行

✅ 每個 CALL 執行都使用新的 SimpleEVM 執行子程式 (獨立的 stack, memory, storage, storage is unique by contract address)

✅ 模擬 CALL 的 Gas forwarding (目前沒想到怎麼做沒用完的 Gas 歸還，暫時就吃掉吧... XD)

✅ 子合約執行成功則回傳值到 stack

✅ 子合約失敗（OOG, exception）→ stack 推 0 表示失敗

### CALL/RETURN

EVM 是基於 "message call" 模型：每個 CALL 都是一個新的上下文（stack, memory, storage...）

實作 CALL/RETURN 之後，你就能模擬：

函式呼叫（例如 Solidity 的 function）

合約間互動（可模擬內部呼叫，未來支援多合約）

這是一個進入 合約架構與 ABI 模擬 的關鍵橋梁

在設計 bytecode 編譯器前，有 CALL/RETURN 才能產生 可重用邏輯與函式結構

✅ 設計重點

📦 CALL	呼叫一段「bytecode offset」開始的邏輯區塊（模擬 function 呼叫）

🔁 RETURN	將控制權返回到 CALL 的下一行，並還原 stack/memory

🧱 CallFrame	保存 returnPC、stack snapshot、memory snapshot、gas

💾 單一 code 區	同一份 bytecode 中跳到某段邏輯區塊執行（無多合約）

🧠 CALL 操作語意
```
Stack: [call_offset, return_offset]
CALL

→ push current context (stack, memory, pc, gas) into callStack
→ set pc = call_offset
```

🧠 RETURN 操作語意

```
RETURN

→ pop context from callStack
→ restore stack, memory, gas, pc = return_offset
```


### 與 Stack/Memory 的對比

數據位置隔離級別生命週期Stack每個 CallFrame調用結束即銷毀Memory
每個 CallFrame調用結束即銷毀Storage每個合約地址永久存儲（直到合約被銷毀）CallData每個 CallFrame調用期間只讀

實際應用場景:

```
當合約 A 調用合約 B 時：

A 和 B 各自有獨立的 Storage 空間
A 無法直接讀取或修改 B 的 Storage
B 也無法直接讀取或修改 A 的 Storage
如果需要數據交換，必須通過函數調用和返回值
```

TODO: Call - DelegationCall StaticCall Unit Test