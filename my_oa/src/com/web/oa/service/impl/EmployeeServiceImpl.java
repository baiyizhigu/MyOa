package com.web.oa.service.impl;

import java.util.List;

import org.apache.shiro.crypto.hash.Md5Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.parsing.FailFastProblemReporter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.oa.mapper.EmployeeMapper;
import com.web.oa.mapper.SysPermissionMapperCustom;
import com.web.oa.mapper.SysUserRoleMapper;
import com.web.oa.pojo.Employee;
import com.web.oa.pojo.EmployeeCustom;
import com.web.oa.pojo.EmployeeExample;
import com.web.oa.pojo.SysUserRole;
import com.web.oa.pojo.SysUserRoleExample;
import com.web.oa.pojo.SysUserRoleExample.Criteria;
import com.web.oa.service.EmployeeService;

import sun.security.provider.MD5;

@Service("employeeService")
public class EmployeeServiceImpl implements EmployeeService {
	
	@Autowired
	private EmployeeMapper employeeMapper;
	@Autowired
	private SysPermissionMapperCustom permissionMapper;
	@Autowired
	private SysUserRoleMapper userRoleMapper;
	@Override
	public Employee findEmployeeByName(String name) {
		EmployeeExample example = new EmployeeExample();
		EmployeeExample.Criteria criteria = example.createCriteria();
		criteria.andNameEqualTo(name);
		List<Employee> list = employeeMapper.selectByExample(example);
		
		if(list!=null && list.size()>0){
			return list.get(0);
		}
		return null;
	}
	@Override
	public Employee findEmployeeManager(long id) {
		// TODO Auto-generated method stub
		return employeeMapper.selectByPrimaryKey(id);
	}
	@Override
	public List<EmployeeCustom> findUserAndRoleList() {
		return permissionMapper.findUserAndRoleList();
	}
	
	@Override
	public void updateEmployeeRole(String roleId, String userId) {
		SysUserRoleExample example = new SysUserRoleExample();
		SysUserRoleExample.Criteria criteria = example.createCriteria();
		criteria.andSysUserIdEqualTo(userId);
		
		SysUserRole userRole = userRoleMapper.selectByExample(example).get(0);
		userRole.setSysRoleId(roleId);
		
		userRoleMapper.updateByPrimaryKey(userRole);
	}
	@Override
	public List<Employee> findEmployeeByLevel(String level) {
		// TODO Auto-generated method stub
		EmployeeExample example = new EmployeeExample();
		EmployeeExample.Criteria criteria = example.createCriteria();
		if (level.equals("4")) {
			criteria.andRoleEqualTo(Integer.valueOf(level));
		}else if(level.equals("1")){
			Integer valueOf = Integer.valueOf(level);
			valueOf++;
			criteria.andRoleEqualTo(valueOf);
		}
		else {
			criteria.andRoleGreaterThan(level);
		}
		
		List<Employee> list = employeeMapper.selectByExample(example);
		return list;
	}
	@Override
	public int addusers(Employee user,String id) {
		// TODO Auto-generated method stub
		Employee findEmployeeByName = this.findEmployeeByName(user.getName());
		EmployeeExample employeeExample = new EmployeeExample();
		com.web.oa.pojo.EmployeeExample.Criteria createCriteria = employeeExample.createCriteria();
		createCriteria.andEmailEqualTo(user.getEmail());
		  List<Employee> selectByExample = employeeMapper.selectByExample(employeeExample);
		  Employee employee=null;
		  if (selectByExample!=null && selectByExample.size()>0) {
			  employee=selectByExample.get(0);
		  }
		if (findEmployeeByName!=null) {
			return 1;
		}else if(employee!=null) {
			return 2;
		}
		
		else {
			Md5Hash md5Hash = new Md5Hash(user.getPassword(),user.getSalt(),2);
			user.setPassword(md5Hash.toString());
			employeeMapper.insert(user);
			Employee findEmployeeByName2 = this.findEmployeeByName(user.getName());
			SysUserRole sysUserRole = new SysUserRole();
			sysUserRole.setId(String.valueOf(findEmployeeByName2.getId()));
			//自己的角色id也就是名字
			String roleId =user.getRole().toString();
			sysUserRole.setSysUserId(user.getName().toString());
			//上级id
			sysUserRole.setSysRoleId(id);
			userRoleMapper.insert(sysUserRole);
			return 0;
		}
		
	
	}
	@Override
	public SysUserRole findUserAndRoleById(String level) {
		// TODO Auto-generated method stub
		return userRoleMapper.selectByPrimaryKey(level);
	}
	@Override
	public SysUserRole findUserAndRoleByUserAndRoleId(String level) {
		// TODO Auto-generated method stub
		SysUserRoleExample sysUserRoleExample = new SysUserRoleExample();
		Criteria createCriteria = sysUserRoleExample.createCriteria();
		createCriteria.andSysRoleIdEqualTo(level);
		List<SysUserRole> selectByExample = userRoleMapper.selectByExample(sysUserRoleExample);
		if(selectByExample!=null && selectByExample.size()>0){
			return selectByExample.get(0);
		}
		return null;
	}
	@Override
	public void deleteEmployee(String id) {
		// TODO Auto-generated method stub
		employeeMapper.deleteByPrimaryKey(Long.valueOf(id));
	}
	@Override
	public void deleteUserAndRoleById(String id) {
		// TODO Auto-generated method stub
		userRoleMapper.deleteByPrimaryKey(id);
	}
	
	
	

}
