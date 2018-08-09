package com.pinyougou.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import com.pinyougou.pojo.TbSeckillGoodsExample.Criteria;

@Component
public class SeckillTask {

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper seckillGoodsMapper;

	@Scheduled(cron = "0/5 * * * * ?")
	public void refreshSeckillGoods() {
		System.out.println("执行了秒杀商品增量更新任务调度" + new Date());
		// 查询缓存中的秒杀商品ID集合
		List goodsIdList = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
		System.out.println(goodsIdList);
		TbSeckillGoodsExample example = new TbSeckillGoodsExample();
		Criteria criteria = example.createCriteria();
		criteria.andStatusEqualTo("1");// 审核通过的商品
		criteria.andStockCountGreaterThan(0);// 库存数大于0
		criteria.andStartTimeLessThanOrEqualTo(new Date());// 开始日期小于等于当前日期
		criteria.andEndTimeGreaterThanOrEqualTo(new Date());// 截止日期大于等于当前日期

		if (goodsIdList.size() > 0) {
			criteria.andIdNotIn(goodsIdList);// 排除缓存中已经存在的商品数据
		}

		List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

		// 将新的数据再次存入缓存
		for (TbSeckillGoods seckillGoods : seckillGoodsList) {
			redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(), seckillGoods);
			System.out.println("增量更新秒杀商品ID：" + seckillGoods.getId());
		}

		System.out.println("****end*****");
	}

	/**
	 * 移除秒杀商品
	 */
	@Scheduled(cron = "* * * * * ?")
	public void removeSeckillGoods() {
		System.out.println("移除秒杀商品任务在执行");
		// 扫描缓存中秒杀商品列表，发现过期的移除
		List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
		for (TbSeckillGoods seckill : seckillGoodsList) {
			
			if(seckill.getEndTime().getTime() < new Date().getTime()) {// 如果结束日期小于当前日期，则表示过期
				seckillGoodsMapper.updateByPrimaryKey(seckill);// 向数据库保存记录
				redisTemplate.boundHashOps("seckillGoods").delete(seckill.getId());// 移除缓存数据
				System.out.println("移除秒杀商品" + seckill.getId());
			}
		}
		System.out.println("移除秒杀商品任务结束");
	}

}
