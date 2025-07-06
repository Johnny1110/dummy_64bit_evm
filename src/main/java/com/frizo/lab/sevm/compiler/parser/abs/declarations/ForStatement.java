package com.frizo.lab.sevm.compiler.parser.abs.declarations;

import com.frizo.lab.sevm.compiler.parser.abs.AbsNode;
import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

import com.frizo.lab.sevm.compiler.parser.abs.expressions.Expression;

public class ForStatement extends AbsNode {

    private final AbsNode init;
    private final Expression condition;
    private final Expression update;
    private final AbsNode body;

    public ForStatement(AbsNode init, Expression condition, Expression update, AbsNode body) {
        this.init = init;
        this.condition = condition;
        this.update = update;
        this.body = body;
    }

    public AbsNode getInit() { return init; }
    public Expression getCondition() { return condition; }
    public Expression getUpdate() { return update; }
    public AbsNode getBody() { return body; }

    @Override
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("ForStatement{init=%s, condition=%s, update=%s}",
                init, condition, update);
    }
}
