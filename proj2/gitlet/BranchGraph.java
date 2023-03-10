package gitlet;

import java.lang.invoke.StringConcatFactory;
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

    public Set<String> allCommit() {
        Set<String> all = new HashSet<>();
        for (String s : adjMap.keySet()) {
            all.add(s);
            for (String t : adjMap.get(s)) {
                all.add(t);
            }
        }
        return all;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String s : adjMap.keySet()) {
            builder.append(Repository.shortenOID(s, 7)).append("-->");
            for (String p : adjMap.get(s)) {
                builder.append(Repository.shortenOID(p, 7)).append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }
}
