import java.io.IOException;
import java.net.Socket;

public class MainClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8087)) {
            byte[] bytes = {65, 66, 67, 49, 50, 51};
            socket.getOutputStream().write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
