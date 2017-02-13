package com.bailiangroup.osp.modules.sys.web;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import net.sf.json.JSONArray;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.bailiangroup.osp.base.core.service.ctx.ContextService;
import com.bailiangroup.osp.common.domain.OmniScope.OmniStore;
import com.bailiangroup.osp.common.domain.entity.Dict;
import com.bailiangroup.osp.common.domain.entity.Store;
import com.bailiangroup.osp.common.domain.entity.User;
import com.bailiangroup.osp.common.service.DictService;
import com.bailiangroup.osp.common.service.SiteStoreService;
import com.bailiangroup.osp.common.utils.DictUtils;
import com.bailiangroup.osp.common.web.BaseController;
import com.google.common.collect.Lists;

@Controller
@RequestMapping(value = "${adminPath}/sys/omni")
public class OmniController extends BaseController {
	
	@Inject
	private DictService dictService;
	
	@Inject
	private ContextService contextService;
	
	@Inject
	private SiteStoreService storeService; 
	
	
	/**
	* 多选树形结构
	* @param store
	* @param model
	* @param typeStr
	* @param ids
	* @return
	*/
	@RequestMapping(value = "selectStore")
	public String selectStore(Store store, Model model,String typeStr,String ids) {
		List<Dict> dictList=new ArrayList<Dict>();
		if(StringUtils.isNotBlank(typeStr)){
			dictList=dictService.findDictListByType(typeStr);
		}
		User currentUser = contextService.<User>getCurrentUser();
		model.addAttribute("dictList", dictList);
		model.addAttribute("ids", ids);
		model.addAttribute("bizType", currentUser.getBizType());
		return "modules/sys/dialogSelectStore";
	}
	
	/**
	* 单选树形结构
	* @param store
	* @param model
	* @param typeStr
	* @param ids
	* @return
	*/
	@RequestMapping(value = "selectStoreSingle")
	public String selectStoreSingle(Store store, Model model,String typeStr,String ids) {
		List<Dict> dictList=new ArrayList<Dict>();
		List<String> valueList=new ArrayList<String>();
		User currentUser = contextService.<User>getCurrentUser();
		String bizTypes=currentUser.getBizType();
		String ruleBizId=currentUser.getRuleBizId();
		String ruleStr="";
		StringBuffer result = new StringBuffer();
		if(StringUtils.isNotEmpty(ruleBizId)){
			JSONArray array=JSONArray.fromObject(ruleBizId);
			for(int i=0;i<array.size();i++){
				valueList.add(array.getString(i));
			}
			ruleStr=DictUtils.listToString(valueList).trim();
		}
		
		result.append(bizTypes);
		if(StringUtils.isNotEmpty(bizTypes.trim()))
			result.append(",");
		result.append(ruleStr);
		if(StringUtils.isNotBlank(typeStr)){
			dictList = DictUtils.getDictListByValueList(typeStr,result.toString());
		}
		model.addAttribute("dictList", dictList);
		model.addAttribute("ids", ids);
		return "modules/sys/selectStoreSingle";
	}
	
	@RequestMapping(value="selOmniStore/{omniCode}", 
			method={RequestMethod.POST, RequestMethod.GET}, produces="application/json")
	public String selStore(@PathVariable String omniCode, Model model){
		
		List< OmniStore> stores = null;
		
		if ( StringUtils.isNotBlank(omniCode)) {
			 stores = storeService.getOmniStoresByCode(omniCode);
		}
		
		if ( CollectionUtils.isEmpty(stores)) {
			
			stores = Lists.newArrayList();
		}
		model.addAttribute("omnistore", stores);
		
		return "modules/sys/dialogSelectStore"; 
	}

}
