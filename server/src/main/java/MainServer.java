import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.Iterator;

public class MainServer implements Runnable {
    //TODO Сделать инициализацию сервера
    private ServerSocketChannel serverSocketChannel;
    private Selector selector;
    private ByteBuffer buffer;
    private final ByteBuffer welcome = ByteBuffer.wrap("Вы подключились к CloudStore!\n".getBytes());
    private static String filePath = "server_repository/temp.txt"; // Потом получать путь перед записью файла

    public MainServer() throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(8787));
        this.serverSocketChannel.configureBlocking(false);
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        this.buffer = ByteBuffer.allocate(1024);
    }

    @Override
    public void run() {
        try {
            ConsoleHelper.printMessage("Server with port 8787 is Running");
            Iterator<SelectionKey> iterator;
            SelectionKey key;
            while (this.serverSocketChannel.isOpen()) {
                selector.select();
                iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    }
                    if (key.isReadable()) {
                        handleRead(key);
                    }
                    if (key.isWritable()) {
                        handleWrite(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept(SelectionKey key) {
        SocketChannel sc = null;
        try {
            sc = ((ServerSocketChannel) key.channel()).accept();
            String clientName = key.channel().toString();
            sc.configureBlocking(false);
            sc.register(selector, SelectionKey.OP_READ, clientName);
            sc.write(welcome);
            welcome.rewind();
            System.out.println("Подключился новый клиент " + clientName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleRead(SelectionKey key) {
        try {
            SocketChannel sc = (SocketChannel) key.channel();
            Path path = Paths.get(filePath);
            FileChannel fileChannel = FileChannel.open(path,
                    EnumSet.of(StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING,
                            StandardOpenOption.WRITE)
            );
            buffer.clear();
            while (sc.read(buffer) > 0) {
                buffer.flip();
                byte[] bytes = new byte[buffer.limit()];
                buffer.get(bytes);
                fileChannel.write(buffer);
                buffer.clear();
            }
            fileChannel.close();
            ConsoleHelper.printMessage("Connection is Closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleWrite(SelectionKey key) {

    }

    public static void main(String[] args) {
        //TODO Сделать методы ответа сервера в зависимости от запроса
        try {
            new Thread(new MainServer()).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        try {
//            ServerSocketChannel serverSC; //
//            SocketChannel sc; //
//            serverSC = ServerSocketChannel.open(); //
//            serverSC.socket().bind(new InetSocketAddress(8787)); //
//            ConsoleHelper.printMessage("Server is running"); //
//            sc = serverSC.accept(); //
//            ConsoleHelper.printMessage("Connection is on"); //
//            Path path = Paths.get(filePath);
//            FileChannel fileChannel = FileChannel.open(path,
//                    EnumSet.of(StandardOpenOption.CREATE,
//                            StandardOpenOption.TRUNCATE_EXISTING,
//                            StandardOpenOption.WRITE)
//            ); // Подсмотрел в интеренете как писать, если нет файла на сервере или если надо переписать содержимое
//            ByteBuffer buffer = ByteBuffer.allocate(1024);
//            while (sc.read(buffer) > 0) {
//                buffer.flip();
//                fileChannel.write(buffer);
//                buffer.clear();
//            }
//            fileChannel.close();
//            ConsoleHelper.printMessage("Connection is Closed");
//            sc.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }
}