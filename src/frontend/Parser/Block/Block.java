package frontend.Parser.Block;

import java.util.ArrayList;

public class Block {

    public ArrayList<BLockItem> bLockItems;

    public Block() {
        bLockItems = new ArrayList<>();
    }

    public void addBlockItem(BLockItem bLockItem) {
        bLockItems.add(bLockItem);
    }

    public BLockItem getLast() {
        int len = bLockItems.size();
        return bLockItems.get(len - 1);
    }
}
