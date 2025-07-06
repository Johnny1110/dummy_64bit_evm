package com.frizo.lab.sevm.compiler.parser.abs.expressions;

import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

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
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("Literal{value=%s, type='%s'}", value, type);
    }
}
