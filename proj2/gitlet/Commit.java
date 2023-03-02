package gitlet;

// TODO: any imports you need here

import java.text.SimpleDateFormat;
import java.util.Date; // TODO: You'll likely use this in this class
import java.util.TreeMap;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit {
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
    private String author;
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
    private String tree;

    /* TODO: fill in the rest of this class. */
    public Commit(String author, String parent, String message) {
        this.author = author;
        this.date = simpleDateFormat.format(new Date());
        this.parent = parent;
        this.message = message;
    }

    public void dateTest() {
        System.out.println(date);
    }
}
