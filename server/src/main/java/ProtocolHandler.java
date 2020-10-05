import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {
    // TODO заменить хелпер на логи

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = ((ByteBuf) msg);
        byte firstByte = buf.readByte();
        if (firstByte == CommandHelper.getCommandLogin()) {
            try {
                authorization(ctx, buf);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (firstByte == CommandHelper.getCommandUpload()) {
            try {
                uploading(ctx, buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (firstByte == CommandHelper.getCommandDownload()) {
            downloading(ctx, buf);
        }
        if (firstByte == CommandHelper.getCommandDelete()) {
            try {
                deleting(ctx, buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (firstByte == CommandHelper.getCommandView()) {
            viewing(ctx, buf);
        }
    }

    private void authorization(ChannelHandlerContext ctx, ByteBuf buf) throws SQLException {
        // Получение логина
        short loginSize = buf.readShort();
        byte[] loginBytes = new byte[loginSize];
        buf.readBytes(loginBytes);
        String login = new String(loginBytes);
        // Если написать логин на русском языке то тогда не корректно проходит чтение short passwordSize = buf.readShort();
        // Получение пароля
        short passwordSize = buf.readShort();
        byte[] passBytes = new byte[passwordSize];
        buf.readBytes(passBytes);
        String password = new String(passBytes);
        // Проверка логина и пароля
        String nick = ServiceSQL.getNickByLoginAndPass(login, password);
        if (nick != null) {
            byte[] nickBytes = nick.getBytes();
            byte[] resBytes = new byte[nickBytes.length + 1];
            resBytes[0] = CommandHelper.getCommandLogin();
            System.arraycopy(nickBytes, 0, resBytes, 1, nickBytes.length);
            CommandHelper.printMessage("Nick is exist");
            ctx.channel().writeAndFlush(resBytes);
        } else {
            CommandHelper.printMessage("Nick NOT FND Error");
            ctx.channel().writeAndFlush(new byte[]{CommandHelper.getNickNotFound()});
        }
        ctx.close();
    }

    private void uploading(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        // Получение ника клиента
        short nickSize = buf.readShort();
        byte[] nickBytes = new byte[nickSize];
        buf.readBytes(nickBytes);
        String nick = new String(nickBytes);
        // Получение файла
        short fileNameSize = buf.readShort();
        byte[] fileNameBytes = new byte[fileNameSize];
        buf.readBytes(fileNameBytes);
        String fileName = new String(fileNameBytes);
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
            CommandHelper.printMessage("UPL Success");
            ctx.channel().writeAndFlush(new byte[] {CommandHelper.getCommandUpload()});
            ctx.close();
        }
    }

    private void downloading(ChannelHandlerContext ctx, ByteBuf buf) {
        short nickSize = buf.readShort();
        byte[] nickBytes = new byte[nickSize];
        buf.readBytes(nickBytes);
        String nick = new String(nickBytes);
        short fileNameSize = buf.readShort();
        byte[] fileNameBytes = new byte[fileNameSize];
        buf.readBytes(fileNameBytes);
        String fileName = new String(fileNameBytes);
        try {
            if (Files.exists(Paths.get("server_repository/" + nick + "/" + fileName))) {
                byte[] fileBytes = Files.readAllBytes(Paths.get("server_repository/" + nick + "/" + fileName));
                byte[] resBytes = new byte[fileBytes.length + 1];
                resBytes[0] = CommandHelper.getCommandDownload();
                System.arraycopy(fileBytes, 0, resBytes, 1, fileBytes.length);
                CommandHelper.printMessage("File is exist");
                ctx.channel().writeAndFlush(resBytes);
            } else {
                CommandHelper.printMessage("NOT FND Error");
                ctx.channel().writeAndFlush(new byte[] {CommandHelper.getNotFound()});
            }
            ctx.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void deleting(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        // Получение ника клиента
        short nickSize = buf.readShort();
        byte[] nickBytes = new byte[nickSize];
        buf.readBytes(nickBytes);
        String nick = new String(nickBytes);
        // Получение файла
        short fileNameSize = buf.readShort();
        byte[] fileNameBytes = new byte[fileNameSize];
        buf.readBytes(fileNameBytes);
        String fileName = new String(fileNameBytes);
        if (Files.exists(Paths.get("server_repository/" + nick + "/" + fileName))) {
            Files.delete(Paths.get("server_repository/" + nick + "/" + fileName));
            CommandHelper.printMessage("DEL Success");
            ctx.channel().writeAndFlush(new byte[] {CommandHelper.getCommandDelete()});
        } else {
            CommandHelper.printMessage("DEL Error");
            ctx.channel().writeAndFlush(new byte[] {CommandHelper.getNotFound()});
        }
        ctx.close();
    }

    private void viewing(ChannelHandlerContext ctx, ByteBuf buf) {
        short nickSize = buf.readShort();
        byte[] nickBytes = new byte[nickSize];
        buf.readBytes(nickBytes);
        String nick = new String(nickBytes);
        if (Files.exists(Paths.get("server_repository/" + nick))) {
            StringBuilder sb = new StringBuilder();
            Path dir = Paths.get("server_repository/" + nick);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path file: stream) {
                    sb.append(file.getFileName()).append(" ");
                }
            } catch (IOException | DirectoryIteratorException e) {
                e.printStackTrace();
            }
            String resultLine = sb.toString().trim();
            byte[] bytes = resultLine.getBytes();
            if (bytes.length != 0) {
                CommandHelper.printMessage("VIEW Success {" + resultLine + "}");
                byte[] resBytes = new byte[bytes.length + 1];
                resBytes[0] = CommandHelper.getCommandView();
                System.arraycopy(bytes, 0, resBytes, 1, bytes.length);
                ctx.channel().writeAndFlush(resBytes);
            } else {
                CommandHelper.printMessage("EMPTY");
                ctx.channel().writeAndFlush(new byte[]{CommandHelper.getEMPTY()});
            }
        } else {
            ctx.channel().writeAndFlush(new byte[]{CommandHelper.getEMPTY()});
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
