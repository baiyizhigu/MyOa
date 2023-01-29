package com.web.oa.service;

import java.util.List;

import com.web.oa.pojo.Employee;
import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.pojo.SysRole;




public interface SysService {

	List<MenuTree> loadMenuTree();

	List<SysPermission> findPermissionListByUserId(String userId) throws Exception;

	List<SysRole> findAllRoles();

	SysRole findRolesAndPermissionsByUserId(String userName);

	List<SysPermission> findAllMenus();

	List<SysRole> findRolesAndPermissions();

	List<MenuTree> getAllMenuAndPermision();

	List<SysPermission> findPermissionsByRoleId(String roleId);

	void updateRoleAndPermissions(String roleId, int[] permissionIds);

	void addSysPermission(SysPermission permission);

	void addRoleAndPermissions(SysRole role, int[] permissionIds);

	void deleteSysPermission(String roleId);

	SysRole findRoleByName(String rolename);

	void deleteSysPermissions(int[] permissionIds);

	void deleteSysRoleAndPermission(int[] permissionIds);

	void deleteSysRoleAndPermission(String id);
	

	

}
