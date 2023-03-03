package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

public class GitletObject implements Serializable {
    private String type;
    private byte[] contents;

    public GitletObject(String type, byte[] contents) {
        this.type = type;
        this.contents = contents;
    }

    public GitletObject(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public byte[] getContents() {
        return contents;
    }
}
