package cn.huohuas001.tools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private final AtomicInteger threadCounter = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;
    private final int priority;

    /**
     * 创建非守护线程工厂
     */
    public NamedThreadFactory(String poolName) {
        this(poolName, false, Thread.NORM_PRIORITY);
    }

    /**
     * 完整参数构造函数
     */
    public NamedThreadFactory(String poolName, boolean daemon, int priority) {
        this.namePrefix = poolName + "-";
        this.daemon = daemon;
        this.priority = Math.min(Thread.MAX_PRIORITY,
                Math.max(Thread.MIN_PRIORITY, priority));
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, namePrefix + threadCounter.getAndIncrement());

        // 基本配置
        thread.setDaemon(daemon);
        thread.setPriority(priority);

        // 异常处理（推荐使用项目统一的异常处理）
        thread.setUncaughtExceptionHandler((t, e) -> {
            System.err.printf("Thread %s failed: %s%n", t.getName(), e.getMessage());
            e.printStackTrace();
        });

        return thread;
    }
}
