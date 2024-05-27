package frontend.Lexer;

import java.util.ArrayList;

public class Lexer {
    private String input;
    private int pos;
    public int line = 0;
    private static LexicalType lexicalType = new LexicalType();
    private int fsSt;
    private String fs;
    private int noteSt;//标记注释状态，0代表无注释，1代表单行，2代表多行
    private ArrayList<Token> tokens = new ArrayList<>();

    public Lexer() {
        pos = 0;
    }

    public void setInput(String input) {
        this.input = input;
        line++;
        pos = 0;
        if (noteSt == 1) {
            noteSt = 0;
        }
        fs = "";
        if (line == 28) {
            fs = "";
        }
        scan();
    }

    public ArrayList<Token> getTokens() {
        return tokens;
    }

    public void scan() {
        while (pos < input.length()) {
            if (fsSt == 1) {
                while (input.charAt(pos) != '"') {
                    fs += input.charAt(pos);
                    pos += 1;
                }
//                fs += "\"";
                fsSt = 0;//字符串状态
                pos += 1;//越过末尾的"
                tokens.add(new FS(fs, line));
                fs = "";
            } else if (noteSt == 0) {
                char c = input.charAt(pos);
                if (Character.isWhitespace(c)) {//越过空白字符
                    pos++;
                } else if (Character.isDigit(c)) {
                    String num = "";
                    num += c;
                    pos += 1;
                    if (c == '0') {
                        tokens.add(new Num(num, line));
                    } else {
                        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                            num += input.charAt(pos);
                            pos += 1;
                        }
                        tokens.add(new Num(num, line));
                    }
                } else if (Character.isLetter(c) || c == '_') {
                    String ident = "";
                    ident += c;
                    pos += 1;
                    while (pos < input.length() && lexicalType.isId(input.charAt(pos))) {
                        ident += input.charAt(pos);
                        pos += 1;
                    }
                    if (lexicalType.isReserve(ident)) {
                        if (ident.equals("bitand")) {
                            tokens.add(new Op(ident, line));
                        } else {
                            tokens.add(new Reserve(ident, line));
                        }
                    } else {
                        tokens.add(new Id(ident, line));
                    }
                } else if (c == '/') {
                    pos += 1;
                    if (pos < input.length() && input.charAt(pos) == '/') {//单行注释，需要预读一个判读
                        noteSt = 1;
                    } else if (pos < input.length() && input.charAt(pos) == '*') {//多行注释，需要预读一个判断
                        noteSt = 2;
                        pos += 1;
                    } else {//除法
                        tokens.add(new Op("/", line));
                    }
                } else if (lexicalType.isDouble(c)) {//一个字符是另一个的前缀
                    String op = "";
                    op += c;
                    pos += 1;
                    if (pos < input.length() && input.charAt(pos) == '=') {
                        op += "=";
                        pos += 1;
                    }
                    tokens.add(new Op(op, line));
                } else if (lexicalType.isSingle(c)) {
                    if (c == '|') {
                        pos += 2;
                        tokens.add(new Op("||", line));
                    } else if (c == '&') {
                        pos += 2;
                        tokens.add(new Op("&&", line));
                    } else {
                        pos += 1;
                        tokens.add(new Op(Character.toString(c), line));
                    }
                } else if (lexicalType.isSep(c)) {
                    pos += 1;
                    tokens.add(new Sep(c, line));
                } else if (c == '"') {
//                    fs = "\"";
                    fsSt = 1;
                    pos++;
                }
            } else if (noteSt == 1) {
                break;
            } else {// /*的判断:*/
                char c = input.charAt(pos);
                if (c == '*') {
                    if (pos < input.length() - 1 && input.charAt(pos + 1) == '/') {
                        pos += 2;
                        noteSt = 0;
                    } else {
                        pos += 1;
                    }
                } else {
                    pos += 1;
                }
            }
        }
    }

}
