package server;

import file.FileDescription;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

public class ServerData implements Serializable{

    public FileDescription fileDescription;
    public Hashtable<Integer, ArrayList<String>> peerIPs;
    public Hashtable<Integer, ArrayList<Integer>> peerPorts;

    public ServerData(FileDescription fileDescription, String peerIp, int peerPort) {
        this.fileDescription = fileDescription;
        this.peerIPs = new Hashtable<Integer, ArrayList<String>>();
        this.peerPorts = new Hashtable<Integer, ArrayList<Integer>>();
        for(int i = 0; i < fileDescription.getSequenceLength(); i++) {
            ArrayList<String> pIP = new ArrayList<String>();
            ArrayList<Integer> pPort = new ArrayList<Integer>();
            pIP.add(peerIp);
            pPort.add(peerPort);
            peerIPs.put(i, pIP);
            peerPorts.put(i, pPort);
        }
    }

    public void addPeerIp(String peerIp, int sequence) {
        ArrayList<String> pIP = peerIPs.get(sequence);
        pIP.add(peerIp);
        peerIPs.put(sequence, pIP);
    }

    public void addPeerPort(int peerPort, int sequence) {
        ArrayList<Integer> pPort = peerPorts.get(sequence);
        pPort.add(peerPort);
        peerPorts.put(sequence, pPort);
    }

    public void setFileDescription(FileDescription fileDescription) {
        this.fileDescription = fileDescription;
    }

    public FileDescription getFileDescription() {
        return fileDescription;
    }

    public void setPeerIPs(Hashtable<Integer, ArrayList<String>> peerIPs) {
        this.peerIPs = peerIPs;
    }

    public Hashtable<Integer, ArrayList<String>> getPeerIPs() {
        return peerIPs;
    }

    public void setPeerPorts(Hashtable<Integer, ArrayList<Integer>> peerPorts) {
        this.peerPorts = peerPorts;
    }

    public Hashtable<Integer, ArrayList<Integer>> getPeerPorts() {
        return peerPorts;
    }
}
