package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Scanner;

public class ServerNetty {
    private static int port;
    private static int numberOfClients;
    private static final Logger log = LoggerFactory.getLogger(ServerNetty.class);

    private static ChannelFuture f;
    private static NioEventLoopGroup bossGroup;
    private static NioEventLoopGroup workerGroup;

    public ServerNetty(int port, int numberOfClients){
        ServerNetty.port = port;
        ServerNetty.numberOfClients = numberOfClients;
    }

    public void run() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup(numberOfClients);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new ServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            f = b.bind(port).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static void shutdown() {
        System.out.println("Stopping server");
        try {
            bossGroup.shutdownGracefully().sync();
            workerGroup.shutdownGracefully().sync();
            f.channel().closeFuture().sync();
        }
        catch (InterruptedException e) {
            log.error(e.toString());
        }
    }


    public static void executeCommand(){
        System.out.println("""
                Available commands:
                load <filename> - loading data from a file
                save <filename> - saving data to a file
                exit - terminating the program""");

        Scanner sc = new Scanner(System.in);
        String input;
        do{
            input = sc.nextLine();
            if(input.startsWith("load ")){
                try{
                    log.info("start load from {}", input.substring(5));
                    VotingStructure.getInstance().load(input.substring(5));
                    System.out.println("load is done!");
                }catch (Exception err){
                    System.out.println("smth went wrong");
                    log.error(err.toString());
                }

            }else if(input.startsWith("save ")){
                try {
                    log.info("start saving as {}", input.substring(5));
                    VotingStructure.getInstance().save(input.substring(5));
                    System.out.println("Save is done!");
                } catch (JsonProcessingException err) {
                    System.out.println("smth went wrong");
                    log.error(err.toString());
                }
            }
        } while(!input.equals("exit"));
        shutdown();
        log.info("Server stopped");
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        int numberOfClients = 10;

        log.info("Start log from {}", ServerNetty.class.getName());
        Thread execute = new Thread(ServerNetty::executeCommand);
        execute.start();
        new ServerNetty(port, numberOfClients).run();
    }
}

