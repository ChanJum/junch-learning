package cn.junch.crawler.mapper;

import java.util.Collection;

import org.apache.ibatis.annotations.Param;

import cn.junch.crawler.pojo.Item;

public interface ItemMapper {

	public void saveItems(@Param("items") Collection<Item> items);
}
