package com.frizo.lab.sevm.compiler.parser.abs.declarations;

import com.frizo.lab.sevm.compiler.parser.abs.AbsNode;
import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

import java.util.List;

public class BlockStatement extends AbsNode {

    private final List<AbsNode> statements;

    public BlockStatement(List<AbsNode> statements) {
        this.statements = statements;
    }

    public List<AbsNode> getStatements() { return statements; }

    @Override
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("BlockStatement{statements=%d}", statements.size());
    }
}
