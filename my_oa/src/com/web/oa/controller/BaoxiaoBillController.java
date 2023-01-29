package com.web.oa.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.BaoxiaoService;
import com.web.oa.service.WorkFlowService;
import com.web.oa.utils.Constants;

@Controller
public class BaoxiaoBillController {

	@Autowired
	private BaoxiaoService baoxiaoService;

	@Autowired
	private WorkFlowService workFlowService;
	//如果认证成功就会跳转index
	@RequestMapping("/main")
	public String main(ModelMap model) {
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		model.addAttribute("activeUser", activeUser);
		return "index";
	}
	//我的报销单
	@RequestMapping("/myBaoxiaoBill")
	public String home(ModelMap model, HttpSession session,String page) {
		// 1：查询所有的请假信息（对应a_leavebill），返回List<LeaveBill>
		// Employee emp = (Employee)
		// session.getAttribute(Constants.GLOBLE_USER_SESSION);
		int page1=1;
		if (page!= null && !page.equals("")) {
			page1=Integer.valueOf(page);
		}
		PageHelper.startPage(page1,5);
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		List<BaoxiaoBill> list = baoxiaoService.findLeaveBillListByUser(activeUser.getId());
		PageInfo pageInfo = new PageInfo(list);
		// 放置到上下文对象中
		model.addAttribute("baoxiaoList", list);
		model.addAttribute("pageInfo",pageInfo);
		return "baoxiaobill";
	}
	@RequestMapping("/viewCurrentImageByBill")
	public String viewCurrentImageByBill(long billId, ModelMap model) {
		System.out.println("你好");
		String BUSSINESS_KEY = Constants.BAOXIAO_KEY + "." + billId;
		Task task = this.workFlowService.findTaskByBussinessKey(BUSSINESS_KEY);
		/** 一：查看流程图 */
		// 1：获取任务ID，获取任务对象，使用任务对象获取流程定义ID，查询流程定义对象
		ProcessDefinition pd = workFlowService.findProcessDefinitionByTaskId(task.getId());

		model.addAttribute("deploymentId", pd.getDeploymentId());
		model.addAttribute("imageName", pd.getDiagramResourceName());
		/** 二：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中 */
		Map<String, Object> map = workFlowService.findCoordingByTask(task.getId());

		model.addAttribute("acs", map);
		return "viewimage";
	}
	@RequestMapping("/leaveBillAction_delete")
	public String leaveBillAction_delete(String id) {
		int billId=Integer.valueOf(id);
		baoxiaoService.deleteBaoxiaoBillById(billId);
		return "redirect:myBaoxiaoBill";
	}
}
