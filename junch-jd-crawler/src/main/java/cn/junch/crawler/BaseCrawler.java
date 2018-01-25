package cn.junch.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.junch.crawler.mapper.ItemMapper;
import cn.junch.crawler.pojo.Item;
import cn.junch.crawler.service.HttpClientService;
import cn.junch.crawler.thread.ThreadPool;

public abstract class BaseCrawler implements Crawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCrawler.class);

    private HttpClientService httpService;

    private ItemMapper itemMapper;

    private IndexWriter indexWriter;

    /**
     * 开始抓取数据
     */
    public void start() {
        Integer totalPage = getTotalPage();
        // 分页抓取
        for (int i = 1; i <= totalPage; i++) {
            LOGGER.info("当前第{}页，总共{}页。", i, totalPage);
            Collection<Item> items = doStart(i);
            if (items == null) {
                LOGGER.info("抓取到 0 条数据");
                continue;
            }
            LOGGER.info("抓取到{}条数据", items.size());

            // 下载图片，将文档中的图片地址替换成自己的url
            Map<String, String> urlMapping = new HashMap<String, String>();
            for (Item item : items) {
                // 下载商品的图片
                String newName = StringUtils.replace(UUID.randomUUID().toString(), "-", "") + "."
                        + StringUtils.substringAfterLast(item.getImage(), ".");
                urlMapping.put(item.getImage(), newName);

                item.setImage("http://image.taotao.com/jd/" + newName);

                // 下载商品描述中的图片
//                 String desc = item.getItemDesc().getItemDesc();
//                 if (StringUtils.isNotBlank(desc)) {
//					org.jsoup.nodes.Document document = Jsoup.parse(desc);
//					Elements imgs = document.select("img");
//					List<String> find = new ArrayList<String>();
//					List<String> replace = new ArrayList<String>();
//					for (Element element : imgs) {
//						String imgUrl = element.hasAttr("data-lazyload") ? element.attr("data-lazyload") : element.attr("src");
//						String newName2 = StringUtils.replace(UUID.randomUUID().toString(), "-", "") + "." + StringUtils.substringAfterLast(imgUrl, ".");
//						urlMapping.put(imgUrl, newName2);
//						find.add(imgUrl);
//						replace.add("http://image.taotao.com/jd/" + newName2);
//					}
//					desc = StringUtils.replaceEach(desc, find.toArray(new String[] {}), replace.toArray(new String[] {}));
//					//设置回对象中
//					item.getItemDesc().setItemDesc(desc);
//				}
            }
            
            // 启动新线程下载图片
            ThreadPool.runInThread(new ImageDownloadCrawler(urlMapping));

            // 保存商品数据
            saveDataToDB(items);
            LOGGER.info("将数据保存到数据库完成 ({})!", items.size());

            // 写入到索引库
            saveDataToLucene(items);
            LOGGER.info("将数据保存到索引库完成 ({})!", items.size());
        }
    }

    private void saveDataToDB(Collection<Item> items) {
        itemMapper.saveItems(items);
    }

    private void saveDataToLucene(Collection<Item> items) {
        List<Document> docs = new ArrayList<Document>(items.size());
        for (Item item : items) {
            try {
                docs.add(item.toDocument());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            this.indexWriter.addDocuments(docs);
            this.indexWriter.commit();
        } catch (IOException e) {
            LOGGER.error("写入索引库失败!", e);
        }
    }

    public String doGet(String url) throws Exception {
        return this.httpService.doGet(url);
    }

    public String doGet(String url, String encode) throws Exception {
        return this.httpService.doGet(url, encode);
    }

    /**
     * 抓取获取到商品集合
     * 
     * @param page
     * @return
     */
    protected Collection<Item> doStart(Integer page) {
        String url = getPageUrl(page);
        LOGGER.info(" URL is " + url);
        String html = null;
        try {
            html = this.httpService.doGet(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (html == null) {
            return null;
        }
        return doParser(html);
    }

    /**
     * 解析html，生成Item对象
     * 
     * @param html
     * @return
     */
    protected abstract Collection<Item> doParser(String html);

    /**
     * 根据页数得到url
     * 
     * @param page
     * @return
     */
    protected abstract String getPageUrl(Integer page);

    /**
     * 获取总页数
     * 
     * @return
     */
    protected abstract Integer getTotalPage();

    @Override
    public void run() {
        start();
    }

    public void setHttpService(HttpClientService httpService) {
        this.httpService = httpService;
    }

    public void setItemMapper(ItemMapper itemMapper) {
        this.itemMapper = itemMapper;
    }

    public void setIndexWriter(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

}
