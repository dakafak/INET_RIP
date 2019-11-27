/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dev.fanger.inet.rip.Routing;

/**
 *
 * @author Dakafak
 */
public class Path {
	int cost;
	Node neighborPort;
	
	public Path(int cost, Node destination){
		this.cost = cost;
		this.neighborPort = destination;
	}
}
