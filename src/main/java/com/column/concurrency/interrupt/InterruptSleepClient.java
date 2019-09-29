package com.column.concurrency.interrupt;

/**
 * interrupte 方法中断的不是线程。它中断的其实是可中断方法，如 sleep 。可中断方法被中断后，会把 interrupted 状态归位，改回 false 。
 *
 * @Author heshang.ink
 * @Date 2019/9/20 11:13
 */
public class InterruptSleepClient {
	public static void main(String[] args) throws InterruptedException {
		Thread xiaopang = new Thread(() -> {
			for (int i = 0; i < 100; i++) {
				System.out.println("I'm doing my work");
				try {
					System.out.println("I will sleep");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					System.out.println("My sleeping was interrupted");
				}
				System.out.println("I'm interrupted?" + Thread.currentThread().isInterrupted());
			}
		});
		xiaopang.start();
		Thread.sleep(1);
		xiaopang.interrupt();
	}
}
