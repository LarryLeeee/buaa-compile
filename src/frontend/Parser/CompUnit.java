package frontend.Parser;

import frontend.Parser.Decl.ConstDecl;
import frontend.Parser.Decl.VarDecl;
import frontend.Parser.Func.FuncDef;
import frontend.Parser.Func.MainFuncDef;

import java.util.ArrayList;

public class CompUnit {
    private ArrayList<ConstDecl> constDecls;
    private ArrayList<VarDecl> varDecls;
    private ArrayList<FuncDef> funcDefs;
    private MainFuncDef mainFuncDef;

    public CompUnit() {
        constDecls = new ArrayList<>();
        varDecls = new ArrayList<>();
        funcDefs = new ArrayList<>();
    }

    public void addConstDecl(ConstDecl constDecl) {
        constDecls.add(constDecl);
    }

    public void addVarDecl(VarDecl varDecl) {
        varDecls.add(varDecl);
    }

    public void addFuncDef(FuncDef funcDef) {
        funcDefs.add(funcDef);
    }

    public void addMainFuncDef(MainFuncDef mainFuncDef) {
        this.mainFuncDef = mainFuncDef;
    }

    public ArrayList<ConstDecl> getConstDecls() {
        return constDecls;
    }

    public ArrayList<VarDecl> getVarDecls() {
        return varDecls;
    }

    public ArrayList<FuncDef> getFuncDefs() {
        return funcDefs;
    }

    public MainFuncDef getMainFuncDef() {
        return mainFuncDef;
    }
}
