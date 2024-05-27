package frontend.Parser.Func;

import frontend.Parser.Exp.Exp;

import java.util.ArrayList;

public class FuncRParams {
    public ArrayList<Exp> exps;

    public FuncRParams() {
        exps = new ArrayList<>();
    }

    public void addExp(Exp exp) {
        exps.add(exp);
    }

}
