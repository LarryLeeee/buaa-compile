package frontend.Parser.Decl;

import java.util.ArrayList;

public class ConstDecl {
    private ArrayList<ConstDef> constDefs;

    public ConstDecl() {
        constDefs = new ArrayList<>();
    }

    public void addConstDef(ConstDef constDef) {
        constDefs.add(constDef);
    }

    public ArrayList<ConstDef> getConstDefs() {
        return constDefs;
    }
}
