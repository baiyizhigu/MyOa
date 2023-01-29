package com.web.oa.service;

import java.util.List;

import com.web.oa.pojo.Employee;
import com.web.oa.pojo.EmployeeCustom;
import com.web.oa.pojo.SysRole;
import com.web.oa.pojo.SysUserRole;

public interface EmployeeService {
	//根据名字查找员工
	Employee findEmployeeByName(String username);

	//根据主键查找员工
	Employee findEmployeeManager(long id);
	//查询所有员工(对应employee表)
	List<EmployeeCustom> findUserAndRoleList();

	void updateEmployeeRole(String roleId, String userId);

	List<Employee> findEmployeeByLevel(String level);

	int addusers(Employee user, String roleName);

	SysUserRole findUserAndRoleById(String level);

	SysUserRole findUserAndRoleByUserAndRoleId(String level);

	void deleteEmployee(String id);

	void deleteUserAndRoleById(String id);

	
}
