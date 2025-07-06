package com.frizo.lab.sevm.compiler.parser.abs.declarations;

import com.frizo.lab.sevm.compiler.parser.abs.AbsNode;
import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

public class EmitStatement extends AbsNode {
    private final FunctionCall eventCall;

    public EmitStatement(FunctionCall eventCall) {
        this.eventCall = eventCall;
    }

    public FunctionCall getEventCall() { return eventCall; }

    @Override
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("EmitStatement{event=%s}", eventCall);
    }
}
