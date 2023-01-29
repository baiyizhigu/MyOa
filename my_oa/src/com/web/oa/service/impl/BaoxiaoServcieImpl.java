package com.web.oa.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.oa.mapper.BaoxiaoBillMapper;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.pojo.BaoxiaoBillExample;
import com.web.oa.service.BaoxiaoService;

@Service
public class BaoxiaoServcieImpl implements BaoxiaoService {
	
	@Autowired
	private BaoxiaoBillMapper baoxiaoBillMapper;

	@Override
	public void saveBaoxiao(BaoxiaoBill baoxiaoBill) {
		// TODO Auto-generated method stub
		Long id = baoxiaoBill.getId();
		if (id==null) {
			baoxiaoBillMapper.insert(baoxiaoBill);
		}
		
		else{
			//1：执行update的操作，完成更新
			baoxiaoBillMapper.updateByPrimaryKey(baoxiaoBill);
		}
	}

	@Override
	public List<BaoxiaoBill> findLeaveBillListByUser(long userid) {
		// TODO Auto-generated method stub
		BaoxiaoBillExample example = new BaoxiaoBillExample();
		example.setOrderByClause("creatdate DESC");
		BaoxiaoBillExample.Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(userid);
		List<BaoxiaoBill> selectByExample = baoxiaoBillMapper.selectByExample(example);
		return selectByExample;
	}

	@Override
	public BaoxiaoBill findBaoxiaoBillById(long id) {
		// TODO Auto-generated method stub
		BaoxiaoBill baoxiaoBill = this.baoxiaoBillMapper.selectByPrimaryKey(id);
		return baoxiaoBill;
	}

	@Override
	public void deleteBaoxiaoBillById(int billId) {
		// TODO Auto-generated method stub
		this.baoxiaoBillMapper.deleteByPrimaryKey(Long.valueOf(billId));
	}

	

	

}
