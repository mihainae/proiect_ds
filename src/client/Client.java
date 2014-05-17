package client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;
import file.FileDescription;

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

        ArrayList<byte []> chunks = files.get(filename);
        File newFile = new File(filename);
        FileOutputStream fos = new FileOutputStream(newFile);
        newFile.createNewFile();
        for(byte [] ch: chunks) {
            fos.write(ch);
            fos.flush();
        }
        fos.close();

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

                out.writeObject(String.valueOf(serverSocket.getLocalPort()));

                peersLock.lock();
                try {
                    peers.add(out);
                } finally {
                    peersLock.unlock();
                }

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                //int port = Integer.parseInt((String) in.readObject());

                while (true) {

                    String message = (String) in.readObject();
                    sendLock.lock();
                    messages.add(message);
                    System.out.println("Received message: " + message);
                    out.writeObject("ACK");

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
}
