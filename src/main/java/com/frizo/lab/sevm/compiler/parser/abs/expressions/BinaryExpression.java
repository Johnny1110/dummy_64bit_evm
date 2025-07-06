package com.frizo.lab.sevm.compiler.parser.abs.expressions;

import com.frizo.lab.sevm.compiler.lexer.TokenType;
import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

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
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("BinaryExpression{%s %s %s}", left, operator, right);
    }
}
