import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

public class EchoProtocolHandler extends ChannelOutboundHandlerAdapter {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        /* Почему массив байтов - хочу, чтобы сервер возвращал массив, где первый байт это - ответная команда,
         * что делать с полседующими байтами и в зависимости от нее что писать в буфер для ответа клиенту.
         * Пробовал по аналогии с ProtocolHandler писать по порядку сначала байт, потом массив байтов, но почему-то
         * читается только то, что отправил первым */
        byte[] bytes = (byte[]) msg;
        if (bytes[0] == CommandHelper.getCommandUpload()) {
            returnAnswer(ctx, "[Успешно загружено]");
        }
        if (bytes[0] == CommandHelper.getCommandDelete()) {
            returnAnswer(ctx, "[Успешно удалено]");
        }
        if (bytes[0] == CommandHelper.getErrorDelete()) {
            returnAnswer(ctx, "[Данный файл отсутствует]");
        }
    }

    private void returnAnswer(ChannelHandlerContext ctx, String answer) {
        String str = answer + "\n";
        byte[] arr = str.getBytes();
        ByteBuf buf = ctx.alloc().buffer(arr.length);
        buf.writeBytes(arr);
        ctx.writeAndFlush(buf);
        buf.release();
    }
}
