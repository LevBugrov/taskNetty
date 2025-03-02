package org.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;

/**
 * Handles a server-side channel
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(ServerHandler.class);
    private String clientName;

    @Override
    public void channelActive(final ChannelHandlerContext ctx){
        log.info("connect client");
        ByteBuf message = Unpooled.copiedBuffer("Connection established", StandardCharsets.UTF_8);
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf m = (ByteBuf) msg;
        String msgString = m.toString(StandardCharsets.UTF_8);
        log.info("Message from client: {}", msgString);
        if(msgString.equals("exit")){
            ctx.close();
            return;
        }

        String response2client;
        if (clientName != null)
            response2client = VotingStructure.executeCommand(msgString +" "+ clientName);
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
        log.warn("Probably loose connection with exception: {}", cause);
        //cause.printStackTrace();
        ctx.close();
    }

    private String login(String input){
        if(input.startsWith("login -u=")){
            clientName = input.substring(9);
            log.info("{} login successful", clientName);
            return clientName+" login successful";
        }
        return "Please login with command \"login -u=username\"";
    }

    public String getClientName() { return clientName; }
}