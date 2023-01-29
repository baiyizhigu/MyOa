package com.web.oa.service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;

import com.web.oa.pojo.BaoxiaoBill;

public interface WorkFlowService {

	void saveNewDeploye(InputStream in, String processName);

	List<Deployment> findDeploymentList();

	List<ProcessDefinition> findProcessDefinitionList();

	void deleteProcessDefinitionByDeploymentId(String deploymentId);

	InputStream findImageInputStream(String deploymentId, String imageName);

	void saveStartProcess(Long id, String username);

	List<Task> findTaskListByName(String username);

	BaoxiaoBill findBaoxiaoBillByTaskId(String taskId);

	List<Comment> findCommentByTaskId(String taskId);

	List<String> findOutComeListByTaskId(String taskId);

	void saveSubmitTask(long id, String taskId, String comment, String outcome, String username);

	Task findTaskByBussinessKey(String bUSSINESS_KEY);

	ProcessDefinition findProcessDefinitionByTaskId(String id);

	Map<String, Object> findCoordingByTask(String id);

	List<Comment> findCommentByBaoxiaoBillId(long id);


	
	
}
