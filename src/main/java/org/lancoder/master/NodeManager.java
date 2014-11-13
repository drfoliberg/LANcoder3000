package org.lancoder.master;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.lancoder.common.Node;
import org.lancoder.common.events.Event;
import org.lancoder.common.events.EventEnum;
import org.lancoder.common.events.EventListener;
import org.lancoder.common.network.cluster.messages.ConnectRequest;
import org.lancoder.common.network.cluster.messages.ConnectResponse;
import org.lancoder.common.status.NodeState;
import org.lancoder.common.task.ClientTask;
import org.lancoder.common.task.video.ClientVideoTask;

public class NodeManager {

	private final static int FAILURE_THRESHOLD = 10;

	private EventListener listener;
	private MasterConfig masterConfig;
	private final HashMap<String, Node> nodes = new HashMap<>();

	public NodeManager(EventListener listener, MasterConfig masterConfig, MasterSavedInstance instance) {
		this.listener = listener;
		this.masterConfig = masterConfig;
		if (instance != null) {
			nodes.putAll(instance.getNodes());
		}
	}

	public HashMap<String, Node> getNodeHashMap() {
		return this.nodes;
	}

	/**
	 * Returns a node object from a node id
	 * 
	 * @param nodeId
	 *            The node ID to get
	 * @return The node object or null if not found
	 */
	public Node identifySender(String nodeId) {
		Node n = this.nodes.get(nodeId);
		if (n == null) {
			System.err.printf("WARNING could not FIND NODE %s\n" + "Size of nodesByUNID: %d\n"
					+ "Size of nodes arraylist:%d\n", nodeId, nodes.size(), nodes.size());
		}
		return n;
	}

	public synchronized ArrayList<Node> getNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Entry<String, Node> e : this.nodes.entrySet()) {
			nodes.add(e.getValue());
		}
		return nodes;
	}

	public synchronized ArrayList<Node> getOnlineNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Entry<String, Node> e : this.nodes.entrySet()) {
			Node n = e.getValue();
			if (n.getStatus() != NodeState.PAUSED && n.getStatus() != NodeState.NOT_CONNECTED && !n.isLocked()) {
				nodes.add(n);
			}
		}
		return nodes;
	}

	/**
	 * Get a list of nodes currently completely free. Video tasks will use all threads.
	 * 
	 * @return A list of nodes that can accept a video task
	 */
	public synchronized ArrayList<Node> getFreeVideoNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Node node : this.getOnlineNodes()) {
			if (node.getStatus() == NodeState.FREE) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	/**
	 * Get a list of nodes that can encode audio. Audio tasks only need one thread.
	 * 
	 * @return A list of nodes that can accept an audio task
	 */
	public synchronized ArrayList<Node> getFreeAudioNodes() {
		ArrayList<Node> nodes = new ArrayList<>();
		for (Node node : this.getOnlineNodes()) {
			if (isAvailable(node)) {
				nodes.add(node);
			}
		}
		return nodes;
	}

	private boolean isAvailable(Node node) {
		boolean nodeAvailable = true;
		// TODO allow dynamic failure threshold
		if (node.getStatus() != NodeState.WORKING && node.getStatus() != NodeState.FREE
				&& node.getFailureCount() < FAILURE_THRESHOLD) {
			for (ClientTask task : node.getCurrentTasks()) {
				if (task instanceof ClientVideoTask) {
					nodeAvailable = false;
					break;
				}
			}
			// TODO check for each task the task's thread requirements
			nodeAvailable = nodeAvailable && node.getCurrentTasks().size() < node.getThreadCount();
		}
		return nodeAvailable;
	}

	/**
	 * Adds a node to the node list. Assigns a new ID to the node if it's non-existent. The node will be picked up by
	 * the node checker automatically if work is available.
	 * 
	 * @param n
	 *            The node to be added
	 * @return if the node could be added
	 */
	public synchronized boolean addNode(Node n) {
		boolean success = true;
		// Is this a new node ?
		if (n.getUnid() == null || n.getUnid().equals("")) {
			n.setUnid(getNewUNID(n));
		}
		Node masterInstance = nodes.get(n.getUnid());
		if (masterInstance != null && masterInstance.getStatus() == NodeState.NOT_CONNECTED) {
			// Node with same unid reconnecting
			masterInstance.setStatus(NodeState.FREE);
			System.out.printf("Node %s with id %s reconnected.%n", n.getName(), n.getUnid());
		} else if (masterInstance == null) {
			n.setStatus(NodeState.FREE);
			nodes.put(n.getUnid(), n);
			System.out.printf("Added new node %s with id %s.%n", n.getName(), n.getUnid());
		} else {
			success = false;
		}
		if (success) {
			listener.handle(new Event(EventEnum.WORK_NEEDS_UPDATE));
		}
		return success;
	}

	/**
	 * Set disconnected status to node and cancel node's tasks. Use shutdownNode() to gracefully shutdown a node.
	 * 
	 * 
	 * @param n
	 *            The node to disconnect
	 */
	public synchronized void removeNode(Node n) {
		if (n != null) {
			n.setStatus(NodeState.NOT_CONNECTED);
			// Cancel node's tasks status if any
			for (ClientTask t : n.getCurrentTasks()) {
				t.getProgress().reset();
			}
			n.getCurrentTasks().clear();
			listener.handle(new Event(EventEnum.WORK_NEEDS_UPDATE));
		} else {
			System.err.println("Could not mark node as disconnected as it was not found");
		}
	}

	private String getNewUNID(Node n) {
		String algorithm = Master.ALGORITHM;
		String result = "";
		System.out.println("MASTER: generating a unid for node " + n.getName());
		long ms = System.currentTimeMillis();
		String input = ms + n.getName();
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			// print and handle exception
			// if a null string is given back to the client, it won't connect
			e.printStackTrace();
			System.out.printf("Could not get an instance of %s to produce a UNID.%n");
			return "";
		}
		byte[] byteArray = md.digest(input.getBytes());
		result = "";
		for (int i = 0; i < byteArray.length; i++) {
			result += Integer.toString((byteArray[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	public ConnectResponse connectRequest(ConnectRequest cm, InetAddress detectedIp) {
		String unid = null;
		Node sender = cm.getNode();
		sender.setNodeAddress(detectedIp);
		sender.setUnid(cm.getUnid());
		if (this.addNode(sender)) {
			unid = sender.getUnid();
		}
		return new ConnectResponse(unid, "http", masterConfig.getApiServerPort());
	}

	public void disconnectRequest(ConnectRequest cm) {
		Node n = this.identifySender(cm.getUnid());
		this.removeNode(n);
	}

}
