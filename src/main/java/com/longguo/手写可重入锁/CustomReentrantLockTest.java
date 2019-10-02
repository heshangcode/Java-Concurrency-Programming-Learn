package com.longguo.手写可重入锁;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 手写可重入锁测试类
 *
 * @Author heshang.ink
 * @Date 2019/10/1 16:34
 */
public class CustomReentrantLockTest {

	private CustomReentrantLock lock = new CustomReentrantLock();

	private int value = 0;

	public void a() {
		lock.lock();
		System.out.println("A");
		b();
		lock.unlock();
	}

	public void b() {
		lock.lock();
		System.out.println("B");
		lock.unlock();
	}

	public int getValue() {
		lock.lock();
		try {
			Thread.sleep(300);
			return value++;
		} catch (InterruptedException e) {
			throw new RuntimeException("");
		}finally {
			lock.unlock();
		}
	}

	public static void main(String[] args) {
		ExecutorService executorService = Executors.newFixedThreadPool(3);
		CustomReentrantLockTest lockTest = new CustomReentrantLockTest();
		executorService.execute(() -> {
			lockTest.a();
		});
		for (int i = 0; i < 3; i++) {
			executorService.execute(()->{
				while (true) {
					System.out.println(lockTest.getValue());
				}

			});
		}


	}
}
