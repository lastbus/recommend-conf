/**
 * Copyright &copy; 2014-2020 <a href="https://www.bailiangroup.com/osp">Bailian Group OSP</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bailiangroup.osp.modules.sys.web;

import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bailiangroup.osp.base.core.util.pagination.PageAdapter;
import com.bailiangroup.osp.base.core.util.pagination.ViewPage;
import com.bailiangroup.osp.common.domain.entity.Log;
import com.bailiangroup.osp.common.service.LogService;
import com.bailiangroup.osp.common.web.BaseController;

/**
 * 日志Controller
 * @author IBM Consultant Team
 * @version 2013-6-2
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/log")
public class LogController extends BaseController {

	@Inject
	private LogService logService;

	@RequiresPermissions("sys:log:view")
	@RequestMapping(value = {"list", ""})
	public String list(@RequestParam Map<String, Object> paramMap, HttpServletRequest request, HttpServletResponse response, Model model) {
		Page<Log> page = logService.find(new PageAdapter<Log>(new ViewPage<Log>(request, response)), paramMap); 
        model.addAttribute("page", new PageAdapter<Log>(page));
        model.addAllAttributes(paramMap);
		return "modules/sys/logList";
	}

}
