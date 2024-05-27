package frontend.Parser.Func;

import errorHandler.Ident;
import frontend.Lexer.Id;
import frontend.Parser.Block.Block;

public class FuncDef implements Ident {
    public FuncType funcType;
    public Id id;
    public FuncFParams funcFParams;
    public Block block;

    public FuncDef() {
        funcFParams = new FuncFParams();
    }

    public void addFuncType(FuncType funcType) {
        this.funcType = funcType;
    }

    public void addId(Id id) {
        this.id = id;
    }

    public void addFuncFParams(FuncFParams funcFParams) {
        this.funcFParams = funcFParams;
    }

    public void addBlock(Block block) {
        this.block = block;
    }
}
