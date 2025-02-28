package org.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class ServerNetty {
    private static int port;
    private static int numberOfClients;

    public ServerNetty(int port, int numberOfClients){
        ServerNetty.port = port;
        ServerNetty.numberOfClients = numberOfClients;
    }

    public void run() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup(numberOfClients);
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

            ChannelFuture f = b.bind(port).sync();

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void executeCommand(Logger logger){
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
                VotingStructure.getInstance().load(input.substring(5));
                logger.info("start saving as "+input.substring(5));
            }else if(input.startsWith("save ")){
                VotingStructure.getInstance().load(input.substring(5));
                logger.info("start saving as "+input.substring(5));
            }
        } while(!input.equals("exit"));
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        int numberOfClients = 10;
//        new ServerNetty(port, numberOfClients).run();

        Logger logger = Logger.getLogger(ServerNetty.class.getName());
        logger.setUseParentHandlers(false);
        FileHandler fileHandler = new FileHandler("src/main/resources/status.log");
        logger.addHandler(fileHandler);

        logger.info("Start log from "+ServerNetty.class.getName());
        executeCommand(logger);
        logger.info("Server exit");


    }
}

