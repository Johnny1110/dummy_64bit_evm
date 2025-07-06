package com.frizo.lab.sevm.compiler.parser.abs.declarations;

import com.frizo.lab.sevm.compiler.parser.abs.AbsNode;
import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

import java.util.List;

public class EventDeclaration extends AbsNode {
    private final String name;
    private final List<VariableDeclaration> parameters;

    public EventDeclaration(String name, List<VariableDeclaration> parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public String getName() { return name; }
    public List<VariableDeclaration> getParameters() { return parameters; }

    @Override
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("EventDeclaration{name='%s', params=%d}", name, parameters.size());
    }
}
