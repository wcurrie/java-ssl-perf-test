package x.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jpos.iso.ISOMsg;
import x.Messages;
import x.Result;

import java.util.Queue;

public class EchoClientHandler extends SimpleChannelInboundHandler<ISOMsg> {

    private final Queue<Result> results;
    private long sentTime;

    public EchoClientHandler(Queue<Result> results) {
        this.results = results;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ISOMsg ping = Messages.ping();
        sentTime = System.currentTimeMillis();
        ctx.writeAndFlush(ping);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        new Result(sentTime, cause);
        ctx.close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ISOMsg msg) throws Exception {
        long receivedTime = System.currentTimeMillis();
        ctx.close();
        results.add(new Result(sentTime, receivedTime, -1));
    }
}
