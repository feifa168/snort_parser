package com.ids.syslog.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.log4j.Logger;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;

public class NettySyslogClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    private static final Logger logger = Logger.getLogger(NettySyslogClientHandler.class);
    private Channel channel;

    public NettySyslogClientHandler() {
    }
    public NettySyslogClientHandler(Channel channel) {
        this.channel = channel;
    }

    public void setChannel(Channel channel) { this.channel = channel; }
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("client channel is read!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        ByteBuf buf = msg.content();
        byte[] content = new byte[buf.readableBytes()];
        buf.readBytes(content);
        String recvMsg = new String(content);
        logger.info("receive from server msg is " + recvMsg);
    }

    public void sendMessage(final String msg, final InetSocketAddress addr) {
        if (null != msg) {
            sendInternal(datagramPacket(msg, addr));
        }
    }

    public void sendInternal(final DatagramPacket datagram, List<Channel> channelList) {
        for (Channel channel : channelList) {
            if (null != null) {
                channel.writeAndFlush(datagram).addListener(new GenericFutureListener<ChannelFuture>() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        logger.info("send result is " + future.isSuccess());
                    }
                });
            }
        }
    }

    private static DatagramPacket datagramPacket(String msg, InetSocketAddress addr) {
        ByteBuf buf = Unpooled.copiedBuffer(msg, Charset.forName("UTF-8"));
        return new DatagramPacket(buf, addr);
    }

    private void sendInternal(final DatagramPacket datagram) {
        //Channel channel = NettySyslogClient.getChannel();
        logger.info("client channel " + channel);
        if (null != channel) {
            channel.writeAndFlush(datagram).addListener(new GenericFutureListener<ChannelFuture>() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    logger.info("send result is " + future.isSuccess());
                }
            });
        } else {
            throw new NullPointerException("channel is null");
        }
    }
}
