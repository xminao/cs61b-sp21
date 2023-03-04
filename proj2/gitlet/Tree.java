package gitlet;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class Tree extends GitletObject {
    // Mapping names to references to blobs and other trees
    private TreeMap<String, String> _mapping;

    // Constructor
    // Has the previous Tree.
    public Tree(Tree prev) {
        super(Type.TREE);
        TreeMap<String, String> prev_map = prev.get_mapping();
        _mapping = new TreeMap<>();
        for (String key : prev_map.keySet()) {
            _mapping.put(key, prev_map.get(key));
        }
    }

    public Tree() {
        super(Type.TREE);
        _mapping = new TreeMap<>();
    }

    public TreeMap<String, String> get_mapping() {
        return _mapping;
    }

    public void add(String name, String hashcode) {
        _mapping.put(name, hashcode);
    }
}
