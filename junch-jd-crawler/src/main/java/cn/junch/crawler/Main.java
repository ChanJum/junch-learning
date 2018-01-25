package cn.junch.crawler;

import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.junch.crawler.thread.ThreadPool;

public class Main {
	
	public static ClassPathXmlApplicationContext applicationContext;

	public static void main(String[] args) {
		applicationContext = new ClassPathXmlApplicationContext("spring/applicationContext*.xml");
		Map<String, Crawler> map = applicationContext.getBeansOfType(Crawler.class);
		for(Crawler cl : map.values()){
			ThreadPool.runInThread(cl);
		}
	}

}
