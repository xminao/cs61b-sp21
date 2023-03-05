package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;
/**
 * Represents the index/cache/stage for repository.
 */
public class Index {
    /**
     * List of status of file.
     */
    public enum State {
        NEW_FILE("new file"),
        DELETED("deleted"),
        MODIFIED("modified"),
        WORKING("working");

        private String name;
        State(String name) {
            this.name = name;
        }
        @Override
        public String toString() {
            return name;
        }
    }

    public static final File INDEX_AREA = Repository.STAGE_AREA;

    public static void initIndex() {
        IndexTree index_tree = new IndexTree();
        try {
            INDEX_AREA.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveIndexTree(index_tree);
    }

    public static void addIndex(File file) {
        String filename = file.getName();

        // initialize index (stage area).
        if (!INDEX_AREA.exists()) {
            initIndex();
        }

        // HEAD commit root tree.
        Tree HEAD_tree = Repository.getHeadCommit().getTree();
        // index tree (stage-area tree).
        IndexTree index_tree = getIndexTree();

        // hash file.
        //String objID = Repository.addObjToDatabase(blob);
        String objID = Repository.hashObj(new Blob(file));

        // file be tracked in HEAD commit.
        if (HEAD_tree.has(filename)) {
            // file modified (not stage if file unmodified)
            if (!HEAD_tree.getObjID(filename).equals(objID)) {
                index_tree.add(filename,  State.MODIFIED, objID);
            } else {
                index_tree.del(filename);
            }
        } else { // file not tracked in HEAD commit.
            index_tree.add(filename, State.NEW_FILE, objID);
        }

        saveIndexTree(index_tree);
    }

    public static void delIndex(String filename) {
        // HEAD commit root tree.
        Tree HEAD_tree = Repository.getHeadCommit().getTree();
        // index tree (stage-area tree).
        IndexTree index_tree = getIndexTree();
        // staged file.
        if (index_tree.has(filename)) {
            index_tree.del(filename);
        }
        // be tracked file in HEAD commit.
        if (HEAD_tree.has(filename)) {
            index_tree.add(filename, State.DELETED, HEAD_tree.getObjID(filename));
            restrictedDelete(filename);
        }

        saveIndexTree(index_tree);
    }

    public static Tree generateIndexTree() {
        return null;
    }

    public static Tree generateCommitTree() {
        return null;
    }

    /**
     * Returns the tree of index.
     */
    private static IndexTree getIndexTree() {
        return readObject(INDEX_AREA, IndexTree.class);
    }

    /**
     * Returns a files' tree of working directory.
     */
    private static IndexTree getWorkingTree() {
        List<String> files = plainFilenamesIn(Repository.CWD);
        IndexTree working_tree = new IndexTree();
        if (files != null) {
            for (String filename : files) {
                File f = join(Repository.CWD, filename);
                String objID = Repository.hashObj(new Blob(f));
                working_tree.add(filename, State.WORKING, objID);
            }
        }
        return working_tree;
    }

    /**
     * Saves tree of index in file.
     */
    private static void saveIndexTree(IndexTree tree) {
        writeObject(INDEX_AREA, tree);
    }

    /**
     * Determine the file is tracked or not.
     * be tacked: staged or in HEAD commit.
     */
    public static boolean isTracked(String filename) {
        // HEAD commit root tree.
        Tree HEAD_tree = Repository.getHeadCommit().getTree();
        // index tree (stage-area tree).
        IndexTree index_tree = getIndexTree();

        if (!HEAD_tree.has(filename) && !index_tree.has(filename)) {
            return false;
        }
        return true;
    }

    /**
     * Returns list of untracked file.
     */
    public static List<String> untrackedList() {
        List<String> work = plainFilenamesIn(Repository.CWD);
        List<String> untracked = new ArrayList<>();
        for (String filename : work) {
            if (!isTracked(filename)) {
                untracked.add(filename);
            }
        }
        return untracked;
    }

    /**
     * Returns list of staged files.
     */
    public static List<String> stagedList() {
        // index tree (stage-area tree).
        IndexTree index_tree = getIndexTree();

        List<String> list = new ArrayList<>();
        for (String filename : index_tree) {
            // not staged for removal
            if (!index_tree.getState(filename).equals(State.DELETED)) {
                list.add(filename);
            }
        }
        return list;
    }

    /**
     * Returns list of removed filesr.
     * removed: tracked in current commit but staged for removal.
     */
    public static List<String> removedList() {
        // index tree (stage-area tree).
        IndexTree index_tree = getIndexTree();

        List<String> list = new ArrayList<>();
        for (String filename : index_tree) {
            // staged for removal
            if (index_tree.getState(filename).equals(State.DELETED)) {
                list.add(filename);
            }
        }
        return list;
    }

    /**
     * Returns list of be tracked but not staged file.
     * Modified Priority: Index > HEAD commit.
     * (means if HEAD commit contains A.txt("AAA"), then changes A.txt("BBB") and staged,
     * status will display 'modified: A.txt', then changes back A.txt("AAA") and staged,
     * status will display 'modified: A.txt' not unmodified.)
     */
    public static List<String> notStagedList() {
        // HEAD commit root tree.
        Tree HEAD_tree = Repository.getHeadCommit().getTree();
        // index tree (stage-area tree).
        IndexTree index_tree = getIndexTree();
        // tree of current working directory.
        IndexTree working_tree = getWorkingTree();
        // index tree for be tracked but not staged files.
        IndexTree not_staged_tree = new IndexTree();

        // deleted
        for (String filename : HEAD_tree) {
            // not staged for removal and deleted from working directory.
            if (!index_tree.has(filename) && !working_tree.has(filename)) {
                not_staged_tree.add(filename, State.DELETED, HEAD_tree.getObjID(filename));
            }
        }

        // modified
        // diff with HEAD commit.
        for (String filename : HEAD_tree) {
            if (working_tree.has(filename)) {
                // tracked in current commit, changed in working dir, but not staged
                if (!working_tree.getObjID(filename).equals(HEAD_tree.getObjID(filename))) {
                    not_staged_tree.add(filename, State.MODIFIED, working_tree.getObjID(filename));
                }
            }
        }
        // diff with Index(staged area).
        for (String filename : index_tree) {
            if (working_tree.has(filename)) {
                // staged but with different contents than in the working dir.
                if (!working_tree.getObjID(filename).equals(index_tree.getObjID(filename))) {
                    not_staged_tree.add(filename, State.MODIFIED, working_tree.getObjID(filename));
                }
            }
        }

        List<String> list = new ArrayList<>();
        for (String filename : not_staged_tree) {
            String name = filename + " (" + not_staged_tree.getState(filename) + ")";
            list.add(name);
        }
        return list;
    }

}
