package gitlet;

import java.io.File;
import static gitlet.Utils.*;

public class Blob extends GitletObject {
    private byte[] contents;

    public Blob(File file) {
        super(Type.BLOB);
        this.contents = readContents(file);
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    @Override
    public String toString() {
        return new String(contents);
    }
}
