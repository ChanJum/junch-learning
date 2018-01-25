package cn.junch.lucene;

import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;

public class TestQuery {


    /**
     * 词条搜索
     * @param query
     * @throws Exception
     */
    public void query(Query query) throws Exception {
        // 创建索引存储目录
        Directory directory = FSDirectory.open(new File("D:\\learning\\Lucene\\index"));

        // 创建IndexReader
        IndexReader indexReader = DirectoryReader.open(directory);

        // 创建索引搜索器
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        // 搜索
        TopDocs topDocs = indexSearcher.search(query, 2);

        System.out.println("命中文档数有：" + topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc sd: scoreDocs) {
            Document doc = indexSearcher.doc(sd.doc);
            System.out.println("id = " + doc.get("id"));
            System.out.println("title = " + doc.get("title"));
            System.out.println("price = " + doc.get("price"));
            System.out.println("pic = " + doc.get("pic"));
        }
    }

    /**
     * 测试词条查询
     * @throws Exception
     */
    @Test
    public void testTermQuery() throws Exception{
        Query query = new TermQuery(new Term("title", "apple"));
        this.query(query);
    }

    /**
     * 数值范围搜索
     * @throws Exception
     */
    @Test
    public void testNumericRangeQuery() throws Exception {
        Query query = NumericRangeQuery.newIntRange("id", 10, 20, true, true);
        this.query(query);
    }

    /**
     * 全部匹配查询
     * @throws Exception
     */
    @Test
    public void testMatchAllDocsQuery() throws Exception {
        Query query = new MatchAllDocsQuery();
        this.query(query);
    }

    /**
     * 规则：
     * 1、?代表1个任意字符
     * 2、*代表0或多个任意字符
     * 模糊查询
     * @throws Exception
     */
    @Test
    public void testWildcardQuery() throws Exception {
        Query query = new WildcardQuery(new Term("title", "3*"));
        this.query(query);
    }

    /**
     * 原理：采用编辑距离算法实现
     * 相似度查询
     * @throws Exception
     */
    @Test
    public void testFuzzyQuery() throws Exception {
        Query query = new FuzzyQuery(new Term("title", "appla"));
        this.query(query);
    }

    /**
     * MUST 必须包含
     * MUST_NOT 不能包含
     * SHOULD  或
     *
     * 布尔查询（组合查询）
     * @throws Exception
     */
    @Test
    public void testBooleanQuery() throws Exception{
        BooleanQuery booleanQuery = new BooleanQuery();
        //MUST必须包含
        //查询标题中包含8的文档
/*		booleanQuery.add(new WildcardQuery(new Term("title", "8*")), Occur.MUST);
		//查询id在5-9之间的文档
		booleanQuery.add(NumericRangeQuery.newIntRange("id", 5, 10, true, false), Occur.MUST);
*/
        //SHOULD 或者
        booleanQuery.add(new WildcardQuery(new Term("title", "3*")), BooleanClause.Occur.SHOULD);//11
        booleanQuery.add(NumericRangeQuery.newIntRange("id", 5, 10, true, false), BooleanClause.Occur.SHOULD);//5

        //查询的数据
        query(booleanQuery);
    }

    /**
     * 创建索引
     * @throws IOException
     */
    @Test
    public void testIndexWriter() throws IOException {

        //创建索引存放目录
        Directory directory = FSDirectory.open(new File("D:\\learning\\Lucene\\index"));

        //创建标准分词器
        //Analyzer analyzer = new StandardAnalyzer();
        Analyzer analyzer = new IKAnalyzer();

        //创建索引编写配置对象
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_4_10_2, analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);//每次对索引目录都是重新开始编写，如果已经存在则删除后重写
        //indexWriterConfig.setOpenMode(OpenMode.APPEND);//每次对索引目录都是重新开始编写，如果已经存在则删除后重写

        //创建索引编写对象
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);

        for(int i = 1; i <=100; i++){
            //创建文档（相当于数据库表中的一条记录），field 相当于 表的字段
            Document document = new Document();
            document.add(new IntField("id", i, Field.Store.YES));
            document.add(new TextField("title", "Apple iPhone 7 Plus (A1661) 128G 玫瑰金色 移动联通电信4G手机.军陈" + i, Field.Store.YES));
            document.add(new LongField("price", 7188L, Field.Store.YES));
            document.add(new StringField("pic", "//img13.360buyimg.com/n1/s450x450_jfs/t3235/100/1618018440/139400/44fd706e/57d11c33N5cd57490.jpg", Field.Store.YES));

            //添加文档
            indexWriter.addDocument(document);
        }

        //提交文件
        indexWriter.commit();

        //关闭
        indexWriter.close();

    }

}
