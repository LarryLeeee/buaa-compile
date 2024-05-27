package backend;

import java.util.ArrayList;

public class Global {
    public String name;
    public ArrayList<Integer> values;

    public Global(String name, ArrayList<Integer> values) {
        this.name = name;
        this.values = values;
    }

    public String string() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(":\n");
        for (Integer value : values) {
            sb.append(".word ").append(value).append("\n");
        }
        return sb.toString();
    }
}
