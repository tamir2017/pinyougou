package com.pinyougou.search.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

public class ItemDeleteListener implements MessageListener {

	@Autowired
	private ItemSearchService itemSearchService;
	
	@Override
	public void onMessage(Message message) {

		ObjectMessage objectMessage = (ObjectMessage)message;
		try {
			Long[] goodIds = (Long[]) objectMessage.getObject();
			System.out.println("监听到消息:" + goodIds);
			
			//从索引库中删除
			itemSearchService.deleteByGoodsIds(Arrays.asList(goodIds));
			System.out.println("删除solr索引库");
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
