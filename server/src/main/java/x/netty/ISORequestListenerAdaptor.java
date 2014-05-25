package x.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;

public class ISORequestListenerAdaptor extends SimpleChannelInboundHandler<ISOMsg> {

    private final ISORequestListener[] listeners;

    public ISORequestListenerAdaptor(ISORequestListener... listeners) {
        this.listeners = listeners;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ISOMsg msg) throws Exception {
        for (ISORequestListener listener : listeners) {
            if (listener.process(new NettyISOSource(ctx), msg)) {
                break;
            }
        }
    }
}
