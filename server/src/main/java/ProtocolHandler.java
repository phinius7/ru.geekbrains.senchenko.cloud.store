import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {
    private enum DataType {
        EMPTY((byte) -1),
        COMMAND_LOGIN((byte) 15),
        COMMAND_VIEW((byte) 10),
        COMMAND_UPLOAD((byte) 33),
        COMMAND_DOWNLOAD((byte) 37),
        COMMAND_DELETE((byte) 66);

        byte aByte;

        DataType(byte aByte) {
            this.aByte = aByte;
        }

        static DataType getDataTypeFromByte(byte b) {
            if (b == COMMAND_LOGIN.aByte) {
                return COMMAND_LOGIN;
            }
            if (b == COMMAND_VIEW.aByte) {
                return COMMAND_VIEW;
            }
            if (b == COMMAND_UPLOAD.aByte) {
                return COMMAND_UPLOAD;
            }
            if (b == COMMAND_DOWNLOAD.aByte) {
                return COMMAND_DOWNLOAD;
            }
            if (b == COMMAND_DELETE.aByte) {
                return COMMAND_DELETE;
            }
            return EMPTY;
        }
    }

    private DataType type = DataType.EMPTY;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        byte firstByte = buf.readByte();
        type = DataType.getDataTypeFromByte(firstByte);
        if (type == DataType.COMMAND_UPLOAD) {
            // Получение ника клиента
            short nickSize = buf.readShort();
            byte[] nickBytes = new byte[nickSize];
            buf.readBytes(nickBytes);
            String nick = new String(nickBytes);
            ConsoleHelper.printMessage(nick);
            // Получение файла
            short fileNameSize = buf.readShort();
            byte[] fileNameBytes = new byte[fileNameSize];
            buf.readBytes(fileNameBytes);
            String fileName = new String(fileNameBytes);
            ConsoleHelper.printMessage(fileName);
            // Создание директории клиента на сервере
            if (!Files.exists(Paths.get("server_repository/" + nick))) {
                Files.createDirectories(Paths.get("server_repository/" + nick));
            }
            // Загрузка файла
            long size = buf.readLong();
            try (OutputStream out = new BufferedOutputStream(new FileOutputStream("server_repository/" + nick + "/" + fileName))) {
                for (int i = 0; i < size; i++) {
                    out.write(buf.readByte());
                }
            }
            ctx.writeAndFlush("Успешно"); // Отпраувляю назад, ответ уходит в EchoProtocolHandler
        }
        // Тут далее другие условия в зависимости от DataType
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
