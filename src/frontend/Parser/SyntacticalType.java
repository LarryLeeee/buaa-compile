package frontend.Parser;

import frontend.Lexer.Op;
import frontend.Lexer.Reserve;
import frontend.Lexer.Token;

public class SyntacticalType {
    public SyntacticalType() {

    }

    public boolean isUnaryOp(Token token) {
        String name = token.getName();
        return token instanceof Op && (name.equals("+") || name.equals("-") || name.equals("!"));
    }

    public boolean isMulOp(Token token) {
        String name = token.getName();
        return token instanceof Op && (name.equals("*") || name.equals("/") || name.equals("%") || name.equals("bitand"));
    }

    public boolean isAddOp(Token token) {
        String name = token.getName();
        return token instanceof Op && (name.equals("+") || name.equals("-") || name.equals("%"));
    }

    public boolean isRelOp(Token token) {
        String name = token.getName();
        return token instanceof Op && (name.equals("<") || name.equals(">") || name.equals("<=") || name.equals(">="));
    }

    public boolean isEqOp(Token token) {
        String name = token.getName();
        return token instanceof Op && (name.equals("==") || name.equals("!="));
    }

    public boolean isLAndOp(Token token) {
        String name = token.getName();
        return token instanceof Op && (name.equals("&&"));
    }

    public boolean isLOrOp(Token token) {
        String name = token.getName();
        return token instanceof Op && (name.equals("||"));
    }

}
