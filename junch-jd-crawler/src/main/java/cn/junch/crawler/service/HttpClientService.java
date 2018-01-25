package cn.junch.crawler.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HttpClientService {
	
	private static final String CHARSET = "utf-8";

	@Autowired(required = false)
	private CloseableHttpClient httpClient;

	@Autowired(required = false)
	private RequestConfig requestConfig;
	
	public String doGet(String url) throws ClientProtocolException, IOException, URISyntaxException {
		return doGet(url, CHARSET);
	}

	/**
	 * 执行get请求
	 * 
	 * @param url
	 * @param encode
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public String doGet(String url, String encode) throws ClientProtocolException, IOException, URISyntaxException {
		
		// 创建httpGet连接
		HttpGet httpGet = new HttpGet(url);
		httpGet.setConfig(requestConfig);

		CloseableHttpResponse response = null;
		try {
			// 利用httpClient执行httpGet请求
			response = httpClient.execute(httpGet);
			// 处理结果
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				if(StringUtils.isBlank(encode)){
					encode = CHARSET;
				}
				String content = EntityUtils.toString(response.getEntity(), encode);
				return content;
			}
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return null;
	}
	
    /**
     * 下载文件
     * 
     * @param url 文件url
     * @param dest 目标目录
     * @throws Exception
     */
    public void downloadFile(String url, File dest) throws Exception {
    	if(url.startsWith("//")){
    		url = "http:" + url;
    	}
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = httpClient.execute(httpGet);
        try {
            FileUtils.writeByteArrayToFile(dest, IOUtils.toByteArray(response.getEntity().getContent()));
        } finally {
            response.close();
        }
    }


}
