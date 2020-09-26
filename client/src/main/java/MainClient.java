import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainClient {
    public static void main(String[] args) {
        String filePath = "C:/Test/temp.txt"; // Потом запрашивать в консоли у клиента

        try {
            SocketChannel clientSocketChannel = SocketChannel.open();
            SocketAddress socketAddr = new InetSocketAddress("localhost", 8787);
            clientSocketChannel.connect(socketAddr);
            Path path = Paths.get(filePath);
            FileChannel fileChannel = FileChannel.open(path);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while(fileChannel.read(buffer) > 0) {
                buffer.flip();
                clientSocketChannel.write(buffer);
                buffer.clear();
            }
            fileChannel.close();
            ConsoleHelper.getMessage("File sent");
            clientSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
