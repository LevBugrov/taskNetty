package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            ByteBuf in = (ByteBuf) msg;
            System.out.println(in.toString(StandardCharsets.UTF_8));
            String msg2server;

            msg2server = ClientForm.getRequest();
            sendText(ctx, msg2server);

            if (msg2server.equals("exit")) { ctx.close(); }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public void sendText(ChannelHandlerContext ctx, String text){
        ByteBuf message = Unpooled.copiedBuffer(text, StandardCharsets.UTF_8);
        ctx.writeAndFlush(message);
    }
}