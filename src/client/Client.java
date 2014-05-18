package client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

import file.FileChunk;
import file.FileDescription;
import server.ServerData;

public class Client extends AbstractClient {

    public String clientIp;
    public int clientPort;
    public String serverIp;
    public int serverPort;
    public Socket serverSocket;
    public ServerSocket clientSocket;
    public ObjectOutputStream outToCentral;
    public ObjectInputStream inFromCentral;
    private ReentrantLock peersLock;
    private ReentrantLock sendLock;
    private ArrayList<ObjectOutputStream> peers;
    private ArrayList<String> messages;
    private Hashtable<String, ArrayList<byte []>> files;

    public Client(String clientIp, int clientPort, String serverIp, int serverPort) throws IOException {

        this.clientIp = clientIp;
        this.clientPort = clientPort;
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        messages = new ArrayList<String>();
        peersLock = new ReentrantLock();
        new ReentrantLock();
        sendLock = new ReentrantLock();
        peers = new ArrayList<ObjectOutputStream>();

        files = new Hashtable<String, ArrayList<byte[]>>();

        serverSocket = new Socket(this.serverIp, this.serverPort);
        outToCentral = new ObjectOutputStream(serverSocket.getOutputStream());
        inFromCentral = new ObjectInputStream(serverSocket.getInputStream());

        clientSocket = new ServerSocket(this.clientPort);

        new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {
                        new ClientThread(clientSocket.accept());
                    }
                } catch (IOException e) {
                }
            }
        }).start();
    }
    @Override
    public void publishFile(File file) throws IOException {

        String fileName = file.getName();
        long fileSize = file.length();

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
        int sizeOfFiles = 1024 * 100; // 100KB
        byte [] chunk = new byte[sizeOfFiles];
        int tmp = 0;
        ArrayList<byte []> chunks = new ArrayList<byte[]>();

        int readLength = sizeOfFiles;

        while(fileSize > 0) {
            if (fileSize <= sizeOfFiles) {
                readLength = (int)fileSize;
            }

            chunk = new byte[readLength];
            tmp = bis.read(chunk, 0, readLength);
            chunks.add(chunk);
            fileSize -= readLength;
        }

        files.put(fileName, chunks);

        FileDescription fileDescription = new FileDescription(file.getName(), file.length(),
                sizeOfFiles, chunks.size(), clientIp, clientPort);
        outToCentral.writeObject(fileDescription);

        // Wait a bit before sending the next message.
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String message = null;
        try {
            message = (String) inFromCentral.readObject();
            System.out.println("Received message: " + message);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public File retrieveFile(String filename) throws IOException {

        FileDescription fileDescription = new FileDescription(filename, 1);
        outToCentral.writeObject(fileDescription);

        ServerData message = null;
        try {
            message = (ServerData) inFromCentral.readObject();
            System.out.println("Received message: " + message.getFileDescription().getFileName());

            Hashtable<Integer, ArrayList<String>> peerIPs = message.getPeerIPs();
            Hashtable<Integer, ArrayList<Integer>> peerPorts = message.getPeerPorts();

            ArrayList<CreateFile> threads = new ArrayList<CreateFile>();
            ArrayList<byte []> chunks = new ArrayList<byte[]>();

            for(int i = 0; i < message.getFileDescription().getSequenceLength(); i++) {

                CreateFile createFile = new CreateFile(i, filename, peerIPs, peerPorts);
                threads.add(createFile);

            }

            for(CreateFile cf: threads) {
                try {
                    cf.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Am terminat");

            for(CreateFile cf: threads) {
                chunks.add(cf.getChunk());
            }

            File newFile = new File(filename);
            FileOutputStream fos = new FileOutputStream(newFile);
            newFile.createNewFile();
            for(byte [] chunk: chunks) {
                fos.write(chunk);
                fos.flush();
            }
            fos.close();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    class ClientThread extends Thread {

        private final Socket socket;
        private ObjectOutputStream out;

        public ClientThread(Socket socket) {
            this.socket = socket;
            start();
        }

        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());

                peersLock.lock();
                try {
                    peers.add(out);
                } finally {
                    peersLock.unlock();
                }

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                while (true) {

                    FileChunk message = (FileChunk) in.readObject();
                    sendLock.lock();
                    //messages.add(message);
                    System.out.println("Received message: " + message.type);
                    if(message.type == 1) {
                        String fileName = message.getFileName();
                        ArrayList<byte []> chunks = files.get(fileName);
                        byte [] chunk = chunks.get(message.getSequenceNumber());
                        FileChunk fileChunk = new FileChunk(fileName, message.getSequenceNumber(), 2, chunk);
                        out.writeObject(fileChunk);
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

    class CreateFile extends Thread {

        public int sequenceNumber;
        public String fileName;
        public byte [] chunk;
        public Hashtable<Integer, ArrayList<String>> peerIPs;
        public Hashtable<Integer, ArrayList<Integer>> peerPorts;
        public CreateFile(int sequenceNumber, String fileName,
                          Hashtable<Integer, ArrayList<String>> peerIPs, Hashtable<Integer, ArrayList<Integer>> peerPorts) {
            this.sequenceNumber = sequenceNumber;
            this.peerIPs = peerIPs;
            this.peerPorts = peerPorts;
            this.fileName = fileName;
            start();
        }

        public void run() {

            try {
                Socket peerSocket = new Socket(peerIPs.get(sequenceNumber).get(0), peerPorts.get(sequenceNumber).get(0));

                ObjectOutputStream outToPeer = new ObjectOutputStream(peerSocket.getOutputStream());
                ObjectInputStream inFromPeer = new ObjectInputStream(peerSocket.getInputStream());

                FileChunk fileChunk = new FileChunk(fileName, sequenceNumber, 1);
                outToPeer.writeObject(fileChunk);

                FileChunk response = null;
                try {
                    response = (FileChunk) inFromPeer.readObject();
                    System.out.println("Received response: " + response.sequenceNumber);
                    chunk = response.getChunk();

                    FileDescription recordFile = new FileDescription(fileName, 2, sequenceNumber, clientIp, clientPort);
                    outToCentral.writeObject(recordFile);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                outToPeer.close();
                inFromPeer.close();
                peerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public byte [] getChunk() {
            return chunk;
        }

    }
}
