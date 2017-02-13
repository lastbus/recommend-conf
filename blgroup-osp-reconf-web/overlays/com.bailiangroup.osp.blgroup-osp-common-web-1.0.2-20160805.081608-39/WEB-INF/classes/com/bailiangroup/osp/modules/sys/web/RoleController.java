/**
 * Copyright &copy; 2014-2020 <a href="https://www.bailiangroup.com/osp">Bailian Group OSP</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bailiangroup.osp.modules.sys.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bailiangroup.osp.base.core.service.ctx.ContextService;
import com.bailiangroup.osp.base.core.util.pagination.PageAdapter;
import com.bailiangroup.osp.base.core.util.pagination.ViewPage;
import com.bailiangroup.osp.common.config.Global;
import com.bailiangroup.osp.common.domain.entity.Menu;
import com.bailiangroup.osp.common.domain.entity.Role;
import com.bailiangroup.osp.common.domain.entity.User;
import com.bailiangroup.osp.common.repository.MenuRepository;
import com.bailiangroup.osp.common.service.OfficeService;
import com.bailiangroup.osp.common.service.SystemService;
import com.bailiangroup.osp.common.web.BaseController;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 角色Controller
 * 
 * @author IBM Consultant Team
 * @version 2013-5-15 update 2013-06-08
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/role")
public class RoleController extends BaseController {

	@Inject
	private SystemService systemService;

	@Inject
	private ContextService contextService;

	@Inject
	private OfficeService officeService;

	@Inject
	private MenuRepository menuRepository;

	@ModelAttribute("role")
	public Role get(@RequestParam(required = false) String id) {
		if (StringUtils.isNotBlank(id)) {
			return systemService.getRole(id);
		} else {
			return new Role();
		}
	}

	@RequiresPermissions("sys:role:view")
	@RequestMapping(value = { "list", "" })
	public String list(@RequestParam Map<String, Object> paramMap, Role role, Model model, HttpServletRequest request, HttpServletResponse response) {
		Page<Role> page = systemService.find(new PageAdapter<Role>(new ViewPage<Role>(request, response)), paramMap, role);
		model.addAttribute("page", new PageAdapter<Role>(page));
		return "modules/sys/roleList";
		/*List<Role> list = systemService.findAllRole();
		model.addAttribute("list", list);
		return "modules/sys/roleList";*/
	}

	@RequiresPermissions("sys:role:view")
	@RequestMapping(value = "selectRole")
	public String selectRole(@RequestParam Map<String, Object> paramMap, Role role, Model model, HttpServletRequest request,
			HttpServletResponse response) {
		Page<Role> page = systemService.find(new PageAdapter<Role>(new ViewPage<Role>(request, response)), paramMap, role);
		model.addAttribute("page", new PageAdapter<Role>(page));
		return "modules/sys/selectRole";

	}

	@RequiresPermissions("sys:role:view")
	@RequestMapping(value = "form")
	public String form(Role role, Model model) {

		if (role.getOffice() == null) {
			role.setOffice(this.contextService.<User> getCurrentUser().getOffice());
		}
		if (StringUtils.isBlank(role.getMenuIds())) { // 默认选中我的面板
			List<Menu> myPannel = menuRepository.findByParentIdsLike("%,27,%");
			if (myPannel != null && myPannel.size() > 0) {

				StringBuffer strBuffer = new StringBuffer();
				for (Menu item : myPannel) {
					strBuffer.append(item.getId() + ",");
				}

				role.setMenuIds(strBuffer.toString());
			}
		}
		model.addAttribute("role", role);

		model.addAttribute("menuList", systemService.findAllMenu());
		// model.addAttribute("categoryList", categoryService.findByUser(false, null));
		model.addAttribute("officeList", officeService.findAll());
		return "modules/sys/roleForm";
	}

	@RequiresPermissions("sys:role:edit")
	@RequestMapping(value = "save")
	public String save(Role role, Model model, String oldName, RedirectAttributes redirectAttributes) {

		if (!beanValidator(model, role)) {
			return form(role, model);
		}
		if (!"true".equals(checkName(oldName, role.getName()))) {
			addMessage(model, "保存角色'" + role.getName() + "'失败, 角色名已存在");
			return form(role, model);
		}
		systemService.saveRole(role);
		addMessage(redirectAttributes, "保存角色'" + role.getName() + "'成功");
		return "redirect:" + Global.getAdminPath() + "/sys/role/?repage";
	}

	@RequiresPermissions("sys:role:edit")
	@RequestMapping(value = "delete")
	public String delete(@RequestParam String id, RedirectAttributes redirectAttributes) {

		if (Role.isAdmin(id)) {
			addMessage(redirectAttributes, "删除角色失败, 不允许内置角色或编号空");
		} else {
			systemService.deleteRole(id);
			addMessage(redirectAttributes, "删除角色成功");
		}
		return "redirect:" + Global.getAdminPath() + "/sys/role/?repage";
	}

	@RequiresPermissions("sys:role:edit")
	@RequestMapping(value = "deletesome")
	public String delete(String id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
		String ids[] = request.getParameterValues("subcheck");
		List<String> list = new ArrayList<String>();
		for (String _id : ids) {
			if (!Role.isAdmin(_id)) {
				list.add(_id);
			}
		}
		this.systemService.delete((String[]) list.toArray());
		addMessage(redirectAttributes, "删除角色成功");
		return "redirect:" + Global.getAdminPath() + "/sys/role/?repage";
	}

	@RequiresPermissions("sys:role:edit")
	@RequestMapping(value = "assign")
	public String assign(Role role, Model model) {

		List<User> users = Lists.newArrayList();
		role = this.systemService.getRole(role.getId());
		if (role != null) {
			users = role.getUserList();
		}
		model.addAttribute("users", users);

		return "modules/sys/roleAssign";
	}

	@RequiresPermissions("sys:role:view")
	@RequestMapping(value = "usertorole")
	public String selectUserToRole(Role role, Model model) {
		model.addAttribute("role", role);
		model.addAttribute("selectIds", role.getUserIds());
		model.addAttribute("officeList", officeService.findAll());
		return "modules/sys/selectUserToRole";
	}

	@RequiresPermissions("sys:role:view")
	@ResponseBody
	@RequestMapping(value = "users")
	public List<Map<String, Object>> users(String officeId, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<User> userList = systemService.findUsersByOfficeId(officeId);
		for (User user : userList) {
			Map<String, Object> map = Maps.newHashMap();
			map.put("id", user.getId());
			map.put("pId", 0);
			map.put("name", user.getName());
			mapList.add(map);
		}
		return mapList;
	}

	@RequiresPermissions("sys:role:edit")
	@RequestMapping(value = "outrole")
	public String outrole(String userId, String roleId, RedirectAttributes redirectAttributes) {

		Role role = systemService.getRole(roleId);
		User user = systemService.getUser(userId);
		if (user.equals(this.contextService.<User> getCurrentUser())) {
			addMessage(redirectAttributes, "无法从角色【" + role.getName() + "】中移除用户【" + user.getName() + "】自己！");
		} else {
			Boolean flag = systemService.outUserInRole(role, userId);
			if (!flag) {
				addMessage(redirectAttributes, "用户【" + user.getName() + "】从角色【" + role.getName() + "】中移除失败！");
			} else {
				addMessage(redirectAttributes, "用户【" + user.getName() + "】从角色【" + role.getName() + "】中移除成功！");
			}
		}
		return "redirect:" + Global.getAdminPath() + "/sys/role/assign?id=" + role.getId();
	}

	@RequiresPermissions("sys:role:edit")
	@RequestMapping(value = "assignrole")
	public String assignRole(Role role, String[] idsArr, RedirectAttributes redirectAttributes) {

		StringBuilder msg = new StringBuilder();
		int newNum = 0;
		for (int i = 0; i < idsArr.length; i++) {
			User user = systemService.assignUserToRole(role, idsArr[i]);
			if (null != user) {
				msg.append("<br/>新增用户【" + user.getName() + "】到角色【" + role.getName() + "】！");
				newNum++;
			}
		}
		addMessage(redirectAttributes, "已成功分配 " + newNum + " 个用户" + msg);
		return "redirect:" + Global.getAdminPath() + "/sys/role/assign?id=" + role.getId();
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "checkName")
	public String checkName(String oldName, String name) {
		if (name != null && name.equals(oldName)) {
			return "true";
		} else if (name != null && systemService.findRoleByName(name) == null) {
			return "true";
		}
		return "false";
	}

}
