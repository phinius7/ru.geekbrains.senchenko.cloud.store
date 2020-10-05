import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class MainClient {
    private static boolean isFlag = true;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int FIVE = 5;
    private static String nick = null;

    public static String getNick() {
        return nick;
    }

    public static void main(String[] args) {
        CommandHelper.printMessage("Начало работы. Введите логин и пароль");
        do {
            doAuthorization();
        } while (nick == null);
        // После авторизации
        do {
            CommandHelper.printMessage("Введите команду:\n1.Загрузить\n2.Скачать\n3.Удалить\n4.Список файлов\n5.Выход");
            int answer = CommandHelper.getMenuItems();
            // Работа в зависимости от ответа
            switch (answer) {
                case (ONE):
                    CommandHelper.printMessage("Введите путь к файлу");
                    String filePath = CommandHelper.getText();
                    filePath = "C:/temp.txt"; // заглушка
                    upload(filePath, getNick());
                    break;
                case (TWO):
                    CommandHelper.printMessage("Введите имя файла");
                    String fileName = CommandHelper.getText();
                    fileName = "temp.txt"; // заглушка
                    CommandHelper.printMessage("Введите путь сохранения");
                    String savePath = CommandHelper.getText();
                    savePath = "C:/Temp/"; // заглушка
                    download(fileName, savePath, getNick());
                    break;
                case (THREE):
                    CommandHelper.printMessage("Введите имя файла");
                    String delFileName = CommandHelper.getText();
                    delFileName = "temp.txt"; // заглушка
                    delete(delFileName, getNick());
                    break;
                case (FOUR):
                    view(getNick());
                    break;
                case (FIVE):
                    quit();
            }
        } while (isFlag);
    }

    private static void doAuthorization() {
        String login; // для проверки login1
        String password; // для проверки pass1
        CommandHelper.printMessage("Введите логин");
        login = CommandHelper.getText();
        CommandHelper.printMessage("Введите пароль");
        password = CommandHelper.getText();
        short loginLength = (short) login.length();
        short passwordLength = (short) password.length();
        try {
            Socket socket = new Socket("localhost", 8787);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            // Отправим команду на авторизацию, логин и пароль
            out.writeByte(CommandHelper.getCommandLogin());
            out.writeShort(loginLength);
            out.write(login.getBytes()); // Здесь что-то происходи не так, если писать на русской раскладке
            // Если писать логин на русской раскладке, writeShort(passwordLength) отрабарывает не корректно и выбрасывает ошибку (?)
            out.writeShort(passwordLength);
            out.write(password.getBytes());
            // В ответ придет никнейм
            // Принимаем ответ
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] bytes = in.readAllBytes();
            if (bytes[0] == CommandHelper.getNickNotFound()) {
                CommandHelper.printMessage("Не верный логин и(или) пароль");
            } else {
                nick = new String(bytes);
                CommandHelper.printMessage("Авторизация выполнена. Здравствуйте " + getNick());
            }
            // Закрываем соединение
            in.close();
            out.close();
            socket.close();
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
