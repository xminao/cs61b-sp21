package gitlet;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Queue;

public class BFS {
    private HashMap<String, Boolean> marked;

    public BFS(BranchGraph graph) {
        marked = new HashMap<>();
    }

    private void bfs(BranchGraph graph, String str) {
        Queue<String> queue = new ArrayDeque<>();
        marked.put(str, true);
        queue.add(str);
        while (!queue.isEmpty()) {
            String s = queue.poll();
            for (String w : graph.adj(s)) {
                if (!isMarked(w)) {
                    marked.put(w, true);
                    queue.add(w);
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
}
