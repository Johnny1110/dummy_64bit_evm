package com.frizo.lab.sevm.compiler.lexer;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class Lexer {

    private final String source;
    private int position;
    private int line;
    private int column;
    private final Map<String, TokenType> keywords;
    ;

    public Lexer(String sourceCode) {
        this.source = sourceCode;
        this.position = 0;
        this.line = 1;
        this.column = 1;
        this.keywords = initKeywords();
    }

    private Map<String, TokenType> initKeywords() {
        Map<String, TokenType> map = new HashMap<>();
        map.put("contract", TokenType.CONTRACT);
        map.put("function", TokenType.FUNCTION);
        map.put("for", TokenType.FOR);
        map.put("emit", TokenType.EMIT);
        map.put("event", TokenType.EVENT);
        map.put("public", TokenType.PUBLIC);
        map.put("uint256", TokenType.UINT256);
        map.put("if", TokenType.IF);
        map.put("else", TokenType.ELSE);
        map.put("while", TokenType.WHILE);
        map.put("return", TokenType.RETURN);
        map.put("indexed", TokenType.INDEXED);
        return map;
    }

    private char advance() {
        column++;
        return source.charAt(position++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(position);
    }

    private boolean isAtEnd() {
        return position >= source.length();
    }

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r') {
                // Skip spaces, tabs, and carriage returns
                advance();
            } else if (c == '\n') {
                // Handle newlines
                line++;
                column = 0;
                advance();
            } else {
                break;
            }
        }
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(position) != expected) return false;
        position++;
        column++;
        return true;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) break;

            char c = advance();
            // make char to token.
            Token token = newToken(c);

            if (token != null && token.getType() != TokenType.WHITESPACE &&
                    token.getType() != TokenType.COMMENT) {
                tokens.add(token);
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private Token newToken(char c) {
        switch (c) {
            case '(':
                return new Token(TokenType.LPAREN, "(", line, column - 1);
            case ')':
                return new Token(TokenType.RPAREN, ")", line, column - 1);
            case '{':
                return new Token(TokenType.LBRACE, "{", line, column - 1);
            case '}':
                return new Token(TokenType.RBRACE, "}", line, column - 1);
            case '[':
                return new Token(TokenType.LBRACKET, "[", line, column - 1);
            case ']':
                return new Token(TokenType.RBRACKET, "]", line, column - 1);
            case ';':
                return new Token(TokenType.SEMICOLON, ";", line, column - 1);
            case ',':
                return new Token(TokenType.COMMA, ",", line, column - 1);
            case '.':
                return new Token(TokenType.DOT, ".", line, column - 1);
            case '+':
                if (match('+')) return new Token(TokenType.PLUS_PLUS, "++", line, column - 2);
                if (match('=')) return new Token(TokenType.PLUS_ASSIGN, "+=", line, column - 2);
                return new Token(TokenType.PLUS, "+", line, column - 1);
            case '-':
                if (match('-')) return new Token(TokenType.MINUS_MINUS, "--", line, column - 2);
                if (match('=')) return new Token(TokenType.MINUS_ASSIGN, "-=", line, column - 2);
                return new Token(TokenType.MINUS, "-", line, column - 1);
            case '*':
                return new Token(TokenType.MULTIPLY, "*", line, column - 1);
            case '/':
                return new Token(TokenType.DIVIDE, "/", line, column - 1);
            case '%':
                return new Token(TokenType.MODULO, "%", line, column - 1);
            case '=':
                if (match('=')) return new Token(TokenType.EQUAL, "==", line, column - 2);
                return new Token(TokenType.ASSIGN, "=", line, column - 1);
            case '!':
                if (match('=')) return new Token(TokenType.NOT_EQUAL, "!=", line, column - 2);
                return new Token(TokenType.NOT, "!", line, column - 1);
            case '<':
                if (match('=')) return new Token(TokenType.LESS_EQUAL, "<=", line, column - 2);
                return new Token(TokenType.LESS_THAN, "<", line, column - 1);
            case '>':
                if (match('=')) return new Token(TokenType.GREATER_EQUAL, ">=", line, column - 2);
                return new Token(TokenType.GREATER_THAN, ">", line, column - 1);
            case '&':
                if (match('&')) return new Token(TokenType.AND, "&&", line, column - 2);
                break;
            case '|':
                if (match('|')) return new Token(TokenType.OR, "||", line, column - 2);
                break;
        }

        if (Character.isDigit(c)) {
            return number();
        }

        if (Character.isAlphabetic(c) || c == '_') {
            return identifier();
        }

        if (c == '"') {
            return string();
        }

        return new Token(TokenType.UNKNOWN, String.valueOf(c), line, column - 1);
    }

    private Token number() {
        int start = position - 1;
        while (!isAtEnd() && Character.isDigit(peek())) {
            advance();
        }

        String value = source.substring(start, position);
        return new Token(TokenType.NUMBER, value, line, column - value.length());
    }

    private Token string() {
        int start = position;
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            throw new RuntimeException("Unterminated string at line " + line);
        }

        advance(); // closing "
        String value = source.substring(start, position - 1);
        return new Token(TokenType.STRING, value, line, column - value.length() - 2);
    }

    private Token identifier() {
        int start = position - 1;
        while (!isAtEnd() && (Character.isAlphabetic(peek()) || peek() == '_')) {
            advance();
        }

        String value = source.substring(start, position);
        TokenType type = keywords.getOrDefault(value, TokenType.IDENTIFIER);
        return new Token(type, value, line, column - value.length());
    }

}
