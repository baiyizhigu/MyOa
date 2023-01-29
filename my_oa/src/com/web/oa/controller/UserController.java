package com.web.oa.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.PrimitiveIterator.OfDouble;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Response;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.web.oa.pojo.ActiveUser;
import com.web.oa.pojo.Employee;
import com.web.oa.pojo.EmployeeCustom;
import com.web.oa.pojo.MenuTree;
import com.web.oa.pojo.SysPermission;
import com.web.oa.pojo.SysRole;
import com.web.oa.pojo.SysUserRole;
import com.web.oa.service.EmployeeService;
import com.web.oa.service.SysService;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.CircleCaptcha;

@Controller
public class UserController {

	@Autowired
	private EmployeeService employeeService;
	@Autowired
	private SysService sysService;

	@RequestMapping("/login")
	public String login(HttpServletRequest request, Model model,String validateCode) {

		String exceptionName = (String) request.getAttribute("shiroLoginFailure");
		System.out.println(exceptionName);
		if (exceptionName != null) {
			if (UnknownAccountException.class.getName().equals(exceptionName)) {
				model.addAttribute("errorMsg", "用户账号不存在");
			} else if (IncorrectCredentialsException.class.getName().equals(exceptionName)) {
				model.addAttribute("errorMsg", "密码不正确");
			} else if ("randomcodeError".equals(exceptionName)) {
				model.addAttribute("errorMsg", "验证码不正确");
			} else {
				model.addAttribute("errorMsg", "未知错误");
			}
		}
		return "login";
	}
	@RequestMapping("/findUserList")
	public ModelAndView findUserList(String userId) {
		ModelAndView mv = new ModelAndView();
		List<SysRole> allRoles = sysService.findAllRoles();
		List<EmployeeCustom> list = employeeService.findUserAndRoleList();
		mv.addObject("userList", list);
		mv.addObject("allRoles", allRoles);
		mv.setViewName("userlist");
		return mv;
	}
	//这个是当你点击查看权限发生的事件
	@RequestMapping("/viewPermissionByUser")
	@ResponseBody
	public SysRole viewPermissionByUser(String userName) {
		SysRole sysRole=sysService.findRolesAndPermissionsByUserId(userName);
		return sysRole;
	}
	@RequestMapping("/deleteEmployee")
	public String deletePermission(String id,Model model) {
		employeeService.deleteEmployee(id);
		employeeService.deleteUserAndRoleById(id);
		model.addAttribute("message", "删除成功");
		return "forward:findUserList";
	}
	
	//更改角色的时候发生的事件
	@RequestMapping("/assignRole")
	@ResponseBody
	public Map<String, String> assignRole(String roleId, String userId) {
		Map<String, String> map = new HashMap<>();
		try {
			employeeService.updateEmployeeRole(roleId, userId);
			map.put("msg", "分配权限成功");
		} catch (Exception e) {
			e.printStackTrace();
			map.put("msg", "分配权限失败");
		}
		return map;
	}
	//更改自己的上司
	@RequestMapping("/findNextManager")
	@ResponseBody
	public List<Employee> findNextManager(String level) {
		//level自己的级别 // 加一，表示下一个级别

		SysUserRole sysUserRole= employeeService.findUserAndRoleByUserAndRoleId(level);
		System.out.println(sysUserRole);
		String sysUserId = sysUserRole.getSysUserId();
		
		 Employee findEmployeeByName = employeeService.findEmployeeByName(sysUserId);
		 Integer role = findEmployeeByName.getRole();
		 System.out.println(role);
		 List<Employee> list = employeeService.findEmployeeByLevel(String.valueOf(role));
		System.out.println(list);
		return list;
	}
	@RequestMapping("/saveUser")
	public String saveUser(Employee user, ModelMap model,String roleName) throws IOException {
		System.out.println(user);
		System.out.println(roleName);
		user.setSalt("eteokues");
		 if(user.getName().equals("")||user.getName()==null) {
			model.addAttribute("message","账号不能为空");
			return "forward:findUserList";
		}else if(user.getPassword().equals("")||user.getPassword()==null) {
			model.addAttribute("message","密码不能为空");
			return "forward:findUserList";
		}else if(user.getEmail().equals("")||user.getEmail()==null) {
			model.addAttribute("message","邮箱不能为空");
			return "forward:findUserList";
		}else if(user.getManagerId().equals("")||user.getManagerId()==null) {
			model.addAttribute("message","上级主管不能为空");
			return "forward:findUserList";
		}
		 SysRole findRoleByName = sysService.findRoleByName(roleName);
		 System.out.println(findRoleByName);
		int addusers = employeeService.addusers(user,findRoleByName.getId());
		if (addusers==0) {
			model.addAttribute("message","注册成功");
			return "forward:findUserList";
		}else if (addusers==1) {
			model.addAttribute("message","该用户已被注册");
			return "forward:findUserList";
		}else if(addusers ==2){
			model.addAttribute("message","该邮箱已被注册");
			return "forward:findUserList";
		} else {
			model.addAttribute("message","未知错误");
			return "forward:findUserList";
		}
		
	}
	
