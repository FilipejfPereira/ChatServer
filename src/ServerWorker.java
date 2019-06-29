import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class ServerWorker implements Runnable {

    private Socket clientSocket;
    Client client;
    String s;
    private boolean messageToSent = false;
    DataOutputStream out;

    public ServerWorker(Socket server, Client client) {
        clientSocket = server;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            while (true) {


                out = new DataOutputStream(clientSocket.getOutputStream());
                BufferedReader bReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String msg = bReader.readLine();



                if(msg !=null) {
                    s = (msg + "\n");
                   messageToSent = true;
                }
            }
        } catch (SocketException s) {
            System.out.println("Connection closed");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getS() {
        return s;
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
}
