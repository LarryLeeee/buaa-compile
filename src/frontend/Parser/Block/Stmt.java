package frontend.Parser.Block;

import frontend.Lexer.FS;
import frontend.Parser.Exp.Exp;
import frontend.Parser.Exp.LVal;
import ir.Value.BasicBlock;

import java.util.ArrayList;

public class Stmt {

    public String type;
    public Cond ifCond;
    public Cond whileCond;
    public ArrayList<Stmt> stmts;
    public Exp returnExp;
    public ArrayList<Exp> printfExps;
    public Exp lValExp;
    public Exp exp;
    public FS fs;
    public Block block;
    public LVal lVal;
    public boolean isElse = false;
    public BasicBlock nextBlock = null;

    public Stmt() {
        stmts = new ArrayList<>();
        printfExps = new ArrayList<>();
        returnExp = null;
        lVal = null;
        lValExp = null;
        exp = null;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addLVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void addExp(Exp exp) {
        switch (type) {
            case "return":
                returnExp = exp;
                break;
            case "printf":
                printfExps.add(exp);
                break;
            case "LVal":
                lValExp = exp;
                break;
            case "Exp":
                this.exp = exp;
                break;
            default:
                break;
        }
    }

    public void addBlock(Block block) {
        this.block = block;
    }

    public void addStmt(Stmt stmt) {
        stmts.add(stmt);
    }

    public void addCond(Cond cond) {
        if (type.equals("if")) {
            ifCond = cond;
        } else {
            whileCond = cond;
        }
    }

    public void addFS(FS fs) {
        this.fs = fs;
    }

}
