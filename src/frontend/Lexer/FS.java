package frontend.Lexer;

public class FS implements Token{
    private int line;
    public String content;

    public FS(String content,int line){
        this.line=line;
        this.content=content;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String getType() {
        return "FormatString";
    }

    @Override
    public String getName() {
        return content;
    }

}
