# Instruction

<br>

---

<br>

## 主要

```java
// Main.java - 主程式入口
package com.evmcompiler;

import com.evmcompiler.lexer.Lexer;
import com.evmcompiler.parser.Parser;
import com.evmcompiler.parser.ast.ASTNode;
import com.evmcompiler.codegen.EVMCodeGenerator;
import com.evmcompiler.evm.Instruction;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 測試 Solidity 程式碼: for loop with emit log
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

// =================================================================
// Token.java - 詞法單元
package com.evmcompiler.lexer;

public class Token {
    private final TokenType type;
    private final String value;
    private final int line;
    private final int column;

    public Token(TokenType type, String value, int line, int column) {
        this.type = type;
        this.value = value;
        this.line = line;
        this.column = column;
    }

    public TokenType getType() { return type; }
    public String getValue() { return value; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    @Override
    public String toString() {
        return String.format("Token{%s, '%s', %d:%d}", type, value, line, column);
    }
}

// =================================================================
// TokenType.java - 詞法單元類型
package com.evmcompiler.lexer;

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

// =================================================================
// Lexer.java - 詞法分析器
package com.evmcompiler.lexer;

import java.util.*;
        import java.util.regex.Pattern;

public class Lexer {
    private final String source;
    private int position;
    private int line;
    private int column;
    private final Map<String, TokenType> keywords;

    public Lexer(String source) {
        this.source = source;
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

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            skipWhitespace();
            if (isAtEnd()) break;

            int startLine = line;
            int startColumn = column;

            Token token = nextToken();
            if (token != null && token.getType() != TokenType.WHITESPACE &&
                    token.getType() != TokenType.COMMENT) {
                tokens.add(token);
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, column));
        return tokens;
    }

    private Token nextToken() {
        char c = advance();

        switch (c) {
            case '(': return new Token(TokenType.LPAREN, "(", line, column - 1);
            case ')': return new Token(TokenType.RPAREN, ")", line, column - 1);
            case '{': return new Token(TokenType.LBRACE, "{", line, column - 1);
            case '}': return new Token(TokenType.RBRACE, "}", line, column - 1);
            case '[': return new Token(TokenType.LBRACKET, "[", line, column - 1);
            case ']': return new Token(TokenType.RBRACKET, "]", line, column - 1);
            case ';': return new Token(TokenType.SEMICOLON, ";", line, column - 1);
            case ',': return new Token(TokenType.COMMA, ",", line, column - 1);
            case '.': return new Token(TokenType.DOT, ".", line, column - 1);
            case '+':
                if (match('+')) return new Token(TokenType.PLUS_PLUS, "++", line, column - 2);
                if (match('=')) return new Token(TokenType.PLUS_ASSIGN, "+=", line, column - 2);
                return new Token(TokenType.PLUS, "+", line, column - 1);
            case '-':
                if (match('-')) return new Token(TokenType.MINUS_MINUS, "--", line, column - 2);
                if (match('=')) return new Token(TokenType.MINUS_ASSIGN, "-=", line, column - 2);
                return new Token(TokenType.MINUS, "-", line, column - 1);
            case '*': return new Token(TokenType.MULTIPLY, "*", line, column - 1);
            case '/': return new Token(TokenType.DIVIDE, "/", line, column - 1);
            case '%': return new Token(TokenType.MODULO, "%", line, column - 1);
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

    private Token identifier() {
        int start = position - 1;
        while (!isAtEnd() && (Character.isAlphanumeric(peek()) || peek() == '_')) {
            advance();
        }

        String value = source.substring(start, position);
        TokenType type = keywords.getOrDefault(value, TokenType.IDENTIFIER);
        return new Token(type, value, line, column - value.length());
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

    private void skipWhitespace() {
        while (!isAtEnd()) {
            char c = peek();
            if (c == ' ' || c == '\t' || c == '\r') {
                advance();
            } else if (c == '\n') {
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
}

// =================================================================
// ASTNode.java - 抽象語法樹節點基類
package com.evmcompiler.parser.ast;

public abstract class ASTNode {
    public abstract void accept(ASTVisitor visitor);
    public abstract String toString();
}

// =================================================================
// ASTVisitor.java - 訪問者模式介面
package com.evmcompiler.parser.ast;

import com.evmcompiler.parser.ast.statements.*;
        import com.evmcompiler.parser.ast.expressions.*;
        import com.evmcompiler.parser.ast.declarations.*;

public interface ASTVisitor {
    void visit(ContractDeclaration node);
    void visit(FunctionDeclaration node);
    void visit(EventDeclaration node);
    void visit(ForStatement node);
    void visit(EmitStatement node);
    void visit(VariableDeclaration node);
    void visit(Assignment node);
    void visit(BinaryExpression node);
    void visit(Identifier node);
    void visit(Literal node);
    void visit(FunctionCall node);
    void visit(BlockStatement node);
}

// =================================================================
// ContractDeclaration.java - 合約聲明
package com.evmcompiler.parser.ast.declarations;

import com.evmcompiler.parser.ast.ASTNode;
import com.evmcompiler.parser.ast.ASTVisitor;
import java.util.List;

public class ContractDeclaration extends ASTNode {
    private final String name;
    private final List<ASTNode> members;

    public ContractDeclaration(String name, List<ASTNode> members) {
        this.name = name;
        this.members = members;
    }

    public String getName() { return name; }
    public List<ASTNode> getMembers() { return members; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("ContractDeclaration{name='%s', members=%d}", name, members.size());
    }
}

// =================================================================
// FunctionDeclaration.java - 函數聲明
package com.evmcompiler.parser.ast.declarations;

import com.evmcompiler.parser.ast.ASTNode;
import com.evmcompiler.parser.ast.ASTVisitor;
import com.evmcompiler.parser.ast.statements.BlockStatement;
import java.util.List;

public class FunctionDeclaration extends ASTNode {
    private final String name;
    private final List<VariableDeclaration> parameters;
    private final List<String> visibility;
    private final BlockStatement body;

    public FunctionDeclaration(String name, List<VariableDeclaration> parameters,
                               List<String> visibility, BlockStatement body) {
        this.name = name;
        this.parameters = parameters;
        this.visibility = visibility;
        this.body = body;
    }

    public String getName() { return name; }
    public List<VariableDeclaration> getParameters() { return parameters; }
    public List<String> getVisibility() { return visibility; }
    public BlockStatement getBody() { return body; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("FunctionDeclaration{name='%s', params=%d}", name, parameters.size());
    }
}

// =================================================================
// EventDeclaration.java - 事件聲明
package com.evmcompiler.parser.ast.declarations;

import com.evmcompiler.parser.ast.ASTNode;
import com.evmcompiler.parser.ast.ASTVisitor;
import java.util.List;

public class EventDeclaration extends ASTNode {
    private final String name;
    private final List<VariableDeclaration> parameters;

    public EventDeclaration(String name, List<VariableDeclaration> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() { return name; }
    public List<VariableDeclaration> getParameters() { return parameters; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("EventDeclaration{name='%s', params=%d}", name, parameters.size());
    }
}

// =================================================================
// VariableDeclaration.java - 變數聲明
package com.evmcompiler.parser.ast.declarations;

import com.evmcompiler.parser.ast.ASTNode;
import com.evmcompiler.parser.ast.ASTVisitor;

public class VariableDeclaration extends ASTNode {
    private final String type;
    private final String name;
    private final boolean indexed;

    public VariableDeclaration(String type, String name, boolean indexed) {
        this.type = type;
        this.name = name;
        this.indexed = indexed;
    }

    public String getType() { return type; }
    public String getName() { return name; }
    public boolean isIndexed() { return indexed; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("VariableDeclaration{type='%s', name='%s', indexed=%s}",
                type, name, indexed);
    }
}

// =================================================================
// ForStatement.java - For 迴圈語句
package com.evmcompiler.parser.ast.statements;

import com.evmcompiler.parser.ast.ASTNode;
import com.evmcompiler.parser.ast.ASTVisitor;
import com.evmcompiler.parser.ast.expressions.Expression;

public class ForStatement extends ASTNode {
    private final ASTNode init;
    private final Expression condition;
    private final Expression update;
    private final ASTNode body;

    public ForStatement(ASTNode init, Expression condition, Expression update, ASTNode body) {
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public ASTNode getInit() { return init; }
    public Expression getCondition() { return condition; }
    public Expression getUpdate() { return update; }
    public ASTNode getBody() { return body; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("ForStatement{init=%s, condition=%s, update=%s}",
                init, condition, update);
    }
}

// =================================================================
// EmitStatement.java - Emit 語句
package com.evmcompiler.parser.ast.statements;

import com.evmcompiler.parser.ast.ASTNode;
import com.evmcompiler.parser.ast.ASTVisitor;
import com.evmcompiler.parser.ast.expressions.FunctionCall;

public class EmitStatement extends ASTNode {
    private final FunctionCall eventCall;

    public EmitStatement(FunctionCall eventCall) {
        this.eventCall = eventCall;
    }

    public FunctionCall getEventCall() { return eventCall; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("EmitStatement{event=%s}", eventCall);
    }
}

// =================================================================
// BlockStatement.java - 代碼塊
package com.evmcompiler.parser.ast.statements;

import com.evmcompiler.parser.ast.ASTNode;
import com.evmcompiler.parser.ast.ASTVisitor;
import java.util.List;

public class BlockStatement extends ASTNode {
    private final List<ASTNode> statements;

    public BlockStatement(List<ASTNode> statements) {
        this.statements = statements;
    }

    public List<ASTNode> getStatements() { return statements; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("BlockStatement{statements=%d}", statements.size());
    }
}

// =================================================================
// Assignment.java - 賦值語句
package com.evmcompiler.parser.ast.statements;

import com.evmcompiler.parser.ast.ASTNode;
import com.evmcompiler.parser.ast.ASTVisitor;
import com.evmcompiler.parser.ast.expressions.Expression;

public class Assignment extends ASTNode {
    private final Expression left;
    private final Expression right;

    public Assignment(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }

    public Expression getLeft() { return left; }
    public Expression getRight() { return right; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("Assignment{left=%s, right=%s}", left, right);
    }
}

// =================================================================
// Expression.java - 表達式基類
package com.evmcompiler.parser.ast.expressions;

import com.evmcompiler.parser.ast.ASTNode;

public abstract class Expression extends ASTNode {
    // 表達式基類
}

// =================================================================
// BinaryExpression.java - 二元表達式
package com.evmcompiler.parser.ast.expressions;

import com.evmcompiler.parser.ast.ASTVisitor;
import com.evmcompiler.lexer.TokenType;

public class BinaryExpression extends Expression {
    private final Expression left;
    private final TokenType operator;
    private final Expression right;

    public BinaryExpression(Expression left, TokenType operator, Expression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public Expression getLeft() { return left; }
    public TokenType getOperator() { return operator; }
    public Expression getRight() { return right; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("BinaryExpression{%s %s %s}", left, operator, right);
    }
}

// =================================================================
// Identifier.java - 標識符
package com.evmcompiler.parser.ast.expressions;

import com.evmcompiler.parser.ast.ASTVisitor;

public class Identifier extends Expression {
    private final String name;

    public Identifier(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("Identifier{name='%s'}", name);
    }
}

// =================================================================
// Literal.java - 字面量
package com.evmcompiler.parser.ast.expressions;

import com.evmcompiler.parser.ast.ASTVisitor;

public class Literal extends Expression {
    private final Object value;
    private final String type;

    public Literal(Object value, String type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() { return value; }
    public String getType() { return type; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("Literal{value=%s, type='%s'}", value, type);
    }
}

// =================================================================
// FunctionCall.java - 函數調用
package com.evmcompiler.parser.ast.expressions;

import com.evmcompiler.parser.ast.ASTVisitor;
import java.util.List;

public class FunctionCall extends Expression {
    private final String name;
    private final List<Expression> arguments;

    public FunctionCall(String name, List<Expression> arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() { return name; }
    public List<Expression> getArguments() { return arguments; }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("FunctionCall{name='%s', args=%d}", name, arguments.size());
    }
}

// =================================================================
// Parser.java - 語法分析器
package com.evmcompiler.parser;

import com.evmcompiler.lexer.Token;
import com.evmcompiler.lexer.TokenType;
import com.evmcompiler.parser.ast.*;
        import com.evmcompiler.parser.ast.declarations.*;
        import com.evmcompiler.parser.ast.expressions.*;
        import com.evmcompiler.parser.ast.statements.*;
        import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public ASTNode parse() {
        return parseContract();
    }

    private ContractDeclaration parseContract() {
        consume(TokenType.CONTRACT, "Expected 'contract'");
        String name = consume(TokenType.IDENTIFIER, "Expected contract name").getValue();
        consume(TokenType.LBRACE, "Expected '{'");

        List<ASTNode> members = new ArrayList<>();
        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            if (check(TokenType.EVENT)) {
                members.add(parseEvent());
            } else if (check(TokenType.FUNCTION)) {
                members.add(parseFunction());
            } else {
                throw new RuntimeException("Unexpected token in contract: " + peek().getValue());
            }
        }

        consume(TokenType.RBRACE, "Expected '}'");
        return new ContractDeclaration(name, members);
    }

    private EventDeclaration parseEvent() {
        consume(TokenType.EVENT, "Expected 'event'");
        String name = consume(TokenType.IDENTIFIER, "Expected event name").getValue();
        consume(TokenType.LPAREN, "Expected '('");

        List<VariableDeclaration> parameters = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                String type = consume(TokenType.UINT256, "Expected type").getValue();
                boolean indexed = false;
                if (check(TokenType.INDEXED)) {
                    advance();
                    indexed = true;
                }
                String paramName = consume(TokenType.IDENTIFIER, "Expected parameter name").getValue();
                parameters.add(new VariableDeclaration(type, paramName, indexed));
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RPAREN, "Expected ')'");
        consume(TokenType.SEMICOLON, "Expected ';'");

        return new EventDeclaration(name, parameters);
    }

    private FunctionDeclaration parseFunction() {
        consume(TokenType.FUNCTION, "Expected 'function'");
        String name = consume(TokenType.IDENTIFIER, "Expected function name").getValue();
        consume(TokenType.LPAREN, "Expected '('");

        List<VariableDeclaration> parameters = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                String type = advance().getValue();
                String paramName = consume(TokenType.IDENTIFIER, "Expected parameter name").getValue();
                parameters.add(new VariableDeclaration(type, paramName, false));
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RPAREN, "Expected ')'");

        List<String> visibility = new ArrayList<>();
        if (check(TokenType.PUBLIC)) {
            visibility.add(advance().getValue());
        }

        BlockStatement body = parseBlock();

        return new FunctionDeclaration(name, parameters, visibility, body);
    }

    private BlockStatement parseBlock() {
        consume(TokenType.LBRACE, "Expected '{'");
        List<ASTNode> statements = new ArrayList<>();

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(parseStatement());
        }

        consume(TokenType.RBRACE, "Expected '}'");
        return new BlockStatement(statements);
    }

    private ASTNode parseStatement() {
        if (check(TokenType.FOR)) {
            return parseForStatement();
        } else if (check(TokenType.EMIT)) {
            return parseEmitStatement();
        } else if (check(TokenType.UINT256)) {
            return parseVariableDeclaration();
        } else if (check(TokenType.IDENTIFIER)) {
            return parseAssignment();
        } else {
            throw new RuntimeException("Unexpected statement: " + peek().getValue());
        }
    }

    private ForStatement parseForStatement() {
        consume(TokenType.FOR, "Expected 'for'");
        consume(TokenType.LPAREN, "Expected '('");

        // Init
        ASTNode init = null;
        if (check(TokenType.UINT256)) {
            init = parseVariableDeclaration();
        }

        // Condition
        Expression condition = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';'");

        // Update
        Expression update = parseExpression();
        consume(TokenType.RPAREN, "Expected ')'");

        // Body
        ASTNode body = parseStatement();

        return new ForStatement(init, condition, update, body);
    }

    private EmitStatement parseEmitStatement() {
        consume(TokenType.EMIT, "Expected 'emit'");
        String eventName = consume(TokenType.IDENTIFIER, "Expected event name").getValue();
        consume(TokenType.LPAREN, "Expected '('");

        List<Expression> arguments = new ArrayList<>();
        if (!check(TokenType.RPAREN)) {
            do {
                arguments.add(parseExpression());
            } while (match(TokenType.COMMA));
        }

        consume(TokenType.RPAREN, "Expected ')'");
        consume(TokenType.SEMICOLON, "Expected ';'");

        return new EmitStatement(new FunctionCall(eventName, arguments));
    }

    private VariableDeclaration parseVariableDeclaration() {
        String type = consume(TokenType.UINT256, "Expected type").getValue();
        String name = consume(TokenType.IDENTIFIER, "Expected variable name").getValue();

        if (match(TokenType.ASSIGN)) {
            Expression value = parseExpression();
            consume(TokenType.SEMICOLON, "Expected ';'");
            // 這裡簡化處理，實際需要更複雜的初始化邏輯
        } else {
            consume(TokenType.SEMICOLON, "Expected ';'");
        }

        return new VariableDeclaration(type, name, false);
    }

    private Assignment parseAssignment() {
        Expression left = parseExpression();

        if (match(TokenType.ASSIGN)) {
            Expression right = parseExpression();
            consume(TokenType.SEMICOLON, "Expected ';'");
            return new Assignment(left, right);
        } else if (match(TokenType.PLUS_PLUS)) {
            consume(TokenType.SEMICOLON, "Expected ';'");
            // i++ 轉換為 i = i + 1
            return new Assignment(left, new BinaryExpression(left, TokenType.PLUS, new Literal(1, "uint256")));
        } else {
            throw new RuntimeException("Expected assignment operator");
        }
    }

    private Expression parseExpression() {
        return parseComparison();
    }

    private Expression parseComparison() {
        Expression expr = parseAddition();

        while (match(TokenType.GREATER_THAN, TokenType.GREATER_EQUAL,
                TokenType.LESS_THAN, TokenType.LESS_EQUAL,
                TokenType.EQUAL, TokenType.NOT_EQUAL)) {
            TokenType operator = previous().getType();
            Expression right = parseAddition();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression parseAddition() {
        Expression expr = parseMultiplication();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            TokenType operator = previous().getType();
            Expression right = parseMultiplication();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression parseMultiplication() {
        Expression expr = parsePrimary();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MODULO)) {
            TokenType operator = previous().getType();
            Expression right = parsePrimary();
            expr = new BinaryExpression(expr, operator, right);
        }

        return expr;
    }

    private Expression parsePrimary() {
        if (match(TokenType.NUMBER)) {
            return new Literal(Integer.parseInt(previous().getValue()), "uint256");
        }

        if (match(TokenType.IDENTIFIER)) {
            return new Identifier(previous().getValue());
        }

        if (match(TokenType.LPAREN)) {
            Expression expr = parseExpression();
            consume(TokenType.RPAREN, "Expected ')' after expression");
            return expr;
        }

        throw new RuntimeException("Unexpected token: " + peek().getValue());
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw new RuntimeException(message + " at line " + peek().getLine() +
                ", got: " + peek().getValue());
    }
}

// =================================================================
// Instruction.java - EVM 指令
package com.evmcompiler.evm;

public class Instruction {
    private final byte opcode;
    private final Object[] operands;
    private final String mnemonic;

    public Instruction(byte opcode, String mnemonic, Object... operands) {
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.operands = operands;
    }

    public byte getOpcode() { return opcode; }
    public Object[] getOperands() { return operands; }
    public String getMnemonic() { return mnemonic; }

    @Override
    public String toString() {
        if (operands.length == 0) {
            return mnemonic;
        } else {
            StringBuilder sb = new StringBuilder(mnemonic);
            sb.append(" ");
            for (int i = 0; i < operands.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(operands[i]);
            }
            return sb.toString();
        }
    }
}

// =================================================================
// EVMCodeGenerator.java - EVM 程式碼生成器
package com.evmcompiler.codegen;

import com.evmcompiler.parser.ast.*;
        import com.evmcompiler.parser.ast.declarations.*;
        import com.evmcompiler.parser.ast.expressions.*;
        import com.evmcompiler.parser.ast.statements.*;
        import com.evmcompiler.evm.Instruction;
import java.util.*;

public class EVMCodeGenerator implements ASTVisitor {
    private final List<Instruction> instructions;
    private final Map<String, Integer> variables;
    private final Map<String, Integer> jumpLabels;
    private int nextVariableOffset;
    private int nextLabelId;

    public EVMCodeGenerator() {
        this.instructions = new ArrayList<>();
        this.variables = new HashMap<>();
        this.jumpLabels = new HashMap<>();
        this.nextVariableOffset = 0;
        this.nextLabelId = 0;
    }

    public List<Instruction> generate(ASTNode ast) {
        instructions.clear();
        variables.clear();
        jumpLabels.clear();
        nextVariableOffset = 0;
        nextLabelId = 0;

        ast.accept(this);

        // 添加程式結束指令
        instructions.add(new Instruction((byte) 0x00, "STOP"));

        return new ArrayList<>(instructions);
    }

    @Override
    public void visit(ContractDeclaration node) {
        // 生成合約初始化程式碼
        instructions.add(new Instruction((byte) 0x60, "PUSH1", 0x80)); // 設定初始記憶體指標
        instructions.add(new Instruction((byte) 0x52, "MSTORE"));

        // 處理合約成員
        for (ASTNode member : node.getMembers()) {
            member.accept(this);
        }
    }

    @Override
    public void visit(EventDeclaration node) {
        // 事件聲明在編譯時處理，這裡暫時不生成程式碼
        // 實際上需要生成 ABI 和事件簽名
    }

    @Override
    public void visit(FunctionDeclaration node) {
        // 生成函數標籤
        String funcLabel = "func_" + node.getName();
        instructions.add(new Instruction((byte) 0x5B, "JUMPDEST")); // 函數入口點

        // 處理函數體
        node.getBody().accept(this);

        // 函數返回
        instructions.add(new Instruction((byte) 0x00, "STOP"));
    }

    @Override
    public void visit(ForStatement node) {
        // 生成 for 迴圈程式碼

        // 1. 初始化
        if (node.getInit() != null) {
            node.getInit().accept(this);
        }

        // 2. 迴圈標籤
        int loopStart = instructions.size();
        instructions.add(new Instruction((byte) 0x5B, "JUMPDEST")); // 迴圈開始

        // 3. 條件檢查
        if (node.getCondition() != null) {
            node.getCondition().accept(this);
            // 如果條件為 false，跳出迴圈
            instructions.add(new Instruction((byte) 0x15, "ISZERO")); // 反轉條件
            int exitJumpPos = instructions.size();
            instructions.add(new Instruction((byte) 0x60, "PUSH1", 0)); // 佔位符，稍後填入
            instructions.add(new Instruction((byte) 0x57, "JUMPI")); // 條件跳轉
        }

        // 4. 迴圈體
        node.getBody().accept(this);

        // 5. 更新
        if (node.getUpdate() != null) {
            node.getUpdate().accept(this);
        }

        // 6. 跳回迴圈開始
        instructions.add(new Instruction((byte) 0x60, "PUSH1", loopStart));
        instructions.add(new Instruction((byte) 0x56, "JUMP"));

        // 7. 迴圈結束標籤
        int loopEnd = instructions.size();
        instructions.add(new Instruction((byte) 0x5B, "JUMPDEST")); // 迴圈結束

        // 更新條件跳轉的目標地址
        if (node.getCondition() != null) {
            // 更新之前的跳轉指令
            instructions.set(exitJumpPos, new Instruction((byte) 0x60, "PUSH1", loopEnd));
        }
    }

    @Override
    public void visit(EmitStatement node) {
        // 生成 emit 事件程式碼
        FunctionCall eventCall = node.getEventCall();

        // 1. 準備事件參數
        for (Expression arg : eventCall.getArguments()) {
            arg.accept(this);
        }

        // 2. 生成事件簽名 hash (簡化版本)
        String eventSignature = eventCall.getName() + "(uint256)";
        int eventHash = eventSignature.hashCode();

        // 3. 推送事件 hash 到棧
        instructions.add(new Instruction((byte) 0x60, "PUSH1", eventHash & 0xFF));
        instructions.add(new Instruction((byte) 0x60, "PUSH1", (eventHash >> 8) & 0xFF));
        instructions.add(new Instruction((byte) 0x60, "PUSH1", (eventHash >> 16) & 0xFF));
        instructions.add(new Instruction((byte) 0x60, "PUSH1", (eventHash >> 24) & 0xFF));

        // 4. 生成 LOG 指令
        instructions.add(new Instruction((byte) 0xA1, "LOG1")); // LOG1 - 1個 topic
    }

    @Override
    public void visit(VariableDeclaration node) {
        // 記錄變數位置
        variables.put(node.getName(), nextVariableOffset);
        nextVariableOffset++;

        // 如果有初始值，存儲到記憶體
        // 這裡簡化處理，實際需要更複雜的記憶體管理
    }

    @Override
    public void visit(Assignment node) {
        // 生成賦值程式碼

        // 1. 計算右側表達式
        node.getRight().accept(this);

        // 2. 存儲到變數
        if (node.getLeft() instanceof Identifier) {
            String varName = ((Identifier) node.getLeft()).getName();
            Integer offset = variables.get(varName);
            if (offset != null) {
                // 存儲到記憶體
                instructions.add(new Instruction((byte) 0x60, "PUSH1", offset));
                instructions.add(new Instruction((byte) 0x52, "MSTORE"));
            }
        }
    }

    @Override
    public void visit(BinaryExpression node) {
        // 生成二元運算程式碼

        // 1. 計算左運算元
        node.getLeft().accept(this);

        // 2. 計算右運算元
        node.getRight().accept(this);

        // 3. 執行運算
        switch (node.getOperator()) {
            case PLUS:
                instructions.add(new Instruction((byte) 0x01, "ADD"));
                break;
            case MINUS:
                instructions.add(new Instruction((byte) 0x03, "SUB"));
                break;
            case MULTIPLY:
                instructions.add(new Instruction((byte) 0x02, "MUL"));
                break;
            case DIVIDE:
                instructions.add(new Instruction((byte) 0x04, "DIV"));
                break;
            case LESS_THAN:
                instructions.add(new Instruction((byte) 0x10, "LT"));
                break;
            case GREATER_THAN:
                instructions.add(new Instruction((byte) 0x11, "GT"));
                break;
            case LESS_EQUAL:
                // a <= b 等價於 !(a > b)
                instructions.add(new Instruction((byte) 0x11, "GT"));
                instructions.add(new Instruction((byte) 0x15, "ISZERO"));
                break;
            case GREATER_EQUAL:
                // a >= b 等價於 !(a < b)
                instructions.add(new Instruction((byte) 0x10, "LT"));
                instructions.add(new Instruction((byte) 0x15, "ISZERO"));
                break;
            case EQUAL:
                instructions.add(new Instruction((byte) 0x14, "EQ"));
                break;
            case NOT_EQUAL:
                instructions.add(new Instruction((byte) 0x14, "EQ"));
                instructions.add(new Instruction((byte) 0x15, "ISZERO"));
                break;
        }
    }

    @Override
    public void visit(Identifier node) {
        // 載入變數值
        String varName = node.getName();
        Integer offset = variables.get(varName);
        if (offset != null) {
            // 從記憶體載入
            instructions.add(new Instruction((byte) 0x60, "PUSH1", offset));
            instructions.add(new Instruction((byte) 0x51, "MLOAD"));
        } else {
            // 未定義變數錯誤
            throw new RuntimeException("Undefined variable: " + varName);
        }
    }

    @Override
    public void visit(Literal node) {
        // 推送字面量到棧
        if (node.getType().equals("uint256")) {
            int value = (Integer) node.getValue();

            // 根據值的大小選擇適當的 PUSH 指令
            if (value <= 0xFF) {
                instructions.add(new Instruction((byte) 0x60, "PUSH1", value));
            } else if (value <= 0xFFFF) {
                instructions.add(new Instruction((byte) 0x61, "PUSH2", value));
            } else {
                instructions.add(new Instruction((byte) 0x63, "PUSH4", value));
            }
        }
    }

    @Override
    public void visit(FunctionCall node) {
        // 處理函數調用
        for (Expression arg : node.getArguments()) {
            arg.accept(this);
        }

        // 這裡簡化處理，實際需要更複雜的函數調用邏輯
        instructions.add(new Instruction((byte) 0xFC, "ICALL")); // 內部調用
    }

    @Override
    public void visit(BlockStatement node) {
        // 處理代碼塊
        for (ASTNode stmt : node.getStatements()) {
            stmt.accept(this);
        }
    }
}
```


