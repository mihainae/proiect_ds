package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import file.FileDescription;

public class Server {

    public ServerSocket serverSocket;
    private ArrayList<String> messages;
    private ReentrantLock peersLock;
    private ArrayList<ObjectOutputStream> peers;
    private ReentrantLock sendLock;
    public String serverIp;
    public int serverPort;
    public Hashtable<String, ServerData> resources;

    public Server(String serverIp, int serverPort /*, ArrayList<Integer> peerPorts*/) throws IOException {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        serverSocket = new ServerSocket(serverPort);
        messages = new ArrayList<String>();

        peersLock = new ReentrantLock();
        new ReentrantLock();
        sendLock = new ReentrantLock();
        peers = new ArrayList<ObjectOutputStream>();

        resources = new Hashtable<String, ServerData>();

        /*
        for(int i = 0; i < peerPorts.size(); i++) {
            Socket socket = new Socket("localhost", peerPorts.get(i));
            new ServerThread(socket);
        }
        */

        new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {
                        new ServerThread(serverSocket.accept());
                    }
                } catch (IOException e) {
                }
            }
        }).start();

        System.out.println("Am iesit!");
    }

    class ServerThread extends Thread {

        private final Socket socket;
        private ObjectOutputStream out;

        public ServerThread(Socket socket) {
            this.socket = socket;
            start();
        }

        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());

                //out.writeObject(String.valueOf(serverSocket.getLocalPort()));

                peersLock.lock();
                try {
                    peers.add(out);
                } finally {
                    peersLock.unlock();
                }

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                //int port = Integer.parseInt((String) in.readObject());

                while (true) {

                    FileDescription message = (FileDescription) in.readObject();
                    sendLock.lock();
                    //messages.add(message);
                    System.out.println("Received message: " + message.getFileName() + " " + message.getClientPort() + " " + message.getType());
                    if(message.getType() == 0) { //publish a file
                        if(resources.get(message.getFileName()) == null) {
                            ServerData serverData = new ServerData(message, message.getClientIp(), message.getClientPort());
                            resources.put(message.getFileName(), serverData);
                        }
                        else {
                            ServerData serverData = resources.get(message.getFileName());
                            for(int i = 0; i <= message.getSequenceLength(); i++) {
                                serverData.addPeerIp(message.getClientIp(), i);
                                serverData.addPeerPort(message.getClientPort(), i);
                            }
                        }
                        out.writeObject("ACK");
                    }
                    else {
                        if(message.getType() == 1) { //query for a file
                            String fileName = message.getFileName();
                            System.out.println("Filename: " + fileName);
                            if(resources.get(fileName) != null) {
                                out.writeObject(resources.get(fileName));
                            }
                        }
                        else {
                            if(message.getType() == 2) { //chunk was downloaded
                                String fileName = message.getFileName();
                                int sequenceNumber = message.getSequenceNumber();
                                //System.out.println("Received sequence: " + sequenceNumber);
                                ServerData serverData = resources.get(fileName);
                                serverData.addPeerIp(message.getClientIp(), sequenceNumber);
                                serverData.addPeerPort(message.getClientPort(), sequenceNumber);
                            }
                        }
                    }


                    sendLock.unlock();
                }
            } catch (java.net.SocketException t) {
                peersLock.lock();
                System.out.print(t.getMessage());
                try {
                    peers.remove(out);
                } finally {
                    peersLock.unlock();
                }
                /*try {
                    out.close();
                    socket.close();
                    System.exit(1);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) throws IOException {
        Server server = new Server("localhost", 3000);

    }
}
