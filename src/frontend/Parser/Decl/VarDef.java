package frontend.Parser.Decl;

import errorHandler.Ident;
import frontend.Lexer.Id;

import java.util.ArrayList;

public class VarDef implements Ident {
    public Id id;
    public ArrayList<ConstExp> constExps;
    public InitVal initVal;
    public String type = "Ident";
    public int dim2 = 0;
    public boolean isGetint = false;

    public VarDef() {
        id = null;
        constExps = new ArrayList<>();
        initVal = null;
    }

    public void addId(Id id) {
        this.id = id;
    }

    public void addConstExp(ConstExp constExp) {
        constExps.add(constExp);
        type = constExps.size() == 1 ? "Arr" : "twoDimArr";
    }

    public void addInitVal(InitVal initVal) {
        this.initVal = initVal;
    }
}
