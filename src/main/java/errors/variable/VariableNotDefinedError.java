package errors.variable;

import errors.SimplyError;

public class VariableNotDefinedError implements SimplyError {

    final String varName;

    public VariableNotDefinedError(String varName) {
        this.varName = varName;
    }

    @Override
    public String getErrorMessage() {
        var message = new StringBuilder();
        message.append("Error:");
        message.append(this.getErrorType()).append(" ").append("\n");
        message.append("Variable ").append(this.varName).append(" not defined");

        return message.toString();
    }

    @Override
    public String getErrorType() {
        return "Variable:VariableNotDefinedError";
    }

    @Override
    public int getLineNumber() {
        return 0;
    }
}
