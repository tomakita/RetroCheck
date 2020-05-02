package com.retrocheck.graph;

import java.util.*;
import java.util.stream.Collectors;

public class Processor {
    private Preprocessor preprocessor = new Preprocessor();

    public AugmentedGraph process(AugmentedGraph graph, Randomizer r) {

        // we choose edges first, and then nodes, because choosing nodes
        // can remove edges from the graph, and can thus bias the choice of edges.

        List<Edge<?, ?>> chosenEdges = chooseEdges(graph.getEdges(), r);
        chooseNodes(graph.getNodesById(), chosenEdges, r);
        graph.setEdges(chosenEdges);
        graph.setSourceIds(preprocessor.findSources(graph.getNodesById(), chosenEdges));

        return graph;
    }

    public Workflow traverse(AugmentedGraph graph) {
        Map<UUID, List<Edge<?, ?>>> graphMap = preprocessor.makeGraph(new ArrayList<>(graph.getNodesById().values()), graph.getEdges());
        preTraverse(graphMap, graph.getNodesById(), graph.getSourceIds());

        for (UUID sourceId : graph.getSourceIds()) {
            dfs(sourceId, graphMap, graph.getNodesById(), false);
        }

        List<Entity<Object>> dataSetup = makeDataSetup(graph.getNodesById(), graph.getLevelsByNodeId());

        return new Workflow(dataSetup);
    }

    private void chooseNodes(Map<UUID, Node<?>> nodesById, List<Edge<?, ?>> edges, Randomizer r) {
        List<Edge<?, ?>> removedEdges = new ArrayList<>();
        for (Map.Entry<UUID, Node<?>> nodeById : nodesById.entrySet()) {
            UUID id = nodeById.getKey();
            Node<?> node = nodeById.getValue();
            boolean destiny = BucketOfDestiny.fromProbability(node.probability(), r);
            if (!destiny) {
                // TODO: why do we merely nullify these nodes -- why not simply remove them from nodesById, instead?
                //       i think this is a change i should make in the future.  this is an example of NULL leaking out
                //       into the rest of the codebase, where you didn't want it to!
                nodesById.get(id).nullify();

                List<Edge<?, ?>> outEdges = edges.stream().filter(edge -> edge.getLeftEndpoint() == node).collect(Collectors.toList());
                removedEdges.addAll(outEdges);
                List<Edge<?, ?>> inEdges = edges.stream().filter(edge -> edge.getRightEndpoint() == node).collect(Collectors.toList());
                removedEdges.addAll(inEdges);
            }
        }

        edges.removeAll(removedEdges);
    }

    private List<Edge<?, ?>> chooseEdges(List<Edge<?, ?>> edges, Randomizer r) {
        // singleedges should only contain terminal leaf edges, i.e. edges which go from a leaf node to NULL.
        List<Edge<?, ?>> singleedges = edges.stream().filter(edge -> !edge.isMultiedge()).collect(Collectors.toList());
        // This is a LinkedHashMap in order to have a deterministic ordering across multiple invocations of this function with the same seed.
        LinkedHashMap<Optional<String>, List<Edge<?, ?>>> multiedges = edges.stream().filter(edge -> edge.isMultiedge()).collect(Collectors.groupingBy(Edge::containingSetId, LinkedHashMap::new, Collectors.toList()));

        List<Edge<?, ?>> chosenEdges = new ArrayList<>();
        for (Map.Entry<Optional<String>, List<Edge<?, ?>>> multiedge : multiedges.entrySet()) {
            List<Tuple<Edge<?, ?>, Integer>> edgesByProbability = multiedge.getValue().stream().map(edge -> new Tuple<Edge<?, ?>, Integer>(edge, edge.probability())).collect(Collectors.toList());
            BucketOfDestiny<Edge<?, ?>> destiny = BucketOfDestiny.fromProbabilities(edgesByProbability, r);
            Edge<?, ?> chosenEdge = destiny.choose();

            chosenEdges.add(chosenEdge);
        }

        chosenEdges.addAll(singleedges);

        return chosenEdges;
    }

    private List<Entity<Object>> makeDataSetup(Map<UUID, Node<?>> nodesById, Map<UUID, Integer> levelsByNodeId) {
        List<Tuple<UUID, Integer>> levelsAndNodeIds = levelsByNodeId.entrySet().stream().filter(kvp -> !nodesById.get(kvp.getKey()).getName().equals("_")).map(entry -> new Tuple<>(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        Comparator<Tuple<UUID, Integer>> levelComparator = (Comparator<Tuple<UUID, Integer>>) (t1, t2) -> {
            return t1.getSecond() - t2.getSecond();
        };

        Collections.sort(levelsAndNodeIds, levelComparator);

        List<Entity<Object>> dataSetup = new ArrayList<>();
        for (Tuple<UUID, Integer> levelAndNodeId : levelsAndNodeIds) {
            UUID nodeId = levelAndNodeId.getFirst();
            Entity<Object> export = nodesById.get(nodeId).export();
            dataSetup.add(export);
        }

        return dataSetup;
    }

    private void preTraverse(Map<UUID, List<Edge<?, ?>>> graphMap, Map<UUID, Node<?>> nodesById, List<UUID> sourceIds) {
        for (UUID sourceId : sourceIds) {
            preDfs(sourceId, graphMap, nodesById);
        }
    }

    private void preDfs(UUID nodeId, Map<UUID, List<Edge<?, ?>>> graph, Map<UUID, Node<?>> nodesById) {
        Node<?> u = nodesById.get(nodeId);
        if (u.toString().equals("_")) {
            return;
        }

        u.refine();

        List<Edge<?, ?>> outedges = graph.get(u.identify());
        if (outedges == null) {
            return;
        }

        for (Edge<?, ?> edge : outedges) {
            preDfs(edge.identify().getSecond(), graph, nodesById);
        }
    }

    // the input graph is a tree with directed edges, so we don't have to keep track of nodes visited.
    private void dfs(UUID nodeId, Map<UUID, List<Edge<?, ?>>> graph, Map<UUID, Node<?>> nodesById, boolean isConstrained) {
        Node<?> u = nodesById.get(nodeId);
        if (u.toString().equals("_")) {
            return;
        }

        List<Edge<?, ?>> outedges = graph.get(nodeId);
        if (outedges == null) {
            return;
        }

        for (Edge<?, ?> edge : outedges) {
            edge.refine();
            dfs(edge.identify().getSecond(), graph, nodesById, edge.isConstrained());
        }
    }
}