<br>

## 測試:

```java
import com.evmcompiler.lexer.*;
import com.evmcompiler.parser.*;
import com.evmcompiler.parser.ast.ASTNode;
import com.evmcompiler.codegen.EVMCodeGenerator;
import com.evmcompiler.evm.Instruction;
import java.util.List;

public class CompilerTest {
    public static void main(String[] args) {
        System.out.println("=== EVM Solidity Compiler Test ===\n");
        
        // 測試 1: 簡單的 for 迴圈 emit
        testSimpleForLoop();
        
        // 測試 2: 詞法分析器測試
        testLexer();
        
        // 測試 3: 語法分析器測試
        testParser();
    }
    
    private static void testSimpleForLoop() {
        System.out.println("測試 1: 簡單的 for 迴圈 emit");
        System.out.println("=" .repeat(50));
        
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
        
        System.out.println("原始 Solidity 程式碼:");
        System.out.println(solidityCode);
        
        try {
            // 詞法分析
            Lexer lexer = new Lexer(solidityCode);
            List<Token> tokens = lexer.tokenize();
            
            System.out.println("\n詞法分析結果 (前20個Token):");
            for (int i = 0; i < Math.min(20, tokens.size()); i++) {
                System.out.println(i + ": " + tokens.get(i));
            }
            if (tokens.size() > 20) {
                System.out.println("... 還有 " + (tokens.size() - 20) + " 個Token");
            }
            
            // 語法分析
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();
            
            System.out.println("\n語法分析結果:");
            System.out.println(ast.toString());
            
            // 程式碼生成
            EVMCodeGenerator codeGen = new EVMCodeGenerator();
            List<Instruction> instructions = codeGen.generate(ast);
            
            System.out.println("\n生成的 EVM 指令:");
            for (int i = 0; i < instructions.size(); i++) {
                System.out.printf("%3d: %s\n", i, instructions.get(i));
            }
            
            // 分析生成的指令
            analyzeInstructions(instructions);
            
        } catch (Exception e) {
            System.err.println("編譯錯誤: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\n" + "=".repeat(50) + "\n");
    }
    
    private static void testLexer() {
        System.out.println("測試 2: 詞法分析器測試");
        System.out.println("=" .repeat(50));
        
        String[] testCases = {
            "uint256 i = 1;",
            "for (uint256 i = 1; i <= 10; i++)",
            "emit TestEvent(i);",
            "function testLoop() public {}"
        };
        
        for (String testCase : testCases) {
            System.out.println("輸入: " + testCase);
            try {
                Lexer lexer = new Lexer(testCase);
                List<Token> tokens = lexer.tokenize();
                System.out.print("輸出: ");
                for (Token token : tokens) {
                    if (token.getType() != TokenType.EOF) {
                        System.out.print(token.getType() + "(" + token.getValue() + ") ");
                    }
                }
                System.out.println();
            } catch (Exception e) {
                System.err.println("錯誤: " + e.getMessage());
            }
            System.out.println();
        }
        
        System.out.println("=".repeat(50) + "\n");
    }
    
    private static void testParser() {
        System.out.println("測試 3: 語法分析器測試");
        System.out.println("=" .repeat(50));
        
        String simpleContract = """
            contract Simple {
                event Log(uint256 indexed num);
                function test() public {
                    uint256 x = 5;
                    emit Log(x);
                }
            }
            """;
        
        System.out.println("簡單合約:");
        System.out.println(simpleContract);
        
        try {
            Lexer lexer = new Lexer(simpleContract);
            List<Token> tokens = lexer.tokenize();
            
            Parser parser = new Parser(tokens);
            ASTNode ast = parser.parse();
            
            System.out.println("\nAST 結構:");
            printASTStructure(ast, 0);
            
        } catch (Exception e) {
            System.err.println("語法分析錯誤: " + e.getMessage());
        }
        
        System.out.println("=".repeat(50) + "\n");
    }
    
    private static void printASTStructure(ASTNode node, int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + node.getClass().getSimpleName() + ": " + node.toString());
        
        // 這裡可以根據需要添加更詳細的 AST 遍歷邏輯
        // 由於時間關係，簡化處理
    }
    
    private static void analyzeInstructions(List<Instruction> instructions) {
        System.out.println("\n指令分析:");
        System.out.println("-".repeat(30));
        
        int pushCount = 0;
        int jumpCount = 0;
        int logCount = 0;
        int arithCount = 0;
        
        for (Instruction instr : instructions) {
            String mnemonic = instr.getMnemonic();
            if (mnemonic.startsWith("PUSH")) {
                pushCount++;
            } else if (mnemonic.contains("JUMP")) {
                jumpCount++;
            } else if (mnemonic.startsWith("LOG")) {
                logCount++;
            } else if (mnemonic.equals("ADD") || mnemonic.equals("SUB") || 
                      mnemonic.equals("MUL") || mnemonic.equals("DIV")) {
                arithCount++;
            }
        }
        
        System.out.println("總指令數: " + instructions.size());
        System.out.println("PUSH 指令: " + pushCount);
        System.out.println("JUMP 指令: " + jumpCount);
        System.out.println("LOG 指令: " + logCount);
        System.out.println("算術指令: " + arithCount);
        
        System.out.println("\n關鍵指令序列:");
        boolean inLoop = false;
        for (int i = 0; i < instructions.size(); i++) {
            Instruction instr = instructions.get(i);
            if (instr.getMnemonic().equals("JUMPDEST")) {
                System.out.println(i + ": " + instr + " <- 迴圈開始");
                inLoop = true;
            } else if (instr.getMnemonic().startsWith("LOG")) {
                System.out.println(i + ": " + instr + " <- 發送事件");
            } else if (instr.getMnemonic().equals("JUMP") && inLoop) {
                System.out.println(i + ": " + instr + " <- 跳回迴圈");
                inLoop = false;
            }
        }
    }
}
```
