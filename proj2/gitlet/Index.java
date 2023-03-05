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
    public enum Status {
        NEW_FILE,
        DELETED,
        MODIFIED
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

    public static void add(String... args) {
        String filename = args[0];

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
        String objID = Repository.hashObj(new Blob(join(Repository.CWD, filename)));

        // file be tracked in HEAD commit.
        if (HEAD_tree.has(filename)) {
            // file modified (not stage if file unmodified)
            if (!HEAD_tree.getObjID(filename).equals(objID)) {
                index_tree.add(filename, Status.MODIFIED, objID);
            } else {
                index_tree.del(filename);
            }
        } else { // file not tracked in HEAD commit.
            index_tree.add(filename, Status.NEW_FILE, objID);
        }

        saveIndexTree(index_tree);
    }

    public static void rm(String filename) {
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
            index_tree.add(filename, Status.DELETED, HEAD_tree.getObjID(filename));
            restrictedDelete(filename);
        }
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
    public static IndexTree getIndexTree() {
        return readObject(INDEX_AREA, IndexTree.class);
    }

    /**
     * Saves tree of index in file.
     */
    public static void saveIndexTree(IndexTree tree) {
        writeObject(INDEX_AREA, tree);
    }

    /**
     * Determine the file is tracked or not.
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

    public static void ls_stage() {
        if (!INDEX_AREA.exists()) {
            System.exit(0);
        }
        // index tree (stage-area tree).
        IndexTree index_tree = getIndexTree();
        System.out.println(index_tree);
    }

    public static String getStatus(String[] record) {
        String str = "";
        if (record[0].equals(Status.NEW_FILE.toString())) {
            str = "new file";
        } else if (record[0].equals(Status.MODIFIED.toString())) {
            str = "modified";
        } else if (record[0].equals(Status.DELETED.toString())) {
            str = "deleted";
        }
        return str;
    }

}
