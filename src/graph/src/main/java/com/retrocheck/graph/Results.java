package com.retrocheck.graph;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Results {
    private String title = "";
    private List<Result> results = new ArrayList<>();

    public Results() {}

    public Results(String title) {
        this.title = title;
    }

    public void add(Result result) {
        results.add(result);
    }

    public void writeToFile() {
        // TODO: how to label edges with constraints?  for now just using probability as label...

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            // exclude diagnostic graph
            results.remove(1);

            List<List<CyNode>> cyNodeVersions = results.stream().map(nodesAndEdges -> nodesAndEdges.getGraphChoice().getNodes()).collect(Collectors.toList());
            List<List<CyEdge>> cyEdgeVersions = results.stream().map(nodesAndEdges -> nodesAndEdges.getGraphChoice().getEdges()).collect(Collectors.toList());
            List<String> outcomeVersions = results.stream().map(result -> result.getCyWorkflow() == null ? null : result.getCyWorkflow().getOutcome()).collect(Collectors.toList());
            List<List<CyWorkflow.Data>> workflowVersions = results.stream().map(result -> result.getCyWorkflow() == null ? null : result.getCyWorkflow().getWorkflow()).collect(Collectors.toList());

            String nodes = mapper.writeValueAsString(cyNodeVersions);
            //System.out.println(nodes);

            String edges = mapper.writeValueAsString(cyEdgeVersions);
            //System.out.println(edges);

            String outcomes = mapper.writeValueAsString(outcomeVersions);

            String workflows = mapper.writeValueAsString(workflowVersions);

            String graphTitle = mapper.writeValueAsString(title);

            String js = getResource("/graph_visualization.js");
            String jsWithNodes = js.replace("$ NODES DOIN THANGS $", nodes);
            String jsWithEdges = jsWithNodes.replace("$ EDGES DOIN THANGS $", edges);
            String jsWithOutcomes = jsWithEdges.replace("$ OUTCOMES DOIN THANGS $", outcomes);
            String jsWithWorkflows = jsWithOutcomes.replace("$ WORKFLOWS DOIN THANGS $", workflows);
            String jsWithTitle = jsWithWorkflows.replace("$ TITLE DOIN THANGS $", graphTitle);

            writeFile("graph_visualization.js", jsWithTitle);
            writeFile("graph_visualization.html", getResource("/graph_visualization.html"));
            writeFile("graph_visualization.css", getResource("/graph_visualization.css"));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getResource(String path) {
        InputStream stream = getClass().getResourceAsStream(path);
        Scanner scanner = new Scanner(stream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private void writeFile(String fileName, String contents) throws IOException {
        File file = new File(fileName);
        file.createNewFile();
        Files.write(file.toPath(), Collections.singleton(contents), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
