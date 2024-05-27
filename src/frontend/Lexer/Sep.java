package frontend.Lexer;

//Separator
public class Sep implements Token {
    private int line;
    private char c;

    public Sep(char c, int line) {
        this.c = c;
        this.line = line;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String getType() {
        return Character.toString(c);
    }

    @Override
    public String getName() {
        return Character.toString(c);
    }

    public char getC() {
        return c;
    }

}
