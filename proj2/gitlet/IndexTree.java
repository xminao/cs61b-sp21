package gitlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

public class IndexTree implements Serializable, Iterable<String> {

    private class Record implements Serializable {
        private Index.State state;
        private String objID;
        public Record(Index.State status, String objID) {
            this.state = status;
            this.objID = objID;
        }
        public Index.State getState() {
            return state;
        }
        public String getObjID() {
            return objID;
        }
    }

    private TreeMap<String, Record> _mapping;

    public IndexTree() {
        _mapping = new TreeMap<>();
    }

    public TreeMap<String, Record> get_mapping() {
        return _mapping;
    }

    public void add(String filename, Index.State status, String objID) {
        Record record = new Record(status, objID);
        _mapping.put(filename, record);
    }

    public void del(String filename) {
        if (has(filename)) {
            _mapping.remove(filename);
        }
    }

    public String getObjID(String name) {
        if (has(name)) {
            return _mapping.get(name).getObjID();
        }
        return null;
    }

    public Index.State getState(String name) {
        if (has(name)) {
            return _mapping.get(name).getState();
        }
        return null;
    }

    public boolean has(String filename) {
        return _mapping.containsKey(filename);
    }

    public boolean has(String filename, String objID) {
        if (!has(filename)) {
            return false;
        }
        return getObjID(filename).equals(objID);
    }

    public boolean isEmpty() {
        return _mapping.isEmpty();
    }

    /**
     * Returns file name list by state (new file, deleted, modified).
     */
    public List<String> listByState(Index.State state) {
        List<String> list = new ArrayList<>();
        for (String filename : this) {
            if (get_mapping().get(filename).getState().equals(state)) {
                list.add(filename);
            }
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (String key : this) {
//            if (!_mapping.get(key).getState().equals(Index.State.DELETED)) {
//                out.append(getObjID(key));
//                out.append(" ").append(key);
//                out.append("\n");
//            }
            out.append(_mapping.get(key).getState().toString());
            out.append(": ");
            out.append(key);
            out.append("\n");
        }
        return out.toString();
    }

    @Override
    public Iterator<String> iterator() {
        return _mapping.keySet().iterator();
    }
}
