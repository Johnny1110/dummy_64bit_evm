package com.frizo.lab.sevm.compiler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SEVMCompiler {

    public static void main(String[] args) {
        try {
            // 步驟1: 詞法分析
            Lexer lexer = new Lexer(solidityCode);
            var tokens = lexer.tokenize();

            // 步驟2: 語法分析
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();

            // 步驟3: 程式碼生成
            EVMCodeGenerator codeGen = new EVMCodeGenerator();
            List<Instruction> instructions = codeGen.generate(ast);

            // 步驟4: 輸出結果
            System.out.println("生成的 EVM 指令:");
            for (Instruction instr : instructions) {
                System.out.println(instr.toString());
            }

        } catch (Exception e) {
            System.err.println("編譯錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
