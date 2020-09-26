import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class MainServer {
    public static void main(String[] args) {
        String filePath = "C:/Test2/temp.txt"; // Потом получать путь перед записью файла
        try {
            ServerSocketChannel serverSC;
            SocketChannel sc;
            serverSC = ServerSocketChannel.open();
            serverSC.socket().bind(new InetSocketAddress(8787));
            sc = serverSC.accept();
            ConsoleHelper.getMessage("Connection is on");
            Path path = Paths.get(filePath);
            FileChannel fileChannel = FileChannel.open(path,
                    EnumSet.of(StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE)
            ); // Подсмотрел в интеренете как писать, если нет файла на сервере или если надо переписать содержимое
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while(sc.read(buffer) > 0) {
                buffer.flip();
                fileChannel.write(buffer);
                buffer.clear();
            }
            fileChannel.close();
            ConsoleHelper.getMessage("File received");
            sc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}