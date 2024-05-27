package frontend.Lexer;

//IntConst
public class Num implements Token {
    private int value;
    private int line;

    public Num(String value, int line) {
        this.value = Integer.parseInt(value);
        this.line = line;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public String getType() {
        return "IntConst";
    }

    @Override
    public String getName() {
        return Integer.toString(value);
    }
}
