package ast;

import visitors.BaseAstVisitor;

import java.util.List;
import java.util.Objects;

public abstract class ASTNode {

    public abstract List<ASTNode> getChildren();

    public abstract void accept(BaseAstVisitor visitor);
}