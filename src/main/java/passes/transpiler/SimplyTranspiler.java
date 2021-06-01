package passes.transpiler;

import ast.*;
import ast.util.AssignmentOperatorMapper;
import ast.util.DataTypeMapper;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import universalJavaPortal.JavaLibrary;
import universalJavaPortal.JavaPortalServiceProvider;
import universalJavaPortal.SimplyFunctionModel;
import visitors.BaseAstVisitor;

import java.util.ArrayList;
import java.util.List;

public class SimplyTranspiler extends BaseAstVisitor<String> {

    public final StringBuilder code = new StringBuilder();
    final STGroup group = new STGroupFile("src/main/resources/templates/javaTemplate.stg");

    @Override
    public String visit(ArgNode node) {
        ST st = group.getInstanceOf("parameter");
        var isList = node.isList();
        var dataType = node.getDataType();
        var paraName = node.getName();
        st.add("isList", isList);
        st.add("type", DataTypeMapper.getJavaType(dataType));
        st.add("identifier", paraName);
        return st.render();
    }

    @Override
    public String visit(ArithmeticExpressionNode.AdditionExpressionNode node) {
        ST st = group.getInstanceOf("arithmeticStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "+");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(ArithmeticExpressionNode.DivisionExpressionNode node) {
        ST st = group.getInstanceOf("arithmeticStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "/");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(ArithmeticExpressionNode.ModulusExpressionNode node) {
        ST st = group.getInstanceOf("arithmeticStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "%");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(ArithmeticExpressionNode.MultiplicationExpressionNode node) {
        ST st = group.getInstanceOf("arithmeticStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "*");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(ArrayAccessExpressionNode node) {
        var arrName = node.getArrayName();
        var index = visit(node.getAccessValueExpression());

        ST st = group.getInstanceOf("arrayAccess");
        st.add("identifier", arrName);
        st.add("index", index);
        return st.render();
    }

    @Override
    public String visit(ArithmeticExpressionNode.SubtractionExpressionNode node) {
        ST st = group.getInstanceOf("arithmeticStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "-");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(ArrayInitializationNode node) {
        StringBuilder arrayElements = new StringBuilder();
        if(node instanceof EmptyArrayInitializationNode) {
            return null;
        } else {
            var newNode = (NonEmptyArrayInitializationNode) node;
            for(ExpressionNode elementNode : newNode.getArrayValues()) {
                var value = visit(elementNode);
                arrayElements.append(value).append(",");
            }
            // Removing the additional ','
            return arrayElements.deleteCharAt(arrayElements.length() - 1).toString();
        }
    }

    @Override
    public String visit(ArrayVariableDeclarationNode node) {

        var arrValues = visit(node.getValue());
        var dataType = DataTypeMapper.getJavaWrapper(node.getDataType());
        var arrName = node.getName();
        var isEmptyArray = true;
        if(arrValues != null) {
            isEmptyArray = false;
        }

        ST st = group.getInstanceOf("arrayDec");
        st.add("type", dataType);
        st.add("identifier", arrName);
        st.add("isEmptyInit", isEmptyArray);
        st.add("values", arrValues);
        return st.render();
    }


    @Override
    public String visit(AssignmentStatementNode.ArrayVariableAssignmentStatementNode node) {
        var arrName = node.getName();
        var value = visit(node.getValue());

        ST st = group.getInstanceOf("arrAssign");
        st.add("identifier", arrName);
        st.add("val", value);
        return st.render();
    }


    @Override
    public String visit(AssignmentStatementNode.PrimitiveVariableAssignmentStatementNode node) {
        ST st = group.getInstanceOf("varAssign");
        st.add("identifier", node.getName());
        st.add("assignOperator", AssignmentOperatorMapper.getAssignmentOperator(node.getAssignmentOperator()));
        var assignedValue = visit(node.getValue());
        st.add("val", assignedValue);
        return st.render();
    }

    @Override
    public String visit(BlockNode node) {
        StringBuilder funcBody = new StringBuilder();
        for(StatementNode stmtNode : node.getStatementNodeList()) {

            if(stmtNode instanceof PrimitiveVariableDeclarationNode) {
                var primNode = (PrimitiveVariableDeclarationNode) stmtNode;
                funcBody.append(visit(primNode));
            } else if(stmtNode instanceof ArrayVariableDeclarationNode) {
                var arrayNode = (ArrayVariableDeclarationNode) stmtNode;
                funcBody.append(visit(arrayNode));
            } else if(stmtNode instanceof AssignmentStatementNode.PrimitiveVariableAssignmentStatementNode) {
                var primAssignNode = (AssignmentStatementNode.PrimitiveVariableAssignmentStatementNode) stmtNode;
                funcBody.append(visit(primAssignNode));
            } else if(stmtNode instanceof AssignmentStatementNode.ArrayVariableAssignmentStatementNode) {
                var arrAssignNode = (AssignmentStatementNode.ArrayVariableAssignmentStatementNode) stmtNode;
                funcBody.append(visit(arrAssignNode));
            } else if(stmtNode instanceof IfStatementNode) {
                var ifStmt = (IfStatementNode) stmtNode;
                funcBody.append(visit(ifStmt));
            } else if(stmtNode instanceof IterateStatementNode) {
                var iterateStmt = (IterateStatementNode) stmtNode;
                funcBody.append(visit(iterateStmt));
            } else if(stmtNode instanceof FunctionCallStatementNode) {
                var funcCallNode = (FunctionCallStatementNode) stmtNode;
                funcBody.append(visit(funcCallNode));
            } else {
                var returnNode = (ReturnStatementNode) stmtNode;
                funcBody.append(visit(returnNode));
            }

        }
        return funcBody.toString();
    }

    @Override
    public String visit(CompilationUnitNode node) {
        ST st = group.getInstanceOf("program");
        var libImportsSection = visit(node.getLibImportNodeList());
        st.add("libImportSection", libImportsSection);
        var globalVarsSection = visit(node.getGlobalVariableDeclarationNodeList());
        st.add("globalSection", globalVarsSection);
        var functionSection = visit(node.getFunctionDeclarationNodeList());
        st.add("functionSection", functionSection);

//        code.append(libImportsSection);
//        code.append(globalVarsSection);

        var program = st.render();
        code.append(program);
        return code.toString();
    }

    @Override
    public String visit(EmptyArrayInitializationNode node) {
        return null;
    }

    @Override
    public String visit(FunctionCallExpressionNode node) {
        StringBuilder parameters = new StringBuilder();
        var libRef = node.getLibRef();
        var funcName = node.getFuncName();

        if(libRef == null) {
            ST st = group.getInstanceOf("funcCall");
            st.add("funcName", funcName);
            for(ExpressionNode expNode : node.getParameterList()) {
                parameters.append(visit(expNode)).append(",");
            }
            if(parameters.length() != 0) { parameters.deleteCharAt(parameters.length() - 1); }
            st.add("parameters", parameters);
            return st.render();
        } else if(libRef.equals("io")) {
            ST st = group.getInstanceOf("print");
            for(ExpressionNode expNode : node.getParameterList()) {
                parameters.append(visit(expNode)).append("+");
            }
            parameters.deleteCharAt(parameters.length() - 1).toString();         // Removing the additional '+'
            st.add("content", parameters);
            return st.render();
        } else {
            for(ExpressionNode expNode : node.getParameterList()) {
                parameters.append(visit(expNode)).append(",");
            }
            if(parameters.length() != 0) { parameters.deleteCharAt(parameters.length() - 1); }

            ST st = group.getInstanceOf("libFuncCall");
            st.add("libRef", libRef);
            st.add("funcName", funcName);
            st.add("parameters", parameters);
            return st.render();
        }
    }

    @Override
    public String visit(FunctionCallStatementNode node) {
        return visit(node.getFunctionCallExpressionNode());
    }

    @Override
    public String visit(FunctionDeclarationNode node) {
        ST st = group.getInstanceOf("funcDec");

        var funcName = node.getFunctionSignatureNode().getFunctionName();
        if(funcName.equals("main")) {
            st.add("isMain", true);
        } else {
            st.add("isMain", false);
        }
        st.add("name", funcName);
        var parameters = visit(node.getFunctionSignatureNode());
        st.add("parameterList", parameters);
        var returnType = node.getReturnType();
        st.add("returnType", DataTypeMapper.getJavaType(returnType));
        var funcBody = visit(node.getFunctionBody());
        st.add("body", funcBody);
        return st.render();
    }

    @Override
    public String visit(FunctionDeclarationNode.FunctionSignatureNode node) {
        StringBuilder parameters = new StringBuilder();
        for(ArgNode argNode : node.getFunctionArgumentNodeList()) {
            parameters.append(visit(argNode)).append(',');
        }
        // Removing the additional ','
        if(parameters.length() != 0) { parameters.deleteCharAt(parameters.length() - 1); }
        return parameters.toString();
    }

    @Override
    public String visit(FunctionDeclarationNodeList node) {
        StringBuilder line = new StringBuilder();

        for(FunctionDeclarationNode functionDeclarationNode : node.getFunctionDeclarationNodes()) {
            line.append(visit(functionDeclarationNode));
        }
        return line.toString();
    }

    @Override
    public String visit(GlobalVariableDeclarationNodeList node) {
        StringBuilder line = new StringBuilder();

        for(VariableDeclarationNode variableDeclarationNode : node.getVariableDeclarationNodes()){
            if(variableDeclarationNode instanceof PrimitiveVariableDeclarationNode){
                var primNode = (PrimitiveVariableDeclarationNode) variableDeclarationNode;
                line.append("static ").append(visit(primNode));
            }else if(variableDeclarationNode instanceof ArrayVariableDeclarationNode){
                var arrayNode = (ArrayVariableDeclarationNode) variableDeclarationNode;
                line.append("static ").append(visit(arrayNode));
            }
        }

        return line.toString();
    }

    @Override
    public String visit(IdentifierNode node) {
        return node.getName();
    }

    @Override
    public String visit(IfStatementNode node) {
        StringBuilder elseifStmt = new StringBuilder();
        ST st = group.getInstanceOf("ifStmt");

        var ifNode = visit(node.getIfBlockNode());
        st.add("ifBlock", ifNode);
        for(IfStatementNode.IfBlockNode ifBlockNode : node.getElseIfBlockNodeList()) {
            var elseIfNodeBlock = visit(ifBlockNode);
            elseifStmt.append("else ").append(elseIfNodeBlock);
        }
        st.add("elseIfBlock", elseifStmt.toString());
        var elseNode = visit(node.getElseBlockNode());
        st.add("elseBlock", elseNode);
        return st.render();
        //return ifStmt.toString();
        //return null;
    }

    @Override
    public String visit(IfStatementNode.ElseBlockNode node) {
        ST st = group.getInstanceOf("elseBlock");
        var elseBlock = visit(node.getBlockNode());
        st.add("body", elseBlock);
        return st.render();
    }

    @Override
    public String visit(IfStatementNode.IfBlockNode node) {
        ST st = group.getInstanceOf("ifBlock");
        var condition = visit(node.getConditionExpressionNode());
        var ifBody = visit(node.getBlockNode());
        st.add("condition", condition);
        st.add("body", ifBody);
        return st.render();
    }

    @Override
    public String visit(IterateStatementNode node) {
        ST st = group.getInstanceOf("loopStmt");

        var loopHeader = visit(node.getIterateConditionExpressionNode());
        var body = visit(node.getBlockNode());

        st.add("loop", loopHeader);
        st.add("body", body);
        return st.render();
    }

    public String visit(IterateStatementNode.IterateConditionExpressionNode node){
        // visitors with implementations of IterateConditionExpressionNode
        if(node instanceof IterateStatementNode.IterateConditionExpressionNode.BooleanIterateExpressionNode) {
            var whileLoop = (IterateStatementNode.IterateConditionExpressionNode.BooleanIterateExpressionNode) node;
            return visit(whileLoop);
        } else if (node instanceof IterateStatementNode.IterateConditionExpressionNode.NewRangeExpressionNode) {
            var newRangeForLoop = (IterateStatementNode.IterateConditionExpressionNode.NewRangeExpressionNode) node;
            return visit(newRangeForLoop);
        } else if (node instanceof IterateStatementNode.IterateConditionExpressionNode.ArrayIterateExpressionNode) {
            var forEachLoop = (IterateStatementNode.IterateConditionExpressionNode.ArrayIterateExpressionNode) node;
            return visit(forEachLoop);
        } else if (node instanceof IterateStatementNode.IterateConditionExpressionNode.RangeIterateExpressionNode) {
            var rangeForLoop = (IterateStatementNode.IterateConditionExpressionNode.RangeIterateExpressionNode) node;
            return visit(rangeForLoop);
        } else {
            return null;
        }
    }

    @Override
    public String visit(IterateStatementNode.IterateConditionExpressionNode.ArrayIterateExpressionNode node) {
        var dataType = node.getArgNode().getDataType();
        var iteratorName = node.getArgNode().getName();
        var arrName = visit(node.getExpressionNode());

        ST st = group.getInstanceOf("forEachLoop");
        st.add("dataType", DataTypeMapper.getJavaWrapper(dataType));
        st.add("iterator", iteratorName);
        st.add("arrName", arrName);
        return st.render();
    }

    @Override
    public String visit(IterateStatementNode.IterateConditionExpressionNode.BooleanIterateExpressionNode node) {
        ST st = group.getInstanceOf("whileLoop");
        var condition = visit(node.getExpressionNode());
        st.add("condition", condition);
        return st.render();
    }

    @Override
    public String visit(IterateStatementNode.IterateConditionExpressionNode.RangeIterateExpressionNode node) {
        ST st = group.getInstanceOf("modifiedForLoop");

        var controlVarDeclaration = visit(node.getArgNode());
        var controlVarName = node.getArgNode().getName();
        var startVal = visit(node.getFromValue());
        var endVal = visit(node.getToValue());

        st.add("init", controlVarDeclaration);
        st.add("controlVar", controlVarName);
        st.add("start", startVal);
        st.add("end", endVal);
        if(Integer.parseInt(endVal) > Integer.parseInt(startVal)) {
            st.add("isPositive", true);
            st.add("stepVal", "1");
            st.add("isReverse", false);
        } else {
            st.add("isPositive", false);
            st.add("stepVal", "1");
            st.add("isReverse", true);
        }
        return st.render();
    }

    @Override
    public String visit(LibImportNode node) {
        StringBuilder library = new StringBuilder();
        library.append(node.getLibName());
        if(library.toString().equals("io")) {
            return "java.io.*";
        } else {
            return library.append(".h").toString();
        }

    }

    @Override
    public String visit(LibImportNodeList node) {
        ST st = group.getInstanceOf("libImport");
        List<String> libList = new ArrayList<>();

        for(LibImportNode libImportNode : node.getLibImportNodes()){
            libList.add(visit(libImportNode));
        }
        st.add("libList", libList);
        return st.render();
    }

    public String visit(LiteralExpressionNode node){
        // visitors with implementations of LiteralExpressionNode
        if(node instanceof LiteralExpressionNode.BoolLiteralExpressionNode) {
            var newNode = (LiteralExpressionNode.BoolLiteralExpressionNode) node;
            return visit(newNode);
        }else if(node instanceof LiteralExpressionNode.CharLiteralExpressionNode) {
            var newNode = (LiteralExpressionNode.CharLiteralExpressionNode) node;
            return visit(newNode);
        }else if(node instanceof LiteralExpressionNode.FloatLiteralExpressionNode) {
            var newNode = (LiteralExpressionNode.FloatLiteralExpressionNode) node;
            return visit(newNode);
        }else if(node instanceof LiteralExpressionNode.IntegerLiteralExpressionNode) {
            var newNode = (LiteralExpressionNode.IntegerLiteralExpressionNode) node;
            return visit(newNode);
        }else if(node instanceof LiteralExpressionNode.StringLiteralExpressionNode) {
            var newNode = (LiteralExpressionNode.StringLiteralExpressionNode) node;
            return visit(newNode);
        }
        return null;
    }

    @Override
    public String visit(LiteralExpressionNode.BoolLiteralExpressionNode node) {
        return Boolean.toString(node.isValue());
    }

    @Override
    public String visit(LiteralExpressionNode.CharLiteralExpressionNode node) {
        return Character.toString(node.getValue());
    }

    @Override
    public String visit(LiteralExpressionNode.FloatLiteralExpressionNode node) {
        return Float.toString(node.getValue());
    }

    @Override
    public String visit(LiteralExpressionNode.IntegerLiteralExpressionNode node) {
        return Integer.toString(node.getValue());
    }

    @Override
    public String visit(LiteralExpressionNode.StringLiteralExpressionNode node) {
        return node.getValue();
    }

    public String visit(LogicExpressionNode node){
        if(node instanceof LogicExpressionNode.AndExpressionNode) {
            var andNode = (LogicExpressionNode.AndExpressionNode) node;
            return visit(andNode);
        } else if(node instanceof LogicExpressionNode.OrExpressionNode) {
            var orNode = (LogicExpressionNode.OrExpressionNode) node;
            return visit(orNode);
        } else if(node instanceof LogicExpressionNode.EqualsExpressionNode) {
            var eqNode = (LogicExpressionNode.EqualsExpressionNode) node;
            return visit(eqNode);
        } else if(node instanceof LogicExpressionNode.NotEqualsExpressionNode) {
            var neqNode = (LogicExpressionNode.NotEqualsExpressionNode) node;
            return visit(neqNode);
        } else if(node instanceof LogicExpressionNode.GreaterThanExpressionNode) {
            var gtNode = (LogicExpressionNode.GreaterThanExpressionNode) node;
            return visit(gtNode);
        } else if(node instanceof LogicExpressionNode.GreaterOrEqualThanExpressionNode) {
            var gteqNode = (LogicExpressionNode.GreaterOrEqualThanExpressionNode) node;
            return visit(gteqNode);
        } else if(node instanceof LogicExpressionNode.LessThanExpressionNode) {
            var ltNode = (LogicExpressionNode.LessThanExpressionNode) node;
            return visit(ltNode);
        } else if(node instanceof LogicExpressionNode.LessOrEqualThanExpressionNode) {
            var lteqNode = (LogicExpressionNode.LessOrEqualThanExpressionNode) node;
            return visit(lteqNode);
        }
        return null;
    }

    @Override
    public String visit(LogicExpressionNode.AndExpressionNode node) {
        ST st = group.getInstanceOf("conditionStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "&&");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(LogicExpressionNode.EqualsExpressionNode node) {
        ST st = group.getInstanceOf("conditionStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "==");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(LogicExpressionNode.GreaterOrEqualThanExpressionNode node) {
        ST st = group.getInstanceOf("conditionStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", ">=");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(LogicExpressionNode.GreaterThanExpressionNode node) {
        ST st = group.getInstanceOf("conditionStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", ">");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(LogicExpressionNode.LessOrEqualThanExpressionNode node) {
        ST st = group.getInstanceOf("conditionStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "<=");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(LogicExpressionNode.LessThanExpressionNode node) {
        ST st = group.getInstanceOf("conditionStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "<");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(LogicExpressionNode.NotEqualsExpressionNode node) {
        ST st = group.getInstanceOf("conditionStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "!=");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(LogicExpressionNode.OrExpressionNode node) {
        ST st = group.getInstanceOf("conditionStmt");
        var lhsNode = visit(node.getLeft());
        var rhsNode = visit(node.getRight());
        st.add("lhs", lhsNode);
        st.add("operator", "||");
        st.add("rhs", rhsNode);
        return st.render();
    }

    @Override
    public String visit(LoopControlStatementNode node) {
        return null;
    }

    @Override
    public String visit(NonEmptyArrayInitializationNode node) {
        return null;
    }

    @Override
    public String visit(PrimitiveVariableDeclarationNode node) {
        ST st = group.getInstanceOf("varDec");

        var isConst = "";
        if(node.isConst()) { isConst = "final "; }
        st.add("constant", isConst);

        var dataType = node.getDataType();
        st.add("type", DataTypeMapper.getJavaType(dataType));

        var varName = node.getName();
        st.add("identifier", varName);

        var expNode = node.getValue();
        var initValue = visit(expNode);
        st.add("val", initValue);

        return st.render();
    }

    @Override
    public String visit(ReturnStatementNode node) {
        ST st = group.getInstanceOf("returnStmt");
        var returnStmt = visit(node.getValue());
        st.add("content", returnStmt);
        return st.render();
    }

    public String visit(UnaryExpressionNode node){
        // visitors with implementations of LiteralExpressionNode
        return null;
    }

    @Override
    public String visit(UnaryExpressionNode.ParenExpressionNode node) {
        return null;
    }

    @Override
    public String visit(UnaryExpressionNode.PrefixMinusExpressionNode node) {
        return null;
    }

    @Override
    public String visit(UnaryExpressionNode.PrefixNotExpressionNode node) {
        return null;
    }

    @Override
    public String visit(UnaryExpressionNode.PrefixPlusExpressionNode node) {
        return null;
    }

    @Override
    public String visit(IterateStatementNode.IterateConditionExpressionNode.NewRangeExpressionNode node) {
        ST st = group.getInstanceOf("modifiedForLoop");

        var controlVarDeclaration = visit(node.getArgNode());
        var controlVarName = node.getArgNode().getName();
        var startVal = visit(node.getFromValue());
        var endVal = visit(node.getToValue());
        var stepVal = visit(node.getNextValue());

        st.add("init", controlVarDeclaration);
        st.add("controlVar", controlVarName);
        st.add("start", startVal);
        st.add("end", endVal);
        if(Integer.parseInt(stepVal) > 0) {
            st.add("isPositive", true);
            st.add("stepVal", stepVal);
            st.add("isReverse", false);
        } else {
            st.add("isPositive", false);
            st.add("stepVal", java.lang.Math.abs(Integer.parseInt(stepVal)));
            st.add("isReverse", true);
        }
        return st.render();
    }

    public String visit(ExpressionNode node){
        if(node instanceof ArithmeticExpressionNode){
            if(node instanceof ArithmeticExpressionNode.AdditionExpressionNode) {
                var addNode = (ArithmeticExpressionNode.AdditionExpressionNode) node;
                return visit(addNode);
            } else if(node instanceof ArithmeticExpressionNode.SubtractionExpressionNode) {
                var subNode = (ArithmeticExpressionNode.SubtractionExpressionNode) node;
                return visit(subNode);
            } else if(node instanceof ArithmeticExpressionNode.MultiplicationExpressionNode) {
                var mulNode = (ArithmeticExpressionNode.MultiplicationExpressionNode) node;
                return visit(mulNode);
            } else if(node instanceof ArithmeticExpressionNode.DivisionExpressionNode) {
                var divNode = (ArithmeticExpressionNode.DivisionExpressionNode) node;
                return visit(divNode);
            } else if(node instanceof ArithmeticExpressionNode.ModulusExpressionNode) {
                var modNode = (ArithmeticExpressionNode.ModulusExpressionNode) node;
                return visit(modNode);
            }
        }else if(node instanceof ArrayAccessExpressionNode){
            var arrAccessNode = (ArrayAccessExpressionNode) node;
            return visit(arrAccessNode);
        }else if(node instanceof FunctionCallExpressionNode){
            var funcCall = (FunctionCallExpressionNode) node;
            return visit(funcCall);
        }else if(node instanceof IdentifierNode){
            var idNode = (IdentifierNode) node;
            return visit(idNode);
        }else if(node instanceof IterateStatementNode.IterateConditionExpressionNode){
            return null;
        }else if(node instanceof LiteralExpressionNode) {
            var litNode = (LiteralExpressionNode) node;
            return visit(litNode);
        }else if(node instanceof LogicExpressionNode){
            var logicNode = (LogicExpressionNode) node;
            return visit(logicNode);
        } else if(node instanceof UnaryExpressionNode){
            return null;
        }

        return null;

    }

    //////////////////////////////////////////////////
    //////////////// Symbol Table ////////////////////
    //////////////////////////////////////////////////


    @Override
    public String enterFunctionDeclaration(FunctionDeclarationNode node) {
        return null;
    }

    @Override
    public String enterBlockNode(BlockNode node) {
        return null;
    }

}
