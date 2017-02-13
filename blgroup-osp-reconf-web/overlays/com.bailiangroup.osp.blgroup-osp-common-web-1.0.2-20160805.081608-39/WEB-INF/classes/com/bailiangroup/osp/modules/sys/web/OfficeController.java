/**
 * Copyright &copy; 2014-2020 <a href="https://www.bailiangroup.com/osp">Bailian Group OSP</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bailiangroup.osp.modules.sys.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.SerializationUtils;
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
import com.bailiangroup.osp.common.domain.entity.Office;
import com.bailiangroup.osp.common.domain.entity.User;
import com.bailiangroup.osp.common.service.OfficeService;
import com.bailiangroup.osp.common.web.BaseController;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 机构Controller
 * @author IBM Consultant Team
 * @version 2013-5-15
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/office")
public class OfficeController extends BaseController {

	@Inject
	private OfficeService officeService;
	
	@Inject
	private ContextService contextService;
	
	@ModelAttribute("office")
	public Office get(@RequestParam(required = false) String id) {
		
		if (StringUtils.isNotBlank(id)) {
			
			Office office = officeService.get(id);
			
			if ( office!= null && office.getParent()!= null) {
				office.setParent((Office)(SerializationUtils.clone(office.getParent())));
			}
			
			return office;
		} else {
			return new Office();
		}
	}

	@RequiresPermissions("sys:office:view")
	@RequestMapping({"list", ""})
	public String list(Office office, Model model) {

		office.setId("1");
		
		model.addAttribute("office", office);
		List<Office> list = (List<Office>) CacheUtils.get("OfficeCache.officeSortedList");
		
		if ( list == null || list.isEmpty()) {
			
			list = Lists.newArrayList();
			List<Office> sourcelist = officeService.findAll();
			Office.sortList(list, sourcelist, office.getId());
			CacheUtils.put("OfficeCache.officeSortedList", list);
		}
		
        model.addAttribute("list", list);
		return "modules/sys/officeList";
	}

	@RequiresPermissions("sys:office:view")
	@RequestMapping("form")
	public String form(Office office, Model model) {
		
		User user = contextService.getCurrentUser();
		if (office.getParent() == null || office.getParent().getId() == null) {
			office.setParent(user.getOffice());
		}
		office.setParent(officeService.get(office.getParent().getId()));
		if (office.getArea() == null) {
			office.setArea(office.getParent().getArea());
		}
		model.addAttribute("office", office);
		return "modules/sys/officeForm";
	}
	
	@RequiresPermissions("sys:office:edit")
	@RequestMapping("save")
	public String save(Office office, Model model, RedirectAttributes redirectAttributes) {

		if (!beanValidator(model, office)) {
			return form(office, model);
		}
		officeService.save(office);
		addMessage(redirectAttributes, "保存机构'" + office.getName() + "'成功");
		return "redirect:" + Global.getAdminPath() + "/sys/office/";
	}
	
	@RequiresPermissions("sys:office:edit")
	@RequestMapping("delete")
	public String delete(String id, RedirectAttributes redirectAttributes) {

		if (Office.isRoot(id)) {
			addMessage(redirectAttributes, "删除机构失败, 不允许删除顶级机构或编号空");
		} else {
			officeService.delete(id);
			addMessage(redirectAttributes, "删除机构成功");
		}
		return "redirect:" + Global.getAdminPath() + "/sys/office/";
	}

	@RequiresUser
	@ResponseBody
	@RequestMapping("treeData")
	public List<Map<String, Object>> treeData(HttpServletResponse response,
			@RequestParam(required = false) String extId,
			@RequestParam(required = false) Long type,
			@RequestParam(required = false) Long grade,
			@RequestParam(required = false )String parentId
			) {
		
		response.setContentType("application/json; charset=UTF-8");
		List<Map<String, Object>> mapList = Lists.newArrayList();
		
//		User user = UserUtils.getUser();
		List<Office> list =new ArrayList<Office>();
		if(StringUtils.isNotEmpty(parentId))
			list= officeService.findByParentIdsLike(parentId);
		else
			list= officeService.findAll();
			
		for (int i=0; i<list.size(); i++){
			Office e = list.get(i);
			
			if (
				(extId == null || (extId!=null && !extId.equals(e.getId()) && e.getParentIds().indexOf(","+extId+",")==-1))
					&& (type == null || (type != null && Integer.parseInt(e.getType()) <= type.intValue()))
					&& (grade == null || (grade != null && Integer.parseInt(e.getGrade()) <= grade.intValue())))
			{
				Map<String, Object> map = Maps.newHashMap();
				map.put("id", e.getId());
//				map.put("pId", !user.isAdmin() && e.getId().equals(user.getOffice().getId())?0:e.getParent()!=null?e.getParent().getId():0);
				map.put("pId", e.getParent() != null ? e.getParent().getId() : 0);
				map.put("name", e.getName());
				map.put("code", e.getCode());
				mapList.add(map);
			}
		}
		return mapList;
	}
	
	
	@RequestMapping("checkCode")
	@ResponseBody
	public Map<String,Object> checkDKeyNumber(String code,HttpServletRequest request,Model model,RedirectAttributes redirectAttributes){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		int count =this.officeService.checkCode(code);
		if(count>0)
			resultMap.put("result",1);
		else
			resultMap.put("result",0);
		
		return resultMap;
	}
}
