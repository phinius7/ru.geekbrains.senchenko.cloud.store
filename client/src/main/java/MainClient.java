import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

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
                    download("temp.txt", "C:/Temp/"); // заглушка
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

    private static void doAuthorization() { // Возможно надо будет совместить с doConnection()
        try (Socket socket = new Socket("localhost", 8787)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
//       TODO У ConsoleHelper сделать метод приема логина и пароля и по out отравить данные для регистрации
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
            String absPath = file.getAbsolutePath();
            short nickNameLength = (short) nick.length();
            short fileNameLength = (short) fileName.length();
            Socket socket = new Socket("localhost", 8787);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeByte(CommandHelper.getCommandUpload());
            out.writeShort(nickNameLength);
            out.write(nick.getBytes());
            out.writeShort(fileNameLength);
            out.write(fileName.getBytes());
            out.writeLong(file.length());
            byte[] buff = Files.readAllBytes(Paths.get(absPath));
            out.write(buff);
            // Принимаем ответ
            Scanner in = new Scanner(socket.getInputStream());
            String x = in.nextLine();
            CommandHelper.printMessage(x);
            // Закрываем соединение
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void download(String fileName, String filePath) {
        /* Не понятно, как вернуть массив байтов через DataInputStream. Пробовал заменить Scanner на DataInputStream,
         * но он повисает - не понимаю почему. А так в теории следует отправить на сервер: команду, ник клиента, имя файла,
         * а сервер должен вернуть массив байтов файла. А уже в этом методе записать данные в файл по пути, переданным клиентом.*/
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
            // Принимаем ответ
            Scanner in = new Scanner(socket.getInputStream());
            String x = in.nextLine();
            CommandHelper.printMessage(x);
            // Закрываем соединение
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void view(String nick) {
        try (Socket socket = new Socket("localhost", 8787)) {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeByte(CommandHelper.getCommandView());
            /* Анологично должен вернуть массив байтов, а в методе перевести его в String и распечатать.*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void quit() {
        CommandHelper.printMessage("До свидания!");
        isFlag = false;
    }
}
