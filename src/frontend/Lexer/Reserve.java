package frontend.Lexer;

//ReservedWord
public class Reserve implements Token {

    private int line;
    private String name;

    public Reserve(String name, int line) {
        this.name = name;
        this.line = line;
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
