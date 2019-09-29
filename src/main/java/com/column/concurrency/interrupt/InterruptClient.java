package com.column.concurrency.interrupt;

/**
 * interrrup 的意思是打断。调用了 interrupt 方法后，线程会怎么样？不知道你的答案是什么。我在第一次学习 interrupt 的时候，第一感觉是让线程中断。其实，并不是这样。inerrupt
 * 方法的作用是让可中断方法，比如让 sleep 中断。也就是说其中断的并不是线程的逻辑，中断的是线程的阻塞。
 * <p>
 * 调用 interrupte 方法，并不会影响可中断方法之外的逻辑。线程不会中断，会继续执行。这里的中断概念并不是指中断线程；
 * 一旦调用了 interrupte 方法，那么线程的 interrupted 状态会一直为 ture（没有通过调用可中断方法或者其他方式主动清除标识的情况下）；
 * 通过上面实现我们了解了 interrupte 方法中断的不是线程。它中断的其实是可中断方法，如 sleep 。可中断方法被中断后，会把 interrupted 状态归位，改回 false 。
 *
 * @Author heshang.ink
 * @Date 2019/9/20 11:09
 */
public class InterruptClient {
	public static void main(String[] args) throws InterruptedException {
		Thread thread = new Thread(() -> {
			for (int i = 0; i < 100; i++) {
				System.out.println("I'm doing my work");
				System.out.println("I'm interrupted?" + Thread.currentThread().isInterrupted());
			}
		});
		thread.start();
		Thread.sleep(1);
		thread.interrupt();
	}
}
