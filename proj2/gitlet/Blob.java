package gitlet;

import java.io.Serializable;

public class Blob extends GitletObject {
    private byte[] contents;

    public Blob() {
        super(Type.BLOB);
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }
}
