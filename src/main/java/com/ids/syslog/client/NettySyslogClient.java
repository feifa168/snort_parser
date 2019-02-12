package com.ids.syslog.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.GenericFutureListener;

import java.net.InetSocketAddress;

public class NettySyslogClient {

    public NettySyslogClient() {
        bootstrap = new Bootstrap();
        workGroup = new NioEventLoopGroup();
        handler = new NettySyslogClientHandler();
        bootstrap.group(workGroup)
                .channel(NioDatagramChannel.class)
                //.option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<NioDatagramChannel>() {
                    @Override
                    protected void initChannel(NioDatagramChannel ch) throws Exception {
                        ChannelPipeline pipe = ch.pipeline();
                        pipe.addLast(handler);
                    }
                });
    }

    public void start(String host, int port) throws InterruptedException {
        this.host = host;
        this.port = port;

        try {
            channel = bootstrap.bind(0).sync().channel();
            handler.setChannel(channel);
            channel.closeFuture().addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                }
            });
            //channel.closeFuture().await();
        } finally {
            //workGroup.shutdownGracefully();
        }
    }

    public void stop() throws InterruptedException {
        workGroup.shutdownGracefully();
    }

    public <T extends SyslogBuild> void sendMessage(T  t) {
        handler.sendMessage(new String(t.build()), new InetSocketAddress(host, port));
    }

    private Bootstrap bootstrap;
    private NioEventLoopGroup workGroup;
    private Channel channel;
    private String host;
    private int port;
    private NettySyslogClientHandler handler;
}
