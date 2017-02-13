/**
 * Copyright &copy; 2014-2020 <a href="https://www.bailiangroup.com/osp">Bailian Group OSP</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bailiangroup.osp.modules.sys.web;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bailiangroup.osp.base.core.service.ctx.ContextService;
import com.bailiangroup.osp.base.core.util.CacheUtils;
import com.bailiangroup.osp.common.config.Global;
import com.bailiangroup.osp.common.domain.entity.Area;
import com.bailiangroup.osp.common.domain.entity.User;
import com.bailiangroup.osp.common.service.AreaService;
import com.bailiangroup.osp.common.web.BaseController;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 区域Controller
 * @author IBM Consultant Team
 * @version 2013-5-15
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/area")
public class AreaController extends BaseController {

	@Inject
	private ContextService contextService;
	
	@Inject
	private AreaService areaService;
	
	@ModelAttribute("area")
	public Area get(@RequestParam(required=false) String id) {
		if (StringUtils.isNotBlank(id)){
			return areaService.get(id);
		}else{
			return new Area();
		}
	}

	@RequiresPermissions("sys:area:view")
	@RequestMapping(value = {"list", ""})
	public String list(Area area, Model model) {
//		User user = UserUtils.getUser();
//		if(user.isAdmin()){
			area.setId("1");
//		}else{
//			area.setId(user.getArea().getId());
//		}
		model.addAttribute("area", area);
		List<Area> list = (List<Area>) CacheUtils.get("AreaCache.areaSortedList");
		
		if ( list == null || list.isEmpty()) {
			
			list = Lists.newArrayList();
			List<Area> sourcelist = areaService.findAll();
			Area.sortList(list, sourcelist, area.getId());
			CacheUtils.put("AreaCache.areaSortedList", list);
		}
		
        model.addAttribute("list", list);
		return "modules/sys/areaList";
	}

	@RequiresPermissions("sys:area:view")
	@RequestMapping(value = "form")
	public String form(Area area, Model model) {
		
		User currentUser = contextService.<User>getCurrentUser();
		
		if (area.getParent()==null||area.getParent().getId()==null){
			area.setParent(currentUser.getOffice().getArea());
		}
		area.setParent(areaService.get(area.getParent().getId()));
		model.addAttribute("area", area);
		return "modules/sys/areaForm";
	}
	
	@RequiresPermissions("sys:area:edit")
	@RequestMapping(value = "save")
	public String save(Area area, Model model, RedirectAttributes redirectAttributes) {

		if (!beanValidator(model, area)){
			return form(area, model);
		}
		areaService.save(area);
		addMessage(redirectAttributes, "保存区域'" + area.getName() + "'成功");
		return "redirect:"+Global.getAdminPath()+"/sys/area/";
	}
	
	@RequiresPermissions("sys:area:edit")
	@RequestMapping(value = "delete")
	public String delete(String id, RedirectAttributes redirectAttributes) {

		if (Area.isAdmin(id)){
			addMessage(redirectAttributes, "删除区域失败, 不允许删除顶级区域或编号为空");
		}else{
			areaService.delete(id);
			addMessage(redirectAttributes, "删除区域成功");
		}
		return "redirect:"+Global.getAdminPath()+"/sys/area/";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "treeData")
	public List<Map<String, Object>> treeData(@RequestParam(required=false) String extId, HttpServletResponse response) {
		
		response.setContentType("application/json; charset=UTF-8");
		List<Map<String, Object>> mapList = Lists.newArrayList();
//		User user = UserUtils.getUser();
		List<Area> list = areaService.findAll();
		for (int i=0; i<list.size(); i++){
			Area e = list.get(i);
			if (extId == null || (extId!=null && !extId.equals(e.getId()) && e.getParentIds().indexOf(","+extId+",")==-1)){
				Map<String, Object> map = Maps.newHashMap();
				map.put("id", e.getId());
//				map.put("pId", !user.isAdmin()&&e.getId().equals(user.getArea().getId())?0:e.getParent()!=null?e.getParent().getId():0);
				map.put("pId", e.getParent()!=null?e.getParent().getId():0);
				map.put("name", e.getName());
				mapList.add(map);
			}
		}
		return mapList;
	}
}