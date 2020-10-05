import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        byte firstByte = buf.readByte();
        if (firstByte == CommandHelper.getCommandUpload()) {
            uploading(ctx, buf);
        }
        if (firstByte == CommandHelper.getCommandDownload()) {
            downloading(ctx, buf);
        }
        if (firstByte == CommandHelper.getCommandDelete()) {
            deleting(ctx, buf);
        }
        if (firstByte == CommandHelper.getCommandView()) {
            viewing(ctx, buf);
        }
    }

    private void uploading(ChannelHandlerContext ctx, ByteBuf buf) throws IOException {
        // Получение ника клиента
        short nickSize = buf.readShort();
        byte[] nickBytes = new byte[nickSize];
        buf.readBytes(nickBytes);
        String nick = new String(nickBytes);
        CommandHelper.printMessage(nick);
        // Получение файла
        short fileNameSize = buf.readShort();
        byte[] fileNameBytes = new byte[fileNameSize];
        buf.readBytes(fileNameBytes);
        String fileName = new String(fileNameBytes);
        CommandHelper.printMessage(fileName);
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
            ctx.channel().writeAndFlush(new byte[] {CommandHelper.getCommandUpload()}); // Отправляю назад, ответ уходит в EchoProtocolHandler
            ctx.close();
        }
    }

    private void downloading(ChannelHandlerContext ctx, ByteBuf buf) {
        short nickSize = buf.readShort();
        byte[] nickBytes = new byte[nickSize];
        buf.readBytes(nickBytes);
        String nick = new String(nickBytes);
        CommandHelper.printMessage(nick);
        short fileNameSize = buf.readShort();
        byte[] fileNameBytes = new byte[fileNameSize];
        buf.readBytes(fileNameBytes);
        String fileName = new String(fileNameBytes);
        CommandHelper.printMessage(fileName);
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
        CommandHelper.printMessage(nick);
        // Получение файла
        short fileNameSize = buf.readShort();
        byte[] fileNameBytes = new byte[fileNameSize];
        buf.readBytes(fileNameBytes);
        String fileName = new String(fileNameBytes);
        CommandHelper.printMessage(fileName);
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
        CommandHelper.printMessage(nick);
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
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
