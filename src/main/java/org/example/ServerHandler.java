package org.example;

import io.netty.buffer.ByteBuf;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import java.nio.charset.StandardCharsets;

/**
 * Handles a server-side channel
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private String clienName;

    @Override
    public void channelActive(final ChannelHandlerContext ctx){
        System.out.println("connect");
        ByteBuf message = Unpooled.copiedBuffer("Connection established", StandardCharsets.UTF_8);
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        String msgString = m.toString(StandardCharsets.UTF_8);
        System.out.println(msgString);
        if(msgString.equals("exit")){
            ctx.close();
            return;
        }

        String response2client;
        if (clienName != null)
            response2client = VotingStructure.executeCommand(msgString +" "+ clienName);
        else
            response2client = login(msgString);

        ByteBuf message = Unpooled.copiedBuffer(response2client, StandardCharsets.UTF_8);
        ctx.writeAndFlush(message);
        }

//        ctx.writeAndFlush(msg);


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public String getClienName() { return clienName; }

    private String login(String input){
        if(input.startsWith("login -u=")){
            clienName = input.substring(9);
            return clienName+" login successful";
        }
        return "Please login with command \"login -u=username\"";
    }
}