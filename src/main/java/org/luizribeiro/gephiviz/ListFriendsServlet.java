package org.luizribeiro.gephiviz;

import com.restfb.DefaultFacebookClient;
import com.restfb.Facebook;
import com.restfb.FacebookClient;
import com.restfb.types.User;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.labelAdjust.LabelAdjust;
import org.gephi.layout.plugin.labelAdjust.LabelAdjustBuilder;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.plugin.NodeColorTransformer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.plugin.transformer.AbstractSizeTransformer;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;

class Friend {

    @Facebook("uid2")
    String uid;
}

class MutualFriend {

    @Facebook
    String uid1;
    @Facebook
    String uid2;
}

class Name {

    @Facebook
    String uid;
    @Facebook
    String name;
}

class MultiqueryResults {

    @Facebook
    List<Friend> friends;
    @Facebook("mutualfriends")
    List<MutualFriend> mutualFriends;
    @Facebook
    List<Name> names;
}

public class ListFriendsServlet extends HttpServlet {

    private static final String ACCESS_TOKEN = "AAACEdEose0cBAF8bYkrspZAGEpHm3XQ3tuArTu6kLShhRTduOzpv6pXtyvGuZBK6K82wH5h9nWCeZBEvtOZB7zmNyOaZCQXZBrArmvEpakMAZDZD";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("image/png");
        ServletOutputStream output = response.getOutputStream();
        try {
            // setup facebook client
            String accessToken = request.getParameter("access_token");
            if (accessToken == null) {
                accessToken = ACCESS_TOKEN;
            }
            FacebookClient client = new DefaultFacebookClient(ACCESS_TOKEN);

            // setup Gephi
            ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
            projectController.newProject();
            Workspace workspace = projectController.getCurrentWorkspace();

            // get models and controllers for the newly created workspace
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
            AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
            LayoutController layoutController = Lookup.getDefault().lookup(LayoutController.class);
            PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
            RankingController rankingController = Lookup.getDefault().lookup(RankingController.class);
            PreviewModel previewModel = Lookup.getDefault().lookup(PreviewController.class).getModel();
            ExportController exportController = Lookup.getDefault().lookup(ExportController.class);

            // grab user information
            User user = client.fetchObject("me", User.class);

            // run FQL multiquery and prepare data
            Map<String, String> queries = new HashMap<String, String>();
            queries.put("friends", "SELECT uid2 FROM friend WHERE uid1=" + user.getId());
            queries.put("mutualfriends", "SELECT uid1, uid2 FROM friend WHERE uid1 IN (SELECT uid2 FROM #friends) AND uid2 IN (SELECT uid2 FROM #friends)");
            queries.put("names", "SELECT uid, name FROM user WHERE uid in (SELECT uid2 FROM #friends)");
            MultiqueryResults multiqueryResults = client.executeMultiquery(queries, MultiqueryResults.class);

            // build graph from multiquery results
            // create nodes
            Map<String, Node> nodes = new HashMap<String, Node>();
            for (Friend friend : multiqueryResults.friends) {
                Node newNode = graphModel.factory().newNode();
                graphModel.getUndirectedGraph().addNode(newNode);
                nodes.put(friend.uid, newNode);
            }

            // put labels on nodes
            for (Name name : multiqueryResults.names) {
                Node node = nodes.get(name.uid);
                node.getNodeData().setLabel(name.name);
            }

            // create edges
            for (MutualFriend mutualFriend : multiqueryResults.mutualFriends) {
                Node nodeA = nodes.get(mutualFriend.uid1);
                Node nodeB = nodes.get(mutualFriend.uid2);
                graphModel.getUndirectedGraph().addEdge(nodeA, nodeB);
            }

            // compute betweenness centrality
            GraphDistance graphDistance = new GraphDistance();
            graphDistance.setDirected(false);
            graphDistance.execute(graphModel, attributeModel);

            // rank size by centrality
            AttributeColumn centralityColumn = attributeModel.getNodeTable().getColumn(GraphDistance.BETWEENNESS);
            Ranking centralityRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, centralityColumn.getId());
            AbstractSizeTransformer sizeTransformer = (AbstractSizeTransformer) rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE);
            sizeTransformer.setMinSize(10);
            sizeTransformer.setMaxSize(50);
            rankingController.transform(centralityRanking, sizeTransformer);

            // compute modularity
            Modularity modularity = new Modularity();
            modularity.execute(graphModel, attributeModel);

            // partition by modularity class
            AttributeColumn modularityColumn = attributeModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
            Partition partition = partitionController.buildPartition(modularityColumn, graphModel.getGraph());
            NodeColorTransformer nodeColorTransformer = new NodeColorTransformer();
            nodeColorTransformer.randomizeColors(partition);
            partitionController.transform(partition, nodeColorTransformer);

            // run YifanHu layout for 250 iterations
            YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
            layout.setGraphModel(graphModel);
            layout.resetPropertiesValues();
            layout.setOptimalDistance(400f);
            layout.initAlgo();
            for (int i = 0; i < 250 && layout.canAlgo(); i++) {
                layout.goAlgo();
            }

            // setup preview
            previewModel.getProperties().putValue(PreviewProperty.SHOW_NODE_LABELS, Boolean.TRUE);
            previewModel.getProperties().putValue(PreviewProperty.EDGE_THICKNESS, new Float(0.1f));

            // export to PNG
            PNGExporter pngExporter = (PNGExporter) exportController.getExporter("png");
            pngExporter.setWidth(2048);
            pngExporter.setHeight(2048);
            exportController.exportStream(output, pngExporter);
        } finally {
            output.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
