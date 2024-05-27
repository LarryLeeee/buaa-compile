package frontend.Parser.Exp;

public class Exp {
    public AddExp addExp;

    public Exp() {
        addExp = null;
    }

    public void addAddExp(AddExp addExp) {
        this.addExp = addExp;
    }

}
