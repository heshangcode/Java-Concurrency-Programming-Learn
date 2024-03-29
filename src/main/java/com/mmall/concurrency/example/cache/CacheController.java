package com.mmall.concurrency.example.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 验证Redis
 *
 * @Author heshang.ink
 * @Date 2019/9/23 9:10
 */
@Controller
@RequestMapping("/cache")
public class CacheController {
	@Autowired
	private RedisClient redisClient;

	@RequestMapping("/set")
	@ResponseBody
	public String set(@RequestParam("k") String k, @RequestParam("v") String v) throws Exception {
		redisClient.set(k, v);
		return "SUCCESS";
	}

	@RequestMapping("/get")
	@ResponseBody
	public String set(@RequestParam("k") String k) throws Exception {
		return redisClient.get(k);
	}
}
