package com.frizo.lab.sevm.compiler.parser.abs.declarations;

import com.frizo.lab.sevm.compiler.parser.abs.AbsNode;
import com.frizo.lab.sevm.compiler.parser.abs.AbsVisitor;

import java.util.List;

public class ContractDeclaration extends AbsNode {

    private final String name;
    private final List<AbsNode> members;

    public ContractDeclaration(String name, List<AbsNode> members) {
        this.name = name;
        this.members = members;
    }

    public String getName() { return name; }
    public List<AbsNode> getMembers() { return members; }

    @Override
    public void accept(AbsVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return String.format("ContractDeclaration{name='%s', members=%d}", name, members.size());
    }
}
