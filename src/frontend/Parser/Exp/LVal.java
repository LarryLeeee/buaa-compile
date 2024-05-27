package frontend.Parser.Exp;

import frontend.Lexer.Id;

import java.util.ArrayList;

public class LVal {
    public Id id;
    public ArrayList<Exp> exps;
    public String type = "Ident";

    public LVal() {
        exps = new ArrayList<>();
    }

    public void addId(Id id) {
        this.id = id;
    }

    public void addExp(Exp exp) {
        type = (type.equals("Ident")) ? "Array" : "TwoDims";
        exps.add(exp);
    }

}
