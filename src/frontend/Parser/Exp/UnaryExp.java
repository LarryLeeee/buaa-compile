package frontend.Parser.Exp;

import frontend.Lexer.Id;
import frontend.Parser.Func.FuncRParams;

public class UnaryExp {
    public Id id;
    public PrimaryExp primaryExp;
    public FuncRParams funcRParams;
    public UnaryOp unaryOp;
    public UnaryExp unaryExp;
    public String type;

    public UnaryExp() {
        type = null;
        funcRParams = null;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addPrimaryExp(PrimaryExp primaryExp) {
        this.primaryExp = primaryExp;
    }

    public void addIdent(Id id) {
        this.id = id;
    }

    public void addFuncRParams(FuncRParams funcRParams) {
        this.funcRParams = funcRParams;
    }

    public void addUnaryOp(UnaryOp unaryOp) {
        this.unaryOp = unaryOp;
    }

    public void addUnaryExp(UnaryExp unaryExp) {
        this.unaryExp = unaryExp;
    }

}
