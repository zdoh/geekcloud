package ru.crabushka.geekcloud.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private String name;
    private boolean logged;
    private String rootDirectory = "rootDir";


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.write("Hello \n");
        ctx.write("Lets start");
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) return;
            if (msg instanceof AbstractMessage) {
                proccessMsg
            }

        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.write("Got an error");
        System.out.println(cause.getMessage());
        ctx.close();
    }
}
