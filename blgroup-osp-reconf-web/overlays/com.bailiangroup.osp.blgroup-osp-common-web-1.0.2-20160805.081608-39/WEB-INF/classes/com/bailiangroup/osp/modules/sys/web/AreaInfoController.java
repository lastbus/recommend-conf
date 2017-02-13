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
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.bailiangroup.osp.base.core.util.StringUtils;
import com.bailiangroup.osp.common.config.Global;
import com.bailiangroup.osp.common.domain.TreeNode;
import com.bailiangroup.osp.common.domain.entity.AreaInfo;
import com.bailiangroup.osp.common.service.AreaInfoService;
import com.bailiangroup.osp.common.utils.DictUtils;
import com.bailiangroup.osp.common.web.BaseController;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * 区域Controller
 * @author IBM Consultant Team
 * @version 2016-04-18
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/areaInfo")
public class AreaInfoController extends BaseController {

	@Inject
	private AreaInfoService areaInfoService;

	/**
	* 区域列表
	* @param Model model
	* @return
	*/
	@RequiresPermissions("sys:areaInfo:view")
	@RequestMapping(value = {"list", ""})
	public String list(Model model) {
		//获取中国所有省，直辖市
		List<AreaInfo> pList = areaInfoService.getAllByParentId("0");
        model.addAttribute("list", pList);
		return "modules/sys/areaInfoList";
	}

	/**
	* 编辑区域节点
	* @param AreaInfo area
	* @param Model model
	* @param RedirectAttributes redirectAttributes
	* @return
	*/
	@RequiresPermissions("sys:areaInfo:view")
	@RequestMapping(value = "form")
	public String form(String id, String pid, AreaInfo areaInfo, Model model, RedirectAttributes redirectAttributes) {
		
		AreaInfo area;
		if(!StringUtils.isEmpty(id)) {
			//修改，编辑
			area = areaInfoService.get(id);
		} else if(!StringUtils.isEmpty(pid)) {
			//添加下级区域
			AreaInfo parent = areaInfoService.get(pid);
			area = new AreaInfo();
			area.setParent(parent);
			//区县节点无法添加下级区域
			if("3".equals(parent.getLevelId())){
				addMessage(redirectAttributes, "该节点无法添加下级区域");
				return "redirect:"+Global.getAdminPath()+"/sys/areaInfo/";
			}
 			area.setLevelId(String.valueOf(Integer.parseInt(parent.getLevelId())+1));
			area.setParentIds(parent.getParentIds()+parent.getId()+",");
			area.setCountryId(parent.getCountryId());
		} else {
			area = areaInfo;
		}
		
		model.addAttribute("areaInfo", area);
		return "modules/sys/areaInfoForm";
	}
	
	/**
	* 保存区域节点
	* @param AreaInfo area
	* @param Model model
	* @param RedirectAttributes redirectAttributes
	* @return
	*/
	@RequiresPermissions("sys:areaInfo:edit")
	@RequestMapping(value = "save")
	public String save(AreaInfo area, Model model, RedirectAttributes redirectAttributes) {
		//校验
		if (!beanValidator(model, area)){
			redirectAttributes.addAttribute("areaInfo", area);
			return "modules/sys/areaInfo/";
		}
		//获取父级节点
		AreaInfo parent = areaInfoService.get(area.getParent().getId());
		if(parent!=null &&Integer.parseInt(parent.getLevelId()) >= Integer.parseInt(area.getLevelId()) ) {
			redirectAttributes.addAttribute("areaInfo", area);
			addMessage(redirectAttributes, "上级区域必须大于该区域");
			return "redirect:"+Global.getAdminPath()+"/sys/areaInfo/";
		}
		//保存
		AreaInfo areaInfo = this.areaInfoService.save(area);
		addMessage(redirectAttributes, "保存区域'" + areaInfo.getAreaNameS() + "'成功");
		return "redirect:"+Global.getAdminPath()+"/sys/areaInfo/";
	}
	
	/**
	* 删除区域节点
	* @param String id
	* @param RedirectAttributes redirectAttributes
	* @return
	*/
	@RequiresPermissions("sys:areaInfo:edit")
	@RequestMapping(value = "delete")
	public String delete(String id, RedirectAttributes redirectAttributes) {
		//不允许删除根节点
		if (AreaInfo.isAdmin(id)){
			addMessage(redirectAttributes, "删除区域失败, 不允许删除顶级区域或编号为空");
		}else{
			//删除节点成功
			this.areaInfoService.delete(id);
			addMessage(redirectAttributes, "删除区域成功");
		}
		return "redirect:"+Global.getAdminPath()+"/sys/areaInfo/";
	}
	
	/**
	* 异步获取子节点
	* @param String id
	* @return Map<String, Object> resultMap
	*/
	@RequiresPermissions("sys:areaInfo:view")
	@RequestMapping(value = "children")
	@ResponseBody
	public Map<String, Object> getChildren(String id) {
		//获取所有子节点
		List<AreaInfo> childList = areaInfoService.getAllByParentId(id);
		
		if(childList == null){
			return null;
		}
		List<TreeNode> dataList = new ArrayList<TreeNode>();
		for (AreaInfo area : childList) {
			TreeNode treeNode = new TreeNode();
			treeNode.setId(area.getId());
			treeNode.setpId(area.getParent().getId());
			treeNode.setName(area.getAreaNameS());
			treeNode.setCode(area.getAreaCode());
			treeNode.setLevelId(area.getLevelId());
			treeNode.setLevel(DictUtils.getDictLabel(area.getLevelId(), "sys_area_level", area.getLevelId()));
			treeNode.setPinyin(area.getPinyinSm()==null?"":area.getPinyinSm());
			dataList.add(treeNode);
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("data", dataList);
		return resultMap;
	}
	
	/**
	* 获取区域树
	* @param String extId
	* @param HttpServletResponse response
	* @return Map<String, Object> resultMap
	*/
	@RequiresUser
	@ResponseBody
	@RequestMapping(value = "treeData")
	public List<Map<String, Object>> treeData(@RequestParam(required=false) String extId, HttpServletResponse response) {
		
		response.setContentType("application/json; charset=UTF-8");
		List<Map<String, Object>> mapList = Lists.newArrayList();
		List<AreaInfo> list = areaInfoService.findAll();
		for (int i=0; i<list.size(); i++){
			AreaInfo e = list.get(i);
			if (extId == null || (extId!=null && !extId.equals(e.getId()))){
				Map<String, Object> map = Maps.newHashMap();
				map.put("id", e.getId());
				map.put("pId", e.getParent()!=null?e.getParent().getId():0);
				map.put("name", e.getAreaNameS());
				mapList.add(map);
			}
		}
		return mapList;
	}
}
