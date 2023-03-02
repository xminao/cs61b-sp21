package gitlet;

import java.io.Serializable;

public class Blob implements Serializable {
    private String hashcode;
    private String filename;

    public Blob(String hashcode, String filename) {
        this.hashcode = hashcode;
        this.filename = filename;
    }
}
