package ir.Value;

import java.util.ArrayList;

public class Module {
    public ArrayList<Function> functions;
    public ArrayList<GlobalVariable> globalVariables;
    public ArrayList<Str> outputs;

    public Module() {
        functions = new ArrayList<>();
        globalVariables = new ArrayList<>();
        outputs = new ArrayList<>();
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public void addGlobalVariable(GlobalVariable globalVariable) {
        globalVariables.add(globalVariable);
    }

    public void addOutput(Str str) {
        outputs.add(str);
    }

    public void getOutputs(ArrayList<String> outputs) {
        outputs.add("declare i32 @getint()");
        outputs.add("declare void @putint(i32)");
        outputs.add("declare void @putch(i32)");
        outputs.add("declare void @putstr(i8*)");
        outputs.add("");
        if (!globalVariables.isEmpty()){
            for (GlobalVariable globalVariable : globalVariables) {
                globalVariable.getOutputs(outputs);
            }
            outputs.add("");
        }
        for (Str str : this.outputs) {
            str.getOutputs(outputs);
        }
        for (Function function : functions) {
            outputs.add("");
            function.getOutputs(outputs);
        }
    }

}
