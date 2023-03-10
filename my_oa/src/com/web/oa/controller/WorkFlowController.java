package com.web.oa.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.BaoxiaoService;
import com.web.oa.service.WorkFlowService;

@Controller
public class WorkFlowController {

	@Autowired
	private WorkFlowService workFlowService;

	@Autowired
	private BaoxiaoService baoxiaoService;

	// 部署流程
	@RequestMapping("/deployProcess")
	public String deployProcess(String processName, MultipartFile fileName) {

		try {
			workFlowService.saveNewDeploye(fileName.getInputStream(), processName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "redirect:/processDefinitionList";
	}

	// 查看流程
	@RequestMapping("/processDefinitionList")
	public ModelAndView processDefinitionList() {
		ModelAndView mv = new ModelAndView();

		// 1:查询部署对象信息，对应表（act_re_deployment）
		List<Deployment> depList = workFlowService.findDeploymentList();
		// 2:查询流程定义的信息，对应表（act_re_procdef）
		List<ProcessDefinition> pdList = workFlowService.findProcessDefinitionList();
		// 放置到上下文对象中
		mv.addObject("depList", depList);
		mv.addObject("pdList", pdList);
		mv.setViewName("workflow_list");
		return mv;
	}

	/**
	 * 删除部署信息
	 */
	@RequestMapping("/delDeployment")
	public String delDeployment(String deploymentId) {
		workFlowService.deleteProcessDefinitionByDeploymentId(deploymentId);
		return "redirect:/processDefinitionList";
	}

	/**
	 * 查看流程图
	 * 
	 * @throws IOException
	 * @throws Exception
	 */
	@RequestMapping("/viewImage")
	public String viewImage(String deploymentId, String imageName, HttpServletResponse response) throws IOException {

		// 2：获取资源文件表（act_ge_bytearray）中资源图片输入流InputStream
		InputStream in = workFlowService.findImageInputStream(deploymentId, imageName);
		System.out.println("测试");
		// 3：从response对象获取输出流
		OutputStream out = response.getOutputStream();
		// 4：将输入流中的数据读取出来，写到输出流中
		for (int b = -1; (b = in.read()) != -1;) {
			out.write(b);
		}
		out.close();
		in.close();
		return null;
	}

	// 提交请假单
	@RequestMapping("/saveStartBaoxiao")
	public String saveStartBaoxiao(BaoxiaoBill baoxiaoBill, HttpSession session) {
		// 设置当前时间
		baoxiaoBill.setCreatdate(new Date());
		// 设置申请人ID
		// Employee employee = (Employee)
		// session.getAttribute(Constants.GLOBLE_USER_SESSION);
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		baoxiaoBill.setUserId(activeUser.getId());
		// 更新状态从0变成1（初始录入-->审核中）
		baoxiaoBill.setState(1);
		baoxiaoService.saveBaoxiao(baoxiaoBill);
		workFlowService.saveStartProcess(baoxiaoBill.getId(), activeUser.getUsername());
		return "redirect:/myTaskList";
	}

	// 查询待办事务
	@RequestMapping("/myTaskList")
	public ModelAndView getTaskList(HttpSession session) {
		ModelAndView mv = new ModelAndView();
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		List<Task> list = workFlowService.findTaskListByName(activeUser.getUsername());
		mv.setViewName("workflow_task");
		mv.addObject("taskList", list);
		return mv;
	}

	// 办理任务
	@RequestMapping("/viewTaskForm")
	public ModelAndView viewTaskForm(String taskId) {
		ModelAndView mv = new ModelAndView();
		// 你的待办事务报销表baoxiaobill
		BaoxiaoBill bill = this.workFlowService.findBaoxiaoBillByTaskId(taskId);
		// 查询批注
		List<Comment> list = this.workFlowService.findCommentByTaskId(taskId);
		// 连线
		List<String> outcomeList = this.workFlowService.findOutComeListByTaskId(taskId);

		mv.addObject("baoxiaoBill", bill);
		mv.addObject("commentList", list);
		mv.addObject("outcomeList", outcomeList);
		mv.addObject("taskId", taskId);

		mv.setViewName("approve_baoxiao");
		return mv;
	}

	// 提交请假单
	@RequestMapping("/submitTask")
	public String submitTask(long id, String taskId, String comment, String outcome, HttpSession session) {
		// String username =
		// ((Employee)session.getAttribute(Constants.GLOBLE_USER_SESSION)).getName();
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		String username = activeUser.getUsername();
		this.workFlowService.saveSubmitTask(id, taskId, comment, outcome, username);
		return "redirect:/myTaskList";
	}

	@RequestMapping("/viewCurrentImage")
	public String viewCurrentImage(String taskId, ModelMap model) {
		/** 一：查看流程图 */
		// 1：获取任务ID，获取任务对象，使用任务对象获取流程定义ID，查询流程定义对象
		ProcessDefinition pd = workFlowService.findProcessDefinitionByTaskId(taskId);

		model.addAttribute("deploymentId", pd.getDeploymentId());
		model.addAttribute("imageName", pd.getDiagramResourceName());
		/** 二：查看当前活动，获取当期活动对应的坐标x,y,width,height，将4个值存放到Map<String,Object>中 */
		Map<String, Object> map = workFlowService.findCoordingByTask(taskId);

		model.addAttribute("acs", map);
		return "viewimage";
	}

	// 查看历史的批注信息
	@RequestMapping("/viewHisComment")
	public String viewHisComment(long id, ModelMap model) {
		// 1：使用报销单ID，查询报销单对象
		BaoxiaoBill bill = baoxiaoService.findBaoxiaoBillById(id);
		model.addAttribute("baoxiaoBill", bill);
		// 2：使用请假单ID，查询历史的批注信息
		List<Comment> commentList = workFlowService.findCommentByBaoxiaoBillId(id);
		model.addAttribute("commentList", commentList);
		System.out.println("你好");
		return "workflow_commentlist";
	}
	// 用户管理

}
