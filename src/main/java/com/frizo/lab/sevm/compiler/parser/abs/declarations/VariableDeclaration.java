package com.frizo.lab.sevm.compiler.parser.abs.declarations;

import com.frizo.lab.sevm.compiler.parser.abs.AbsNode;
import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

public class VariableDeclaration extends AbsNode {
    private final String type;
    private final String name;
    private final boolean indexed;

    public VariableDeclaration(String type, String name, boolean indexed) {
        this.type = type;
        this.name = name;
        this.indexed = indexed;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isIndexed() {
        return indexed;
    }

    @Override
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("VariableDeclaration{type='%s', name='%s', indexed=%s}",
                type, name, indexed);
    }
}
