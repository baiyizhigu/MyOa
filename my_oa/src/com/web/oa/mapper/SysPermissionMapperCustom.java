package com.web.oa.mapper;

import java.util.List;

import com.web.oa.pojo.EmployeeCustom;
import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.pojo.SysRole;


public interface SysPermissionMapperCustom {
	
	//根据用户id查询菜单
	public List<SysPermission> findMenuListByUserId(String userid)throws Exception;
	//根据用户id查询权限url
	public List<SysPermission> findPermissionListByUserId(String userid)throws Exception;
	//查询菜单
	public List<MenuTree> getMenuTree();
	//查询权限
	public List<SysPermission> getSubMenu();
	//查询角色
	public List<EmployeeCustom> findUserAndRoleList();
	
	public SysRole findRoleAndPermissionListByUserId(String userId);
	
	public List<SysRole> findRoleAndPermissionList();
	
	public List<SysPermission> findMenuAndPermissionByUserId(String userId);
	
	public List<MenuTree> getAllMenuAndPermision();
	
	public List<SysPermission> findPermissionsByRoleId(String roleId);
}
