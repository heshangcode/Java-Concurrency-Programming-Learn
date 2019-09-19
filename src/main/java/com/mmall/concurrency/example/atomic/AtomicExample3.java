package com.mmall.concurrency.example.atomic;

import com.mmall.concurrency.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.LongAdder;

/**
 * LongAdder
 * LongAdder和AtomicLong的区别、首先LongAdder
 * <p>
 * jvm会把64位的long和double的读或写拆分成2个32位的操作，LongAdder核心将AtomicLong里的内部核心数据value分离成一个数组，每个线程访问时，通过hash算法，让一个数字计数，最后这个计数为这个数组的求和累加，其中热点数据value分成独立的cell，每个cell维护自己单元的值，当前最后那个值为cell的累计和成，热点分离提高了并行度，相当于在AtomicLong
 * 的基础上，把单点的更新压力分散到了各个节点上，在低并发的时候通过直接对base的修改，性能跟AtomicLong差不多，而在高并发的时候通过分散提高了性能。
 * 缺点LongAdder在统计的时候如果有并发更新，可能导致统计的数据有误差。
 *
 * @Author heshang.ink
 * @Date 2019/9/19 8:08
 */
@Slf4j
@ThreadSafe
public class AtomicExample3 {
	// 请求总数
	public static int clientTotal = 5000;

	// 同时并发执行的线程数
	public static int threadTotal = 200;

	public static LongAdder count = new LongAdder();

	public static void main(String[] args) throws InterruptedException {
		ExecutorService executorService = Executors.newCachedThreadPool();

		Semaphore semaphore = new Semaphore(threadTotal);

		CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
		for (int i = 0; i < clientTotal; i++) {
			executorService.execute(() -> {
				try {
					semaphore.acquire();
					add();
					semaphore.release();
				} catch (InterruptedException e) {
					log.error("exception", e);
				}
				countDownLatch.countDown();
			});
		}
		countDownLatch.await();
		executorService.shutdown();
		log.info("count:{}", count);
	}

	private static void add() {
		count.increment();
	}

}
