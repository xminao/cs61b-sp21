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
    public static final File BRANCHES_DIR = join(REFS_DIR, "heads");
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
    //public static String CURRENT_BRANCH;

    /**
     * Creates a new Gitlet version-control system in the current directory.
     * Initialize .gitlet dir and objects dir.
     */
    public static void init() {
        validateNotRepo();

        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        BRANCHES_DIR.mkdir();

        try {
            STAGE_AREA.createNewFile();
            HEAD_FILE.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize Empty Commit. (first commit)
        Commit init_commit = new Commit();
        String hashcode = addObjToDatabase(init_commit);
        System.out.println("init commit hashcode: " + hashcode);
        initBranch("master", init_commit);
        System.out.println("current branch: " + getHeadRef());

        // Initialize stage-area
//        Tree stage_tree = new Tree();
//        addObjToDatabase(sha1(serialize(stage_tree)), serialize(stage_tree));
//        System.out.println("initial stage tree:" + sha1(serialize(stage_tree)));
//        writeContents(STAGE_AREA, sha1(serialize(stage_tree)));
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     * Get a new stage tree(extends prev stage tree) when operated.
     */
    public static void add(String... args) {
        validateRepo();
        String filename = args[1];
        validateFile(filename);

        Tree cache_tree = new Tree();
        Tree tracked = getHeadCommit().getTree();

        File file = join(CWD, filename);
        Blob blob = new Blob(file);
        String hashcode = addObjToDatabase(blob);
        System.out.println("added file hashcode: " + hashcode);

        cache_tree.add(filename, hashcode);
        String cache_tree_hashcode = addObjToDatabase(cache_tree);
        updateStage(cache_tree_hashcode);
        System.out.println("stage-area tree hashcode: " + cache_tree_hashcode);


//        String stage_hashcode = readContentsAsString(STAGE_AREA);
//        Tree prev_tree = readObject(join(OBJECTS_DIR, stage_hashcode), Tree.class);
//        Tree stage_tree = new Tree(prev_tree);
//
//        // Using Blob Object.
//        byte[] contents = readContents(join(CWD, filename));
//        Blob blob = new Blob(contents);
//        addObjToDatabase(sha1(serialize(blob)), serialize(blob));
//        stage_tree.add(filename, sha1(serialize(blob)));
//        addObjToDatabase(sha1(serialize(stage_tree)), serialize(stage_tree));
//        System.out.println("blob hashcode:  " + sha1(serialize(blob)));
//        System.out.println("stage tree hashcode: " + sha1(serialize(stage_tree)));
//
//        // Update stage-area
//        updateStage(sha1(serialize(stage_tree)));
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
        System.out.println("\n" + "=== Staged Files ===");
        ls_stage();
        System.out.println("\n" + "=== Removed Files ===");
        System.out.println("\n" + "=== Modifications Not Staged For Commit ===");
        System.out.println("\n" + "=== Untracked Files ===");
    }

    /**
     * Checkout is a kind of general command that can do a few different things depending on what its arguments are.
     *
     * 1. checkout -- [file name]
     *
     *  Takes the version of the file as it exists in the head commit and puts it in the working directory,
     *  overwriting the version of the file that’s already there if there is one. The new version of the file
     *  is not staged.
     *
     *
     * 2. checkout [commit id] -- [file name]
     *
     *  Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
     *  overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
     *
     *
     * 3. java gitlet.Main checkout [branch name]
     *
     *  Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     *  overwriting the versions of the files that are already there if they exist. Also, at the end of this command,
     *  the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current
     *  branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the
     *  checked-out branch is the current branch
     */
    public static void check_out(String... args) {
        // checkout [branch name]
        if(args.length == 1) {

        }
    }



    /**
     * Creates a new branch with the given name, and points it at the current head commit.
     */
    public static void branch(String branch) {
        File f = join(BRANCHES_DIR, branch);
        if (f.exists() && f.isFile()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initial branch (master).
     */
    public static void initBranch(String branch, Commit commit) {
        branch(branch);
        File f = join(BRANCHES_DIR, branch);
        writeContents(f, sha1(serialize(commit)));
        setHeadRef(branch);
    }

    /**
     * List all branches in current repository.
     */
    private static List<String> branchList() {
        List<String> list = new ArrayList<>();
        if (BRANCHES_DIR.exists() && BRANCHES_DIR.isDirectory()) {
            File[] files = BRANCHES_DIR.listFiles();
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

//        String type = typeOf(hashcode);
//        if (type.equals("tree")) {
//            listOfTree(hashcode);
//        } else {
//            byte[] read = Utils.readContents(join(OBJECTS_DIR, hashcode));
//            System.out.println(new String(read));
//        }
        GitletObject obj = readObject(join(OBJECTS_DIR, hashcode), GitletObject.class);
        System.out.println(obj);
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
     * Validate current working directory is a repository. (for all gitlet operations in addition to init)
     * Exit if not.
     */
    public static void validateRepo() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /**
     * Validate current working directory is not a repository. (for init)
     * Exit if is.
     */
    public static void validateNotRepo() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
    }

    /**
     * validate object:hashcode in objects-database.
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
     * Validate file:filename in CWD.
     * Exist if not.
     * Omit the subdirectory
     */
    public static void validateFile(String filename) {
        List<String> file_list = plainFilenamesIn(CWD);
        if (file_list != null) {
            for (String file : file_list) {
                System.out.println(file);
            }
        }
        if (file_list == null || !file_list.contains(filename)) {
            System.out.println("File does not exist.");
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

    public static String addObjToDatabase(GitletObject obj) {
        byte[] contents = serialize(obj);
        String hashcode = sha1(contents);

        File obj_f = join(OBJECTS_DIR, hashcode);
        if (!obj_f.exists()) {
            try {
                obj_f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Object exist.");
            System.exit(0);
        }
        writeContents(obj_f, contents);

        return hashcode;
    }

    /**
     * Set the ref of HEAD pointer branch.
     */
    public static void setHeadRef(String ref) {
        String head = "ref: refs/heads/" + ref;
        writeContents(HEAD_FILE, head);
    }

    /**
     * Returns the ref of HEAD pointer branch.
     */
    public static String getHeadRef() {
        String head = readContentsAsString(HEAD_FILE);
        String[] list = head.split("/");
        return list[list.length - 1];
    }

    /**
     * Returns the commit of current HEAD pointer.
     */
    public static Commit getHeadCommit() {
        String hashcode = readContentsAsString(join(BRANCHES_DIR, getHeadRef()));
        Commit commit = readObject(join(OBJECTS_DIR, hashcode), Commit.class);
        return commit;
    }
}
