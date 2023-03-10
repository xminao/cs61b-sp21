package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author xminao
 */
public class Repository {
    /**
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
        String obj_ID = addObjToDatabase(init_commit);
        //System.out.println("init commit hashcode: " + obj_ID);

        // Initialize default branch : master
        initBranch("master", obj_ID);
        //System.out.println("current branch: " + getHeadRef());

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
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
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
        validateRepo();
        if (Index.isIndexEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message.trim().isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        // Get the current HEAD commit.
        Commit HEAD = getHeadCommit();

        // generate new root tree.
        Tree root = Index.generateCommitTree();

        // do not commit if generate tree same as parent commit
        if (root.equals(HEAD.getTree())) {
            return;
        }

        // new commit
        Commit newCommit = new Commit(new String[]{getHeadCommitID()}, message.trim(), root);
        String objID = addObjToDatabase(newCommit);
        setHeadCommitID(objID);
        //System.out.println("new commit objID: " + objID);

        // clean the stage-area.
        Index.clearIndex();
    }

    /**
     * Displays what branches currently exist, and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal. An example of
     * the exact format it should follow is as follows.
     */
    public static void status() {
        validateRepo();

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
     * Starting at the current head commit, display information about each commit backwards
     * along the commit tree until the initial commit, following the first parent commit links,
     * ignoring any second parents found in merge commits.
     * format follow:
     *      ===
     *      commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
     *      Date: Thu Nov 9 17:01:33 2017 -0800
     *      Another commit message.
     *
     *      ===
     *      commit e881c9575d180a215d1a636545b8fd9abfb1d2bb
     *      Date: Wed Dec 31 16:00:00 1969 -0800
     *      initial commit
     */
    public static void log() {
        validateRepo();

        // HEAD commit ID.
        String OID = getHeadCommitID();
        while (OID != null) {
            Commit commit = readObject(join(OBJECTS_DIR, OID), Commit.class);
            logPrinter(OID);
            if (commit.getParent() != null) {
                OID = commit.getParent()[0];
            } else {
                OID = null;
            }
        }
    }

    /**
     * Like log, except displays information about all commits ever made. The order of the commits does not matter.
     */
    public static void global_log() {
        validateRepo();

        Set<String> set = allValidCommits();

        for (String OID : set) {
            logPrinter(OID);
        }
    }

    private static void logPrinter(String OID) {
        Commit commit = readObject(join(OBJECTS_DIR, OID), Commit.class);
        System.out.println("===");
        System.out.println("commit " + OID);
        if (commit.getParent() != null && commit.getParent().length != 1) {
            StringBuilder builder = new StringBuilder();
            builder.append("Merge: ");
            for (String str : commit.getParent()) {
                builder.append(shortenOID(str)).append(" ");
            }
            System.out.println(builder);
        }
        System.out.println("Date: " + commit.getDate());
        System.out.println(commit.getMessage() + "\n");
    }

    /**
     * Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits,
     * it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message,
     * put the operand in quotation marks, as for the commit command below.
     */
    public static void find(String message) {
        validateRepo();

        Set<String> all = allValidCommits();
        Set<String> set = new HashSet<>();
        for (String OID : all) {
            Commit commit = readObject(join(OBJECTS_DIR, OID), Commit.class);
            if (commit.getMessage().contains(message)) {
                set.add(OID);
            }
        }

        if (set.size() == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }

        for (String OID : set) {
            System.out.println(OID);
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
    public static void checkout(String... args) {
        validateRepo();

        // TODO: abbreviated
        // checkout [branch name]
        if(args.length == 2) {
            String branch_ref = args[1];
            // set as current branch (HEAD).
            if (getHeadRef().equals(branch_ref)) {
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
            }
            Commit checkout_commit = getCommitByRef(branch_ref);
            Tree root = checkout_commit.getTree();
            overwritingCWD(root);
            setHeadRef(branch_ref);
        } else if (args.length == 3 && args[1].equals("--")) { // checkout -- [file name]
            String filename = args[2];
            // HEAD pointer.
            Commit head_commit = getHeadCommit();
            Tree root = head_commit.getTree();
            overwritingCWDFile(root, filename);
        } else if (args.length == 4 && args[2].equals("--")) { // checkout [commit id] -- [file name]
            String commit_ID = args[1];
            String filename = args[3];
            // Commit Pointer
            Commit commit = getCommitByID(commit_ID);
            Tree root = commit.getTree();
            overwritingCWDFile(root, filename);
        } else {
            // error message (class later...)
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /**
     * Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     */
    private static void overwritingCWD(Tree checkout_tree) {
        // current branch commit (HEAD)
        Commit head_commit = getHeadCommit();
        Tree head_tree = head_commit.getTree();

        List<String> list = plainFilenamesIn(CWD);
        if (list != null) {
            // delete file tracked in current branch not present in
            // the checked-out branch.
            for (String filename : list) {
                if (head_tree.has(filename) && !checkout_tree.has(filename)) {
                    restrictedDelete(filename);
                }
                if (!head_tree.has(filename) && checkout_tree.has(filename)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }

        // overwriting CWD with checked-out tree
        for (String filename : checkout_tree) {
            String OID = checkout_tree.getObjID(filename);
            Blob blob = readObject(join(OBJECTS_DIR, OID), Blob.class);
            writeContents(join(CWD, filename), blob.toString());
        }

        // clear the staging area.
        Index.clearIndex();
    }

    /**
     * Overwriting file in CWD with file [filename] in tree.
     */
    private static void overwritingCWDFile(Tree tree, String filename) {
        if (tree.has(filename)) {
            String OID = tree.getObjID(filename);
            Blob blob = readObject(join(OBJECTS_DIR, OID), Blob.class);
            writeContents(join(CWD, filename), blob.toString());
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }


    /**
     * Creates a new branch with the given name, and points it at the current head commit.
     */
    public static void branch(String branch) {
        validateRepo();

        File ref_f = join(BRANCHES_DIR, branch);
        if (ref_f.exists() && ref_f.isFile()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        try {
            ref_f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String obj_ID = getHeadCommitID();
        writeContents(ref_f, obj_ID);
    }

    /**
     * Deletes the branch with the given name. This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits that were created under the branch, or anything like that.
     */
    public static void rm_branch(String branch) {
        validateBranch(branch);

        File ref_f = join(BRANCHES_DIR, branch);
        String head_ref = getHeadRef();
        if (head_ref.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        ref_f.delete();
    }

    /**
     * Initial branch (master).
     */
    private static void initBranch(String branch, String objID) {
        File ref_f = join(BRANCHES_DIR, branch);
        if (!ref_f.exists()) {
            try {
                ref_f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        writeContents(ref_f, objID);
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
     * Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit.
     * Also moves the current branch’s head to that commit node.
     */
    public static void reset(String commitID) {
        Commit commit = getCommitByID(commitID);
        Tree root = commit.getTree();
        overwritingCWD(root);

        String ref = getHeadRef();
        File ref_f = join(BRANCHES_DIR, ref);
        writeContents(ref_f, commitID);

        //System.out.println(getHeadCommitID());
    }

    /**
     * Merges files from the given branch into the current branch.
     */
    public static void merge(String branch) {
        validateBranch(branch);

        String head_CID = getHeadCommitID();
        String merge_CID = getCommitIDByRef(branch);
        String split_CID = splitPoint(head_CID, merge_CID);

        if (split_CID.equals(merge_CID)) {
            // the split point is the same commit as the given branch.
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (split_CID.equals(head_CID)) {
            // the split point is the same commit as the current branch.
            checkout("", branch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        } else {
            // the split point is not the same commit as the current branch or given branch.
            Tree head_tree = getHeadCommit().getTree();
            Tree merge_tree = getCommitByID(merge_CID).getTree();
            Tree split_tree = getCommitByID(split_CID).getTree();

            // TODO: file not present in split point.

            // file not in split point nor given branch, but in current branch.
            // do nothing.

            // file not in split point nor current branch, but in given branch.
            // checked out and staged.
            for (String file : merge_tree) {
                if (!split_tree.has(file) && !head_tree.has(file)) {
                    checkout("", merge_CID, "--", file);
                    Index.addIndex(join(CWD, file));
                }
            }

            // TODO: file present in split point.

            // -- TODO: file present in current branch and given branch.

            // file modified in given branch, but not in current branch.
            // changed version in the given branch and staged.
            for (String file : split_tree) {
                String split_BID = split_tree.getObjID(file);
                if (head_tree.has(file) && merge_tree.has(file)) {
                    String head_BID = head_tree.getObjID(file);
                    String merge_BID = merge_tree.getObjID(file);
                    if (head_BID.equals(split_BID) && !merge_BID.equals(split_BID)) {
                        checkout("", merge_CID, "--", file);
                        Index.addIndex(join(CWD, file));
                    }
                }
            }

            // file modified in current branch, but not in given branch.
            // stay as they are.

            // file modified in current branch and given branch in same way (same contents / both removed).
            // left unchanged.
            // if a file was removed from both the current and given branch, but a file of same name is present in work DIR.
            // not tracked nor staged.

            // -- TODO: file present in current branch not in given branch.

            // file unmodified in the current branch
            // removed (and untracked).
            for (String file : split_tree) {
                String split_BID = split_tree.getObjID(file);
                if (head_tree.has(file) && !merge_tree.has(file)) {
                    String head_BID = head_tree.getObjID(file);
                    if (head_BID.equals(split_BID)) {
                        Index.delIndex(file);
                    }
                }
            }

            // -- TODO: file present in given branch not in current branch.

            // file unmodified in the given branch
            // remain absent.


            // TODO: in conflict

            // present in split point
            // file modified in current branch and given branch in different way.
            // in conflict.
            for (String file : split_tree) {
                String split_BID = split_tree.getObjID(file);
                // present in both the current and given branch
                if (head_tree.has(file) && merge_tree.has(file)) {
                    String head_BID = head_tree.getObjID(file);
                    String merge_BID = merge_tree.getObjID(file);
                    if (!head_BID.equals(split_BID) && !merge_BID.equals(split_BID)) {
                        if (!head_BID.equals(merge_BID)) {
                            Blob merge_blob = readObject(join(OBJECTS_DIR, merge_BID), Blob.class);
                            Blob head_blob = readObject(join(OBJECTS_DIR, head_BID), Blob.class);
                            writeContents(join(CWD, file), contentsOfConflict(head_blob.toString(), merge_blob.toString()));
                            Index.addIndex(join(CWD, file));
                            System.out.println("Encountered a merge conflict.");
                        }
                    }
                }

                // present in current not in given branch
                if (head_tree.has(file) && !merge_tree.has(file)) {
                    String head_BID = head_tree.getObjID(file);
                    if (!head_BID.equals(split_BID)) {
                        Blob head_blob = readObject(join(OBJECTS_DIR, head_BID), Blob.class);
                        writeContents(join(CWD, file), contentsOfConflict(head_blob.toString(), ""));
                        Index.addIndex(join(CWD, file));
                        System.out.println("Encountered a merge conflict.");
                    }
                }

                // present in given not in current branch
                if (!head_tree.has(file) && merge_tree.has(file)) {
                    String merge_BID = merge_tree.getObjID(file);
                    if (!merge_BID.equals(split_BID)) {
                        Blob merge_blob = readObject(join(OBJECTS_DIR, merge_BID), Blob.class);
                        writeContents(join(CWD, file), contentsOfConflict("", merge_blob.toString()));
                        Index.addIndex(join(CWD, file));
                        System.out.println("Encountered a merge conflict.");
                    }
                }
            }

            // not present in split point, present in both the current and given branches.
            // has different contents in the given and current branches.
            for (String file : head_tree) {
                if (!split_tree.has(file) && merge_tree.has(file)) {
                    String head_BID = head_tree.getObjID(file);
                    String merge_BID = merge_tree.getObjID(file);
                    if (!head_BID.equals(merge_BID)) {
                        Blob head_blob = readObject(join(OBJECTS_DIR, head_BID), Blob.class);
                        Blob merge_blob = readObject(join(OBJECTS_DIR, merge_BID), Blob.class);
                        writeContents(join(CWD, file), contentsOfConflict(merge_blob.toString(), head_blob.toString()));
                        Index.addIndex(join(CWD, file));
                        System.out.println("Encountered a merge conflict.");
                    }
                }
            }

            String[] parent = {getHeadCommitID(), getCommitIDByRef(branch)};
            String message = "Merged " + branch +" into " + getHeadRef() +".";
            // generate new root tree.
            Tree root = Index.generateCommitTree();
            // new commit
            Commit merge_commit = new Commit(parent, message, root);
            String objID = addObjToDatabase(merge_commit);
            setHeadCommitID(objID);
            // clean the stage-area.
            Index.clearIndex();

        }
    }

    private static String contentsOfConflict(String current, String merge) {
        StringBuilder builder = new StringBuilder();
        builder.append("<<<<<<< HEAD").append("\n");
        builder.append(current);
        builder.append("=======").append("\n");
        builder.append(merge);
        builder.append(">>>>>>>");
        return builder.toString();
    }

    /**
     * Returns the valid commit.
     */
    public static Set<String> allValidCommits() {
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        Set<String> set = new HashSet<>();
        if (branches != null) {
            for (String ref : branches) {
                String OID = getCommitIDByRef(ref);
                while (OID != null) {
                    Commit commit = readObject(join(OBJECTS_DIR, OID), Commit.class);
                    set.add(OID);
                    if (commit.getParent() != null) {
                        OID = commit.getParent()[0];
                    } else {
                        OID = null;
                    }
                }
            }
        }
        return set;
    }

    /**
     * Returns the graph of branches.
     */
    private static BranchGraph generateBranchGraph() {
        BranchGraph graph = new BranchGraph();

        List<String> files = plainFilenamesIn(BRANCHES_DIR);
        for (String file : files) {
            File f = join(BRANCHES_DIR, file);
            String current_ID = readContentsAsString(f);

            // recursion
            graphHelper(current_ID, graph);
        }

        return graph;
    }

    private static void graphHelper(String current_ID, BranchGraph graph) {
        if (current_ID == null) {
            return;
        }
        Commit commit = readObject(join(OBJECTS_DIR, current_ID), Commit.class);
        String[] parent = commit.getParent();
        if (parent != null) {
            for (String parent_ID : parent) {
                graph.addEdge(current_ID, parent_ID);
                graphHelper(parent_ID, graph);
            }
        }
    }

    public static void printSplitPoint(String OID_1, String OID_2) {
        System.out.println(splitPoint(OID_1, OID_2));
    }

    /**
     * Returns the Split Point of two branch.
     */
    private static String splitPoint(String OID_1, String OID_2) {
        BranchGraph G = generateBranchGraph();
        HashMap<String, Boolean> common = new HashMap<>();
        BFS bfs = new BFS(G, common);
        bfs.bfs(OID_1);
        bfs = new BFS(G, common);
        bfs.bfs(OID_2);

        String closer = null;
        for (String s : common.keySet()) {
            // is common ancestor
            if (common.get(s)) {
                if (closer == null) {
                    closer = s;
                } else {
                    if (bfs.dist(s) < bfs.dist(closer)) {
                        closer = s;
                    }
                }
            }
        }
        return closer;
    }

    /**
     * Cat contents of an SHA-1 object file in objects-database.
     * */
    public static void cat_file(String hashcode) {
        validateObj(hashcode);

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
     * Validate branch
     */
    public static void validateBranch(String branch) {
        File ref_f = join(BRANCHES_DIR, branch);
        if (!ref_f.exists() || !ref_f.isFile()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    /**
     * Add Object file to database.
     */
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
        List<String> branches = plainFilenamesIn(BRANCHES_DIR);
        if (branches == null || !branches.contains(ref)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (getHeadRef().equals(ref)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
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
        String obj_ID = readContentsAsString(join(BRANCHES_DIR, getHeadRef()));
        return readObject(join(OBJECTS_DIR, obj_ID), Commit.class);
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

    /**
     * Returns the commit by ID.
     */
    public static Commit getCommitByID(String ID) {
        File obj_f;
        if (ID.length() == 6 && completeOID(ID) != null) {
            obj_f = join(OBJECTS_DIR, completeOID(ID));
        } else {
            obj_f = join(OBJECTS_DIR, ID);
        }
        if (!obj_f.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return readObject(obj_f, Commit.class);
    }

    /**
     * Returns the commit by branch ref.
     */
    public static Commit getCommitByRef(String ref) {
        File ref_f = join(BRANCHES_DIR, ref);
        if (!ref_f.exists() || !ref_f.isFile()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String OID = readContentsAsString(ref_f);
        return readObject(join(OBJECTS_DIR, OID), Commit.class);
    }

    /**
     * Returns the commit ID by branch ref.
     */
    public static String getCommitIDByRef(String ref) {
        File ref_f = join(BRANCHES_DIR, ref);
        if (!ref_f.exists() || !ref_f.isFile()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        return readContentsAsString(ref_f);
    }

    /**
     * Returns shorten Object ID.
     */
    public static String shortenOID(String OID) {
        return OID.substring(0, 6);
    }

    /**
     * Returns the complete Object ID by short Object ID.
     */
    public static String completeOID(String shortOID) {
        List<String> list = plainFilenamesIn(OBJECTS_DIR);
        if (list != null) {
            for (String filename : list) {
                if (filename.substring(0, 6).equals(shortOID)) {
                    return filename;
                }
            }
        }
        return null;
    }

}
