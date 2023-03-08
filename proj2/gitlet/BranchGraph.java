package gitlet;

import java.util.*;

public class BranchGraph {

    private HashMap<String, Set<String>> adjMap;

    public BranchGraph() {
        this.adjMap = new HashMap<>();
    }

    public void addEdge(String v, String w) {
        if (adjMap.containsKey(v)) {
            adjMap.get(v).add(w);
        } else {
            Set<String> adjList = new HashSet<>();
            adjList.add(w);
            adjMap.put(v, adjList);
        }
    }

    public Set<String> adj(String v) {
        return adjMap.get(v);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String s : adjMap.keySet()) {
            builder.append(Repository.shortenOID(s)).append("-->");
            for (String p : adjMap.get(s)) {
                builder.append(Repository.shortenOID(p)).append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
