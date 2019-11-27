package dev.fanger.inet.rip.Routing;

import java.util.ArrayList;
import java.util.HashMap;

public class Node {
    int id;
    int x, y;
    double ripTime;
    ArrayList<Node> neighbors;
    HashMap<Node, Path> routingTable;

    public Node(int id, int x, int y) {
        ripTime = 0;
        this.id = id;
        this.x = x;
        this.y = y;
        neighbors = new ArrayList<>();
        routingTable = new HashMap<>();
    }

    void addRoute(Path newPath, Node to) {
        if (to.id != id) {
            if (routingTable.containsKey(to)) {
                int oldCost = routingTable.get(to).cost;
                int newCost = newPath.cost;
                if (newCost < oldCost) {
                    routingTable.replace(to, newPath);
                }
            } else {
                routingTable.put(to, newPath);
            }
        }
    }

    void printRoutingTable() {
        for (int i = 0; i < routingTable.keySet().size(); i++) {
            Node key = (Node) routingTable.keySet().toArray()[i];
            Path currentKeyPath = routingTable.get(key);
        }
    }

    boolean containsNode(int id) {
        boolean contains = false;
        for (int i = 0; i < routingTable.keySet().size(); i++) {
            Node key = (Node) routingTable.keySet().toArray()[i];

            if (key.id == id) {
                contains = true;
                break;
            }
        }
        return contains;
    }

    HashMap<Node, Path> getRoutingTable() {
        return routingTable;
    }

    public void setNeighbors(ArrayList<Node> n) {
        this.neighbors = n;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
