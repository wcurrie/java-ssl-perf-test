package x.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.XMLPackager;
import x.Messages;

import java.io.EOFException;
import java.nio.charset.Charset;
import java.util.List;

public class ISOMsgDecoder extends LineBasedFrameDecoder {

    private StringBuilder sb;
    private int depth;

    public ISOMsgDecoder() {
        super(65*1024);
        this.sb = new StringBuilder();
        this.depth = 0;
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        ByteBuf oneLine = (ByteBuf) super.decode(ctx, buffer);
        if (oneLine == null) {
            return null;
        }
        ISOMsg msg = null;
        String line = oneLine.toString(Charset.defaultCharset());
        sb.append(line);
        if (line.contains("<" + XMLPackager.ISOMSG_TAG))
            depth++;
        if (line.contains("</" + XMLPackager.ISOMSG_TAG + ">")) {
            if (--depth == 0) {
                msg = Messages.unpack(sb.toString().getBytes());
                sb = new StringBuilder();
            }
        }
        return msg;
    }

}
