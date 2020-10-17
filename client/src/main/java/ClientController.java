import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.*;

public class ClientController {
    private static String nick;

    public static String getNick() {
        return nick;
    }

    static void doAuthorization() {
        CommandHelper.printMessage("Введите логин");
        String login = CommandHelper.getText();
        CommandHelper.printMessage("Введите пароль");
        String password = CommandHelper.getText();
        short loginLength = (short) login.length();
        short passwordLength = (short) password.length();
        try {
            Socket socket = new Socket("localhost", 8787);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeByte(CommandHelper.getCommandLogin());
            out.writeShort(loginLength);
            out.write(login.getBytes());
            out.writeShort(passwordLength);
            out.write(password.getBytes());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            byte[] bytes = in.readAllBytes();
            if (bytes[0] == CommandHelper.getNickNotFound()) {
                CommandHelper.printMessage("Не верный логин и(или) пароль");
            } else {
                nick = new String(bytes);
                CommandHelper.printMessage("Авторизация выполнена. Здравствуйте " + nick);
            }
            in.close();
            out.close();
            socket.close();
        } catch (Exception e) {
            CommandHelper.printMessage("Введены некорректные данные");
        }
    }

    static void upload() {
        String filePath = "";
        CommandHelper.printMessage("1.Загрузить с локального репозитория\n2.Загрузить по указанному пути\n0.Назад");
        int choice = CommandHelper.choiceBetweenThree();
        if (choice == 0) {
            return;
        }
        if (choice == 1) {
            CommandHelper.printMessage("Введите ИМЯ файла");
            String fileName = CommandHelper.getText();
            filePath = "client_repository/" + fileName;
        }
        if (choice == 2) {
            CommandHelper.printMessage("Введите ПУТЬ к файлу");
            filePath = CommandHelper.getText();
        }
            if (Files.exists(Paths.get(filePath))) {
                try {
                    File file = new File(filePath);
                    String fileName = file.getName();
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
                    byte[] buff = Files.readAllBytes(Paths.get(filePath));
                    out.write(buff);
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    byte[] bytes = in.readAllBytes();
                    if (bytes.length == 1 && bytes[0] == CommandHelper.getLogOut()) {
                        CommandHelper.printMessage("Ошибка аутентификации");
                    } else {
                        CommandHelper.printMessage("> " + new String(bytes));
                    }
                    in.close();
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    CommandHelper.printMessage("Ошибка чтения файла");
                }
            } else {
                CommandHelper.printMessage("Неверно указан путь к файлу");
            }
    }

    static void download() {
        String filePath = "client_repository/";
        CommandHelper.printMessage("1.Сохранить на локальный репозиторий\n2.Сохранить по указанному пути\n0.Назад");
        int choice = CommandHelper.choiceBetweenThree();
        if (choice == 0) {
            return;
        }
        CommandHelper.printMessage("Введите ИМЯ файла");
        String fileName = CommandHelper.getText();
        if (choice == 2) {
            CommandHelper.printMessage("Введите ПУТЬ сохранения");
            filePath = CommandHelper.getText();
        }
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
            if (bytes.length == 1 && bytes[0] == CommandHelper.getLogOut()) {
                CommandHelper.printMessage("Ошибка аутентификации");
            } else {
                if (bytes[0] == CommandHelper.getCommandDownload()) {
                    byte[] finalBytes = new byte[bytes.length - 1];
                    System.arraycopy(bytes, 1, finalBytes, 0, bytes.length - 1);
                    Path path = Paths.get(filePath + fileName);
                    Files.write(path, finalBytes);
                    CommandHelper.printMessage("> Сохранение " + filePath + fileName + " выполнено");
                } else {
                    CommandHelper.printMessage("> " + new String(bytes));
                }
            }
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void delete() {
        CommandHelper.printMessage("1.Удалить на репозитории сервера\n2.Удалить на локальном репозитории\n0.Назад");
        int choice = CommandHelper.choiceBetweenThree();
        if (choice == 0) {
            return;
        }
        CommandHelper.printMessage("Введите имя файла");
        String fileName = CommandHelper.getText();
        if (choice == 1) {
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
                if (bytes.length == 1 && bytes[0] == CommandHelper.getLogOut()) {
                    CommandHelper.printMessage("Ошибка аутентификации");
                } else {
                    CommandHelper.printMessage("> " + new String(bytes));
                }
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                CommandHelper.printMessage("Ошибка соединения");
            }
        }
        if (choice == 2) {
            if (Files.exists(Paths.get("client_repository/" + fileName))) {
                try {
                    Files.delete(Paths.get("client_repository/" + fileName));
                } catch (IOException e) {
                    CommandHelper.printMessage("Ошибка доступа к репозитории");
                }
                CommandHelper.printMessage("> Успешно удалено");
            } else {
                CommandHelper.printMessage("> Данный файл отсутствует");
            }
        }
    }

    static void view() {
        CommandHelper.printMessage("1.Просмотр файлов на репозитории сервера\n2.Просмотр файлов на локальном репозитории\n0.Назад");
        int choice = CommandHelper.choiceBetweenThree();
        if (choice == 0) {
            return;
        }
        if (choice == 1) {
            try {
                short nickNameLength = (short) nick.length();
                Socket socket = new Socket("localhost", 8787);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeByte(CommandHelper.getCommandView());
                out.writeShort(nickNameLength);
                out.write(nick.getBytes());
                DataInputStream in = new DataInputStream(socket.getInputStream());
                byte[] bytes = in.readAllBytes();
                if (bytes.length == 1 && bytes[0] == CommandHelper.getLogOut()) {
                    CommandHelper.printMessage("Ошибка аутентификации");
                } else {
                    String res = new String(bytes);
                    res = res.substring(0, (res.length() - 1));
                    res = res.replace(";", "\n> ");
                    CommandHelper.printMessage("> " + res);
                }
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
                CommandHelper.printMessage("Ошибка соединения");
            }
        }
        if (choice == 2) {
            StringBuilder sb = new StringBuilder();
            Path dir = Paths.get("client_repository/");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path file : stream) {
                    sb.append(file.getFileName()).append(";");
                }
                if (sb.length() > 0) {
                    String res = sb.toString();
                    if (!res.equals("ПУСТО")) {
                        res = res.substring(0, (res.length() - 1));
                    }
                    res = res.replace(";", "\n> ");
                    CommandHelper.printMessage("> " + res);
                } else {
                    CommandHelper.printMessage("> ПУСТО");
                }
            } catch (IOException | DirectoryIteratorException e) {
                CommandHelper.printMessage("Ошибка чтения");
            }
        }
    }
}
