import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

public class ServerWorker implements Runnable {

    private Socket clientSocket;
    private Client client;
    private String messageToSend;
    private String name;
    private boolean messageToSent = false;
    private DataOutputStream out;

    public ServerWorker(Socket server, Client client, String name) {
        clientSocket = server;
        this.client = client;
        this.name = name;
    }

    @Override
    public void run() {
        try {

            while (true) {


                out = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out.writeBytes(name + " connected to server" + "\n");
                //String msg = bReader.readLine();
                String msg;

                while((msg = bReader.readLine()) != null){

                        if(msg.equalsIgnoreCase("/quit")){
                            out.writeBytes("You have been disconnected from server");

                            break;
                        }

                        if(msg.contains("/alias")){

                            messageToSend = msg;
                            messageToSent = true;

                        }else if(msg.contains("/list")){

                            messageToSend = msg;
                            messageToSent = true;

                        } else {

                            messageToSend = (msg + "\n");
                            messageToSent = true;

                        }

                }
                clientSocket.close();
            }
        } catch (SocketException s) {
            System.out.println("Connection closed");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getMessageToSend() {
        return messageToSend;
    }

    public boolean isMessageToSent() {
        return messageToSent;
    }

    public void setMessageToSent(boolean messageToSent) {
        this.messageToSent = messageToSent;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
