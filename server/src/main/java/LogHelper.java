import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class LogHelper {
    public static final Logger protocolLogger = Logger.getLogger(ProtocolHandler.class.getName());
    public static final Logger echoLogger = Logger.getLogger(EchoProtocolHandler.class.getName());

    public static void startLog() {
        protocolLogger.setLevel(Level.CONFIG);
        echoLogger.setLevel(Level.CONFIG);
        try {
            Handler protocolLogHandler = new FileHandler("protocol_log", true);
            Handler echoLogHandler = new FileHandler("echo_protocol_log", true);
            protocolLogHandler.setLevel(Level.CONFIG);
            echoLogHandler.setLevel(Level.CONFIG);
            protocolLogger.addHandler(protocolLogHandler);
            echoLogger.addHandler(echoLogHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        protocolLogger.getHandlers()[0].setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm:ss");
                Date date = new Date();
                return dateFormat.format(date) + "\tmsg:" + record.getMessage() + "\tlvl: " + record.getLevel() + "\n";
            }
        });

        echoLogger.getHandlers()[0].setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                DateFormat dateFormat = new SimpleDateFormat("dd-mm-yyyy HH:mm:ss");
                Date date = new Date();
                return dateFormat.format(date) + "\tmsg:" + record.getMessage() + "\tlvl: " + record.getLevel() + "\n";
            }
        });
    }
}
