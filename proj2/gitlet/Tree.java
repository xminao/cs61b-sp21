package gitlet;

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

    public void add(String name, String objID) {
        _mapping.put(name, objID);
    }

    public void del(String name) {
        _mapping.remove(name);
    }

    public String getObjID(String name) {
        if (has(name)) {
            return _mapping.get(name);
        }
        return null;
    }

    public boolean has(String name, String objID) {
        return (_mapping.containsKey(name) && _mapping.get(name).equals(objID));
    }

    public boolean has(String name) {
        return _mapping.containsKey(name);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (String key : _mapping.keySet()) {
            out.append(Repository.typeOf(_mapping.get(key)));
            out.append(" ").append(_mapping.get(key)).append(" ").append(key);
            out.append("\n");
        }
        return out.toString();
    }
}
