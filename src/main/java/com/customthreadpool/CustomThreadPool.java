package com.customthreadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * TODO
 *
 * @Author heshang.ink
 * @Date 2019/9/29 9:53
 */
public class CustomThreadPool {

	private final static Logger LOGGER = LoggerFactory.getLogger(CustomThreadPool.class);
	private final ReentrantLock lock = new ReentrantLock();

	/**
	 * 最小线程数，也叫核心线程数
	 */
	private volatile int miniSize;
	/**
	 * 最大线程数
	 */
	private volatile int maxSize;
	/**
	 * 线程需要被回收的时间
	 */
	private long keepAliveTime;
	private TimeUnit unit;
	/**
	 * 存放线程的阻塞队列
	 */
	private BlockingQueue<Runnable> workQueue;
	/**
	 * 存放线程池
	 */
	private volatile Set<Worker> workers;
	/**
	 * 提交到线程池中的任务总数
	 */
	private AtomicInteger totalTask = new AtomicInteger();
	/**
	 * 是否关闭线程池标志
	 */
	private AtomicBoolean isShutDown = new AtomicBoolean(false);
	/**
	 * 线程池任务全部执行完毕后的通知组件
	 */
	private Object shutDownNotify = new Object();
	public CustomThreadPool(int miniSize, int maxSize, long keepAliveTime, TimeUnit unit,
	                        BlockingQueue<Runnable> workQueue) {
		this.miniSize = miniSize;
		this.maxSize = maxSize;
		this.keepAliveTime = keepAliveTime;
		this.unit = unit;
		this.workQueue = workQueue;

		workers = new ConcurrentHashSet<>();
	}

	public void execute(Runnable runnable) {
		if (runnable == null) {
			throw new NullPointerException("runnable nullPointerException");
		}

		if (isShutDown.get()) {
			LOGGER.info("线程池已经关闭，不能再提交任务！");
			return;
		}

		//提交的线程 计算
		totalTask.incrementAndGet();

		//小于最小线程数时新建线程
		if (workers.size() < miniSize) {
			addWorker(runnable);
			return;
		}

		// 接下来如果大于最小线程数，能否放入队列
		// 优先会往队列里存放。
		boolean offer = workQueue.offer(runnable);
		//写入队列失败，就需要判断线程的数量了
		if (!offer) {

			/**
			 * 一旦写入失败则会判断当前线程池的大小是否大于最大线程数，如果没有则继续创建线程执行。
			 *
			 * 不然则执行会尝试阻塞写入队列（j.u.c 会在这里执行拒绝策略）
			 */

			//判断此时线程数是否大于最大线程数
			if (workers.size() < maxSize) {
				//创建线程
				addWorker(runnable);
				return;
			} else {
				LOGGER.error("超过最大线程数");

				try {
					// 此时是队列已满，已达到线程最大数，官方是产生拒绝策略
					// 自己实现是阻塞
					workQueue.put(runnable);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

	}

	/**
	 * 添加任务，需要加锁
	 *
	 * @param runnable 任务
	 */
	private void addWorker(Runnable runnable) {
		Worker worker = new Worker(runnable, true);
		worker.startTask();
		workers.add(worker);
		System.out.println("线程池线程数量："+workers.size());
	}

	/**
	 * 工作线程
	 */
	private final class Worker extends Thread {
		private Runnable task;

		private Thread thread;
		/**
		 * true --> 创建新的线程执行
		 * false --> 从队列里获取线程执行
		 */
		private boolean isNewTask;

		public Worker(Runnable task, boolean isNewTask) {
			this.task = task;
			this.isNewTask = isNewTask;
			thread = this;
		}

		public void startTask() {
			thread.start();
		}

		public void close() {
			thread.interrupt();
		}

		@Override
		public void run() {

			Runnable task = null;

			if (isNewTask) {
				task = this.task;
			}

			try {
				/**
				 * 第一步是将创建线程时传过来的workQueue.take()任务执行（task.run）,接着会一直不停的从队列里获取任务执行，直到获取不到新任务了。
				 */
				while ((task != null || (task = getTask()) != null)) {
					try {
						//执行任务
						task.run();
					} finally {
						//任务执行完毕
						task = null;
						// 任务执行完毕后将内置的计数器 -1 ，方便后面任务全部执行完毕进行通知。
						int number = totalTask.decrementAndGet();
						//LOGGER.info("提交到线程池中的任务总数 number = {}", number);
						if (number == 0) {
							synchronized (shutDownNotify) {
								shutDownNotify.notify();
							}
						}
					}
				}
			} finally {
				// worker 线程获取不到任务后退出，需要将自己从线程池中释放掉（workers.remove(this)）。
				// 释放线程
				workers.remove(this);
				// 同时在线程需要回收时都会尝试关闭线程
				tryClose(true);
			}
		}

	}

	/**
	 * 立即关闭线程池，会造成任务丢失
	 */
	public void shutDownNow() {
		isShutDown.set(true);
		tryClose(false);
	}

	public void shutdown() {
		isShutDown.set(false);
		tryClose(true);
	}

	/**
	 * 关闭线程池
	 * @param isTry true 尝试关闭      --> 会等待所有任务执行完毕
	 *              false 立即关闭线程池--> 任务有丢失的可能   
	 */
	private void tryClose(boolean isTry) {
		if (!isTry) {
			closeAllTask();
		} else {
			//多个判断 需要所有任务都执行完毕之后才会去中断线程。
			if (isShutDown.get() && totalTask.get() == 0) {
				closeAllTask();
			}
		} 
	}

	/**
	 * 关闭所有任务
	 */
	private void closeAllTask() {
		for (Worker worker : workers) {
			LOGGER.info("开始关闭");
			worker.close();
		}
	}

	/**
	 * 从队列中获取任务
	 *
	 * @return
	 */
	private Runnable getTask() {
		// 关闭标识及任务是否全部完成
		if (isShutDown.get() && totalTask.get() == 0) {
			return null;
		}

		lock.lock();

		try {
			Runnable task = null;
			if (workers.size() > miniSize) {
				//大于核心线程数时需要用到保活时间获取任务
				task = workQueue.poll(keepAliveTime, unit);
			} else {
				//没有任务了就阻塞
				task = workQueue.take();
			}

			if (task != null) {
				return task;
			}
		} catch (InterruptedException e) {
			return null;
		} finally {
			lock.unlock();
		}
		return null;

	}


	/**
	 * 内部存放工作线程容器，并发安全。
	 *
	 * @param <T>
	 */
	private final class ConcurrentHashSet<T> extends AbstractSet<T> {

		// 这里模仿 HashMap实现hashSet的方法实现这个ConcurrentHashSet
		private ConcurrentHashMap<T, Object> map = new ConcurrentHashMap<>();
		private final Object PRESENT = new Object();

		// 统计容器大小 这个容器里的线程数量，用原子类安全
		private AtomicInteger count = new AtomicInteger();

		@Override
		public Iterator<T> iterator() {
			return map.keySet().iterator();
		}

		@Override
		public boolean add(T t) {
			count.getAndIncrement();
			return map.put(t, PRESENT) == null;
		}

		@Override
		public boolean remove(Object o) {
			count.decrementAndGet();
			return map.remove(o) == PRESENT;
		}

		@Override
		public int size() {
			return count.get();
		}

	}

	/**
	 * 获取工作线程数量
	 *
	 * @return
	 */
	public int getWorkerCount() {
		return workers.size();
	}

}
