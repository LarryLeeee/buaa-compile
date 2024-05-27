package frontend.Parser.Exp;

import frontend.Lexer.Op;

import java.util.ArrayList;

public class AddExp {
    public ArrayList<Op> ops;
    public ArrayList<MulExp> mulExps;

    public AddExp() {
        mulExps = new ArrayList<>();
        ops = new ArrayList<>();
    }

    public void addMulExp(MulExp mulExp) {
        mulExps.add(mulExp);
    }

    public void addOp(Op op) {
        ops.add(op);
    }
}
