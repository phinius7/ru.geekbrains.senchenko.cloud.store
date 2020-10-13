import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;

import java.util.logging.Level;

public class EchoProtocolHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        byte[] bytes = (byte[]) msg;

        if (bytes[0] == CommandHelper.getLogOut()) {
            LogHelper.echoLogger.log(Level.INFO, "Command accepted [Log Out]");
            returnBytes(ctx, new byte[]{CommandHelper.getLogOut()});
        }
        if (bytes[0] == CommandHelper.getEMPTY()) {
            LogHelper.echoLogger.log(Level.INFO, "Command accepted [EMPTY]");
            returnBytes(ctx, "ПУСТО".getBytes());
        }
        if (bytes[0] == CommandHelper.getCommandUpload()) {
            LogHelper.echoLogger.log(Level.INFO, "Command accepted [Upload]");
            returnBytes(ctx, "Успешно загружено".getBytes());
        }
        if (bytes[0] == CommandHelper.getCommandDownload()) {
            LogHelper.echoLogger.log(Level.INFO, "Command accepted [Download]");
            returnBytes(ctx, bytes);
        }
        if (bytes[0] == CommandHelper.getCommandView() || bytes[0] == CommandHelper.getCommandLogin()) {
            if (bytes[0] == CommandHelper.getCommandView()) {
                LogHelper.echoLogger.log(Level.INFO, "Command accepted [View]");
            }
            if (bytes[0] == CommandHelper.getCommandLogin()) {
                LogHelper.echoLogger.log(Level.INFO, "Command accepted [Log In]");
            }
            byte[] answer = new byte[bytes.length - 1];
            System.arraycopy(bytes, 1, answer, 0, bytes.length - 1);
            returnBytes(ctx, answer);
        }
        if (bytes[0] == CommandHelper.getCommandDelete()) {
            LogHelper.echoLogger.log(Level.INFO, "Command accepted [Delete]");
            returnBytes(ctx, "Успешно удалено".getBytes());
        }
        if (bytes[0] == CommandHelper.getNotFound()) {
            LogHelper.echoLogger.log(Level.INFO, "Command accepted [Not Found]");
            returnBytes(ctx, "Данный файл отсутствует".getBytes());
        }
        if (bytes[0] == CommandHelper.getNickNotFound()) {
            LogHelper.echoLogger.log(Level.INFO, "Command accepted [Nick Not Found]");
            returnBytes(ctx, new byte[] {CommandHelper.getNickNotFound()});
        }
    }

    private void returnBytes(ChannelHandlerContext ctx, byte[] bytes) {
        ByteBuf buf = ctx.alloc().buffer(bytes.length);
        buf.writeBytes(bytes);
        ctx.writeAndFlush(buf);
        LogHelper.echoLogger.log(Level.INFO, "Data successfully sent");
        ctx.close();
        buf.release();
    }
}
