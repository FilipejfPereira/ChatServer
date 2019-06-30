import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private ServerSocket server;
    private Socket clientsocket;
    private ArrayList<ServerWorker> arrayWorkers;
    private ExecutorService cashedPool;
    private ExecutorService broadcastPool;
    private ExecutorService terminatePool;
    private int numberOfClient = 1;

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

                String clientName = "client " + numberOfClient;

                numberOfClient++;

                Client client = new Client();

                ServerWorker sWorker = new ServerWorker(clientsocket, client, clientName);

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

        public synchronized void broadcastMessage() {

            while (true) {

                for (int i = 0; i < arrayWorkers.size(); i++) {

                    if ((arrayWorkers.get(i)).isMessageToSent()) {

                        if (arrayWorkers.get(i).getMessageToSend().contains("/alias")) {

                            changeName(i);
                            arrayWorkers.get(i).setMessageToSent(false);
                            break;

                        }else if (arrayWorkers.get(i).getMessageToSend().contains("/list")) {

                            try {

                                getListOfNames(i);
                                arrayWorkers.get(i).setMessageToSent(false);
                                break;

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                        for (int j = 0; j < arrayWorkers.size(); j++) {

                            if (i != j) {

                                try {

                                    broadcastMessage(i, j);
                                    System.out.println("1");
                                } catch (SocketException ignored) {

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        System.out.println("2");
                        arrayWorkers.get(i).setMessageToSent(false);
                    }

                }
            }
        }


        public synchronized void changeName(int i) {

                String[] changename = arrayWorkers.get(i).getMessageToSend().split(" ");
                if(changename[1] != null) {
                    arrayWorkers.get(i).setName(changename[1]);

                }else{
                    arrayWorkers.get(i).setName("Change name again pls");
                }
            }


        public void getListOfNames(int client) throws IOException {
            StringBuilder sb = new StringBuilder(1000);
            for (int i = 0; i < arrayWorkers.size(); i++) {

                sb.append(arrayWorkers.get(i).getName()).append("\n");
            }
            DataOutputStream outList = new DataOutputStream(arrayWorkers.get(client).getClientSocket().getOutputStream());
            outList.writeBytes(String.valueOf(sb));

        }


        public void broadcastMessage(int i, int j) throws IOException {
            DataOutputStream out = new DataOutputStream(arrayWorkers.get(j).getClientSocket().getOutputStream());
            out.writeBytes(arrayWorkers.get(i).getName() + ": " + arrayWorkers.get(i).getMessageToSend());
        }

        @Override
        public void run() {
            broadcastMessage();
        }

    }







        private class Terminated implements Runnable {

            public synchronized void terminate() {
                while (true) {
                    for (int i = 0; i < arrayWorkers.size(); i++) {
                        if (arrayWorkers.get(i).getClientSocket().isClosed()) {
                            System.out.println("removed client");
                            arrayWorkers.remove(i);
                            numberOfClient--;
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

