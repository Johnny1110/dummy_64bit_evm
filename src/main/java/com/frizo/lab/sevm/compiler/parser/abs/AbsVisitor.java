package com.frizo.lab.sevm.compiler.parser.abs;

import com.frizo.lab.sevm.compiler.parser.abs.declarations.*;
import com.frizo.lab.sevm.compiler.parser.abs.expressions.Expression;

public interface AbsVisitor {

    void visit(ContractDeclaration node);

    void visit(FunctionDeclaration node);

    void visit(EventDeclaration node);

    void visit(ForStatement node);

    void visit(EmitStatement node);

    void visit(VariableDeclaration node);

    void visit(Assignment node);

    void visit(BinaryExpression node);

    void visit(Identifier node);

    void visit(Literal node);

    void visit(FunctionCall node);

    void visit(BlockStatement node);

    void visit(Expression node);
}
