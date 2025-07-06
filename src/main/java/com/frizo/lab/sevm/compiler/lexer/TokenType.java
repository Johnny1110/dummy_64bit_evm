package com.frizo.lab.sevm.compiler.lexer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum TokenType {
    // 關鍵字
    CONTRACT, FUNCTION, FOR, EMIT, EVENT, PUBLIC, UINT256, IF, ELSE, WHILE, RETURN,

    // 操作符
    ASSIGN, PLUS, MINUS, MULTIPLY, DIVIDE, MODULO,
    EQUAL, NOT_EQUAL, LESS_THAN, GREATER_THAN, LESS_EQUAL, GREATER_EQUAL,
    AND, OR, NOT,
    PLUS_PLUS, MINUS_MINUS,
    PLUS_ASSIGN, MINUS_ASSIGN,

    // 分隔符
    SEMICOLON, COMMA, DOT,
    LPAREN, RPAREN, LBRACE, RBRACE, LBRACKET, RBRACKET,

    // 標識符和字面量
    IDENTIFIER, NUMBER, STRING,

    // 特殊
    EOF, NEWLINE, WHITESPACE, COMMENT,

    // 關鍵字 - 索引
    INDEXED,

    // 未知
    UNKNOWN
}
