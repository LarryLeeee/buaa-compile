package frontend.Parser.Exp;

public class PrimaryExp {
    public Exp exp;
    public LVal lVal;
    public Number number;
    public String type;

    public PrimaryExp() {
        type = null;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addExp(Exp exp) {
        this.exp = exp;
    }

    public void addLVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void addNumber(Number number) {
        this.number = number;
    }

}
