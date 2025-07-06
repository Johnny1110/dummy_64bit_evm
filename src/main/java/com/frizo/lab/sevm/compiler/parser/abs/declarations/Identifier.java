package com.frizo.lab.sevm.compiler.parser.abs.declarations;

import com.frizo.lab.sevm.compiler.parser.abs.AbsNode;
import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

public class Identifier extends AbsNode {
    @Override
    public void accept(AbsVisitor visitor) {
        // Implementation for accepting a visitor
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "";
    }
}
