package x.netty;

import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.GenericFutureListener;
import x.Result;

import java.util.Queue;

public class FailureListener implements GenericFutureListener<ChannelFuture> {
    private final Queue<Result> resultQueue;

    public FailureListener(Queue<Result> resultQueue) {
        this.resultQueue = resultQueue;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            resultQueue.add(new Result(System.currentTimeMillis(), future.cause()));
        }
    }
}
