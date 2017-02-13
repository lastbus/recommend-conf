/**
 * Copyright &copy; 2014-2020 <a href="https://www.bailiangroup.com/osp">Bailian Group OSP</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bailiangroup.osp.modules.sys.web;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bailiangroup.osp.base.core.util.pagination.PageAdapter;
import com.bailiangroup.osp.base.core.util.pagination.ViewPage;
import com.bailiangroup.osp.common.config.Global;
import com.bailiangroup.osp.common.domain.entity.Dict;
import com.bailiangroup.osp.common.service.DictService;
import com.bailiangroup.osp.common.web.BaseController;

/**
 * 字典Controller
 * @author IBM Consultant Team
 * @version 2013-3-23
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/dict")
public class DictController extends BaseController {

	@Inject
	private DictService dictService;
	
	@ModelAttribute("dict")
	public Dict get(@RequestParam(required=false) String id) {
		if (StringUtils.isNotBlank(id)){
			return dictService.get(id);
		}else{
			return new Dict();
		}
	}
	
	@RequiresPermissions("sys:dict:view")
	@RequestMapping(value = {"list", ""})
	public String list(Dict dict, HttpServletRequest request, HttpServletResponse response, Model model) {
		List<String> typeList = dictService.findTypeList();
		model.addAttribute("typeList", typeList);
        Page<Dict> page = dictService.find(new PageAdapter<Dict>(new ViewPage<Dict>(request, response)), dict); 
        model.addAttribute("page", new PageAdapter<Dict>(page));
		return "modules/sys/dictList";
	}

	@RequiresPermissions("sys:dict:view")
	@RequestMapping(value = "form")
	public String form(Dict dict, Model model) {
		List<String> typeList = dictService.findTypeList();
		model.addAttribute("typeList", typeList);
		model.addAttribute("dict", dict);
		return "modules/sys/dictForm";
	}
	
	/**
	 * 通过字典类型获得字典列表
	 * @param dictType
	 * @return jsonString
	 */
	@RequiresPermissions("sys:dict:view")
	@RequestMapping(value="change")
	@ResponseBody
	public JSONObject changeDictType(String dictType){
		List<Dict> dictList= dictService.findDictListByType(dictType);
		JSONObject jsonO=new JSONObject();
		JSONArray array=new JSONArray();
		for(Dict dict:dictList){
			JSONObject dictJson=new JSONObject();
			dictJson.put("id",dict.getId());
			dictJson.put("label",dict.getLabel());
			dictJson.put("value",dict.getValue());
			array.add(dictJson);
		}
		jsonO.put("status",200);
		jsonO.put("list",array);
		return jsonO;
	}
	
	
	
	/**
	 * 通过字典类型获得字典列表
	 * @param dictType
	 * @return jsonString
	 */
	@RequiresPermissions("sys:dict:view")
	@RequestMapping(value="showDict")
	@ResponseBody
	public JSONObject dict(String parentId){
		String dictType= dictService.findDictTypeById(parentId);
		List<Dict> dictList= dictService.findDictListByType(dictType);
		JSONObject jsonO=new JSONObject();
		JSONArray array=new JSONArray();
		for(Dict dict:dictList){
			JSONObject dictJson=new JSONObject();
			dictJson.put("label",dict.getLabel());
			dictJson.put("value",dict.getValue());
			dictJson.put("id", dict.getId());
			array.add(dictJson);
		}
		jsonO.put("status",200);
		jsonO.put("type",dictType);
		jsonO.put("parentId",parentId);
		jsonO.put("list",array);
		return jsonO;
	}
	
	@RequiresPermissions("sys:dict:edit")
	@RequestMapping(value = "save")//@Valid 
	public String save(Dict dict, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {

		if (!beanValidator(model, dict)){
			return form(dict, model);
		}
		dictService.save(dict);
		addMessage(redirectAttributes, "保存字典'" + dict.getLabel() + "'成功");
		return "redirect:"+Global.getAdminPath()+"/sys/dict/?type="+dict.getType();
	}
	
	@RequiresPermissions("sys:dict:edit")
	@RequestMapping(value = "delete")
	public String delete(String id, RedirectAttributes redirectAttributes) {
		
		dictService.delete(id);
		addMessage(redirectAttributes, "删除字典成功");
		return "redirect:"+Global.getAdminPath()+"/sys/dict/?repage";
	}

}
