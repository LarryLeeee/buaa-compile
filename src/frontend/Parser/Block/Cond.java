package frontend.Parser.Block;

import frontend.Parser.Exp.LOrExp;

public class Cond {
    public LOrExp lOrExp;

    public void Cond() {
        lOrExp = null;
    }

    public void addLOrExp(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
    }
}
