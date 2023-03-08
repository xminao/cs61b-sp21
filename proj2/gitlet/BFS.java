package gitlet;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

public class BFS {
    private BranchGraph graph;
    private HashMap<String, Boolean> marked;
    private HashMap<String, Boolean> common;
    // distance between node and root.
    private HashMap<String, Integer> distTo;

    public BFS(BranchGraph graph, HashMap<String, Boolean> common) {
        this.graph = graph;
        this.marked = new HashMap<>();
        this.common = common;
        this.distTo = new HashMap<>();
    }

    public void bfs(String str) {
        helper(graph, str);
    }

    private void helper(BranchGraph graph, String str) {
        Queue<String> queue = new ArrayDeque<>();
        marked.put(str, true);
        if (common.containsKey(str) && !common.get(str)) {
            common.put(str, true);
        } else {
            common.put(str, false);
        }
        distTo.put(str, 0);
        queue.add(str);
        while (!queue.isEmpty()) {
            String s = queue.poll();
            if (graph.adj(s) != null) {
                for (String w : graph.adj(s)) {
                    if (!isMarked(w)) {
                        marked.put(w, true);
                        if (common.containsKey(w) && !common.get(w)) {
                            common.put(w, true);
                        } else {
                            common.put(w, false);
                        }
                        distTo.put(w, distTo.get(s) + 1);
                        queue.add(w);
                    }
                }
            }
        }
    }

    private boolean isMarked(String s) {
        if (marked.containsKey(s) && marked.get(s)) {
            return true;
        }
        return false;
    }

    public int dist(String OID) {
        return distTo.get(OID);
    }
}
