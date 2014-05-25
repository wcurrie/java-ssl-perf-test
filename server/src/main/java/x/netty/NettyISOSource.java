package x.netty;

import io.netty.channel.ChannelHandlerContext;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;

import java.io.IOException;

public class NettyISOSource implements ISOSource {
    private final ChannelHandlerContext ctx;

    public NettyISOSource(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void send(ISOMsg m) throws IOException, ISOException {
        ctx.writeAndFlush(m);
    }

    @Override
    public boolean isConnected() {
        return true;
    }
}
