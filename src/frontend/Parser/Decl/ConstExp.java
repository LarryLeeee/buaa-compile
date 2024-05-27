package frontend.Parser.Decl;

import frontend.Parser.Exp.AddExp;

public class ConstExp {
    public AddExp addExp;

    public ConstExp() {
        addExp = null;
    }

    public void addAddExp(AddExp addExp) {
        this.addExp = addExp;
    }

}
