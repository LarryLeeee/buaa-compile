package frontend.Parser.Decl;

import errorHandler.Ident;
import frontend.Lexer.Id;

import java.util.ArrayList;

public class ConstDef implements Ident {
    public Id id;
    public ArrayList<ConstExp> constExps;
    public ConstInitval constInitval;
    public String type = "Ident";
    public int dim2 = 0;

    public ConstDef() {
        constExps = new ArrayList<>();
        constInitval = null;
    }

    public void addId(Id id) {
        this.id = id;
    }

    public void addConstExp(ConstExp constExp) {
        constExps.add(constExp);
        type = constExps.size() == 1 ? "Arr" : "twoDimArr";
    }

    public void addConstInitval(ConstInitval constInitval) {
        this.constInitval = constInitval;
    }

    public Id getId() {
        return id;
    }

}
