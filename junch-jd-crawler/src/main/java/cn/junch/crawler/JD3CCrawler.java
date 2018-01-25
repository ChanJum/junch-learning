package cn.junch.crawler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.junch.crawler.pojo.Item;
import cn.junch.crawler.pojo.ItemDesc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class JD3CCrawler extends BaseCrawler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JD3CCrawler.class);

    private String baseUrl;

    private Long cid;

    private static final String DETAIL_URL = "http://d.3.cn/desc/{id}";

    private static final String PRICE_URL = "http://p.3.cn/prices/mgets?skuIds=";

    private static final String AD_URL = "http://ad.3.cn/ads/mgets?skuids=";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * 
     * @param baseUrl like http://list.jd.com/list.html?cat=737,794,798&page={page}
     */
    public JD3CCrawler(String baseUrl, Long cid) {
        this.baseUrl = baseUrl;
        this.cid = cid;
    }

    @Override
    protected Collection<Item> doParser(String html) {
        Document document = Jsoup.parse(html);// 解析列表页面
        Elements lis = document.select("#plist li.gl-item");// 获取到商品列表的
        Map<String, Item> items = new HashMap<String, Item>();
        for (Element li : lis) {
            Item item = new Item();
            String id = li.select("div[data-sku]").attr("data-sku");
            String title = li.select(".p-name").text();
            String image = li.select(".p-img img").attr("src");
            if(StringUtils.isBlank(image)){
            	image = li.select(".p-img img").attr("data-lazy-img");
            }
            String desc = getContent(id);
            desc = StringUtils.replace(desc, "data-lazyload", "src");
            item.setId(Long.valueOf(id));
            item.setImage(image);
            item.setTitle(title);
            item.setNum(99999L);
            item.setCid(this.cid);
            item.setStatus(1);

            ItemDesc itemDesc = new ItemDesc();
            itemDesc.setItemId(item.getId());
            itemDesc.setItemDesc(desc);

            item.setItemDesc(itemDesc);

            items.put(id, item);
        }

        // 获取价格
        List<String> ids = new ArrayList<String>();
        for (String id : items.keySet()) {
            ids.add("J_" + id);
        }
        try {
            String priceJson = doGet(PRICE_URL + StringUtils.join(ids, ','));
            ArrayNode arrayNode = (ArrayNode) MAPPER.readTree(priceJson);
            for (JsonNode jsonNode : arrayNode) {
                String id = StringUtils.substringAfter(jsonNode.get("id").asText(), "_");
                Long price = jsonNode.get("p").asLong() * 100;
                items.get(id).setPrice(price);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 获取卖点（广告）
        ids = new ArrayList<String>();
        for (String id : items.keySet()) {
            ids.add("AD_" + id);
        }
        try {
            String adJson = doGet(AD_URL + StringUtils.join(ids, ','));
            ArrayNode arrayNode = (ArrayNode) MAPPER.readTree(adJson);
            for (JsonNode jsonNode : arrayNode) {
                String id = StringUtils.substringAfter(jsonNode.get("id").asText(), "_");
                String ad = jsonNode.get("ad").asText();
                items.get(id).setSellPoint(ad);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items.values();
    }

    private String getContent(String id) {
        String url = StringUtils.replace(DETAIL_URL, "{id}", id);
        String html = null;
        try {
            html = super.doGet(url, "GBK");
            if (StringUtils.contains(html,"404 Not Found")) {
                LOGGER.info("查询不到商品描述数据....... url = " + url);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (StringUtils.isNotBlank(html)) {
			String jsonData = StringUtils.substringAfter(html, "showdesc(");
			jsonData = StringUtils.substringBeforeLast(jsonData, ")");
			// 解析json获取内容数据
			try {
				JsonNode jsonNode = MAPPER.readTree(jsonData);
				return jsonNode.get("content").asText();
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		return null;
    }

    @Override
    protected String getPageUrl(Integer page) {
        return StringUtils.replace(this.baseUrl, "{page}", page + "");
    }

    @Override
    protected Integer getTotalPage() {
        String html = null;
        try {
            html = super.doGet(getPageUrl(1));
        } catch (Exception e) {
            LOGGER.error("getTotalPage error !", e);
            return 0;
        }
        Document document = Jsoup.parse(html);
        String pageHtml = document.select(".p-skip").html();
        String[] no = pageHtml.split("\\D+");
        return Integer.valueOf(no[1]);
    }

}
