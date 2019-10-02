package com.longguo.手写可重入锁;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 用synchronized notify wait实现可重入锁部分功能
 * 可重入性实现关键 threadId lockCount 那几个判断
 * @Author heshang.ink
 * @Date 2019/10/1 16:33
 */
public class CustomReentrantLock implements Lock {

	/**
	 * false 没有被锁
	 * true 被锁
	 */
	private boolean isLocked = false;

	/**
	 * 当前持有锁的线程
	 */
	private Thread threadId = null;

	/**
	 * 锁的数量
	 */
	private int lockCount = 0;

	@Override
	public synchronized void lock() {
		Thread currentThread = Thread.currentThread();

		while (isLocked && threadId != currentThread) { //进来的线程不是持有锁的线程，等待
			//锁被用，其他的线程阻塞
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		isLocked = true;
		threadId = currentThread;
		lockCount++;
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {

	}

	@Override
	public boolean tryLock() {
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public synchronized void unlock() {
		// 是这个线程持有了锁
		if (threadId == Thread.currentThread()) {
			lockCount--;
			if (lockCount == 0) {
				isLocked = false;
				//呼起其他线程的锁
				notify();
			}
		}

	}

	@Override
	public Condition newCondition() {
		return null;
	}
}
