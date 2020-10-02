import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainClient {
    private static boolean isFlag = true;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;

    private static void doAuthorization() { // Возможно надо будет совместить с doConnection()
        try (Socket socket = new Socket("localhost", 8787)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//TODO            У ConsoleHelper сделать метод приема логина и пароля и по out отравить данные для регистрации
            ConsoleHelper.printMessage("Авторизация выполнена");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void upload(String filePath, String nick) {
        try (Socket socket = new Socket("localhost", 8787);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {
            File file = new File(filePath);
            String fileName = file.getName();
            String absPath = file.getAbsolutePath();
            short nickNameLength = (short) nick.length();
            short fileNameLength = (short) fileName.length();
            out.writeByte(33);
            out.writeShort(nickNameLength);
            out.write(nick.getBytes());
            out.writeShort(fileNameLength);
            out.write(fileName.getBytes());
            out.writeLong(file.length());
            byte[] buff = Files.readAllBytes(Paths.get(absPath));
            out.write(buff);
//            DataInputStream inputStream = new DataInputStream(socket.getInputStream());
//            byte[] bytes = inputStream.readAllBytes();
//            String s = new String(bytes);
//            ConsoleHelper.printMessage(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void download(String fileName, String filePath) {

    }

    private static void delete(String fileName) {

    }

    private static void view() {
        try (Socket socket = new Socket("localhost", 8787)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeByte(10);
            byte[] fileNameBytes = "temp.txt".getBytes();
            out.writeInt(fileNameBytes.length);
            out.write(fileNameBytes);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void quit() {
        ConsoleHelper.printMessage("До свидания!");
        isFlag = false;
    }

    public static void main(String[] args) {

        ConsoleHelper.printMessage("Начало работы. Введите логин и пароль");
        doAuthorization();
        // После подключения к серверу
        do {
            ConsoleHelper.printMessage("Введите команду:\n1.Загрузить\n2.Скачать\n3.Удалить\n4.Список файлов\n5.Выход");
            int answer = ConsoleHelper.getAnswer();
            // Работа в зависимости от ответа
            switch (answer) {
                case (ONE):
                    upload("C:/temp.txt", "nickT"); // Потом запрашивать в консоли у клиента
                    break;
                case (TWO):
                    download("temp.txt", "C:/Temp/"); // заглушка
                    break;
                case (THREE):
                    delete("temp.txt"); // заглушка
                    break;
                case (FOUR):
                    view();
                    break;
                case (FIVE):
                    quit();
            }
        } while (isFlag);
    }
}
