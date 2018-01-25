package cn.junch.crawler.thread;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;

import cn.junch.crawler.Main;
import cn.junch.crawler.service.PropertieService;

public class ThreadPool {

	// 线程池维护线程的最少数量
	private static final int COREPOOLSIZE = 2;
	// 线程池维护线程的最大数量
	private static final int MAXINUMPOOLSIZE = Integer.valueOf(Main.applicationContext.getBean(PropertieService.class).MAX_POOL_SIZE);
	// 线程池维护线程所允许的空闲时间
	private static final long KEEPALIVETIME = 4;
	// 线程池维护线程所允许的空闲时间的单位
	private static final TimeUnit UNIT = TimeUnit.SECONDS;
	// 线程池所使用的缓冲队列,这里队列大小为3
	private static final BlockingQueue<Runnable> WORKQUEUE = new ArrayBlockingQueue<Runnable>(3);
	// 线程池对拒绝任务的处理策略：AbortPolicy为抛出异常；CallerRunsPolicy为重试添加当前的任务，他会自动重复调用execute()方法；DiscardOldestPolicy为抛弃旧的任务，DiscardPolicy为抛弃当前的任务
	private static final AbortPolicy HANDLER = new ThreadPoolExecutor.AbortPolicy();
	
	private static ThreadPoolExecutor threadPool =  new ThreadPoolExecutor(COREPOOLSIZE, MAXINUMPOOLSIZE, KEEPALIVETIME, UNIT, WORKQUEUE, HANDLER);
	
	/**
	 * 加入到线程池中执行
	 * 
	 * @param runnable
	 */
	public static void runInThread(Runnable runnable) {
		threadPool.execute(runnable);  
	}

}
