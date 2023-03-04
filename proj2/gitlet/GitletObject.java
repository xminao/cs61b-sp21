package gitlet;

import java.io.Serializable;

public class GitletObject implements Serializable {
    enum Type {
        BLOB,
        TREE,
        COMMIT
    }
    private Type type;

    public GitletObject(Type type) {
        this.type = type;
    }

    public String getType() {
        return type.toString().toLowerCase();
    }
}
