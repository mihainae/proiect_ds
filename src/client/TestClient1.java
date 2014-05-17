package client;

import java.io.File;
import java.net.InetAddress;
import client.Client;

public class TestClient1 {
	
	/* lista de fisiere pe care server-ul le va publica */
	public static String files[] = {"files/kids.jpg", "files/bestalgs.pdf", "files/3DUniverse.jpg", "files/mn2014.txt"};
	
	public static void main(String argv[]) {
		
		System.out.println("[TestClient1]: Starting testing client 1 (this one publish a set of files)");
		if (argv.length < 2) {	
			System.out.println("[TestClient1]: The client starts with command: java Client ServerHost ServerPort ClientPort");
			System.exit(-1);
		} 		
		try {
			
			/* adresarea serverului (ip, port) */
			String serverHost = argv[0];
			int serverPort = Integer.parseInt(argv[1]);
			
			/* adresarea clientului (ip, port) */
			String clientHost = InetAddress.getLocalHost().getHostName();
			int clientPort = Integer.parseInt(argv[2]);

			Client cli = new Client(clientHost,clientPort,serverHost,serverPort);
			
			/* publica fisierele */
			for(String file: files) {
				File curentFile=new File(file);
	            cli.publishFile(curentFile);
	            System.out.println("[TestClient1]: File "+file+" was published... DONE!");
                cli.retrieveFile(curentFile.getName());
                break;
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
