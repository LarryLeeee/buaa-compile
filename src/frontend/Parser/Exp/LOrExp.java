package frontend.Parser.Exp;

import frontend.Lexer.Op;
import ir.Value.BasicBlock;

import java.util.ArrayList;

public class LOrExp {
    public ArrayList<Op> ops;
    public ArrayList<LAndExp> lAndExps;
    public BasicBlock trueBlock;
    public BasicBlock falseBlock;

    public LOrExp() {
        lAndExps = new ArrayList<>();
        ops = new ArrayList<>();
    }

    public void addLAndExp(LAndExp lAndExp) {
        lAndExps.add(lAndExp);
    }

    public void addOp(Op op) {
        ops.add(op);
    }

}
