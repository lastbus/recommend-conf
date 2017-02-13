/**
 * Copyright &copy; 2014-2020 <a href="https://www.bailiangroup.com/osp">Bailian Group OSP</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bailiangroup.osp.modules.sys.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bailiangroup.osp.common.config.Global;
import com.bailiangroup.osp.common.domain.entity.Menu;
import com.bailiangroup.osp.common.domain.entity.User;
import com.bailiangroup.osp.common.dto.MenuClickDTO;
import com.bailiangroup.osp.common.service.SystemService;
import com.bailiangroup.osp.common.utils.DictUtils;
import com.bailiangroup.osp.common.utils.KafkaUtils;
import com.bailiangroup.osp.common.utils.UserUtils;
import com.bailiangroup.osp.common.web.BaseController;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 菜单Controller
 * 
 * @author IBM Consultant Team
 * @version 2013-3-23
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/menu")
public class MenuController extends BaseController {

	@Inject
	private SystemService systemService;

	private static final String level_2 = "2";
	private static final String level_3 = "3";

	private static final Logger LOG = LoggerFactory.getLogger(MenuController.class);

	@ModelAttribute("menu")
	public Menu get(@RequestParam(required = false) String id) {

		if (StringUtils.isNotBlank(id)) {

			Menu menu = systemService.getMenu(id);

			if (menu != null && menu.getParent() != null) {
				menu.setParent((Menu) (SerializationUtils.clone(menu.getParent())));
			}

			return menu;
		} else {
			return new Menu();
		}
	}

	@RequiresPermissions("sys:menu:view")
	@RequestMapping(value = { "list", "" })
	public String list(Model model) {
		/*	org.springframework.cache.Cache menuCache = CacheManager
			.getCacheInstance(CacheManager.CACHE_NAME_OSP_MENU);*/

		List<Menu> list = null;

		/*if ( menuCache != null) {
			
			ValueWrapper menuWrapper =  menuCache.get(CacheManager.CACHE_MENU_SORTED_LIST);
			
			if ( menuWrapper != null) {
				
				Object sortedMenus = menuWrapper.get();
				if ( sortedMenus != null) {
					list = (List<Menu>)sortedMenus;
				}
			}
		}*/

		if (CollectionUtils.isEmpty(list)) {
			list = Lists.newArrayList();
			List<Menu> sourcelist = systemService.findAllMenu();
			Menu.sortList(list, sourcelist, "1");
			// menuCache.put(CacheManager.CACHE_MENU_SORTED_LIST, list);
		}

		model.addAttribute("list", list);
		return "modules/sys/menuList";
	}

	@RequiresPermissions("sys:menu:view")
	@RequestMapping(value = "form")
	public String form(Menu menu, Model model) {

		if (menu.getParent() == null || menu.getParent().getId() == null) {
			menu.setParent(new Menu("1"));
		}
		menu.setParent(systemService.getMenu(menu.getParent().getId()));
		model.addAttribute("menu", menu);
		return "modules/sys/menuForm";
	}

	@RequiresPermissions("sys:menu:edit")
	@RequestMapping(value = "save")
	public String save(Menu menu, Model model, RedirectAttributes redirectAttributes) {

		if (!beanValidator(model, menu)) {
			return form(menu, model);
		}
		systemService.saveMenu(menu);
		addMessage(redirectAttributes, "保存菜单'" + menu.getName() + "'成功");
		return "redirect:" + Global.getAdminPath() + "/sys/menu/";
	}

	@RequiresPermissions("sys:menu:edit")
	@RequestMapping(value = "delete")
	public String delete(String id, RedirectAttributes redirectAttributes) {

		if (Menu.isRoot(id)) {
			addMessage(redirectAttributes, "删除菜单失败, 不允许删除顶级菜单或编号为空");
		} else {
			systemService.deleteMenu(id);
			addMessage(redirectAttributes, "删除菜单成功");
		}
		return "redirect:" + Global.getAdminPath() + "/sys/menu/";
	}

	@RequiresUser
	@RequestMapping(value = "tree")
	public String tree() {
		return "modules/sys/menuTree";
	}

	/**
	 * 同步工作流权限数据
	 */
	@RequiresPermissions("sys:menu:edit")
	@RequestMapping(value = "synToActiviti")
	public String synToActiviti(RedirectAttributes redirectAttributes) {

		addMessage(redirectAttributes, "同步工作流权限数据成功!");
		return "redirect:" + Global.getAdminPath() + "/sys/menu/";
	}

	/**
	 * 批量修改菜单排序
	 */
	@RequiresPermissions("sys:menu:edit")
	@RequestMapping(value = "updateSort")
	public String updateSort(String[] ids, Integer[] sorts, RedirectAttributes redirectAttributes) {

		int len = ids.length;
		Menu[] menus = new Menu[len];
		for (int i = 0; i < len; i++) {
			menus[i] = systemService.getMenu(ids[i]);
			menus[i].setSort(sorts[i]);
			systemService.saveMenu(menus[i]);
		}
		addMessage(redirectAttributes, "保存菜单排序成功!");
		return "redirect:" + Global.getAdminPath() + "/sys/menu/";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "treeData")
	public List<Map<String, Object>> treeData(@RequestParam(required = false) String extId, HttpServletResponse response) {
		response.setContentType("application/json; charset=UTF-8");
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<Menu> list = systemService.findAllMenu();
		for (int i = 0; i < list.size(); i++) {
			Menu e = list.get(i);
			if (extId == null || (extId != null && !extId.equals(e.getId()) && e.getParentIds().indexOf("," + extId + ",") == -1)) {
				Map<String, Object> map = Maps.newHashMap();
				map.put("id", e.getId());
				map.put("pId", e.getParent() != null ? e.getParent().getId() : 0);
				map.put("name", e.getName());
				mapList.add(map);
			}
		}
		return mapList;
	}

	@RequestMapping(value = "menuClick")
	public void menuClick(String menuId1st, String menuId2st, String menuId3st) {
		Producer<String, String> producer = KafkaUtils.getProduce();
		try {
			User currentUser = UserUtils.getUser();
			Menu menu = null;
			if (StringUtils.isNotBlank(menuId2st)) {
				menu = systemService.getMenu(menuId2st);
			}
			if (StringUtils.isNotBlank(menuId3st)) {
				menu = systemService.getMenu(menuId3st);
			}
			if (menu != null) {
				MenuClickDTO mc = new MenuClickDTO();
				mc.setName(currentUser.getName());
				mc.setLoginName(currentUser.getLoginName());
				mc.setLoginDate(currentUser.getLoginDate().toString());
				mc.setLoginId(currentUser.getId());
				mc.setHitTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				mc.setMenuId(menuId2st != null ? menuId2st : menuId3st);
				mc.setMenuName(menu.getName());
				if (StringUtils.isNotBlank(menuId2st))
					mc.setLevel(level_2);
				if (StringUtils.isNotBlank(menuId3st))
					mc.setLevel(level_3);
				// 获取root菜单(一级菜单)的信息
				String rootId = null;
				String[] ParentIds = menu.getParentIds().split(",");
				if (ParentIds != null && ParentIds.length >= 3) {
					rootId = ParentIds[2];
				}
				if (StringUtils.isNotBlank(rootId)) {
					Menu root = systemService.getMenu(rootId);
					if (root != null) {
						mc.setRootMenuId(root.getId());
						mc.setRootMenuName(root.getName());
					}
				}
				ProducerRecord<String, String> record = new ProducerRecord<String, String>(DictUtils.getDictValue("topic.menuclick",
						"sys_kafka_topic", ""), JSONObject.fromObject(mc).toString());
				producer.send(record);
			}
		} catch (Exception e) {
			producer.close();
			LOG.debug("Exception occurred in menuClick method", e.getMessage());
		}
	}
}
