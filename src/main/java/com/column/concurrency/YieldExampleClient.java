package com.column.concurrency;

/**
 * yield 方法我们平时并不常用。yield 单词的意思是让路，在多线程中意味着本线程愿意放弃 CPU 资源，也就是可以让出 CPU 资源。不过这只是给 CPU 一个提示，当 CPU 资源并不紧张时，则会无视 yield
 * 提醒。如果 CPU 没有无视 yield 提醒，那么当前 CPU 会从 RUNNING 变为 RUNNABLE 状态，此时其它等待 CPU 的 RUNNABLE 线程，会去竞争 CPU 资源。讲到这里有个问题，刚刚 yield
 * 的线程同为 RUNNABLE 状态，是否也会参与竞争再次获得 CPU 资源呢？经过我大量测试，刚刚 yield 的线程是不会马上参与竞争获得 CPU 资源的。
 *
 * @Author heshang.ink
 * @Date 2019/9/20 11:01
 */
public class YieldExampleClient {
	public static void main(String[] args) {
		Thread xiaoming = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				System.out.println("小明--" + i);
				if (i == 2) {
					Thread.yield();
				}
			}
		});

		Thread jianguo = new Thread(() -> {
			for (int i = 0; i < 10; i++) {
				System.out.println("建国--" + i);
			}
		});
		xiaoming.start();
		jianguo.start();

	}
}
