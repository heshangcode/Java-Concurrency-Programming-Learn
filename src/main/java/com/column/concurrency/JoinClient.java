package com.column.concurrency;

/**
 * 我们用它能够实现并行化处理。比如主线程需要做两件没有相互依赖的事情，那么可以起 A、B 两个线程分别去做。通过调用 A、B 的 join 方法，让主线程 block 住，直到 A、B 线程的工作全部完成，才继续走下去。
 *
 * @Author heshang.ink
 * @Date 2019/9/20 11:34
 */
public class JoinClient {
	public static void main(String[] args) throws InterruptedException {
		Thread backendDev = createWorker("backed dev", "backend coding");
		Thread frontendDev = createWorker("frontend dev", "frontend coding");
		Thread tester = createWorker("tester", "testing");
		backendDev.start();
		frontendDev.start();

		backendDev.join();
		frontendDev.join();

		tester.start();

	}

	private static Thread createWorker(String role, String work) {
		return new Thread(() -> {
			System.out.println("I finished " + work + " as a " + role);
		});

	}

}
