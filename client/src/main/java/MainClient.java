public class MainClient {
    private static boolean cycleIsOn = true;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;
    private static final int ZERO = 0;

    public static void main(String[] args) {
        CommandHelper.printMessage("Начало работы. Введите логин и пароль");
        do {
            ClientController.doAuthorization();
        } while (ClientController.getNick() == null);
        do {
            CommandHelper.printMessage("Введите команду:\n1.Загрузить\n2.Скачать\n3.Удалить\n4.Список файлов\n0.Выход");
            int answer = CommandHelper.getMenuItems();
            switch (answer) {
                case (ONE):
                    ClientController.upload();
                    break;
                case (TWO):
                    ClientController.download();
                    break;
                case (THREE):
                    ClientController.delete();
                    break;
                case (FOUR):
                    ClientController.view();
                    break;
                case (ZERO):
                    quit();
            }
        } while (cycleIsOn);
    }

    private static void quit() {
        CommandHelper.printMessage("До свидания!");
        cycleIsOn = false;
    }
}
