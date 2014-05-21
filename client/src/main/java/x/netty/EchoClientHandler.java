package x.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jpos.iso.ISOMsg;
import x.Messages;
import x.Result;

import java.util.*;

public class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final Queue<Result> results;
    private long sentTime;

    public EchoClientHandler(Queue<Result> results) {
        this.results = results;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        byte[] pack = Messages.pack(Messages.ping());
//        System.out.println("active, writing [" + new String(pack) + "]");
        sentTime = System.currentTimeMillis();
        ctx.writeAndFlush(Unpooled.copiedBuffer(pack));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        ByteBuf byteBuf = msg.readBytes(msg.readableBytes());
        ISOMsg response = Messages.unpack(byteBuf.array());
//        System.out.println("Client received: " + response);
        long receivedTime = System.currentTimeMillis();
        ctx.close();
        results.add(new Result(sentTime, receivedTime, -1));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        new Result(sentTime, cause);
        ctx.close();
    }
}
