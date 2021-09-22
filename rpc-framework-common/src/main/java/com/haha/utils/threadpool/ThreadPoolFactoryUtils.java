package com.haha.utils.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 线程池工具类
 */
@Slf4j
public class ThreadPoolFactoryUtils {
    /**
     * 通过 threadNamePrefix 来区分不同线程池（我们可以把相同 threadNamePrefix 的线程池看作是为同一业务场景服务）。
     * key: threadNamePrefix
     * value: threadPool
     */
    private static final Map<String, ExecutorService> THREAD_POOLS =  new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtils(){

    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix){
        CustomedThreadPoolConfig customedThreadPoolConfig = new CustomedThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(customedThreadPoolConfig, threadNamePrefix, false);
    }



    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomedThreadPoolConfig customThreadPoolConfig) {
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    private static ExecutorService createCustomThreadPoolIfAbsent(CustomedThreadPoolConfig customedThreadPoolConfig, String threadNamePrefix, boolean daemon) {
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(customedThreadPoolConfig, threadNamePrefix, daemon));
        // 如果 threadPool 被 shutdown 的话就重新创建一个
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadNamePrefix);
            threadPool = createThreadPool(customedThreadPoolConfig, threadNamePrefix, daemon);
            THREAD_POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;

    }


    private static ExecutorService createThreadPool(CustomedThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon){
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix,daemon);
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(), customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(), customThreadPoolConfig.getUnit(), customThreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    /**
     * 关闭所有线程池
     *
     */
    public static void shutDownAllThreadPool() {
        log.info("线程池关闭中~~~");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry->{
            ExecutorService value = entry.getValue();
            value.shutdown();
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), value.isTerminated());
            try {
                value.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool never terminated");
                value.shutdownNow();
            }
        });
    }

    /**
     * 创建 ThreadFactory 。如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     *
     * @param threadNamePrefix 作为创建的线程名字的前缀
     * @param daemon           指定是否为 Daemon Thread(守护线程)
     * @return ThreadFactory
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        if(threadNamePrefix!=null){
            if(daemon!=null){
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix+"-%d")
                        .setDaemon(daemon).build();
            }else{
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix+"-%d")
                        .build();
            }
        }
        return Executors.defaultThreadFactory();
    }
    /**
     * 打印线程池状态
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool){
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1,createThreadFactory("print-thread-pool-status", false));
        scheduledThreadPoolExecutor.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);

    }

}
