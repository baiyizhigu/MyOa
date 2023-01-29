package com.web.oa.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import com.web.oa.mapper.BaoxiaoBillMapper;
import com.web.oa.pojo.BaoxiaoBill;
import com.web.oa.service.WorkFlowService;
import com.web.oa.utils.Constants;

@Service
public class WorkFlowServiceImpl implements WorkFlowService {

	
	@Autowired
	private BaoxiaoBillMapper baoxiaoBillMapper;
//	1.部署流程，查询流程定义和部署信息
//	2.挂起、激活流程定义
//	3.创建模型，获取部署的资源和流程图像
	@Autowired
	private RepositoryService repositoryService;
//	1. RuntimeService为流程运行控制服务，提供的功能：
//	启动流程及对流程数据的控制
//	流程实例（ProcessInstance）与执行流（Execution）查询
//	触发流程操作、接受消息和信号
//	2. RuntimeService启动流程及变量管理
//	启动流程的常用方式（id, key, message）
//	启动流程可选参数（businessKey, variables, tenantId）
	@Autowired
	private RuntimeService runtimeService;
//	对用户任务（UserTask）管理和流程控制
//	设置用户任务（UserTask）的权限信息（拥有者、候选人、办理人）
//	针对用户任务添加用户附件、任务评论和事件记录
//	TaskService对Task管理与流程控制
//	Task对象的创建，删除
//	查询Task、并驱动Task节点完成执行
//	Task相关参数变量（variable）设置
//	TaskService设置Task权限信息
//	候选用户（candidateUser）和候选组（candidateGroup）
//	指定拥有人（Owner）和办理人（Assignee）
//	通过claim设置办理人
//	TaskService设置Task附加信息
//	任务附件（Attachment）创建与查询
//	任务评论（Comment）创建与查询
//	事件记录（Event）创建与查询
	@Autowired
	private TaskService taskService;
	@Autowired
	private FormService formService;
	@Autowired
	private HistoryService historyService;
	@Override
	public void saveNewDeploye(InputStream in, String processName) {
		// TODO Auto-generated method stub
		//2.将File类型的文件转化成ZipInputStream流
		try {
			ZipInputStream zipInputStream = new ZipInputStream(in);
			repositoryService.createDeployment()//创建部署对象
							.name(processName)//取个名
							.addZipInputStream(zipInputStream)//部署文件
							.deploy();//完成部署
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
	}
	@Override
	public List<Deployment> findDeploymentList() {
		// TODO Auto-generated method stub
		List<Deployment> list = repositoryService.createDeploymentQuery()
						  .orderByDeploymenTime().asc()
						  .list();
		return list;
	}
	@Override
	public List<ProcessDefinition> findProcessDefinitionList() {
		// TODO Auto-generated method stub
		List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().orderByProcessDefinitionVersion().asc().list();
		return list;
	}
	@Override
	public void deleteProcessDefinitionByDeploymentId(String deploymentId) {
		// TODO Auto-generated method stub
		this.repositoryService.deleteDeployment(deploymentId,true);
	}
	@Override
	public InputStream findImageInputStream(String deploymentId, String imageName) {
		// TODO Auto-generated method stub
		return repositoryService.getResourceAsStream(deploymentId, imageName);
	}
	@Override
	public void saveStartProcess(Long baoxiaoId, String username) {
		//使用当前对象获取到流程定义的key（对象的名称就是流程定义的key）
		// TODO Auto-generated method stub
		String key= Constants.BAOXIAO_KEY;
		System.out.println(key);
		Map<String, Object> variables = new HashMap<String,Object>();
		variables.put("inputUser", username);//表示惟一用户

		//格式：baoxiao.id的形式（使用流程变量）
		String objId = key+"."+baoxiaoId;
		System.out.println(objId);
		variables.put("objId", objId);
		//5：使用流程定义的key，启动流程实例，同时设置流程变量，同时向正在执行的执行对象表中的字段BUSINESS_KEY添加业务数据，同时让流程关联业务
		runtimeService.startProcessInstanceByKey(key,objId,variables);
	}
	@Override
	public List<Task> findTaskListByName(String username) {
		// TODO Auto-generated method stub
		List<Task> list = taskService.createTaskQuery().taskAssignee(username)
		.orderByTaskCreateTime()
		.asc()
		.list();
		//指定个人任务查询
		return list;
	}
	//通过id查询baoxiaobill表的对应内容
	@Override
	public BaoxiaoBill findBaoxiaoBillByTaskId(String taskId) {
		// TODO Auto-generated method stub
		Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
		//通过流程id查询流程对象
		//再通过流程对象的实例id查询实例对象
		ProcessInstance pi = this.runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
		String businessKey = pi.getBusinessKey();
		System.out.println(businessKey);
		String id = "";
		//再分隔得到他的操作id
		if (StringUtils.isNotBlank(businessKey)) {
			id = businessKey.split("\\.")[1];
		}
		//通过操作id查询代办事务
		BaoxiaoBill bill = baoxiaoBillMapper.selectByPrimaryKey(Long.parseLong(id));
		//返回baoxiaobill表的实体对象
		return bill;
	}
	@Override
	public List<Comment> findCommentByTaskId(String taskId) {
		// TODO Auto-generated method stub
		//通过流程id查询流程对象
		Task task = this.taskService.createTaskQuery().taskId(taskId).singleResult();
		//获取流程对象的实例id
		String processId = task.getProcessInstanceId();
		//通过实例id查询comment表
		List<Comment> list = this.taskService.getProcessInstanceComments(processId);
		return list;
	}
	@Override
	public List<String> findOutComeListByTaskId(String taskId) {
		// TODO Auto-generated method stub
		//返回存放连线的名称集合
				List<String> list = new ArrayList<String>();
				//1:使用任务ID，查询任务对象
				Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
				//2：获取流程定义ID
				String processDefinitionId = task.getProcessDefinitionId();
				//3：查询ProcessDefinitionEntiy对象
				ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processDefinitionId);
				//使用任务对象Task获取流程实例ID
				String processInstanceId = task.getProcessInstanceId();
				//使用流程实例ID，查询正在执行的执行对象表，返回流程实例对象
				ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
							.processInstanceId(processInstanceId)//使用流程实例ID查询
							.singleResult();
				//获取当前活动的id
				String activityId = pi.getActivityId();
				//4：获取当前的活动
				ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);
				//5：获取当前活动完成之后连线的名称
				List<PvmTransition> pvmList = activityImpl.getOutgoingTransitions();
				if(pvmList!=null && pvmList.size()>0){
					for(PvmTransition pvm:pvmList){
						String name = (String) pvm.getProperty("name");
						if(StringUtils.isNotBlank(name)){
							list.add(name);
						} else{
							list.add("默认提交");
						}
					}
				}
				return list;
	}
	@Override
	public void saveSubmitTask(long id, String taskId, String comment, String outcome, String username) {
		// TODO Auto-generated method stub
		/**
		 * 1：在完成之前，添加一个批注信息，向act_hi_comment表中添加数据，用于记录对当前申请人的一些审核信息
		 */
		//使用任务ID，查询任务对象，获取流程流程实例ID
				Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
				//获取流程实例ID
				String processInstanceId = task.getProcessInstanceId();
				/**
				 * 注意：添加批注的时候，由于Activiti底层代码是使用：
				 * 		String userId = Authentication.getAuthenticatedUserId();
					    CommentEntity comment = new CommentEntity();
					    comment.setUserId(userId);
					  所有需要从Session中获取当前登录人，作为该任务的办理人（审核人），对应act_hi_comment表中的User_ID的字段，不过不添加审核人，该字段为null
					 所以要求，添加配置执行使用Authentication.setAuthenticatedUserId();添加当前任务的审核人
				 * */
				//加当前任务的审核人
				Authentication.setAuthenticatedUserId(username);
				
				//添加批注
				taskService.addComment(taskId, processInstanceId, comment);
				
				/**
				 * 2：如果连线的名称是“默认提交”，那么就不需要设置，如果不是，就需要设置流程变量
				 * 在完成任务之前，设置流程变量，按照连线的名称，去完成任务
						 流程变量的名称：outcome
						 流程变量的值：连线的名称
				 */
				Map<String, Object> variables = new HashMap<String,Object>();
				if(outcome!=null && !outcome.equals("默认提交")){
					variables.put("message", outcome);
					//3：使用任务ID，完成当前人的个人任务，同时流程变量
					taskService.complete(taskId, variables);
				} else {
					taskService.complete(taskId);
				}
				/**
				 * 5：在完成任务之后，判断流程是否结束
		   			如果流程结束了，更新请假单表的状态从1变成2（审核中-->审核完成）
				 */
				ProcessInstance pi = runtimeService.createProcessInstanceQuery()//
						.processInstanceId(processInstanceId)//使用流程实例ID查询
						.singleResult();
				//流程结束了
				if(pi==null){
					//更新请假单表的状态从1变成2（审核中-->审核完成）
					BaoxiaoBill bill = baoxiaoBillMapper.selectByPrimaryKey(id);
					bill.setState(2);
					baoxiaoBillMapper.updateByPrimaryKey(bill);
				}
	}
	@Override
	public Task findTaskByBussinessKey(String bUSSINESS_KEY) {
		// TODO Auto-generated method stub
		//通过常量和"."+操作id拼接得到
		Task task = this.taskService.createTaskQuery().processInstanceBusinessKey(bUSSINESS_KEY).singleResult();
		return task;
	}
	
	@Override
	public ProcessDefinition findProcessDefinitionByTaskId(String taskId) {
		//使用任务ID，查询任务对象
		Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
		//获取流程定义ID
		String processDefinitionId = task.getProcessDefinitionId();
		//查询流程定义的对象
		ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()//创建流程定义查询对象，对应表act_re_procdef 
					.processDefinitionId(processDefinitionId)//使用流程定义ID查询
					.singleResult();
		return pd;		// TODO Auto-generated method stub
	}

	
	@Override
	public Map<String, Object> findCoordingByTask(String taskId) {
		//存放坐标
		Map<String, Object> map = new HashMap<String,Object>();
		//使用任务ID，查询任务对象
		Task task = taskService.createTaskQuery()//
					.taskId(taskId)//使用任务ID查询
					.singleResult();
		//获取流程定义的ID
		String processDefinitionId = task.getProcessDefinitionId();
		//获取流程定义的实体对象（对应.bpmn文件中的数据）
		ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity)repositoryService.getProcessDefinition(processDefinitionId);
		//流程实例ID
		String processInstanceId = task.getProcessInstanceId();
		//使用流程实例ID，查询正在执行的执行对象表，获取当前活动对应的流程实例对象
		ProcessInstance pi = runtimeService.createProcessInstanceQuery()//创建流程实例查询
											.processInstanceId(processInstanceId)//使用流程实例ID查询
											.singleResult();
		//获取当前活动的ID
		String activityId = pi.getActivityId();
		//获取当前活动对象
		ActivityImpl activityImpl = processDefinitionEntity.findActivity(activityId);//活动ID
		//获取坐标
		map.put("x", activityImpl.getX());
		map.put("y", activityImpl.getY());
		map.put("width", activityImpl.getWidth());
		map.put("height", activityImpl.getHeight());
		return map;
	}
	@Override
	public List<Comment> findCommentByBaoxiaoBillId(long id) {
		// TODO Auto-generated method stub
		String bussiness_key = Constants.BAOXIAO_KEY +"."+id;
		HistoricProcessInstance pi = this.historyService.createHistoricProcessInstanceQuery().processInstanceBusinessKey(bussiness_key).singleResult();
		List<Comment> processInstanceComments = this.taskService.getProcessInstanceComments(pi.getId());
		
		return processInstanceComments;
	}
	
	
	
	
	
}
