package dev.fanger.inet.rip.Routing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

import static dev.fanger.inet.rip.Routing.Packet.REQUEST;
import static dev.fanger.inet.rip.Routing.Packet.RESPONSE;

public class Graphic extends JPanel implements ActionListener {
    boolean shouldEnd;
    Timer mainRefresh;
    Thread graphicsThread;
    ArrayList<Node> allNodes;
    ArrayList<Packet> allPackets;
    int numNodes;

    int graphWidth;
    int graphHeight;
    int nodeSize;
    int width, height;

    double ripExecute;

    int connectedness;
    int maxNeighbors;

    boolean ripEnabled;

    JTable routeTable;

    ArrayList<Node> nodesToRip;

    public Graphic(int width, int height, JTable routeTable) {
        connectedness = 1;
        maxNodeDistance = 4;
        maxNeighbors = 3;
        ripExecute = 15;
        BANDWIDTH = BANDWIDTH_100MB;
        nodesToRip = new ArrayList<>();
        this.routeTable = routeTable;
        this.width = width;
        this.height = height;
        setupGraphics();
        setupNodes();
        setRouteTable();
        start();
    }

    public void destoy() {
        shouldEnd = true;
        mainRefresh.stop();
    }

    private void start() {
        mainRefresh = new Timer(17, this);
        mainRefresh.start();
    }

    private void setupGraphics() {
        updateSize(width, height);
        setBackground(Color.white);
        graphicsThread = new Thread(new Drawer());
        graphicsThread.start();
    }

    public void updateSize(int width, int height) {
        this.width = width;
        this.height = height;
        nodeSize = (int) (height / 20.0);
        setSize(this.width, this.height);
    }

    private void setRouteTable() {
        String[] columnNames = new String[allNodes.size()];
        String[][] data = new String[allNodes.size()][allNodes.size()];

        routeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < allNodes.size(); i++) {
            columnNames[i] = "Node " + allNodes.get(i).id;
        }

        for (int i = 0; i < allNodes.size(); i++) {
            Node current = allNodes.get(i);
            for (int j = 0; j < current.routingTable.keySet().size(); j++) {
                Node key = (Node) current.routingTable.keySet().toArray()[j];
                Path currentKeyPath = current.routingTable.get(key);
                data[key.id][current.id] = key.id + " -> " + currentKeyPath.neighborPort.id + " | " + currentKeyPath.cost;
            }

            data[current.id][current.id] = "Self " + " | " + 0;
        }

