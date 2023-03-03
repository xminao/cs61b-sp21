package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import static gitlet.Repository.OBJECTS_DIR;
import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit extends GitletObject {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * Format Date.
     */
    private static String pattern = "EEEEE MMMMM d HH:mm:ss yyyy Z";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

    /** The author of this Commit. */
    //private String author;
    /** The created date of this Commit. */
    private String date;
    /** The parent Commit of this Commit. */
    private String parent;
    /** The message of this Commit. */
    private String message;
    /**
     * Directory structures mapping names to references to blobs
     * and other trees (subdirectories).
     */
    private TreeMap<String, String> map;

    /* TODO: fill in the rest of this class. */
    @SuppressWarnings("unchecked")
    public Commit(String parent, String message) {
        super("commit");
        //this.author = author;
        this.date = simpleDateFormat.format(new Date());
        this.parent = parent;
        this.message = message;
        map = readObject(join(OBJECTS_DIR, parent), TreeMap.class);
    }

    /** Initial Commit. */
    public Commit() {
        super("commit");
        this.date = simpleDateFormat.format(new Date(0));
        this.parent = null;
        this.message = "initial commit";
        map = new TreeMap<>();
    }

    /**
     * Returns the SHA-1 of this commit.
     */
    public String hash() {
        return sha1(serialize(this));
    }

    @Override
    public String toString() {
        return "===" +
                "commit " + hash() + '\n' +
                "data " + date + '\n' +
                "message " + message + '\n';
    }
}
