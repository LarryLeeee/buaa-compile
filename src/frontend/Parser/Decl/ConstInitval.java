package frontend.Parser.Decl;

import java.util.ArrayList;

public class ConstInitval {
    public String type;
    public ConstExp constExp;
    public ArrayList<ConstInitval> constInitvals;

    public ConstInitval() {
        constInitvals = new ArrayList<>();
        type = "Array";//考虑到有A[]={}的情况，默认为Array
    }

    public void addConstExp(ConstExp constExp) {
        type = "Ident";
        this.constExp = constExp;
    }

    public void addConstInitval(ConstInitval constInitval) {
        constInitvals.add(constInitval);
    }

}
