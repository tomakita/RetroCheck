var nodeVersions = $ NODES DOIN THANGS $
var edgeVersions = $ EDGES DOIN THANGS $
var outcomes = $ OUTCOMES DOIN THANGS $
var workflows = $ WORKFLOWS DOIN THANGS $
var graphTitle = $ TITLE DOIN THANGS $

var pageTitle = document.getElementById("page-title")
pageTitle.innerHTML = graphTitle

var graphVersion = 0;

var rootSubgraphName = "ROOT";
var subgraphNames = new Set(nodeVersions[0].map(n => n.data.subgraph));
var subgraphNamesArray = [...subgraphNames];
if (!subgraphNames.has(rootSubgraphName)) {
    subgraphNamesArray.unshift(rootSubgraphName);
}

for (var i = 0; i < subgraphNamesArray.length; i++) {
    var selected = i === 0;
    console.log(subgraphNamesArray[i]);
    document.getElementById("subgraph-view").appendChild(new Option(subgraphNamesArray[i], i, selected, selected))
}

function changeSubgraph(i) {
    var subgraphName = subgraphNamesArray[i];

    if (subgraphName === rootSubgraphName) {
        // refresh with all nodes and edges
        refreshGraph(nodeVersions[graphVersion], edgeVersions[graphVersion], workflows[graphVersion]);
        return;
    }

    // filter nodes
    var componentNodes = nodeVersions[graphVersion].filter(node => node.data.subgraph === subgraphName);
    var componentNodeIds = componentNodes.map(node => node.data.id);

    // filter edges
    var componentEdges = edgeVersions[graphVersion].filter(edge => componentNodeIds.includes(edge.data.source));

    // filter workflow
    var componentWorkflow = null;
    if (outcomes[graphVersion]) {
        componentWorkflow = workflows[graphVersion].filter(workflow => componentNodeIds.includes(workflow.id));
    }

    refreshGraph(componentNodes, componentEdges, componentWorkflow);
}

for (var i = 0; i < nodeVersions.length; i++) {
    var selected = i === 0;
    document.getElementById("graph-view").appendChild(new Option(i, i, selected, selected))
}

function refreshGraph(nodes, edges, workflow) {
    // changing the graph
    cy_r.elements().remove();

    var elements = {
        nodes: nodes,
        edges: edges
    }

    cy_r.add(elements);
    cy_r.layout({name: 'dagre', spacingFactor: 2}).run();

    // changing the table
    if (!outcomes[graphVersion]) {
        return;
    }

    var tableNode = document.getElementById("workflow-table");
    tableNode.innerHTML = "";
    //var outcomeNode = document.createTextNode("OUTCOME: " + outcomes[i]);
    //document.getElementById("workflow").insertBefore(outcomeNode, tableNode);
    for (var i = 0; i < workflow.length; i++) {
        var row = tableNode.insertRow(-1);
        var entityName = row.insertCell(0);
        var entityType = row.insertCell(1);
        var entityValue = row.insertCell(2);
        entityName.innerHTML = workflow[i].instanceName;
        entityType.innerHTML = workflow[i].entity;
        entityValue.innerHTML = workflow[i].instance;
    }
}

function changeGraph(i) {
    graphVersion = i;
    document.getElementById("subgraph-view").selectedIndex = 0;
    refreshGraph(nodeVersions[i], edgeVersions[i], workflows[i]);
}

var cy_r = cytoscape({
    container: document.getElementById('cy_r'),

    boxSelectionEnabled: false,
    autounselectify: true,

    style: cytoscape.stylesheet()
        .selector('node')
        .css({
            'content': 'data(name)',
            'width': 100,
            'height': 100,
            'font-size': 30,
            'background-color': '#4eff84'
        })
        .selector('edge')
        .css({
            'content': 'data(probability)',
            'curve-style': 'bezier',
            'font-size': 20,
            'target-arrow-shape': 'triangle',
            'width': 10,
            'line-color': '#ddd',
            'target-arrow-color': '#ddd',
            'control-point-step-size': 125
        })
        .selector('.highlighted')
        .css({
            'background-color': '#9e7aff',
            'line-color': '#9e7aff',
            'target-arrow-color': '#9e7aff',
            'transition-property': 'background-color, line-color, target-arrow-color',
            'transition-duration': '0.5s'
        }),

    elements: {
        nodes: nodeVersions[0],
        edges: edgeVersions[0]
    },

    layout: {
        name: 'dagre',
        spacingFactor: 2
    }
});

// DAGRE LAYOUT OPTIONS:
//var defaults = {
//  // dagre algo options, uses default value on undefined
//  nodeSep: undefined, // the separation between adjacent nodes in the same rank
//  edgeSep: undefined, // the separation between adjacent edges in the same rank
//  rankSep: undefined, // the separation between each rank in the layout
//  rankDir: undefined, // 'TB' for top to bottom flow, 'LR' for left to right,
//  ranker: undefined, // Type of algorithm to assign a rank to each node in the input graph. Possible values: 'network-simplex', 'tight-tree' or 'longest-path'
//  minLen: function( edge ){ return 1; }, // number of ranks to keep between the source and target of the edge
//  edgeWeight: function( edge ){ return 1; }, // higher weight edges are generally made shorter and straighter than lower weight edges
//
//  // general layout options
//  fit: true, // whether to fit to viewport
//  padding: 30, // fit padding
//  spacingFactor: undefined, // Applies a multiplicative factor (>0) to expand or compress the overall area that the nodes take up
//  nodeDimensionsIncludeLabels: false, // whether labels should be included in determining the space used by a node
//  animate: false, // whether to transition the node positions
//  animateFilter: function( node, i ){ return true; }, // whether to animate specific nodes when animation is on; non-animated nodes immediately go to their final positions
//  animationDuration: 500, // duration of animation in ms if enabled
//  animationEasing: undefined, // easing of animation if enabled
//  boundingBox: undefined, // constrain layout bounds; { x1, y1, x2, y2 } or { x1, y1, w, h }
//  transform: function( node, pos ){ return pos; }, // a function that applies a transform to the final node position
//  ready: function(){}, // on layoutready
//  stop: function(){} // on layoutstop
//};