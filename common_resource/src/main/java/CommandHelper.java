import java.util.Scanner;

public class CommandHelper {
    private static final byte EMPTY = -1;
    private static final byte COMMAND_LOGIN = 15;
    private static final byte COMMAND_VIEW = 10;
    private static final byte COMMAND_UPLOAD = 33;
    private static final byte COMMAND_DOWNLOAD = 37;
    private static final byte COMMAND_DELETE = 66;
    private static final byte NOT_FOUND = -19;
    private static final byte NICK_NOT_FOUND = -17;

    public static byte getEMPTY() {
        return EMPTY;
    }

    public static byte getCommandLogin() {
        return COMMAND_LOGIN;
    }

    public static byte getCommandView() {
        return COMMAND_VIEW;
    }

    public static byte getCommandUpload() {
        return COMMAND_UPLOAD;
    }

    public static byte getCommandDownload() {
        return COMMAND_DOWNLOAD;
    }

    public static byte getCommandDelete() {
        return COMMAND_DELETE;
    }

    public static byte getNotFound() {
        return NOT_FOUND;
    }

    public static byte getNickNotFound() {
        return NICK_NOT_FOUND;
    }

    private static Scanner sc = new Scanner(System.in);

    public static void printMessage(String message) {
        System.out.println(message);
    }

    public static int getMenuItems() {
        while (true) {
            try {
                int i;
                String buff = sc.nextLine();
                i = Integer.parseInt(buff);
                if (i > 0 && i < 6) {
                    return i;
                } else {
                    printMessage("Введите пункт меню от 1 до 5");
                }
            } catch (NumberFormatException e) {
                printMessage("Введите пункт меню от 1 до 5");
            }
        }
    }

    public static String getText() {
        return sc.nextLine();
    }
}
