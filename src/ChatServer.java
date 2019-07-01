import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private ServerSocket server;
    private Vector<ServerWorker> arrayWorkers;
    private ExecutorService cashedPool;
    private ExecutorService broadcastPool;
    private ExecutorService terminatePool;
    private int numberOfClient = 1;
    private SimpleDateFormat sdf = new SimpleDateFormat(" HH:mm"); //REPRESENT THE TIME OF THE MESSAGE

    public ChatServer() {
        try {
            server = new ServerSocket(4444);
            cashedPool = Executors.newCachedThreadPool();
            broadcastPool = Executors.newSingleThreadExecutor();
            terminatePool = Executors.newSingleThreadExecutor();
            arrayWorkers = new Vector<>();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void acceptConnection() {
        try {
            Broadcast broadcast = new Broadcast();

            Terminated terminated = new Terminated();
            while (true) {
                Socket clientsocket = server.accept();

                ServerWorker sWorker = newServerWorker(clientsocket);

                arrayWorkers.add(sWorker);

                threadPoolInitializer(broadcast, terminated, sWorker);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    private void threadPoolInitializer(Broadcast broadcast, Terminated terminated, ServerWorker sWorker) {
        cashedPool.submit(sWorker);
        broadcastPool.submit(broadcast);
        terminatePool.submit(terminated);
    }

    private ServerWorker newServerWorker(Socket clientsocket) {
        String clientName = "client " + numberOfClient;

        numberOfClient++;

        Client client = new Client();

        return new ServerWorker(clientsocket, client, clientName);
    }






    private class Broadcast implements Runnable {

        public synchronized void broadcastMessage()  {

            while (true) {
                try {
                    for (int i = 0; i < arrayWorkers.size(); i++) {

                        if ((arrayWorkers.get(i)).isMessageToSent()) {

                            if (arrayWorkers.get(i).getMessageToSend().contains("/alias")) {

                                changeAlias(i);
                                arrayWorkers.get(i).setMessageToSent(false);

                                break;

                            } else if (arrayWorkers.get(i).getMessageToSend().contains("/list")) {

                                getListOfNames(i);
                                arrayWorkers.get(i).setMessageToSent(false);

                                break;

                            } else if (arrayWorkers.get(i).getMessageToSend().contains("/quit")) {
                                terminateConnection(i);

                            }
                            for (int j = 0; j < arrayWorkers.size(); j++) {

                                //if (i != j) {

                                sendMessage(i, j);
                                System.out.println("Message was sent");
                                arrayWorkers.get(i).setMessageToSent(false);

                            }
                        }
                    }
                }catch(SocketException s){

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

                }
            //}



        public synchronized void changeAlias(int i) {

                String[] changename = arrayWorkers.get(i).getMessageToSend().split(" ");
                if(changename[1] != null) {
                    arrayWorkers.get(i).setName(changename[1]);
                  

                }else{
                    arrayWorkers.get(i).setName("Change name again pls");

                }
            }


        public synchronized void getListOfNames(int client) throws IOException {
            StringBuilder sb = new StringBuilder(1000);
            for (int i = 0; i < arrayWorkers.size(); i++) {

                sb.append(arrayWorkers.get(i).getName()).append("\n");
            }
            DataOutputStream outList = new DataOutputStream(arrayWorkers.get(client).getClientSocket().getOutputStream());
            outList.writeBytes(String.valueOf(sb));


        }

        public synchronized void terminateConnection(int i) {
            while (true) {

                    if (arrayWorkers.get(i).getClientSocket().isClosed()) {
                        System.out.println("removed client");
                        arrayWorkers.remove(i);
                        numberOfClient--;
                    }

            }
        }


        public synchronized void sendMessage(int i, int j) throws IOException {
            DataOutputStream out = new DataOutputStream(arrayWorkers.get(j).getClientSocket().getOutputStream());
            out.writeBytes("\n" + arrayWorkers.get(i).getName() + " " + sdf.format(new Date()) + ": " + arrayWorkers.get(i).getMessageToSend());
            arrayWorkers.get(i).setMessageToSent(false);
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

