package com.frizo.lab.sevm.compiler.lexer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

    @Test
    void tokenize() {

        String solidityCode = """
            contract TestContract {
                event TestEvent(uint256 indexed value);
                
                function testLoop() public {
                    for (uint256 i = 1; i <= 10; i++) {
                        emit TestEvent(i);
                    }
                }
            }
            """;

        Lexer lexer = new Lexer(solidityCode);
        var tokens = lexer.tokenize();

        assertFalse(tokens.isEmpty(), "Token list should not be empty");
        assertEquals("contract", tokens.get(0).getValue(), "First token should be 'contract'");
        assertEquals("TestContract", tokens.get(1).getValue(), "Second token should be 'TestContract'");

        tokens.forEach(token -> {
            System.out.println("token: " + token);
        });
    }
}