package frontend.Lexer;

import java.util.HashMap;
import java.util.HashSet;

public class LexicalType {
    private HashMap<String, String> typeCode = new HashMap<>();
    private HashSet<String> reserve = new HashSet<>();

    public LexicalType() {
        typeCode.put("Ident", "IDENFR");
        typeCode.put("IntConst", "INTCON");
        typeCode.put("FormatString", "STRCON");
        typeCode.put("main", "MAINTK");
        typeCode.put("const", "CONSTTK");
        typeCode.put("int", "INTTK");
        typeCode.put("break", "BREAKTK");
        typeCode.put("continue", "CONTINUETK");
        typeCode.put("if", "IFTK");
        typeCode.put("else", "ELSETK");
        typeCode.put("!", "NOT");
        typeCode.put("&&", "AND");
        typeCode.put("||", "OR");
        typeCode.put("while", "WHILETK");
        typeCode.put("getint", "GETINTTK");
        typeCode.put("printf", "PRINTFTK");
        typeCode.put("return", "RETURNTK");
        typeCode.put("+", "PLUS");
        typeCode.put("-", "MINU");
        typeCode.put("void", "VOIDTK");
        typeCode.put("*", "MULT");
        typeCode.put("/", "DIV");
        typeCode.put("%", "MOD");
        typeCode.put("<", "LSS");
        typeCode.put("<=", "LEQ");
        typeCode.put(">", "GRE");
        typeCode.put(">=", "GEQ");
        typeCode.put("==", "EQL");
        typeCode.put("!=", "NEQ");
        typeCode.put("=", "ASSIGN");
        typeCode.put(";", "SEMICN");
        typeCode.put(",", "COMMA");
        typeCode.put("(", "LPARENT");
        typeCode.put(")", "RPARENT");
        typeCode.put("[", "LBRACK");
        typeCode.put("]", "RBRACK");
        typeCode.put("{", "LBRACE");
        typeCode.put("}", "RBRACE");
        reserve.add("main");
        reserve.add("const");
        reserve.add("int");
        reserve.add("break");
        reserve.add("continue");
        reserve.add("if");
        reserve.add("else");
        reserve.add("while");
        reserve.add("getint");
        reserve.add("printf");
        reserve.add("return");
        reserve.add("void");
        reserve.add("bitand");
    }

    public boolean isReserve(String ident) {
        return reserve.contains(ident);
    }

    public String getToken(String name) {
        return typeCode.get(name);
    }

    public boolean isDouble(char c) {
        return c == '!' || c == '<' || c == '>' || c == '=';
    }

    public boolean isSingle(char c) {
        return c == '&' || c == '|' || c == '+' || c == '-' || c == '%' || c == '*';
    }

    public boolean isSep(char c) {
        return c == '[' || c == ']' || c == '{' || c == '}' || c == '(' || c == ')' || c == ',' || c == ';';
    }

    public boolean isId(char c) {
        return Character.isDigit(c) || Character.isLetter(c) || c == '_';
    }

}
