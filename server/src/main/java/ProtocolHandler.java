import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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
        }
    }

    private void downloading(ChannelHandlerContext ctx, ByteBuf buf) {

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
            ctx.channel().writeAndFlush(new byte[] {CommandHelper.getErrorDelete()});
        }
    }

    private void viewing(ChannelHandlerContext ctx, ByteBuf buf) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
