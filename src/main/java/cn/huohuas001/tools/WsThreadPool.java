package cn.huohuas001.tools;

/**
 * WebSocket消息发送全局线程池
 */
public class WsThreadPool {
    /*// 核心线程数 = CPU核心数 * 2
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    // 最大线程数（突发流量缓冲）
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 4;
    private static final ThreadPoolExecutor sharedPool = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new NamedThreadFactory("WS-Sender"),
            new ThreadPoolExecutor.CallerRunsPolicy() // 饱和策略
    );
    // 线程空闲存活时间
    private static final long KEEP_ALIVE_TIME = 30L;
    // 队列容量（防止OOM）
    private static final int QUEUE_CAPACITY = 10_000;*/

    /**
     * 提交发送任务

    public static void submitTask(Runnable task) {
        try {
            sharedPool.execute(task);
        } catch (RejectedExecutionException e) {
            System.err.println("Task rejected: " + e.getMessage());
            // 可扩展为降级处理逻辑
        }
     }*/

    /**
     * 优雅关闭线程池

     public static void shutdown() {
        sharedPool.shutdown();
        try {
            if (!sharedPool.awaitTermination(60, TimeUnit.SECONDS)) {
                sharedPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            sharedPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 获取线程池状态（监控用）

     public static String getPoolStatus() {
        return String.format(
                "Pool Status: [Active: %d, Pool: %d, Queue: %d/%d, Completed: %d]",
                sharedPool.getActiveCount(),
                sharedPool.getPoolSize(),
                sharedPool.getQueue().size(),
                sharedPool.getQueue().remainingCapacity(),
                sharedPool.getCompletedTaskCount()
        );
     }*/
}
