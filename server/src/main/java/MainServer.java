import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
    public static void main(String[] args) {
        try (ServerSocket sskt = new ServerSocket(8087)) {
            System.out.println("Server is ready");
            try(Socket socket = sskt.accept(); BufferedInputStream in = new BufferedInputStream(socket.getInputStream())) {
                System.out.println("Client is connected");
                int n;
                while ((n = in.read()) != -1) {
                    System.out.print((char) n);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}