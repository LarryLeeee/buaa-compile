package frontend.Parser.Exp;

import frontend.Lexer.Op;
import ir.Value.BasicBlock;

import java.util.ArrayList;

public class LAndExp {
    public ArrayList<Op> ops;
    public ArrayList<EqExp> eqExps;
    public BasicBlock trueBlock;
    public BasicBlock falseBlock;

    public LAndExp() {
        eqExps = new ArrayList<>();
        ops = new ArrayList<>();
    }

    public void addOp(Op op) {
        ops.add(op);
    }

    public void addEqExp(EqExp eqExp) {
        eqExps.add(eqExp);
    }

}
