package client.core.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PerformMessages extends Thread{
    private final Socket socket;
    private BufferedReader in;
    public PerformMessages(Socket s) {
        this.socket=s;

    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(),
                    StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String line;
           try {
               while ((line = in.readLine())!= null) {
                    System.out.println(line);
               }
           }
           catch (Exception e){
               e.printStackTrace();
           }
       }
    public void terminate() throws Exception {
        socket.shutdownInput();
    }
}
