package backend;

import java.util.ArrayList;

public class MipsModule {

    public ArrayList<STR> strs;
    public ArrayList<Global> globals;
    public ArrayList<MipsFunc> mipsFuncs;

    public MipsModule() {
        strs = new ArrayList<>();
        globals = new ArrayList<>();
        mipsFuncs = new ArrayList<>();
    }

    public void getOutPuts(ArrayList<String> outputs) {
        outputs.add(".data");
        globals.forEach(global -> outputs.add(global.string()));
        strs.forEach(str -> outputs.add(str.string()));
        outputs.add(".text\n\tlui $3, 0x1000\n\tjal main\n\tli $v0,10\n\tsyscall");
        mipsFuncs.forEach(mipsFunc -> mipsFunc.getOutPuts(outputs));
    }

    public void addGlobal(Global global) {
        globals.add(global);
    }

    public void addStr(STR str) {
        strs.add(str);
    }

    public void addFunc(MipsFunc mipsFunc) {
        mipsFuncs.add(mipsFunc);
    }

}
