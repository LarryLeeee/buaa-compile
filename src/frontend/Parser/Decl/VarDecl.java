package frontend.Parser.Decl;

import java.util.ArrayList;

public class VarDecl {

    private ArrayList<VarDef> varDefs;

    public VarDecl() {
        varDefs = new ArrayList<>();
    }

    public void addVarDef(VarDef varDef) {
        varDefs.add(varDef);
    }

    public ArrayList<VarDef> getVarDefs() {
        return varDefs;
    }
}
