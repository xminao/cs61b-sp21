package gitlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * Format Date.
     */
    private static String pattern = "EEE MMM d HH:mm:ss yyyy Z";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.ENGLISH);

    /** The author of this Commit. */
    //private String author;
    /** The created date of this Commit. */
    private String date;
    /** The parent Commit of this Commit. */
    private String[] parent;
    /** The message of this Commit. */
    private String message;
    /**
     * Directory structures mapping names to references to blobs
     * and other trees (subdirectories).
     */
    private Tree tree;

    public Commit(String[] parent, String message, Tree tree) {
        super(Type.COMMIT);
        //this.author = author;
        this.date = simpleDateFormat.format(new Date());
        this.parent = parent;
        this.message = message;
        this.tree = tree;
    }

    /** Initial Commit. */
    public Commit() {
        super(Type.COMMIT);
        this.date = simpleDateFormat.format(new Date(0));
        this.parent = null;
        this.message = "initial commit";
        tree = new Tree();
    }

    /**
     * Returns the root tree.
     */
    public Tree getTree() {
        return tree;
    }

    /**
     * Returns the parent commit ID.
     */
    public String[] getParent() {
        return parent;
    }

    /**
     * Returns commit created date.
     */
    public String getDate() {
        return date;
    }

    /**
     * Returns message of commit.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the SHA-1 of this commit.
     */
    public String hash() {
        return sha1(serialize(this));
    }

    @Override
    public String toString() {
        return "===" + '\n' +
                "commit " + hash() + '\n' +
                "data " + date + '\n' +
                "message " + message + '\n';
    }
}
