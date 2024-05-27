package frontend.Parser.Exp;

import frontend.Lexer.Op;

import java.util.ArrayList;

public class EqExp {
    public ArrayList<Op> ops;
    public ArrayList<RelExp> relExps;

    public EqExp() {
        relExps = new ArrayList<>();
        ops = new ArrayList<>();
    }

    public void addRelExp(RelExp relExp) {
        relExps.add(relExp);
    }

    public void addOp(Op op) {
        ops.add(op);
    }

}
