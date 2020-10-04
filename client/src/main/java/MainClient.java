import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainClient {
    private static boolean isFlag = true;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;

    public static void main(String[] args) {
        CommandHelper.printMessage("Начало работы. Введите логин и пароль");
        doAuthorization();
        // После подключения к серверу
        do {
            CommandHelper.printMessage("Введите команду:\n1.Загрузить\n2.Скачать\n3.Удалить\n4.Список файлов\n5.Выход");
            int answer = CommandHelper.getAnswer();
            // Работа в зависимости от ответа
            switch (answer) {
                case (ONE):
                    upload("C:/temp.txt", "nickT"); // заглушка. Потом запрашивать в консоли у клиента
                    break;
                case (TWO):
                    download("temp.txt", "C:/Temp/", "nickT"); // заглушка
                    break;
                case (THREE):
                    delete("temp.txt", "nickT"); // заглушка
                    break;
                case (FOUR):
                    view("nickT"); // заглушка
                    break;
                case (FIVE):
                    quit();
            }
        } while (isFlag);
    }

    private static void doAuthorization() {
        try (Socket socket = new Socket("localhost", 8787)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
        // TODO Сделать метод приема логина и пароля и по out отравить данные для регистрации
            CommandHelper.printMessage("Авторизация выполнена");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void upload(String filePath, String nick) {
        try {
            File file = new File(filePath);
            String fileName = file.getName();
            short nickNameLength = (short) nick.length();
            short fileNameLength = (short) fileName.length();
            Socket socket = new Socket("localhost", 8787);
            // Отправляем команду на загрузку, данныйе файла и сам файл
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeByte(CommandHelper.getCommandUpload());
            out.writeShort(nickNameLength);
            out.write(nick.getBytes());
            out.writeShort(fileNameLength);
            out.write(fileName.getBytes());
            out.writeLong(file.length());
            byte[] buff = Files.readAllBytes(Paths.get(filePath));
            out.write(buff);
            // Принимаем ответ
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] bytes = in.readAllBytes();
            CommandHelper.printMessage("> " + new String(bytes));
            // Закрываем соединение
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void download(String fileName, String filePath, String nick) {
        try {
            short nickNameLength = (short) nick.length();
            short fileNameLength = (short) fileName.length();
            Socket socket = new Socket("localhost", 8787);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeByte(CommandHelper.getCommandDownload());
            out.writeShort(nickNameLength);
            out.write(nick.getBytes());
            out.writeShort(fileNameLength);
            out.write(fileName.getBytes());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] bytes = in.readAllBytes();
            if (bytes[0] == CommandHelper.getCommandDownload()) {
                // Логика сохранения файла на диск
                byte[] finalBytes = new byte[bytes.length - 1];
                System.arraycopy(bytes, 1, finalBytes, 0, bytes.length - 1);
                Path path = Paths.get(filePath + fileName);
                Files.write(path, finalBytes);
                CommandHelper.printMessage("> Сохранение " + filePath + fileName + " выполнено");
            } else {
                CommandHelper.printMessage("> " + new String(bytes));
            }
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void delete(String fileName, String nick) {
        try {
            short nickNameLength = (short) nick.length();
            short fileNameLength = (short) fileName.length();
            Socket socket = new Socket("localhost", 8787);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeByte(CommandHelper.getCommandDelete());
            out.writeShort(nickNameLength);
            out.write(nick.getBytes());
            out.writeShort(fileNameLength);
            out.write(fileName.getBytes());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] bytes = in.readAllBytes();
            CommandHelper.printMessage("> " + new String(bytes));
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void view(String nick) {
        try {
            short nickNameLength = (short) nick.length();
            Socket socket = new Socket("localhost", 8787);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeByte(CommandHelper.getCommandView());
            out.writeShort(nickNameLength);
            out.write(nick.getBytes());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] bytes = in.readAllBytes();
            // Изменяем строку для лучшей читабельности
            String x = new String(bytes).replaceAll(" ", "\n> ");
            CommandHelper.printMessage("> " + x);
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void quit() {
        CommandHelper.printMessage("До свидания!");
        isFlag = false;
    }
}
