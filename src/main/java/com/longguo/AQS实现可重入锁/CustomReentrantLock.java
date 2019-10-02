package com.longguo.AQS实现可重入锁;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 利用AQS的tryAcquire和tryRelease来实现一个可重入锁
 *
 * @Author heshang.ink
 * @Date 2019/10/2 17:45
 */
public class CustomReentrantLock implements Lock {
	private Helper helper = new Helper();

	//用一个内部类来继承AQS
	private class Helper extends AbstractQueuedSynchronizer {
		/**
		 * 获取锁
		 *
		 * @param arg
		 * @return
		 */
		@Override
		protected boolean tryAcquire(int arg) {
			// 第一个线程进来，可以获取锁
			// 第二个线程进来，无法获取锁，返回false

			//进来的线程
			Thread thread = Thread.currentThread();

			int state = getState();
			//当前资源没有锁
			if (state == 0) {
				//利用cas实现锁定
				if (compareAndSetState(0, arg)) {// 如果当前状态值等于预期值，则以原子方式将同步状态设置为给定的更新值
					// 设置当前线程
					setExclusiveOwnerThread(thread);
					return true;
				}
			} else if (getExclusiveOwnerThread() == thread) {   // 允许重入锁,当前线程和当前保存的线程是同一个线程
				setState(state + 1);
				return true;
			}

			return true;
		}

		/**
		 * 释放锁
		 * 此方法总是由正在执行释放的线程调用。
		 *
		 * @param arg
		 * @return
		 */
		@Override
		protected boolean tryRelease(int arg) {
			Thread thread = Thread.currentThread();
			// 锁的获取和释放肯定是一一对应的，那么调用此方法的线程一定是当前线程
			if (thread != getExclusiveOwnerThread()) {
				throw new RuntimeException();
			}

			// 主要用来判断是否是重入锁
			boolean flag = false;
			int state = getState() - arg;

			if (state == 0) {   // 当前锁的状态正确
				setExclusiveOwnerThread(null);
				flag = true;
			}
			setState(state);
			return flag;
		}

	}

	@Override
	public void lock() {
		helper.acquire(1);
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		helper.acquireInterruptibly(1);
	}

	@Override
	public boolean tryLock() {
		return helper.tryAcquire(1);
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		return helper.tryAcquireNanos(1, unit.toNanos(time));
	}

	@Override
	public void unlock() {
		helper.tryRelease(1);
	}

	@Override
	public Condition newCondition() {
		return null;
	}
}
