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
            //STAGE_AREA.createNewFile();
            HEAD_FILE.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize root commit (empty commit)
        Commit init_commit = new Commit();
        String hashcode = addObjToDatabase(init_commit);
        System.out.println("init commit hashcode: " + hashcode);

        // Initialize default branch : master
        initBranch("master", init_commit);
        System.out.println("current branch: " + getHeadRef());

        // Initialize stage-area
        Index.initIndex();
    }

    /**
     * Adds a copy of the file as it currently exists to the staging area.
     * Get a new stage tree(extends prev stage tree) when operated.
     */
    public static void add(String filename) {
        validateRepo();
        validateFile(filename);

        // add file to index(stage area).
        File file = join(CWD, filename);
        Index.addIndex(file);
    }

    /**
     * Unstage the file if it is currently staged for addition. If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     */
    public static void rm(String filename) {
        validateRepo();

        // del file in index(stage area).
        Index.delIndex(filename);
    }

    /**
     * Saves a snapshot of tracked files in the current commit and staging area
     * so they can be restored at a later time, creating a new commit. The commit
     * is said to be tracking the saved files.
     */
    public static void commit(String message) {
        // Get the current HEAD commit.
        Commit HEAD = getHeadCommit();

        // generate new root tree.
        Tree root = Index.generateCommitTree();

        // do not commit if generate tree same as parent commit
        if (root.equals(HEAD.getTree())) {
            return;
        }

        // new commit
        Commit newCommit = new Commit(getHeadCommitID(), message, root);
        String objID = addObjToDatabase(newCommit);
        setHeadCommitID(objID);
        System.out.println("new commit objID: " + objID);

        // clean the stage-area.
        Index.clearIndex();
    }

    /**
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal. An example of
     * the exact format it should follow is as follows.
     */
    public static void status() {
        System.out.println("=== Branches ===");
        for (String branch : branchList()) {
            if (branch.equals(getHeadRef())) {
                System.out.println("*" + branch);
            } else {
                System.out.println(branch);
            }
        }
        System.out.println("\n" + "=== Staged Files ===");
        for (String filename : Index.stagedList()) {
            System.out.println(filename);
        }
        System.out.println("\n" + "=== Removed Files ===");
        for (String filename : Index.removedList()) {
            System.out.println(filename);
        }
        System.out.println("\n" + "=== Modifications Not Staged For Commit ===");
        for (String filename : Index.notStagedList()) {
            System.out.println(filename);
        }
        System.out.println("\n" + "=== Untracked Files ===");
        for (String filename : Index.untrackedList()) {
            System.out.println(filename);
        }
    }

    /**
     * Checkout is a kind of general command that can do a few different things depending on what its arguments are.
     *
     * 1. checkout -- [file name]
     *      Takes the version of the file as it exists in the head commit and puts it in the working directory,
     *      overwriting the version of the file that’s already there if there is one. The new version of the file
     *      is not staged.
     *
     *
     * 2. checkout [commit id] -- [file name]
     *      Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory,
     *      overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.
     *
     *
     * 3. checkout [branch name]
     *      Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     *      overwriting the versions of the files that are already there if they exist. Also, at the end of this command,
     *      the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current
     *      branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the
     *      checked-out branch is the current branch
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
            List<String> branches = plainFilenamesIn(BRANCHES_DIR);
            if (branches != null) {
                for (String name : branches) {
                    list.add(name);
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

    /**
     * Returns the type(tree or blob) of SHA-1 object.
     */
    public static String typeOf(String objID) {
        validateObj(objID);
        File obj_f = join(OBJECTS_DIR, objID);
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
        String objID = hashObj(obj);
        File obj_f = join(OBJECTS_DIR, objID);
        if (!obj_f.exists()) {
            try {
                obj_f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writeObject(join(OBJECTS_DIR, objID), obj);
        }
        return objID;
    }

    /**
     * Returns the hashcode of Gitlet Object(blob, tree, commit).
     */
    public static String hashObj(GitletObject obj) {
        byte[] contents = serialize(obj);
        return sha1(contents);
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

    /**
     * Set the commit ID of HEAD pointer.
     */
    public static void setHeadCommitID(String objID) {
        validateObj(objID);
        writeContents(join(BRANCHES_DIR, getHeadRef()), objID);
    }
    /**
     * Returns the ObjID of current HEAD commit.
     */
    public static String getHeadCommitID() {
        return readContentsAsString(join(BRANCHES_DIR, getHeadRef()));
    }
}
