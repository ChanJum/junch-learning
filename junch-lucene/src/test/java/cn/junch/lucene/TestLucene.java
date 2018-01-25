package cn.junch.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class TestLucene {

    /**
     * 创建索引
     * @throws IOException
     */
    @Test
    public void TestIndexWrite() throws IOException {
        /**
         *  步骤：
         *  1、指定索引库目录位置
         *  2、	创建IndexWriterConfig配置对象：Lucene版本，分词器；
         *  3、创建索引库核心对象IndexWriter;
         *  4、创建文档对象；
         *  5、IndexWriter将文档写入索引库；
         *  6、提交、关闭。
         */
        //创建索引目录
        Directory directory = FSDirectory.open(new File("D:\\learning\\Lucene\\index"));
        //创建标准分词器
        Analyzer analyzer = new StandardAnalyzer();
        //索引配置
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_2, analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        //写索引
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

        //创建文档对象
        Document doc = new Document();
        doc.add(new IntField("id", 10, Field.Store.YES));
        doc.add(new TextField("title", "Apple iPhone 6s Plus (A1699) 64G 金色 移动联通电信4G手机", Field.Store.YES));
        doc.add(new LongField("price", 6388L, Field.Store.YES));
        doc.add(new StringField("pic", "http://item.jd.com/bigimage.aspx?id=1861095", Field.Store.YES));

        //添加文档
        indexWriter.addDocument(doc);

        indexWriter.commit();
        indexWriter.close();

    }

    /**
     * 查看词汇列表
     * @throws Exception
     */
    @Test
    public void testTokenStream() throws Exception {
        Analyzer analyzer = new StandardAnalyzer();

        //词汇列表
        TokenStream tokenStream = analyzer.tokenStream("title", "Apple iPhone 6s Plus (A1699) 64G 金色 移动联通电信4G手机");

        //tokenStream指针指向开始位置
        tokenStream.reset();

        //设置分词偏移量引用
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);

        //设置分词词语引用
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

        //遍历词汇列表
        while(tokenStream.incrementToken()){
            //分词开始位置
            System.out.println("分词开始位置：" + offsetAttribute.startOffset());
            //分词词语
            System.out.println("最小分词单元：" + charTermAttribute);
            //分词结束位置
            System.out.println("分词结束位置：" + offsetAttribute.endOffset());
        }

    }

    /**
     * 步骤：
     * 1、	指定索引库目录位置；
     * 2、	读取索引库索引；
     * 3、	创建搜索索引库核心对象：IndexSearcher；
     * 4、	搜索：搜索词条，关键词；
     * 5、	循环获取文档id；
     * 6、	根据文档id获取文档对象。
     *
     * 搜索索引
     * @throws IOException
     */
    @Test
    public void testIndexSearch() throws IOException {
        //创建索引目录
        Directory directory = FSDirectory.open(new File("D:\\learning\\Lucene\\index"));
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(reader);

        //创建查询对象，搜索词条
        Query query = new TermQuery(new Term("title","apple"));

        //搜索，前10条
        TopDocs topDocs = indexSearcher.search(query, 10);

        System.out.println("命中文档数:" + topDocs.totalHits);

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("id为：" + doc.get("id"));
            System.out.println("标题为：" + doc.get("title"));
            System.out.println("价格为：" + doc.get("price"));
            System.out.println("图片为：" + doc.get("pic"));
        }

    }

    /**
     * 分词器搜索
     * @throws IOException
     * @throws ParseException
     */
    @Test
    public void testIndexSearchByAnalyzer() throws IOException, ParseException {
        //创建索引目录
        Directory directory = FSDirectory.open(new File("D:\\learning\\Lucene\\index"));
        IndexReader reader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(reader);

        //创建分词器
        Analyzer analyzer = new StandardAnalyzer();

        //创建查询解析器
        QueryParser queryParser = new QueryParser("title", analyzer);

        //创建搜索词条
        Query query = queryParser.parse("手机");

        //搜索
        TopDocs topDocs = indexSearcher.search(query, 10);

        System.out.println("命中文档数:" + topDocs.totalHits);

        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("id为：" + doc.get("id"));
            System.out.println("标题为：" + doc.get("title"));
            System.out.println("价格为：" + doc.get("price"));
            System.out.println("图片为：" + doc.get("pic"));
        }

    }
}
