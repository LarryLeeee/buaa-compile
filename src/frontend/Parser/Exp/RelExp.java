package frontend.Parser.Exp;

import frontend.Lexer.Op;

import java.util.ArrayList;

public class RelExp {
    public ArrayList<Op> ops;
    public ArrayList<AddExp> addExps;

    public RelExp() {
        addExps = new ArrayList<>();
        ops = new ArrayList<>();
    }

    public void addOp(Op op) {
        ops.add(op);
    }

    public void addAddExp(AddExp addExp) {
        addExps.add(addExp);
    }

}
