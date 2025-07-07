package com.frizo.lab.sevm.compiler.parser;

import com.frizo.lab.sevm.compiler.lexer.Token;
import com.frizo.lab.sevm.compiler.lexer.TokenType;
import com.frizo.lab.sevm.compiler.parser.abs.AbsNode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public AbsNode parse() {
        return parseContract();
    }

    private AbsNode parseContract() {
        log.info("Parsing contract...");

        consume(TokenType.CONTRACT, "Expected 'contract'");
        String name = consume(TokenType.IDENTIFIER, "Expected contract name").getValue();
        consume(TokenType.LBRACE, "Expected '{'");

        return null;
    }

    private Token consume(TokenType tokenType, String expectedMsg) {
        // TODO
        return null;
    }

}
