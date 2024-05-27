package frontend.Parser.Func;

import errorHandler.Ident;
import frontend.Lexer.Id;
import frontend.Parser.Decl.ConstExp;

public class FuncFParam implements Ident {
    public Id id;
    public ConstExp constExp;
    public String type;
    public int dim2;

    public FuncFParam() {
        id = null;
        type = "int";
        constExp = null;
    }

    public void addId(Id id) {
        this.id = id;
    }

    public void addConstExp(ConstExp constExp) {
        this.constExp = constExp;
        type="twoDimArr";
    }
}
