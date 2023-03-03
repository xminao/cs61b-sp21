package gitlet;

import java.io.Serializable;

public class Blob extends GitletObject {

    public Blob(byte[] contents) {
        super("blob", contents);
    }

    @Override
    public byte[] getContents() {
        return super.getContents();
    }
}