	@RequestMapping("/toAddRole")
	public ModelAndView toAddRole() {
		//菜单
		List<MenuTree> allPermissions = sysService.loadMenuTree();
		List<SysPermission> menus = sysService.findAllMenus();
		List<SysRole> permissionList = sysService.findRolesAndPermissions();

		ModelAndView mv = new ModelAndView();
		mv.addObject("allPermissions", allPermissions);
		mv.addObject("menuTypes", menus);
		mv.addObject("roleAndPermissionsList", permissionList);
		mv.setViewName("rolelist");

		return mv;
	
	}
	@RequestMapping("/findRoles") // rest
	public ModelAndView findRoles() {
		ActiveUser activeUser = (ActiveUser) SecurityUtils.getSubject().getPrincipal();
		List<SysRole> roles = sysService.findAllRoles();
		List<MenuTree> allMenuAndPermissions = sysService.getAllMenuAndPermision();

		ModelAndView mv = new ModelAndView();
		mv.addObject("allRoles", roles);
		mv.addObject("activeUser", activeUser);
		mv.addObject("allMenuAndPermissions", allMenuAndPermissions);

		mv.setViewName("permissionlist");
		return mv;
	}
	
	@RequestMapping("/loadMyPermissions")
	@ResponseBody
	public List<SysPermission> loadMyPermissions(String roleId) {
		List<SysPermission> list = sysService.findPermissionsByRoleId(roleId);

		for (SysPermission sysPermission : list) {
			System.out.println(sysPermission.getId() + "," + sysPermission.getType() + "\n" + sysPermission.getName()
					+ "," + sysPermission.getUrl() + "," + sysPermission.getPercode());
		}
		return list;
	}
	
	
	
	@RequestMapping("/updateRoleAndPermission")
	public String updateRoleAndPermission(String roleId, int[] permissionIds) {
		sysService.updateRoleAndPermissions(roleId, permissionIds);
		return "redirect:/findRoles";
	}
	
	@RequestMapping("/saveSubmitPermission")
	public String saveSubmitPermission(SysPermission permission,Model model) {
		if (permission.getAvailable() == null) {
			permission.setAvailable("0");
		}
		sysService.addSysPermission(permission);
		model.addAttribute("message","添加角色成功");
		return "redirect:/toAddRole";
	}          
	
	@RequestMapping("checkCode")
	@ResponseBody
	public void checkCode(HttpServletRequest request, HttpServletResponse response) {
		CircleCaptcha captcha = CaptchaUtil.createCircleCaptcha(200, 100, 4, 20);

		// 获取验证码 并加入到session中
		String code = captcha.getCode();
		request.getSession().setAttribute("session_code", code);

		try {
			captcha.write(response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//保存角色和权限也就是新建角色
	@RequestMapping("/saveRoleAndPermissions")
	public String saveRoleAndPermissions(SysRole role,int[] permissionIds,Model model) {
		//设置role主键，使用uuid
		
		String Rolename = role.getName();
		SysRole sysRole=sysService.findRoleByName(Rolename);
		System.out.println(sysRole);
		if (role.getName().equals("") || role.getName()==null) {
			model.addAttribute("message2","角色不能为空");
			return "forward:/toAddRole";
		}else if (sysRole!=null) {
			model.addAttribute("message2","已经拥有该角色");
			return "forward:/toAddRole";
		}else if (permissionIds==null) {
			model.addAttribute("message2","权限不能为空");
			return "forward:/toAddRole";
		}
		
		String uuid = UUID.randomUUID().toString();
		role.setId(uuid);
		//默认可用
		role.setAvailable("1");
		
		sysService.addRoleAndPermissions(role, permissionIds);
		
		return "redirect:/toAddRole";
	}
	
	@RequestMapping("/deleteRoleAndPermissions")
	public String deleteRoleAndPermissions(int[] permissionIds,Model model) {
		//设置role主键，使用uuid
		if (permissionIds==null) {
			model.addAttribute("message2","删除权限不能为空");
			return "forward:/toAddRole";
		}
		for (int i = 0; i < permissionIds.length; i++) {
			System.out.println(permissionIds[i]);
		}
		sysService.deleteSysPermissions(permissionIds);
		sysService.deleteSysRoleAndPermission(permissionIds);
		return "redirect:/toAddRole";
	}
	
	
	@RequestMapping("/deletePermissions")
	public String deleteMyPermissions(String id) {
		//设置role主键，使用uuid
		
		sysService.deleteSysPermission(id);
		sysService.deleteSysRoleAndPermission(id);
		return  "redirect:/findRoles";
	}
	

}
