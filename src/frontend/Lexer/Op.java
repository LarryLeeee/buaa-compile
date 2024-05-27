package frontend.Lexer;
//Operation
public class Op implements Token{
    private int line;
    private String name;

    public Op(String name,int line){
        this.name=name;
        this.line=line;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String getType() {
        return name;
    }

    public String getName() {
        return name;
    }

}
