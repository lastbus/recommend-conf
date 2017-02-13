/**
 * Copyright &copy; 2014-2020 <a href="https://www.bailiangroup.com/osp">Bailian Group OSP</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bailiangroup.osp.modules.sys.web;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.bailiangroup.osp.common.domain.entity.Office;
import com.bailiangroup.osp.common.service.OfficeService;
import com.bailiangroup.osp.common.web.BaseController;

/**
 * 标签Controller
 * 
 * @author IBM Consultant Team
 * @version 2013-3-23
 */
@Controller
@RequestMapping("${adminPath}/tag")
public class TagController extends BaseController {
	
	@Inject
	private OfficeService officeService;

	/**
	 * 树结构选择标签（treeselect.tag）
	 */
	@RequiresUser
	@RequestMapping("treeselect")
	public String treeselect(HttpServletRequest request, Model model) {
		model.addAttribute("url", request.getParameter("url")); // 树结构数据URL
		model.addAttribute("extId", request.getParameter("extId")); // 排除的编号ID
		model.addAttribute("checked", request.getParameter("checked")); // 是否可复选
		model.addAttribute("selectIds", request.getParameter("selectIds")); // 指定默认选中的ID
		String code=request.getParameter("code");
		if(StringUtils.isNotEmpty(code)){
			Office office=officeService.findByCode(code);
			if(office!=null){
				model.addAttribute("parentId",office.getId());
			}
		}
		model.addAttribute("module", request.getParameter("module")); // 过滤栏目模型（仅针对CMS的Category树）
		return "modules/sys/tagTreeselect";
	}

	/**
	 * 图标选择标签（iconselect.tag）
	 */
	@RequiresUser
	@RequestMapping("iconselect")
	public String iconselect(HttpServletRequest request, Model model) {
		model.addAttribute("value", request.getParameter("value"));
		return "modules/sys/tagIconselect";
	}

}
