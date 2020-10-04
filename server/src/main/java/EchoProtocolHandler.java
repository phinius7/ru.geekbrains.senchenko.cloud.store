import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.Arrays;

public class EchoProtocolHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        /* Почему массив байтов - хочу, чтобы сервер возвращал массив, где первый байт это - ответная команда,
         * что делать с полседующими байтами и в зависимости от нее что писать в буфер для ответа клиенту.
         * Пробовал по аналогии с ProtocolHandler писать по порядку сначала байт, потом массив байтов, но почему-то
         * читается только то, что отправил первым */
        byte[] bytes = (byte[]) msg;
        if (bytes[0] == CommandHelper.getEMPTY()) {
            returnBytes(ctx, "Файлы отсутствуют".getBytes());
        }
        if (bytes[0] == CommandHelper.getCommandUpload()) {
            returnBytes(ctx, "Успешно загружено".getBytes());
        }
        if (bytes[0] == CommandHelper.getCommandDownload()) {
            returnBytes(ctx, bytes);
        }
        if (bytes[0] == CommandHelper.getCommandView()) {
            byte[] answer = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, answer, 0, bytes.length - 1);
            returnBytes(ctx, answer);
        }
        if (bytes[0] == CommandHelper.getCommandDelete()) {
            returnBytes(ctx, "Успешно удалено".getBytes());
        }
        if (bytes[0] == CommandHelper.getNotFound()) {
            returnBytes(ctx, "Данный файл отсутствует".getBytes());
        }
    }

    private void returnBytes(ChannelHandlerContext ctx, byte[] bytes) {
        ByteBuf buf = ctx.alloc().buffer(bytes.length);
        buf.writeBytes(bytes);
        ctx.writeAndFlush(buf);
        ctx.close();
        buf.release();
    }
}
