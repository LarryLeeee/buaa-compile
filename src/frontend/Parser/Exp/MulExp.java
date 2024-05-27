package frontend.Parser.Exp;

import frontend.Lexer.Op;

import java.util.ArrayList;

public class MulExp {
    public ArrayList<Op> ops;
    public ArrayList<UnaryExp> unaryExps;

    public MulExp() {
        unaryExps = new ArrayList<>();
        ops = new ArrayList<>();
    }

    public void addOp(Op op) {
        ops.add(op);
    }

    public void addUnaryExp(UnaryExp unaryExp) {
        unaryExps.add(unaryExp);
    }

}
