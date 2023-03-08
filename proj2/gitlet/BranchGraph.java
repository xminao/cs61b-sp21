package gitlet;

import java.util.*;

public class BranchGraph {

    private HashMap<String, List<String>> adjMap;
    private Set<String> vSet;

    public BranchGraph() {
        this.adjMap = new HashMap<>();
        this.vSet = new HashSet<>();
    }

    public void addEdge(String v, String w) {
        vSet.add(v);
        vSet.add(w);

        if (adjMap.containsKey(v)) {
            adjMap.get(v).add(w);
        } else {
            List<String> adjList = new ArrayList<>();
            adjList.add(w);
            adjMap.put(v, adjList);
        }
    }

    public List<String> adj(String v) {
        return adjMap.get(v);
    }

    public int V() {
        return vSet.size();
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
