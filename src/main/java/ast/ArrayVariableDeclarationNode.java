package ast;

import ast.util.enums.DataType;
import visitors.BaseAstVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArrayVariableDeclarationNode extends VariableDeclarationNode {
    boolean isConst;
    final DataType dataType;
    final String varName;
    final ArrayInitializationNode initializationNode;

    public ArrayVariableDeclarationNode(
            boolean isConst,
            DataType dataType,
            String varName,
            ArrayInitializationNode initializationNode
    ) {
        this.isConst = isConst;
        this.dataType = dataType;
        this.varName = varName;
        this.initializationNode = initializationNode;
    }

    @Override
    public void setConst() {
        this.isConst = true;
    }

    @Override
    public String getName() {
        return this.varName;
    }

    public boolean isConst() {
        return isConst;
    }

    public DataType getDataType() {
        return dataType;
    }

    public ArrayInitializationNode getValue() { return this.initializationNode; }

    @Override
    public List<ASTNode> getChildren() {
        List<ASTNode> children = new ArrayList<ASTNode>();
        children.add(this.initializationNode);
        return children;
    }

    @Override
    public void accept(BaseAstVisitor visitor) {
        this.getChildren().stream().filter(Objects::nonNull).forEach(node -> node.accept(visitor));
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ArrayVariableDeclarationNode{" +
                "isConst=" + isConst +
                ", dataType=" + dataType +
                ", varName='" + varName + '\'' +
                ", initializationNode=" + initializationNode +
                '}';
    }
}
