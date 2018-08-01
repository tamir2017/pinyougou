package com.pinyougou.manager.controller;
import java.util.Arrays;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	//@Reference(timeout=100000)
	//private ItemSearchService itemSearchService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult  findPage(int page,int rows){			
		return goodsService.findPage(page, rows);
	}
	

	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			
			//
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	@Autowired
	private Destination queueSolrDeleteDestination;
	
	@Autowired
	private Destination topicPageDeleteDestination;
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);
			//从索引库中删除
			//itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
			
			JmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			
			//删除每个服务器上的静态页面
			JmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				
				@Override
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});
			
			
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param brand
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}
	

	@Autowired
	private JmsTemplate JmsTemplate;
	
	@Autowired
	private Destination queueSolrDestination;//用来导入solr索引库的目标对象(点对点)
	
	@Autowired
	private Destination topicPageDestination;//用来生成商品详细页的目标对象(发布订阅)
	
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			
			if("1".equals(status)){//审核通过
				//*****导入索引库
				//得到需要导入的SKU列表
				List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
				//导入到solr
				//itemSearchService.importList(itemList);
				
				//转换为json传输
				final String jsonString = JSON.toJSONString(itemList);
				
				//将消息发送给消息中间件JMS
				JmsTemplate.send(queueSolrDestination, new MessageCreator() {
					
					@Override
					public Message createMessage(Session session) throws JMSException {
						
						return session.createTextMessage(jsonString);
					}
				});
				
				//*****生成商品详情页
				for(final Long goodsId : ids){
				//	itemPageService.genItemHtml(goodsId);
					
					//将消息发送给消息中间件JMS
					JmsTemplate.send(topicPageDestination, new MessageCreator() {
						
						@Override
						public Message createMessage(Session session) throws JMSException {
							return session.createTextMessage(goodsId + "");
						}
					});
				}
				
				
				
			}
			return new Result(true, "修改状态成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改状态失败");
		}
	}
	
	//@Reference(timeout=40000)
	//private ItemPageService itemPageService;
	
	/**
	 * 生成静态页（测试）
	 * @param goodsId
	 */
	//@RequestMapping("/genHtml")
	//public void genHtml(Long goodsId){
	//	itemPageService.genItemHtml(goodsId);	
	//}

	
	
	
	
	
	
}
