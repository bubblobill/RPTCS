package net.rptools.extra.threading;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class ThreadPool {
    private static final Logger log = LogManager.getLogger(ThreadPool.class);
    private static ExecutorService threadPool;
    private static boolean kill = false;
    private static final long THREAD_LIVE_TIME = 120;
    private static final int MAX_THREAD_COUNT = 128000;
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setThreadFactory(MoreExecutors.platformThreadFactory()).setNameFormat("ThreadPool-%d").build();
    private static final SynchronousQueue<Runnable> THREAD_QUEUE = new SynchronousQueue<>();

    private static final RejectedExecutionHandler REJECTED_EXECUTION_HANDLER = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.info("Runnable rejected: " + r.getClass() + "\nExecutor: " + executor.toString());
        }
    };
    private static final Supplier<ExecutorService> poolSupply = () ->
            new ThreadPoolExecutor(0,
                    MAX_THREAD_COUNT,
                    THREAD_LIVE_TIME,
                    TimeUnit.SECONDS,
                    THREAD_QUEUE,
                    THREAD_FACTORY,
                    REJECTED_EXECUTION_HANDLER) {
                @Override
                public String toString() {
                    return "getPoolSize: " + getPoolSize() +
                            "getActiveCount: " + getActiveCount() +
                            "getQueue.size: " + getQueue().size() +
                            "getTaskCount: " + getTaskCount() +
                            super.toString();
                }
            };

    public static ExecutorService getThreadPool() throws RejectedExecutionException {
        if(kill){
            throw new RejectedExecutionException("Pool closed", new Throwable());
        }
        if(threadPool == null || threadPool.isShutdown() || threadPool.isTerminated()){
            threadPool = poolSupply.get();
        }
        return threadPool;
    }

    public static void execute(Runnable r) throws RejectedExecutionException{
        getThreadPool().execute(r);
    }
    public static <T> T execute(Callable<T> c) throws ExecutionException, InterruptedException, TimeoutException {
        Future<T> future = getThreadPool().submit(c);
        try {
            return future.get(1800L, TimeUnit.MILLISECONDS);
        } catch (Exception e){
            log.info(getThreadPool().toString());
        }
        return null;
    }
    public static Future<?> submit(Runnable r){
        return getThreadPool().submit(r);
    }
    public static <T extends Future<?>> Future<T> submit(Runnable r, T result){
        return getThreadPool().submit(r, result);
    }
    public static <T> Future<T> submit(Callable<T> c){
        return getThreadPool().submit(c);
    }
    public static <T> CompletableFuture<T> submitCompletable(Callable<T> c) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        Future<T> future = getThreadPool().submit(c);
            try {
                completableFuture.complete(future.get());
            } catch (Exception e) {
                completableFuture.completeExceptionally(e);
            }
        return completableFuture;
    }
    public static void closePool(){
        shutdownAndAwaitTermination(((ThreadPoolExecutor)getThreadPool()).getTaskCount() * 120L);
    }
    public static void killNow(){
        kill = true;
        shutdownAndAwaitTermination(200);
    }

    private static void shutdownAndAwaitTermination(long milliseconds) {
        kill = true;
        try {
            ExecutorService pool = getThreadPool();
            pool.shutdown(); // Disable new tasks from being submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!pool.awaitTermination(milliseconds, TimeUnit.MILLISECONDS)) {
                    // Cancel currently executing tasks
                    log.info("Threads in pool at kill time: " + getThreadPool().shutdownNow());
                    // Wait a while for tasks to respond to being cancelled
                    if (!pool.awaitTermination(milliseconds, TimeUnit.MILLISECONDS))
                        log.error("Pool did not terminate");
                }
            } catch (InterruptedException ex) {
                // (Re-)Cancel if current thread also interrupted
                pool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        } catch (RejectedExecutionException ree) {
            log.info(ree.getMessage());
        }
    }
    private static class CustomCompletableFuture<T> extends CompletableFuture<T> {
        @Override
        public Executor defaultExecutor() {
            return getThreadPool();
        }
    }
}
