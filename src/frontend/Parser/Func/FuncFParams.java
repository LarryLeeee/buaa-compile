package frontend.Parser.Func;

import java.util.ArrayList;

public class FuncFParams {
    public ArrayList<FuncFParam> funcFParams;

    public FuncFParams() {
        funcFParams = new ArrayList<>();
    }

    public void addFuncFParam(FuncFParam funcFParam) {
        funcFParams.add(funcFParam);
    }
}
