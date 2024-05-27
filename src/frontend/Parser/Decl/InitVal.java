package frontend.Parser.Decl;

import frontend.Parser.Exp.Exp;

import java.util.ArrayList;

public class InitVal {
    public String type;
    public Exp exp;
    public ArrayList<InitVal> initVals;

    public InitVal() {
        initVals = new ArrayList<>();
        type="Array";
    }

    public void addExp(Exp exp) {
        type = "Ident";
        this.exp = exp;
    }

    public void addInitval(InitVal initVal) {
        initVals.add(initVal);
    }
}
