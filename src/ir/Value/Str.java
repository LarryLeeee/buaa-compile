package ir.Value;

import java.util.ArrayList;

public class Str extends Value {
    public String name;
    public int length;
    public String content;

    public Str(String name, int length, String content, Module module) {
        super(name, null);
        this.name = name;
        this.length = length + 1 - (content.length() - content.replace("\\n", "").length()) / 2;
        this.content = content.replaceAll("\\\\n", "\\\\0a");
        module.addOutput(this);
    }

    public void getOutputs(ArrayList<String> outputs) {
        String sb = name + " = constant [" + length + " x i8] c\"" +
                content.concat("\\00") + "\"";
        outputs.add(sb);
    }

}
