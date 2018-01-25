package cn.junch.crawler.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PropertieService {
	
	@Value("${IMAGE_DIR}")
	public String IMAGE_DIR;
	
	@Value("${MAX_POOL_SIZE}")
	public String MAX_POOL_SIZE;

}