        DefaultTableModel tableModel = new DefaultTableModel(data, columnNames);
        routeTable.setModel(tableModel);
        routeTable.invalidate();
    }

    private void setupNodes() {
        numNodes = 1;
        graphWidth = 100;
        graphHeight = 100;

        allNodes = new ArrayList<>();
        allPackets = new ArrayList<>();

        //create nodes
        for (int i = 0; i < numNodes; i++) {
            addNode(0);
        }
    }

    public void addNodes(int num) {
        for (int i = 0; i < num; i++) {
            addNode(0);
        }
    }

    int maxNodeDistance;

    private void addNode(int tries) {
        int id = allNodes.size();
        int x = 50;
        int y = 50;

        int counter = 0;

        Node newNode = new Node(id, x, y);

        if (allNodes.size() > 0) {
            int neighborChance = connectedness;

            if (neighborChance > allNodes.size()) {
                neighborChance = allNodes.size();
            }

            ArrayList<Node> possibleNodes = new ArrayList<>();
            for (int i = 0; i < allNodes.size(); i++) {
                if (allNodes.get(i).id != newNode.id) {
                    if (allNodes.get(i).neighbors.size() <= maxNeighbors) {
                        possibleNodes.add(allNodes.get(i));
                    }
                }
            }

            if (neighborChance > possibleNodes.size()) {
                neighborChance = possibleNodes.size();
            }

            ArrayList<Node> neighbors = new ArrayList<>();
            for (int j = 0; j < neighborChance; j++) {
                //setup neighbor
                int randomNeighbor = (int) Math.floor(Math.random() * possibleNodes.size());
                Node theNeighbor = possibleNodes.get(getNeighborID(newNode.id, randomNeighbor));

                //fix x and y to be close to the first neighbor
                if (j == 0) {
                    int newX = x;
                    int newY = y;
                    counter = 0;
                    do {
                        newX = theNeighbor.x + (int) ((Math.floor(Math.random() * (maxNodeDistance)) - maxNodeDistance / 2) * maxNodeDistance * 2.5);
                        newY = theNeighbor.y + (int) ((Math.floor(Math.random() * (maxNodeDistance)) - maxNodeDistance / 2) * maxNodeDistance * 2.5);
                        counter++;
                    }
                    while (containsNode(newX, newY) && counter < 100);

                    newNode.x = newX;
                    newNode.y = newY;
                }

                if (counter < 100) {
                    //setup neighbor connections
                    neighbors.add(theNeighbor);
                    if (!theNeighbor.neighbors.contains(newNode)) {
                        theNeighbor.neighbors.add(newNode);
                    }
                    possibleNodes.remove(theNeighbor);
                }
            }
            newNode.setNeighbors(neighbors);
        }

        if (counter < 100) {
            allNodes.add(newNode);
            nodesToRip.add(newNode);
        } else if (tries < 100) {
            addNode(tries + 1);
        }

        setRouteTable();
    }

    private boolean containsNode(int x, int y) {
        boolean contains = false;

        for (int i = 0; i < allNodes.size(); i++) {
            Node current = allNodes.get(i);
            if (current.getX() == x && current.getY() == y) {
                contains = true;
                break;
            }
        }

        if (x < 10 || x > 80) {
            contains = true;
        }
        if (y < 10 || y > 80) {
            contains = true;
        }

        return contains;
    }

    private int getNeighborID(int current, int offset) {
        int theID = current + offset;
        if (theID >= allNodes.size()) {
            theID -= allNodes.size();
        }

        return theID;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updateTime();

        checkUpload();
    }

    public void sendOneRip() {
        boolean original = ripEnabled;
        ripEnabled = true;
        for (int i = 0; i < allNodes.size(); i++) {
            nodesToRip.add(allNodes.get(i));
        }
        RIP();
        ripEnabled = original;
    }

    public void toggleRIP() {
        ripEnabled = !ripEnabled;
        if (ripEnabled) {
            for (int i = 0; i < allNodes.size(); i++) {
                nodesToRip.add(allNodes.get(i));
            }
            RIP();
        }
    }

    private void updateTime() {
        //will need to change this to checking nmodes own rip times
        if (ripEnabled) {
            for (int i = 0; i < allNodes.size(); i++) {
                Node current = allNodes.get(i);
                current.ripTime += .017;
                if (current.ripTime >= ripExecute) {
                    nodesToRip.add(current);
                    current.ripTime = 0;
                }
            }
        }

        RIP();
    }

    int BANDWIDTH_100MB = 100;
    int BANDWIDTH_1GB = 1000;
    int BANDWIDTH;

    private void checkUpload() {
        //upload packets
        for (int i = 0; i < allPackets.size(); i++) {
            Packet currentPacket = allPackets.get(i);

            boolean finished = currentPacket.upload(BANDWIDTH);
            if (finished) {
                if (currentPacket.type == 0) {//request
                    //send back response containing own routing table
                    Node destination = currentPacket.destination;
                    Node requester = currentPacket.requester;

                    //setup response routing table
                    Path responsePath = new Path(1, destination);
                    HashMap<Node, Path> destRoutingTable = destination.getRoutingTable();
                    HashMap<Node, Path> responseRoutingTable = new HashMap<>();

                    responseRoutingTable.put(destination, responsePath);

                    //add old routing table with new costs to new routing table
                    for (int k = 0; k < destRoutingTable.keySet().size(); k++) {
                        Node key = (Node) destRoutingTable.keySet().toArray()[k];
                        Path currentKeyPath = destRoutingTable.get(key);

                        //increase cost of everything in response routing table for requester cost
                        Path newPath = new Path(currentKeyPath.cost + 1, destination);
                        responseRoutingTable.put(key, newPath);
                    }

                    Packet responsePacket = new Packet(RESPONSE, destination, requester, responseRoutingTable);
                    allPackets.add(responsePacket);
                } else if (currentPacket.type == 1) {//response
                    //add to routing table
                    Node destination = currentPacket.destination;
                    HashMap<Node, Path> newRoutingTable = currentPacket.data;

                    for (int k = 0; k < newRoutingTable.keySet().size(); k++) {
                        Node key = (Node) newRoutingTable.keySet().toArray()[k];
                        Path currentKeyPath = newRoutingTable.get(key);

                        destination.addRoute(currentKeyPath, key);
                    }
                    destination.printRoutingTable();
                }

                allPackets.remove(currentPacket);
                i--;
                setRouteTable();
            }
        }
    }

    class Drawer implements Runnable, ActionListener {
        Timer refresh;

        public Drawer() {
            refresh = new Timer(17, this);
        }

        @Override
        public void run() {
            refresh.start();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            repaint();
            if (shouldEnd) {
                refresh.stop();
            }
        }

    }

    private void RIP() {
        if (ripEnabled) {
            for (int i = 0; i < nodesToRip.size(); i++) {
                Node currentNode = nodesToRip.get(i);

                for (int j = 0; j < currentNode.neighbors.size(); j++) {
                    Node neighbor = currentNode.neighbors.get(j);

                    Packet newRequestPacket = new Packet(REQUEST, currentNode, neighbor, null);
                    allPackets.add(newRequestPacket);
                }

                nodesToRip.remove(i);
            }
        }
    }

    public void sendRandomPacket() {
        Packet randomPacket = new Packet(REQUEST, allNodes.get(0), allNodes.get(1), allNodes.get(0).getRoutingTable());
        allPackets.add(randomPacket);
    }

    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        //draw node connectors
        for (int i = 0; i < allNodes.size(); i++) {
            Node currentNode = allNodes.get(i);

            int currentNodeDrawX = getDrawX(currentNode);
            int currentNodeDrawY = getDrawY(currentNode);

            //draw network connector
            for (int j = 0; j < currentNode.neighbors.size(); j++) {
                Node neighbor = currentNode.neighbors.get(j);
                g2d.drawLine(currentNodeDrawX, currentNodeDrawY, getDrawX(neighbor), getDrawY(neighbor));
            }
        }

        //draw nodes
        for (int i = 0; i < allNodes.size(); i++) {
            Node currentNode = allNodes.get(i);

            int currentNodeDrawX = getDrawX(currentNode);
            int currentNodeDrawY = getDrawY(currentNode);

            //draw node
            g2d.setColor(Color.red);
            g2d.fillOval(currentNodeDrawX - nodeSize / 2, currentNodeDrawY - nodeSize / 2, nodeSize, nodeSize);
            g2d.setColor(Color.white);
            g2d.setFont(new Font("Times", 0, nodeSize * 6 / 8));
            g2d.drawString("" + currentNode.id, currentNodeDrawX - nodeSize / 4, currentNodeDrawY + nodeSize / 3);
            g2d.setColor(Color.black);
            g2d.drawOval(currentNodeDrawX - nodeSize / 2, currentNodeDrawY - nodeSize / 2, nodeSize, nodeSize);
        }

        //draw packets in flight
        for (int i = 0; i < allPackets.size(); i++) {
            Packet current = allPackets.get(i);

            if (current.type == REQUEST) {
                g2d.setColor(Color.green);
            } else if (current.type == RESPONSE) {
                g2d.setColor(Color.blue);
            } else {
                g2d.setColor(Color.red);
            }

            double theX = current.requester.getX() + ((current.destination.getX() - current.requester.getX()) * (current.percentComplete / 100.0));
            double theY = current.requester.getY() + ((current.destination.getY() - current.requester.getY()) * (current.percentComplete / 100.0));
            int scaledX = (int) Math.round(getScaledX(theX));
            int scaledY = (int) Math.round(getScaledY(theY));
            g2d.fillOval(scaledX - nodeSize / 4, scaledY - nodeSize / 4, nodeSize / 2, nodeSize / 2);
        }
    }

    private double getScaledX(double x) {
        double nodeDrawX = ((x / (graphWidth * 1.0)) * width);
        return nodeDrawX;
    }

    private double getScaledY(double y) {
        double nodeDrawX = ((y / (graphHeight * 1.0)) * height);
        return nodeDrawX;
    }

    private int getDrawX(Node theNode) {
        int nodeDrawX = (int) getScaledX(theNode.getX());
        return nodeDrawX;
    }

    private int getDrawY(Node theNode) {
        int nodeDrawY = (int) getScaledY(theNode.getY());
        return nodeDrawY;
    }
}
