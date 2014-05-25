package x.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.jpos.iso.ISOMsg;
import x.Messages;

public class ISOMsgEncoder extends MessageToByteEncoder<ISOMsg> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ISOMsg msg, ByteBuf out) throws Exception {
        out.writeBytes(Messages.pack(msg));
    }
}
