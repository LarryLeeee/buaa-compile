package frontend.Parser.Func;

import errorHandler.Ident;
import frontend.Lexer.Id;
import frontend.Parser.Block.Block;

public class MainFuncDef implements Ident {
    private Block block;

    public MainFuncDef() {
        block = null;
    }

    public void addBlock(Block block) {
        this.block = block;
    }

    public FuncDef trans() {
        FuncDef funcDef = new FuncDef();
        funcDef.addId(new Id("main", 0));
        funcDef.addBlock(block);
        funcDef.addFuncType(new FuncType());
        return funcDef;
    }
}
