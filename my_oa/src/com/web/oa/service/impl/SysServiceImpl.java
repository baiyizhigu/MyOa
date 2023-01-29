package com.web.oa.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.web.oa.mapper.EmployeeMapper;
import com.web.oa.mapper.SysPermissionMapper;
import com.web.oa.mapper.SysPermissionMapperCustom;
import com.web.oa.mapper.SysRoleMapper;
import com.web.oa.mapper.SysRolePermissionMapper;
import com.web.oa.pojo.Employee;
import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.pojo.SysPermissionExample;
import com.web.oa.pojo.SysRole;
import com.web.oa.pojo.SysRoleExample;
import com.web.oa.pojo.SysRoleExample.Criteria;
import com.web.oa.pojo.SysRolePermission;
import com.web.oa.pojo.SysRolePermissionExample;
import com.web.oa.service.SysService;

@Service
public class SysServiceImpl implements SysService {
	//对应sys_permission表
	@Autowired
	private SysPermissionMapperCustom sysPermissionMapperCustom;
	@Autowired
	private SysRoleMapper roleMapper;
	//对应(sys_role_permission表)存放的是角色可以拥有什么权限
	@Autowired
	private SysRolePermissionMapper rolePermissionMapper;
	//权限列表对应(sys_permission表)
	@Autowired
	private SysPermissionMapper sysPermissionMapper;
	@Override
	public List<MenuTree> loadMenuTree() {
		// TODO Auto-generated method stub
		return sysPermissionMapperCustom.getMenuTree();
	}
	//查询菜单和权限还有角色
//	SELECT 
//	  * 
//	FROM
//	  sys_permission 
//	WHERE TYPE like '%permission%' 
//	  AND id IN 
//	  (SELECT 
//	    sys_permission_id 
//	  FROM
//	    sys_role_permission 
//	  WHERE sys_role_id IN 
//	    (SELECT 
//	      sys_role_id 
//	    FROM
//	      sys_user_role 
//	    WHERE sys_user_id = #{id}))
	@Override
	public List<SysPermission> findPermissionListByUserId(String userid)
			throws Exception {
		return sysPermissionMapperCustom.findPermissionListByUserId(userid);
	}
	@Override
	public List<SysRole> findAllRoles() {
		return roleMapper.selectByExample(null);
	}
	@Override
	public SysRole findRolesAndPermissionsByUserId(String userName) {
		// TODO Auto-generated method stub
		return sysPermissionMapperCustom.findRoleAndPermissionListByUserId(userName);
	}
	@Override
	public List<SysPermission> findAllMenus() {
		// TODO Auto-generated method stub
		SysPermissionExample example = new SysPermissionExample();
		SysPermissionExample.Criteria criteria = example.createCriteria();
		//criteria.andTypeLike("%menu%");
		criteria.andTypeEqualTo("menu");
		return sysPermissionMapper.selectByExample(example);
	}
	@Override
	public List<SysRole> findRolesAndPermissions() {
		// TODO Auto-generated method stub
		return sysPermissionMapperCustom.findRoleAndPermissionList();
	}
	@Override
	public List<MenuTree> getAllMenuAndPermision() {
		// TODO Auto-generated method stub
		return sysPermissionMapperCustom.getAllMenuAndPermision();
	}
	@Override
	public List<SysPermission> findPermissionsByRoleId(String roleId) {
		// TODO Auto-generated method stub
		return sysPermissionMapperCustom.findPermissionsByRoleId(roleId);
	}
	@Override
	public void updateRoleAndPermissions(String roleId, int[] permissionIds) {
		//先删除角色权限关系表中角色的权限关系
		SysRolePermissionExample example = new SysRolePermissionExample();
		SysRolePermissionExample.Criteria criteria = example.createCriteria();
		criteria.andSysRoleIdEqualTo(roleId);
		rolePermissionMapper.deleteByExample(example);
		//重新创建角色权限关系
		for (Integer pid : permissionIds) {
			SysRolePermission rolePermission = new SysRolePermission();
			String uuid = UUID.randomUUID().toString();
			rolePermission.setId(uuid);
			rolePermission.setSysRoleId(roleId);
			rolePermission.setSysPermissionId(pid.toString());
			
			rolePermissionMapper.insert(rolePermission);
		}
	}
	
	@Override
	public void addSysPermission(SysPermission permission) {
		sysPermissionMapper.insert(permission);
	}
	@Override
	public void addRoleAndPermissions(SysRole role, int[] permissionIds) {
		// TODO Auto-generated method stub
		//添加角色
				roleMapper.insert(role);
				//添加角色和权限关系表
				for (int i = 0; i < permissionIds.length; i++) {
					SysRolePermission rolePermission = new SysRolePermission();
					//16进制随机码
					String uuid = UUID.randomUUID().toString();
					rolePermission.setId(uuid);
					rolePermission.setSysRoleId(role.getId());
					rolePermission.setSysPermissionId(permissionIds[i]+"");
					rolePermissionMapper.insert(rolePermission);
				}
	}
	@Override
	public void deleteSysPermission(String roleId) {
		// TODO Auto-generated method stub
		this.roleMapper.deleteByPrimaryKey(roleId);
		this.rolePermissionMapper.deleteSysRoleById(roleId);
	}
	@Override
	public SysRole findRoleByName(String rolename) {
		// TODO Auto-generated method stub
		SysRoleExample sysRoleExample = new SysRoleExample();
		Criteria createCriteria = sysRoleExample.createCriteria();
		createCriteria.andNameEqualTo(rolename);
		List<SysRole> selectByExample = roleMapper.selectByExample(sysRoleExample);
		if(selectByExample!=null && selectByExample.size()>0){
			return selectByExample.get(0);
		}
		return null;
	}
	@Override
	public void deleteSysPermissions(int[] permissionIds) {
		// TODO Auto-generated method stub
		for (int i = 0; i < permissionIds.length; i++) {
			sysPermissionMapper.deleteByPrimaryKey(Long.valueOf(permissionIds[i]));
		}
	}
	@Override
	public void deleteSysRoleAndPermission(int[] permissionIds) {
		// TODO Auto-generated method stub

		for (int i = 0; i < permissionIds.length; i++) {
			rolePermissionMapper.deleteByPrimaryKey(String.valueOf(permissionIds[i]));
		}
	}
	@Override
	public void deleteSysRoleAndPermission(String id) {
		// TODO Auto-generated method stub
		SysRolePermissionExample sysRolePermissionExample = new SysRolePermissionExample();
		com.web.oa.pojo.SysRolePermissionExample.Criteria createCriteria = sysRolePermissionExample.createCriteria();
		createCriteria.andSysRoleIdEqualTo(id);
		rolePermissionMapper.deleteByExample(sysRolePermissionExample);
	}




}
