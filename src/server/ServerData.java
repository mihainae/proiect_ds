package server;

import file.FileDescription;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;

public class ServerData implements Serializable{

    public FileDescription fileDescription;
    public ArrayList<String> peerIPs;
    public ArrayList<Integer> peerPorts;

    public ServerData(FileDescription fileDescription, String peerIp, int peerPort) {
        this.fileDescription = fileDescription;
        this.peerIPs = new ArrayList<String>();
        this.peerPorts = new ArrayList<Integer>();
        peerIPs.add(peerIp);
        peerPorts.add(peerPort);
    }

    public void addPeerIp(String peerIp) {
        peerIPs.add(peerIp);
    }

    public void addPeerPort(int peerPort) {
        peerPorts.add(peerPort);
    }

    public void setFileDescription(FileDescription fileDescription) {
        this.fileDescription = fileDescription;
    }

    public FileDescription getFileDescription() {
        return fileDescription;
    }

    public void setPeerIPs(ArrayList<String> peerIPs) {
        this.peerIPs = peerIPs;
    }

    public ArrayList<String> getPeerIPs() {
        return peerIPs;
    }

    public void setPeerPorts(ArrayList<Integer> peerPorts) {
        this.peerPorts = peerPorts;
    }

    public ArrayList<Integer> getPeerPorts() {
        return peerPorts;
    }
}
