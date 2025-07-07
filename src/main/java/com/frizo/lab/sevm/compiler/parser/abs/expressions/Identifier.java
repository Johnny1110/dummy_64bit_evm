package com.frizo.lab.sevm.compiler.parser.abs.expressions;

import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

public class Identifier extends Expression {
    private final String name;

    public Identifier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("Identifier{name='%s'}", name);
    }
}
