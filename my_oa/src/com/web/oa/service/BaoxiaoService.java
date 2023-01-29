package com.web.oa.service;

import java.util.List;

import com.web.oa.pojo.BaoxiaoBill;

public interface BaoxiaoService {
	
	void saveBaoxiao(BaoxiaoBill baoxiaoBill);
	
	List<BaoxiaoBill> findLeaveBillListByUser(long id);

	BaoxiaoBill findBaoxiaoBillById(long id);

	void deleteBaoxiaoBillById(int billId);
	
	
}
