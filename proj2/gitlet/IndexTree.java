package gitlet;

import java.io.Serializable;
import java.util.TreeMap;

public class IndexTree implements Serializable {
    private class Record implements Serializable {
        private Index.Status status;
        private String objID;
        public Record(Index.Status status, String objID) {
            this.status = status;
            this.objID = objID;
        }
        public Index.Status getStatus() {
            return status;
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

    public void add(String filename, Index.Status status, String objID) {
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

    public boolean has(String filename) {
        return _mapping.containsKey(filename);
    }

    public boolean has(String filename, String objID) {
        if (!has(filename)) {
            return false;
        }
        return _mapping.get(filename).getObjID().equals(objID);
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (String key : _mapping.keySet()) {
            if (!_mapping.get(key).getStatus().equals(Index.Status.DELETED)) {
                out.append(getObjID(key));
                out.append(" ").append(key);
                out.append("\n");
            }
        }
        return out.toString();
    }
}
