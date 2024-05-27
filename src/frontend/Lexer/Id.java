package frontend.Lexer;

//Ident
public class Id implements Token {

    private int line;
    private String name;

    public Id(String name, int line) {
        this.name = name;
        this.line = line;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String getType() {
        return "Ident";
    }

    public String getName() {
        return name;
    }

}