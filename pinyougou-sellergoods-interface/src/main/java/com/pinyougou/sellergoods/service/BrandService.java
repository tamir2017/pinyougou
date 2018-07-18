package com.pinyougou.sellergoods.service;

import java.util.List;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

/**
 * 品牌接口
 * @author lenovo
 *
 */
public interface BrandService {

	/**
	 * 查询数据库中所有品牌信息
	 * @return
	 */
	public List<TbBrand> findAll();
	
	/**
	 * 品牌分页
	 * @param pageNum  当前页面
	 * @param pageSize  每页记录数
	 * @return
	 */
	public PageResult findPage(int pageNum, int pageSize);
	
	/**
	 * 增加品牌
	 * @param brand
	 */
	public void add(TbBrand brand);
	
	/**
	 * 根据ID查询品牌
	 * @param id
	 * @return
	 */
	public TbBrand findOne(Long id);
	
	/**
	 * 修改品牌
	 * @param brand
	 */
	public void update(TbBrand brand);
	
	/**
	 * 批量删除品牌
	 * @param ids
	 */
	public void delete(Long[] ids);
	
	/**
	 * 品牌分页
	 * @param pageNum  当前页面
	 * @param pageSize  每页记录数
	 * @return
	 */
	public PageResult findPage(TbBrand brand,int pageNum, int pageSize);
}
