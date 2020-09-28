import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainClient {
    private static SocketChannel clientSocketChannel;
    private static boolean isFlag = true;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;

    private static void doConnection() {
        try {
            clientSocketChannel = SocketChannel.open();
            SocketAddress socketAddr = new InetSocketAddress("localhost", 8787);
            clientSocketChannel.connect(socketAddr);
        } catch (IOException e) {
            ConsoleHelper.printMessage("Ошибка соединения");
        }
    }

    private static void upload(String filePath) {
        try {
            Path path = Paths.get(filePath);
            FileChannel fileChannel = FileChannel.open(path);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (fileChannel.read(buffer) > 0) {
                buffer.flip();
                clientSocketChannel.write(buffer);
                buffer.clear();
            }
            fileChannel.close();
            ConsoleHelper.printMessage("Файл отправлен");
            clientSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void quit() {
        ConsoleHelper.printMessage("До свидания!");
        isFlag = false;
        try {
            clientSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ConsoleHelper.printMessage("Начало работы. Введите логин и пароль");
        //TODO doAuthorization() Метод для авторизации в БД для последующей работы
        doConnection();
        // После подключения к серверу
        do {
            ConsoleHelper.printMessage("Введите команду:\n1.Загрузить\n2.Скачать\n3.Удалить\n4.Выход");
            int answer = ConsoleHelper.getAnswer();
            // Работа в зависимости от ответа
            switch (answer) {
                case (ONE):
                    upload("C:/temp.txt"); // Потом запрашивать в консоли у клиента
                    break;
                case (TWO):
                    //TODO download() - Сделать метод скачки файла с сервера
                    System.out.println("Работа download()...");
                    break;
                case (THREE):
                    //TODO delete() - Сделать метод удаления файла с сервера
                    System.out.println("Работа delete()...");
                    break;
                case (FOUR):
                    quit();
                    break;
            }
        } while (isFlag);
    }
}
