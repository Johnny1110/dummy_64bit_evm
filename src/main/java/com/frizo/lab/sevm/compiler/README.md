# EVM Compiler

<br>

---

<br>

## 架構

```
evm-solidity-compiler/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── evmcompiler/
│   │   │           ├── Main.java                    # 主程式入口
│   │   │           ├── lexer/                       # 詞法分析器
│   │   │           │   ├── Lexer.java
│   │   │           │   ├── Token.java
│   │   │           │   ├── TokenType.java
│   │   │           │   └── LexerException.java
│   │   │           ├── parser/                      # 語法分析器
│   │   │           │   ├── Parser.java
│   │   │           │   ├── ast/                     # 抽象語法樹
│   │   │           │   │   ├── ASTNode.java
│   │   │           │   │   ├── expressions/
│   │   │           │   │   │   ├── Expression.java
│   │   │           │   │   │   ├── BinaryExpression.java
│   │   │           │   │   │   ├── UnaryExpression.java
│   │   │           │   │   │   ├── Literal.java
│   │   │           │   │   │   ├── Identifier.java
│   │   │           │   │   │   └── FunctionCall.java
│   │   │           │   │   ├── statements/
│   │   │           │   │   │   ├── Statement.java
│   │   │           │   │   │   ├── VariableDeclaration.java
│   │   │           │   │   │   ├── Assignment.java
│   │   │           │   │   │   ├── IfStatement.java
│   │   │           │   │   │   ├── WhileStatement.java
│   │   │           │   │   │   ├── ForStatement.java
│   │   │           │   │   │   └── ReturnStatement.java
│   │   │           │   │   └── declarations/
│   │   │           │   │       ├── ContractDeclaration.java
│   │   │           │   │       ├── FunctionDeclaration.java
│   │   │           │   │       ├── StateVariableDeclaration.java
│   │   │           │   │       └── EventDeclaration.java
│   │   │           │   └── ParserException.java
│   │   │           ├── semantic/                    # 語義分析
│   │   │           │   ├── SemanticAnalyzer.java
│   │   │           │   ├── SymbolTable.java
│   │   │           │   ├── TypeChecker.java
│   │   │           │   ├── ScopeManager.java
│   │   │           │   └── SemanticException.java
│   │   │           ├── types/                       # 型別系統
│   │   │           │   ├── Type.java
│   │   │           │   ├── ElementaryType.java
│   │   │           │   ├── ArrayType.java
│   │   │           │   ├── MappingType.java
│   │   │           │   ├── StructType.java
│   │   │           │   └── FunctionType.java
│   │   │           ├── codegen/                     # 程式碼生成
│   │   │           │   ├── CodeGenerator.java
│   │   │           │   ├── EVMCodeGenerator.java
│   │   │           │   ├── BytecodeGenerator.java
│   │   │           │   ├── ABIGenerator.java
│   │   │           │   └── CodegenException.java
│   │   │           ├── optimizer/                   # 最佳化器
│   │   │           │   ├── Optimizer.java
│   │   │           │   ├── PeepholeOptimizer.java
│   │   │           │   ├── DeadCodeEliminator.java
│   │   │           │   └── ConstantFolder.java
│   │   │           ├── evm/                         # EVM 相關
│   │   │           │   ├── Opcode.java
│   │   │           │   ├── Instruction.java
│   │   │           │   ├── Memory.java
│   │   │           │   ├── Stack.java
│   │   │           │   └── Gas.java
│   │   │           ├── utils/                       # 工具類
│   │   │           │   ├── FileUtils.java
│   │   │           │   ├── StringUtils.java
│   │   │           │   ├── Logger.java
│   │   │           │   └── Config.java
│   │   │           └── exceptions/                  # 異常類
│   │   │               ├── CompilerException.java
│   │   │               ├── SyntaxException.java
│   │   │               └── RuntimeException.java
│   │   └── resources/
│   │       ├── opcodes.json                        # EVM 操作碼定義
│   │       ├── keywords.txt                        # Solidity 關鍵字
│   │       └── config.properties                   # 配置檔案
│   └── test/
│       └── java/
│           └── com/
│               └── evmcompiler/
│                   ├── lexer/
│                   │   └── LexerTest.java
│                   ├── parser/
│                   │   └── ParserTest.java
│                   ├── semantic/
│                   │   └── SemanticAnalyzerTest.java
│                   ├── codegen/
│                   │   └── CodeGeneratorTest.java
│                   └── integration/
│                       └── CompilerIntegrationTest.java
├── examples/                                       # 範例 Solidity 檔案
│   ├── simple_contract.sol
│   ├── token_contract.sol
│   └── complex_contract.sol
├── docs/                                          # 文件
│   ├── architecture.md
│   ├── development_guide.md
│   └── api_reference.md
├── pom.xml                                        # Maven 配置
└── README.md
```

<br>
<br>

## 核心模組說明

1. 詞法分析器 (Lexer)

功能: 將 Solidity 原始碼轉換成 Token 流
關鍵類:

Lexer.java: 主要詞法分析邏輯
Token.java: 表示單個詞法單元
TokenType.java: 定義所有 Token 類型（關鍵字、操作符、識別符等）

2. 語法分析器 (Parser)

功能: 將 Token 流轉換成抽象語法樹 (AST)
關鍵類:

Parser.java: 實現遞歸下降解析器
ast/: 包含所有 AST 節點類型

3. 語義分析器 (Semantic Analyzer)

功能: 型別檢查、作用域管理、語義驗證
關鍵類:

SemanticAnalyzer.java: 主要語義分析邏輯
SymbolTable.java: 符號表管理
TypeChecker.java: 型別檢查

4. 程式碼生成器 (Code Generator)

功能: 將 AST 轉換成 EVM 字節碼
關鍵類:

EVMCodeGenerator.java: 生成 EVM 操作碼
BytecodeGenerator.java: 生成最終字節碼
ABIGenerator.java: 生成 ABI 介面

5. 最佳化器 (Optimizer)

功能: 程式碼最佳化
關鍵類:

PeepholeOptimizer.java: 窺孔最佳化
DeadCodeEliminator.java: 死碼消除
ConstantFolder.java: 常數折疊


<br>

---

<br>

### 階段 1: 基礎架構

實現基本的 Token 和 TokenType
建立簡單的 Lexer 處理基本語法元素

### 階段 2: 詞法分析

完整實現 Lexer
支援所有 Solidity 語法元素
添加錯誤處理和異常管理

### 階段 3: 語法分析

建立 AST 節點類別
實現基本的 Parser
支援基本的 Solidity 語法結構

### 階段 4: 語義分析

實現符號表管理
添加型別檢查
實現作用域管理

### 階段 5: 程式碼生成

建立 EVM 操作碼映射
實現基本的程式碼生成
生成可執行的字節碼

### 階段 6: 最佳化與完善

添加程式碼最佳化
完善錯誤處理
添加更多 Solidity 功能支援