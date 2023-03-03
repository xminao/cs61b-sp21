package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.TreeMap;
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
    /** The refs directory. */
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    /** The heads directory. */
    public static final File BRANCH_DIR = join(REFS_DIR, "heads");
    /** The stage-area(index) file.*/
    public static final File STAGE_AREA = join(GITLET_DIR, "index");
    /** The HEAD file.
     *  Indicate to master branch.
     * */
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    /**
     * Current branch.
     * master branch in default.
     */
    public static String CURRENT_BRANCH;

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * Initialize .gitlet dir and objects dir.
     */
    public static void init() {
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        BRANCH_DIR.mkdir();

        CURRENT_BRANCH = "master";
        File branch = join(BRANCH_DIR, CURRENT_BRANCH);
        try {
            STAGE_AREA.createNewFile();
            HEAD_FILE.createNewFile();
            branch.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize Commit.
        Commit init_commit = new Commit();
        byte[] contents = serialize(init_commit);
        String hashcode = sha1(contents);
        addObjToDatabase(hashcode, contents);
        System.out.println("commit hashcode: " + hashcode);
        writeContents(branch, hashcode);
        writeContents(HEAD_FILE, branch.getAbsolutePath());

        // Initialize stage-area
        Tree stage_tree = new Tree();
        addObjToDatabase(sha1(serialize(stage_tree)), serialize(stage_tree));
        System.out.println("initial stage tree:" + sha1(serialize(stage_tree)));
        writeContents(STAGE_AREA, sha1(serialize(stage_tree)));
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     * Get a new stage tree(extends prev stage tree) when operated.
     */
    public static void add(String... args) {
        validateRepo();

        String filename = args[1];
        List<String> file_list = Utils.plainFilenamesIn(CWD);
        if (!file_list.contains(filename)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        String stage_hashcode = readContentsAsString(STAGE_AREA);
        Tree prev_tree = readObject(join(OBJECTS_DIR, stage_hashcode), Tree.class);
        Tree stage_tree = new Tree(prev_tree);

        // Using Blob Object.
        byte[] contents = readContents(join(CWD, filename));
        Blob blob = new Blob(contents);
        addObjToDatabase(sha1(serialize(blob)), serialize(blob));
        stage_tree.add(filename, sha1(serialize(blob)));
        addObjToDatabase(sha1(serialize(stage_tree)), serialize(stage_tree));
        System.out.println("blob hashcode:  " + sha1(serialize(blob)));
        System.out.println("stage tree hashcode: " + sha1(serialize(stage_tree)));

        // Update stage-area
        updateStage(sha1(serialize(stage_tree)));
    }

    private static void updateStage(String hashcode) {
        validateObj(hashcode);
        writeContents(STAGE_AREA, hashcode);
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging area
     * so they can be restored at a later time, creating a new commit. The commit
     * is said to be tracking the saved files.
     */
    public static void commit(String message) {
        // Get the current HEAD pointer.
        File f = new File(readContentsAsString(HEAD_FILE));
        String parent_hashcode = readContentsAsString(f);

        // Get the parent commit of current HEAD.
        Commit parent = readObject(join(OBJECTS_DIR, parent_hashcode), Commit.class);
        System.out.println(parent.toString());
    }

    /**
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal. An example of
     * the exact format it should follow is as follows.
     */
    public static void status() {
        System.out.println("=== Branches ===");
        for (String branch : branchList()) {
            if (branch.equals(CURRENT_BRANCH)) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println("\n" + "=== Staged Files ===");
        ls_stage();
        System.out.println("\n" + "=== Removed Files ===");
        System.out.println("\n" + "=== Modifications Not Staged For Commit ===");
        System.out.println("\n" + "=== Untracked Files ===");
    }

    /**
     * Creates a new branch with the given name, and points it at the current head commit.
     */
    public static void branch(String branch) {
        File f = join(BRANCH_DIR, branch);
        if (f.exists() && f.isFile()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        try {
            if (f.createNewFile()) {
                CURRENT_BRANCH = branch;
                writeContents(HEAD_FILE, f.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * List all branches in current repository.
     */
    private static List<String> branchList() {
        List<String> list = new ArrayList<>();
        if (BRANCH_DIR.exists() && BRANCH_DIR.isDirectory()) {
            File[] files = BRANCH_DIR.listFiles();
            if (files != null) {
                for (File f : files) {
                    list.add(f.getName());
                }
            }
        }
        return list;
    }

    /**
     * Cat contents of an SHA-1 object file in objects-database.
     * */
    public static void cat_file(String hashcode) {
        validateObj(hashcode);

        String type = typeOf(hashcode);
        if (type.equals("tree")) {
            listOfTree(hashcode);
        } else {
            byte[] read = Utils.readContents(join(OBJECTS_DIR, hashcode));
            System.out.println(new String(read));
        }
    }

    private static void listOfTree(String hashcode) {
        validateObj(hashcode);

        File obj_t = join(OBJECTS_DIR, hashcode);
        Tree tree = readObject(obj_t, Tree.class);
        TreeMap<String, String> map = tree.get_mapping();
        for (String key : map.keySet()) {
            System.out.println(Repository.typeOf(map.get(key)) + " " +
                    map.get(key) + " " +
                    key);
        }
    }

    /**
     * List mapping in stage-area.
     */
    public static void ls_stage() {
//        TreeMap<String, String> map = stage_map();
//        for (String key : map.keySet()) {
//            System.out.println(map.get(key) + " " + key);
//        }
        String stage_hashcode = readContentsAsString(STAGE_AREA);
        Tree tree = readObject(join(OBJECTS_DIR, stage_hashcode), Tree.class);
        for (String key : tree.get_mapping().keySet()) {
            System.out.println(tree.get_mapping().get(key) + " " + key);
        }
    }

    /**
     * Returns the map of stage-area file.
     */
    @SuppressWarnings("unchecked")
//    private static TreeMap<String, String> stage_map() {
//        String stage_hashcode = readContentsAsString(STAGE_AREA);
//        return readObject(join(OBJECTS_DIR, stage_hashcode));
//    }

    /**
     * Returns the type(tree or blob) of SHA-1 object.
     */
    public static String typeOf(String hashcode) {
        validateObj(hashcode);
        File obj_f = join(OBJECTS_DIR, hashcode);
        GitletObject obj_g = readObject(obj_f, GitletObject.class);
        return obj_g.getType();
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
     * Is object in objects-database.
     * Exit if not.
     */
    public static void validateObj(String hashcode) {
        File obj_f = join(OBJECTS_DIR, hashcode);
        if (!obj_f.exists()) {
            System.out.println("Not a valid object name.");
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
        // Create Object file.
        File obj_f = join(OBJECTS_DIR, hashcode);
        if (!obj_f.exists()) {
            try {
                obj_f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Write contents to correspond Object file.
        // Overwrite if file exist.
        writeContents(obj_f, contents);
    }

}
