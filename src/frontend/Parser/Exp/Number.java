package frontend.Parser.Exp;

import frontend.Lexer.Num;

public class Number {
    public Num num;

    public Number() {
        num = null;
    }

    public void addIntConst(Num num) {
        this.num = num;
    }

}
