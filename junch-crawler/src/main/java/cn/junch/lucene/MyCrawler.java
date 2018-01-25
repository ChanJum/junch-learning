package cn.junch.lucene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import cn.junch.lucene.vo.Item;

/**
 * 抓取 https://list.jd.com/list.html?cat=9987,653,655&page=1 下的所有分页数据
 * 解析返回的数据并转换为item对象，处理数据
 *
 */
public class MyCrawler {

    private static String FETCH_URL ="https://list.jd.com/list.html?cat=9987,653,655&page={page}";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        new MyCrawler().start();
    }

    public void start() throws Exception{
        //1、获取总页数
        String html = doGet(StringUtils.replace(FETCH_URL, "{page}", "1"));

        //1.1、使用jsoup转换html字符串为文档对象
        Document document = Jsoup.parse(html);
        //1.2、获取总页数
        String totalPageStr = document.select(".f-pager i").text();

        System.out.println("总页数为：" + totalPageStr);
        int totalPage = Integer.parseInt(totalPageStr);

        //2、遍历总页数
        for(int i = 1; i <= totalPage; i++){
            System.out.println("总页数为:" + totalPage + "；当前第 " + i + " 页。");
            //3、获取每页的数据
            html = doGet(StringUtils.replace(FETCH_URL, "{page}", i+""));
            document = Jsoup.parse(html);
            //3.1、获取商品列表
            Elements itemElements = document.select(".gl-item");

            Map<Long, Item> itemMap = new HashMap<Long, Item>();
            for (Element ie : itemElements) {
                Item item = new Item();

                String idStr = ie.select("div.j-sku-item").attr("data-sku");
                Long id = Long.parseLong(idStr);
                //id
                item.setId(id);

                //标题
                String title = ie.select(".p-name em").text();
                item.setTitle(title);

                //图片
                String image = ie.select(".p-img img").attr("src");
                if(StringUtils.isBlank(image)){
                    image = ie.select(".p-img img").attr("data-lazy-img");
                }
                item.setImage(image);


                itemMap.put(id, item);
            }

            //价格
            //获取地址：https://p.3.cn/prices/mgets?skuIds=J_3237234,J_3158054,J_3133859
            //返回数据格式：[{"id":"J_3237234","p":"2199.00","m":"3111.00","op":"2299.00"}]
            //获取价格的id列表
            Set<Long> idSet = itemMap.keySet();

            List<String> priceIds = new ArrayList<String>();
            for(Long id1: idSet){
                priceIds.add("J_" + id1);
            }

            String priceJsonStr = doGet("https://p.3.cn/prices/mgets?skuIds=" + StringUtils.join(priceIds, ","));
            ArrayNode priceArrayNode = (ArrayNode)MAPPER.readTree(priceJsonStr);
            for (JsonNode p : priceArrayNode) {
                String pidStr = p.get("id").asText().replaceAll("J_", "");
                Long pid = Long.parseLong(pidStr);
                Long price = p.get("p").asLong();
                //回填价格
                itemMap.get(pid).setPrice(price*100);
            }

            //卖点
            /**
             * 请求地址：https://ad.3.cn/ads/mgets?skuids=AD_3237234。。。
             * 返回数据格式：[{"ad":"","id":"AD_3237234"}]
             */
            List<String> spIds = new ArrayList<String>();
            for(Long id1: idSet){
                spIds.add("AD_" + id1);
            }
            String spJsonStr = doGet("https://ad.3.cn/ads/mgets?skuids=" + StringUtils.join(spIds, ","));
            ArrayNode spArrayNode = (ArrayNode)MAPPER.readTree(spJsonStr);
            for (JsonNode p : spArrayNode) {
                String pidStr = p.get("id").asText().replaceAll("AD_", "");
                Long pid = Long.parseLong(pidStr);
                String sellPoint = p.get("ad").asText();
                //回填卖点
                itemMap.get(pid).setSellPoint(sellPoint);

                System.out.println(itemMap.get(pid));
            }

            //4、解析数据

            if(i == 2){
                break;
            }
        }
    }

    public String doGet(String url) throws Exception {
        //创建httpClient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //创建httpGet连接
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpResponse response = null;
        try {
            //利用httpClient执行httpGet请求
            response = httpClient.execute(httpGet);
            //处理结果
            if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
                return EntityUtils.toString(response.getEntity(), "utf-8");
            }
        } finally {
            if(response != null){
                response.close();
            }
            httpClient.close();
        }
        return null;
    }

}