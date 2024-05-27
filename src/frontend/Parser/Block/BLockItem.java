package frontend.Parser.Block;

import frontend.Parser.Decl.ConstDecl;
import frontend.Parser.Decl.VarDecl;

public class BLockItem {

    public String type;
    public ConstDecl constDecl;
    public VarDecl varDecl;
    public Stmt stmt;

    public BLockItem() {
        type = null;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addConstDecl(ConstDecl constDecl) {
        this.constDecl = constDecl;
    }

    public void addVarDecl(VarDecl varDecl) {
        this.varDecl = varDecl;
    }

    public void addStmt(Stmt stmt) {
        this.stmt = stmt;
    }

}
