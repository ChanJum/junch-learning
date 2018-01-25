package cn.junch.crawler.search;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLEncoder;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class TestSearch {
	
	ClassPathXmlApplicationContext applicationContext;

	@Before
	public void setUp() throws Exception {
		applicationContext = new ClassPathXmlApplicationContext("spring/application*.xml");
	}

	@Test
	public void testPage() throws IOException, ParseException {
		//获取索引目录
		Directory directory = applicationContext.getBean(Directory.class);
		//创建索引搜索对象
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		String keyword = "小米";
		
		//创建查询对象
		Query query = new QueryParser("title", new IKAnalyzer()).parse(keyword);
		
		//构建分页信息
		Integer page = 2;
		Integer pageSize = 10;
		Integer startIndex = (page -1 )*pageSize;
		Integer endIndex = startIndex + pageSize;
		//搜索
		TopDocs topDocs = indexSearcher.search(query, endIndex);
		
		//计算总页数
		Integer totalPageCount = (topDocs.totalHits % pageSize == 0)?(topDocs.totalHits / pageSize):(topDocs.totalHits / pageSize +1);
		
		System.out.println(String.format("总共%d条记录，当前第%d页，共%d页", topDocs.totalHits, page, totalPageCount));
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (int i = startIndex; i < endIndex; i++) {
			
			Document document = indexSearcher.doc(scoreDocs[i].doc);
			System.out.println("ID：" + document.get("id"));
			System.out.println("标题：" + document.get("title"));
			System.out.println("价格：" + document.get("price"));
			System.out.println("卖点：" + document.get("sellPoint"));
		}
	}
	
	@Test
	public void testHighLight() throws IOException, ParseException, InvalidTokenOffsetsException {
		//获取索引目录
		Directory directory = applicationContext.getBean(Directory.class);
		//创建索引搜索对象
		IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		String keyword = "小米";
		IKAnalyzer analyzer = new IKAnalyzer();
		//创建查询对象
		Query query = new QueryParser("title", analyzer).parse(keyword);
		
		//定义高亮组件
		Formatter format = new SimpleHTMLFormatter("<span class='red'>", "</span>");
		//构建Scorer，用于选择最佳切片
		Scorer scorer = new QueryScorer(query);
		//实例化Highlighter组件
		Highlighter highlighter = new Highlighter(format,scorer);
		//设置文档切片
		highlighter.setTextFragmenter(new SimpleFragmenter(100));
		highlighter.setEncoder(new SimpleHTMLEncoder());
		
		//构建分页信息
		Integer page = 2;
		Integer pageSize = 10;
		Integer startIndex = (page -1 )*pageSize;
		Integer endIndex = startIndex + pageSize;
		//搜索
		TopDocs topDocs = indexSearcher.search(query, endIndex);
		
		//计算总页数
		Integer totalPageCount = (topDocs.totalHits % pageSize == 0)?(topDocs.totalHits / pageSize):(topDocs.totalHits / pageSize +1);
		
		System.out.println(String.format("总共%d条记录，当前第%d页，共%d页", topDocs.totalHits, page, totalPageCount));
		
		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		for (int i = startIndex; i < endIndex; i++) {
			Document document = indexSearcher.doc(scoreDocs[i].doc);
			System.out.println("ID：" + document.get("id"));
			System.out.println("标题：" + document.get("title"));
			System.out.println("高亮显示的标题：" 
			+ highlighter.getBestFragment(analyzer, "title", document.get("title")));
			System.out.println("价格：" + document.get("price"));
			System.out.println("卖点：" + document.get("sellPoint"));
		}
	}

}
