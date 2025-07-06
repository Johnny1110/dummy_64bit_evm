package com.frizo.lab.sevm.compiler.parser.abs.declarations;

import com.frizo.lab.sevm.compiler.parser.abs.AbsNode;
import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

import java.util.List;

public class FunctionDeclaration extends AbsNode {

    private final String name;
    private final List<VariableDeclaration> parameters;
    private final List<String> visibility;
    private final BlockStatement body;

    public FunctionDeclaration(String name, List<VariableDeclaration> parameters,
                               List<String> visibility, BlockStatement body) {
        this.name = name;
        this.parameters = parameters;
        this.visibility = visibility;
        this.body = body;
    }

    public String getName() { return name; }
    public List<VariableDeclaration> getParameters() { return parameters; }
    public List<String> getVisibility() { return visibility; }
    public BlockStatement getBody() { return body; }

    @Override
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("FunctionDeclaration{name='%s', params=%d}", name, parameters.size());
    }
}
