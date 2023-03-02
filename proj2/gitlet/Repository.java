package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The objects database directory. */
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    /** The stage-area(index) file.*/
    public static final File STAGE_AREA = join(GITLET_DIR, "index");

    /* TODO: fill in the rest of this class. */
    /**
     * Creates a new Gitlet version-control system in the current directory.
     * Initialize .gitlet dir and objects dir.
     */
    public static void init() {
        if (GITLET_DIR.mkdir()) {
            OBJECTS_DIR.mkdir();
        }
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     */
    public static void add(String... args) {
        validateRepo();

        String filename = args[1];
        List<String> file_list = Utils.plainFilenamesIn(CWD);
        if (!file_list.contains(filename)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        // Initialize the stage-area(index file) if not exist.
        if (!STAGE_AREA.exists()) {
            try {
                STAGE_AREA.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Add hashed file to objects-database.
        byte[] contents = readContents(Utils.join(CWD, filename));
        String hashcode = sha1(contents);
        addObjToDatabase(hashcode, contents);

        // Directory structures mapping name to reference to blob.
        HashMap<String, String> map = stage_map();
        map.put(filename, hashcode);
        writeObject(STAGE_AREA, map);
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging area
     * so they can be restored at a later time, creating a new commit. The commit
     * is said to be tracking the saved files.
     */
    public static void commit(String message) {

    }

    /**
     * Cat contents of an SHA-1 object file in objects-database.
     * */
    public static void cat_file(String hashcode) {
        File f = join(OBJECTS_DIR, hashcode);
        if (!f.exists()) {
            System.out.println("Not a valid object name.");
            System.exit(0);
        }
        byte[] read = Utils.readContents(join(OBJECTS_DIR, hashcode));
        System.out.println(new String(read));
    }

    /**
     * List mapping in stage-area.
     */
    public static void ls_stage() {
        HashMap<String, String> map = stage_map();
        for (String key : map.keySet()) {
            System.out.println(map.get(key) + " " + key);
        }
    }

    /**
     * Returns the map of stage-area file.
     */
    @SuppressWarnings("unchecked")
    private static HashMap<String, String> stage_map() {
        if (STAGE_AREA.length() == 0) {
            return new HashMap<>();
        }
        return readObject(STAGE_AREA, HashMap.class);
    }

    /**
     * Is current working directory is git repository.
     * Exit if not.
     */
    public static void validateRepo() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /**
     * Add Object file to database.
     */
    public static void addObjToDatabase(String hashcode, byte[] contents) {
        // Create Object directory.
//        String part_head = hashcode.substring(0, 2);
//        String part_last = hashcode.substring(2);
//
//        File obj_dir = new File(OBJECTS_DIR, part_head);
//        if (!obj_dir.exists()) {
//            obj_dir.mkdir();
//        }
//        File obj = new File(OBJECTS_DIR, part_last);
//        try {
//            obj.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        File obj = new File(OBJECTS_DIR, hashcode);
        if (!obj.exists()) {
            try {
                obj.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Write contents to correspond Object file.
        // Overwrite if file exist.
        Utils.writeContents(obj, contents);
    }

}
