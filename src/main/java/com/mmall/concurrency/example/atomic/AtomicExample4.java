package com.mmall.concurrency.example.atomic;

import com.mmall.concurrency.annoations.ThreadSafe;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicReference;

/**
 * AtomicReference
 *
 * @Author heshang.ink
 * @Date 2019/9/19 8:42
 */
@Slf4j
@ThreadSafe
public class AtomicExample4 {

	private static AtomicReference<Integer> count = new AtomicReference<>(0);

	public static void main(String[] args) {
		count.compareAndSet(0, 2);  //2
		count.compareAndSet(0, 1); // no
		count.compareAndSet(1, 3); // no
		count.compareAndSet(2, 4); // 4
		count.compareAndSet(3, 5); // no
		log.info("count:{}", count.get());
	}
}
