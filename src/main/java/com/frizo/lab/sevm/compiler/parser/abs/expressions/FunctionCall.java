package com.frizo.lab.sevm.compiler.parser.abs.expressions;

import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

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
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("FunctionCall{name='%s', args=%d}", name, arguments.size());
    }
}