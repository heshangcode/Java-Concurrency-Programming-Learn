package com.column.concurrency.interrupt;

/**
 * TODO
 *
 * @Author heshang.ink
 * @Date 2019/9/20 11:15
 */
public class InterruptedClient {
	public static void main(String[] args) throws InterruptedException {
		Thread thread = new Thread(() -> {
			for (int i = 0; i < 100; i++) {
				System.out.println("I'm doing my work");
//原代码  System.out.println("I'm interrupted?"+Thread.currentThread().isInterrupted());
				System.out.println("I'm interrupted?" + Thread.interrupted());
			}
		});
		/*   while (!isInterrupted()) {
			//do somenting
		}   */
		thread.start();
		Thread.sleep(1);
		thread.interrupt();
	}
}
