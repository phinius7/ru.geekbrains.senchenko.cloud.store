import java.util.Scanner;

public class ConsoleHelper {
    private static Scanner sc = new Scanner(System.in);

    public static void printMessage(String message) {
        System.out.println(message);
    }

    public static int getAnswer() {
        while (true) {
            try {
                int i;
                String buff = sc.nextLine();
                i = Integer.parseInt(buff);
                if (i > 0 && i < 5) {
                    return i;
                } else {
                    printMessage("Введите пункт меню от 1 до 4");
                }
            } catch (NumberFormatException e) {
                printMessage("Введите пункт меню от 1 до 4");
            }
        }
    }
}
