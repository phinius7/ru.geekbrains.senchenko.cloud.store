import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.logging.Level;

public class ProtocolHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = ((ByteBuf) msg);


        byte firstByte = buf.readByte();
        if (firstByte == CommandHelper.getCommandLogin()) {
            LogHelper.protocolLogger.log(Level.INFO, "Start to Authorization");
            authorization(ctx, buf);
        }
        if (firstByte == CommandHelper.getCommandUpload()) {
            LogHelper.protocolLogger.log(Level.INFO, "Start to Upload");
            uploading(ctx, buf);
        }
        if (firstByte == CommandHelper.getCommandDownload()) {
            LogHelper.protocolLogger.log(Level.INFO, "Start to Download");
            downloading(ctx, buf);
        }
        if (firstByte == CommandHelper.getCommandDelete()) {
            LogHelper.protocolLogger.log(Level.INFO, "Start to Delete");
            deleting(ctx, buf);
        }
        if (firstByte == CommandHelper.getCommandView()) {
            LogHelper.protocolLogger.log(Level.INFO, "Start to View");
            viewing(ctx, buf);
        }
    }

    private void authorization(ChannelHandlerContext ctx, ByteBuf buf) {
        short loginSize = buf.readShort();
        byte[] loginBytes = new byte[loginSize];
        buf.readBytes(loginBytes);
        String login = new String(loginBytes);
        short passwordSize = buf.readShort();
        byte[] passBytes = new byte[passwordSize];
        buf.readBytes(passBytes);
        String password = new String(passBytes);
        String nick;
        try {
            nick = ServiceSQL.getNickByLoginAndPass(login, password);
            if (nick != null) {
                byte[] nickBytes = nick.getBytes();
                byte[] resBytes = new byte[nickBytes.length + 1];
                resBytes[0] = CommandHelper.getCommandLogin();
                System.arraycopy(nickBytes, 0, resBytes, 1, nickBytes.length);
                LogHelper.protocolLogger.log(Level.CONFIG, nick + " is exist. Access is Allowed");
                ctx.channel().writeAndFlush(resBytes);
            } else {
                LogHelper.protocolLogger.log(Level.CONFIG, "Nick from this data is not found. Access is Denied");
                ctx.channel().writeAndFlush(new byte[]{CommandHelper.getNickNotFound()});
            }
        } catch (SQLException e) {
            LogHelper.protocolLogger.log(Level.WARNING, "SQL exception detected");
            e.printStackTrace();
        } finally {
            ctx.close();
        }
    }

    private void uploading(ChannelHandlerContext ctx, ByteBuf buf) {
        short nickSize = buf.readShort();
        byte[] nickBytes = new byte[nickSize];
        buf.readBytes(nickBytes);
        String nick = new String(nickBytes);
        try {
            if (ServiceSQL.isLogin(nick)) {
                LogHelper.protocolLogger.log(Level.INFO, "Log in confirmed");
                short fileNameSize = buf.readShort();
                byte[] fileNameBytes = new byte[fileNameSize];
                buf.readBytes(fileNameBytes);
                String fileName = new String(fileNameBytes);
                if (!Files.exists(Paths.get("server_repository/" + nick))) {
                    Files.createDirectories(Paths.get("server_repository/" + nick));
                    LogHelper.protocolLogger.log(Level.INFO, "Directory is created");
                }
                long size = buf.readLong();
                try (OutputStream out = new BufferedOutputStream(new FileOutputStream("server_repository/" + nick + "/" + fileName))) {
                    for (int i = 0; i < size; i++) {
                        out.write(buf.readByte());
                    }
                    LogHelper.protocolLogger.log(Level.INFO, "Upload success");
                    ctx.channel().writeAndFlush(new byte[]{CommandHelper.getCommandUpload()});
                }
            } else {
                LogHelper.protocolLogger.log(Level.WARNING, "Log out detected");
                ctx.channel().writeAndFlush(new byte[]{CommandHelper.getLogOut()});
            }
        } catch (SQLException | IOException e) {
            LogHelper.protocolLogger.log(Level.WARNING, "Exception detected");
            e.printStackTrace();
        } finally {
            ctx.close();
        }
    }

    private void downloading(ChannelHandlerContext ctx, ByteBuf buf) {
        short nickSize = buf.readShort();
        byte[] nickBytes = new byte[nickSize];
        buf.readBytes(nickBytes);
        String nick = new String(nickBytes);
        try {
            if (ServiceSQL.isLogin(nick)) {
                LogHelper.protocolLogger.log(Level.INFO, "Log in confirmed");
                short fileNameSize = buf.readShort();
                byte[] fileNameBytes = new byte[fileNameSize];
                buf.readBytes(fileNameBytes);
                String fileName = new String(fileNameBytes);
                try {
                    if (Files.exists(Paths.get("server_repository/" + nick + "/" + fileName))) {
                        LogHelper.protocolLogger.log(Level.INFO, "File found");
                        byte[] fileBytes = Files.readAllBytes(Paths.get("server_repository/" + nick + "/" + fileName));
                        byte[] resBytes = new byte[fileBytes.length + 1];
                        resBytes[0] = CommandHelper.getCommandDownload();
                        System.arraycopy(fileBytes, 0, resBytes, 1, fileBytes.length);
                        ctx.channel().writeAndFlush(resBytes);
                        LogHelper.protocolLogger.log(Level.INFO, "File sent");
                    } else {
                        LogHelper.protocolLogger.log(Level.INFO, "File not found");
                        ctx.channel().writeAndFlush(new byte[]{CommandHelper.getNotFound()});
                    }
                } catch (IOException e) {
                    LogHelper.protocolLogger.log(Level.WARNING, "IOException detected");
                    e.printStackTrace();
                }
            } else {
                LogHelper.protocolLogger.log(Level.WARNING, "Log out detected");
                ctx.channel().writeAndFlush(new byte[]{CommandHelper.getLogOut()});
            }
        } catch (SQLException e) {
            LogHelper.protocolLogger.log(Level.WARNING, "SQL exception detected");
            e.printStackTrace();
        } finally {
            ctx.close();
        }
    }

    private void deleting(ChannelHandlerContext ctx, ByteBuf buf) {
        short nickSize = buf.readShort();
        byte[] nickBytes = new byte[nickSize];
        buf.readBytes(nickBytes);
        String nick = new String(nickBytes);
        try {
            if (ServiceSQL.isLogin(nick)) {
                LogHelper.protocolLogger.log(Level.INFO, "Log in confirmed");
                short fileNameSize = buf.readShort();
                byte[] fileNameBytes = new byte[fileNameSize];
                buf.readBytes(fileNameBytes);
                String fileName = new String(fileNameBytes);
                if (Files.exists(Paths.get("server_repository/" + nick + "/" + fileName))) {
                    Files.delete(Paths.get("server_repository/" + nick + "/" + fileName));
                    LogHelper.protocolLogger.log(Level.INFO, "File found and deleted");
                    ctx.channel().writeAndFlush(new byte[]{CommandHelper.getCommandDelete()});
                } else {
                    LogHelper.protocolLogger.log(Level.INFO, "File not found");
                    ctx.channel().writeAndFlush(new byte[]{CommandHelper.getNotFound()});
                }
            } else {
                LogHelper.protocolLogger.log(Level.WARNING, "Log out detected");
                ctx.channel().writeAndFlush(new byte[]{CommandHelper.getLogOut()});
            }
        } catch (SQLException | IOException e) {
            LogHelper.protocolLogger.log(Level.WARNING, "Exception detected");
            e.printStackTrace();
        } finally {
            ctx.close();
        }
    }

    private void viewing(ChannelHandlerContext ctx, ByteBuf buf) {
        short nickSize = buf.readShort();
        byte[] nickBytes = new byte[nickSize];
        buf.readBytes(nickBytes);
        String nick = new String(nickBytes);
        try {
            if (ServiceSQL.isLogin(nick)) {
                LogHelper.protocolLogger.log(Level.INFO, "Log in confirmed");
                if (Files.exists(Paths.get("server_repository/" + nick))) {
                    StringBuilder sb = new StringBuilder();
                    Path dir = Paths.get("server_repository/" + nick);
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                        for (Path file : stream) {
                            sb.append(file.getFileName()).append(";");
                        }
                    } catch (IOException | DirectoryIteratorException e) {
                        e.printStackTrace();
                    }
                    String resultLine = sb.toString().trim();
                    byte[] bytes = resultLine.getBytes();
                    if (bytes.length != 0) {
                        LogHelper.protocolLogger.log(Level.INFO, "Files found in repository");
                        byte[] resBytes = new byte[bytes.length + 1];
                        resBytes[0] = CommandHelper.getCommandView();
                        System.arraycopy(bytes, 0, resBytes, 1, bytes.length);
                        ctx.channel().writeAndFlush(resBytes);
                    } else {
                        LogHelper.protocolLogger.log(Level.INFO, "Empty in repository");
                        ctx.channel().writeAndFlush(new byte[]{CommandHelper.getEMPTY()});
                    }
                } else {
                    LogHelper.protocolLogger.log(Level.INFO, "Repository is not found");
                    ctx.channel().writeAndFlush(new byte[]{CommandHelper.getEMPTY()});
                }
            } else {
                LogHelper.protocolLogger.log(Level.WARNING, "Log out detected");
                ctx.channel().writeAndFlush(new byte[]{CommandHelper.getLogOut()});
            }
        } catch (SQLException e) {
            LogHelper.protocolLogger.log(Level.WARNING, "SQL exception detected");
            e.printStackTrace();
        } finally {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
