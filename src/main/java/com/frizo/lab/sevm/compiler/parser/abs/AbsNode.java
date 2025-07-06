package com.frizo.lab.sevm.compiler.parser.abs;

// Abstract language node class
public abstract class AbsNode {

    public abstract void accept(AbsVisitor visitor);
    public abstract String toString();
}
