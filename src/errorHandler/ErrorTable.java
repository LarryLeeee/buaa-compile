package errorHandler;

import java.util.ArrayList;
import java.util.HashMap;

public class ErrorTable {
    private ArrayList<HashMap<String, Ident>> tables;

    public ArrayList<ArrayList<String>> types;

    public ErrorTable() {
        tables = new ArrayList<>();
        types = new ArrayList<>();
    }

    public HashMap<String, Ident> top() {
        return tables.get(tables.size() - 1);
    }

    public ArrayList<String> topType() {
        return types.get(types.size() - 1);
    }

    public Ident find(String name) {
        for (int i = tables.size() - 1; i >= 0; i--) {
            Ident t = tables.get(i).get(name);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    public Ident findB(String name) {
        return top().get(name);//如果是函数的话，其在定义的时候处于第一层，因此直接top()即可
        //如果是常变量的话，也是直接本层即可
    }

    public void put(String name, Ident v) {
        top().put(name, v);
    }

    public void putType(String type) {
        topType().add(type);
    }

    public void addLayer() {
        tables.add(new HashMap<>());
    }

    public void popLayer() {
        tables.remove(tables.size() - 1);
    }

    public void addType() {
        types.add(new ArrayList<>());
    }

    public void popType() {
        types.remove(types.size() - 1);
    }

}
