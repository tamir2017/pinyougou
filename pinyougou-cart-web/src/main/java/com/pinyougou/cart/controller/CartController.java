package com.pinyougou.cart.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;

import entity.Result;

@RestController
@RequestMapping("/cart")
public class CartController {
	
	@Reference(timeout=6000)
	private CartService cartService;
	
	@Autowired
	private  HttpServletRequest request;
	
	@Autowired
	private  HttpServletResponse response;
	
	
	/**
	 * 购物车列表
	 * @param request
	 * @return
	 */
	@RequestMapping("/findCartList")
	public List<Cart> findCartList(){
		//当前登录人帐号
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录人: "+name);
		
		String cartListString = util.CookieUtil.getCookieValue(request, "cartList","UTF-8");
		if(cartListString==null || cartListString.equals("")){
			cartListString="[]";
		}
		List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
		
		if(name.equals("anonymousUser")){//未登录
			//从cookie中提取购物车
			System.out.println("从cookie中提取购物车");
			return cartList_cookie;	
		}else{//已登录
			//获取redis购物车
			List<Cart> cartList_redis = cartService.findCartListFromRedis(name);
			if(cartList_cookie.size()>0){//判断本地购物车有数据
				//合并购物车
				List<Cart> cartList = cartService.mergeCartList(cartList_redis, cartList_cookie);
				//将合并后的购物车存入redis
				cartService.saveCartListToRedis(name, cartList);
				//本地购物车清除
				util.CookieUtil.deleteCookie(request, response, "cartList");
				System.out.println("执行了合并购物车逻辑");
				return cartList;
			}
			return cartList_redis;
		}
	}
	
	/**
	 * 添加商品到购物车
	 * @param request
	 * @param response
	 * @param itemId
	 * @param num
	 * @return
	 */
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num){
		//当前登录人帐号
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		System.out.println("当前登录人: "+name);
		
		try {
			//1.获取购物车列表
			List<Cart> cartList =findCartList();
			//2.调用服务方法操作购物车
			cartList = cartService.addGoodsToCartList(cartList, itemId, num);	
			//3.将新的购物车存入cookie 或者 redis 中
			if(name.equals("anonymousUser")){//未登录，存入cookie
				System.out.println("向cookie中存储购物车");
				util.CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList),3600*24,"UTF-8");
			}else{//已登录，存入redis 中
				cartService.saveCartListToRedis(name, cartList);
			}
			return new Result(true, "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}	

}
