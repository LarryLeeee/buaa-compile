package backend;

import ir.Value.Str;

public class STR {
    public String name;
    public String content;

    public STR(Str str) {
        name = str.name;
        content = str.content.replaceAll("\\\\0a", "\\\\n");
    }

    public String string() {
        return name + ":.ascii \"" + content + "\"";
    }

}
