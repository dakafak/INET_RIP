package dev.fanger.inet.rip.Routing;

import static dev.fanger.inet.rip.Tools.EMath.getDistance;

import java.util.HashMap;

public class Packet {

    static int REQUEST = 0;
    static int RESPONSE = 1;

    int packetSize = 1000;
    int type;//0 request, 1 response
    HashMap<Node, Path> data;//currently the hashmap for routing table only for this program example
    Node destination;
    Node requester;
    double percentComplete;

    public Packet(int type, Node req, Node dest, HashMap<Node, Path> data) {
        this.type = type;
        this.requester = req;
        this.destination = dest;
        if (data != null) {
            this.data = data;
        } else {
            data = new HashMap<>();
        }

        percentComplete = 0;
        packetSize = 500 + (1 + data.keySet().size()) * 25;
    }

    public boolean upload(double bandwidth) {//bandwidth in mbps
        double distance = getDistance(requester.getX() * 1.0, requester.getY() * 1.0, destination.getX() * 1.0, destination.getY() * 1.0);

		/*
			currently this scales with both packet size and distance delay

			this could be changed to only distance delay but the idea is how long to
				completely send all data for the packet
				packets really should be the same size anyway.... 
				extra functionality closer to routers should just split data into packets of similar size 
				so a bunch of packets are sent

			bandwidth could potentially be removed from this and only go off of delay...
		*/
        double tickAmount = (((bandwidth * 1000) / 60) / packetSize) / (distance / 10);

        percentComplete += tickAmount;

        if (percentComplete > 100) {
            percentComplete = 100;
            return true;
        } else {
            return false;
        }
    }

}
