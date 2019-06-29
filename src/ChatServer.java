import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private ServerSocket server;
    private Socket clientsocket;
    private ArrayList<ServerWorker> arrayWorkers;
    private ExecutorService cashedPool;
    private ExecutorService broadcastPool;
    private ExecutorService terminatePool;

    public ChatServer() {
        try {
            server = new ServerSocket(4444);
            cashedPool = Executors.newCachedThreadPool();
            broadcastPool = Executors.newSingleThreadExecutor();
            terminatePool = Executors.newSingleThreadExecutor();
            arrayWorkers = new ArrayList<>();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void acceptConnection() {
        try {
            while (true) {
                clientsocket = server.accept();

                Client client = new Client();
                ServerWorker sWorker = new ServerWorker(clientsocket, client);
                Broadcast broadcast = new Broadcast();
                Terminated terminated = new Terminated();

                arrayWorkers.add(sWorker);
                cashedPool.submit(sWorker);
                broadcastPool.submit(broadcast);
                terminatePool.submit(terminated);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class Broadcast implements Runnable {

        public synchronized void broadcast() {
            while (true) {

                for (int i = 0; i < arrayWorkers.size(); i++) {
                    if ((arrayWorkers.get(i)).isMessageToSent()) {
                        for (int j = 0; j < arrayWorkers.size(); j++) {
                            if (i != j) {
                                try {
                                    DataOutputStream out = new DataOutputStream(arrayWorkers.get(j).getClientSocket().getOutputStream());
                                    out.writeBytes("client " + i + ": " + arrayWorkers.get(i).getS());
                                } catch (SocketException s) {

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        (arrayWorkers.get(i)).setMessageToSent(false);
                    }
                }
            }
        }

        @Override
        public void run() {
            broadcast();
        }
    }


    private class Terminated implements Runnable {

        public synchronized void terminate() {
            while(true) {
                for (int i = 0; i < arrayWorkers.size(); i++) {
                    if (arrayWorkers.get(i).getClientSocket().isClosed()) {
                        System.out.println("removed client");
                        arrayWorkers.remove(i);

                    }
                }
            }
        }


        @Override
        public void run() {
        terminate();
        }
    }
}