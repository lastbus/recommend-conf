/**
 * Copyright &copy; 2014-2020 <a href="https://www.bailiangroup.com/osp">Bailian Group OSP</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bailiangroup.osp.modules.sys.web;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson.JSONObject;
import com.bailiangroup.inspektr.audit.annotation.AuditLog;
import com.bailiangroup.osp.base.core.log.Logger;
import com.bailiangroup.osp.base.core.log.LoggerUtils;
import com.bailiangroup.osp.base.core.service.ctx.ContextService;
import com.bailiangroup.osp.base.core.util.DateUtils;
import com.bailiangroup.osp.base.core.util.mapper.JsonMapper;
import com.bailiangroup.osp.base.core.util.pagination.PageAdapter;
import com.bailiangroup.osp.base.core.util.pagination.ViewPage;
import com.bailiangroup.osp.base.core.validator.BeanValidators;
import com.bailiangroup.osp.common.CacheManager;
import com.bailiangroup.osp.common.config.Global;
import com.bailiangroup.osp.common.domain.entity.Office;
import com.bailiangroup.osp.common.domain.entity.Role;
import com.bailiangroup.osp.common.domain.entity.Store;
import com.bailiangroup.osp.common.domain.entity.User;
import com.bailiangroup.osp.common.domain.entity.UserExport;
import com.bailiangroup.osp.common.repository.UserRepository;
import com.bailiangroup.osp.common.service.DictService;
import com.bailiangroup.osp.common.service.OfficeService;
import com.bailiangroup.osp.common.service.SiteStoreService;
import com.bailiangroup.osp.common.service.SystemService;
import com.bailiangroup.osp.common.utils.ConstantUtils;
import com.bailiangroup.osp.common.utils.DKeyUtils;
import com.bailiangroup.osp.common.utils.DictUtils;
import com.bailiangroup.osp.common.utils.excel.ExportExcel;
import com.bailiangroup.osp.common.utils.excel.ImportExcel;
import com.bailiangroup.osp.common.web.BaseController;
import com.google.common.collect.Lists;

import edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.core.util.Base64Encoder;

/**
 * 用户Controller
 * @author IBM Consultant Team
 * @version 2013-5-31
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/user")
public class UserController extends BaseController {

	private final static Logger logger = LoggerUtils.getLogger(UserController.class) ;

	@Inject
	private ContextService contextService;
	
	@Inject
	private SystemService systemService;
	
	@Inject
	private OfficeService officeService;
	
	@Inject
	private OfficeService companyService;
	
	@Inject
	private SiteStoreService storeService;
	
	@Inject UserRepository userRepository;
	
	@Inject
	private DictService dictService;
	
	@ModelAttribute
	public User get(@RequestParam(required=false) String id) {
		if (StringUtils.isNotBlank(id)){
			User usr = systemService.getUser(id);
			if ( usr!= null && usr.getCompany()!= null) {
				usr.setCompany((Office)(SerializationUtils.clone(usr.getCompany())));
			}
			if ( usr!= null && usr.getOffice()!= null) {
				usr.setOffice((Office)(SerializationUtils.clone(usr.getOffice())));
			}
			return usr;
		}else{
			return new User();
		}
	}
	
	@RequiresPermissions("sys:user:view")
	@RequestMapping({"list", ""})
		@AuditLog(action = "OSP用户列表查询", actionResolverName = "INVOKE_SERVICE_RESOLVER", 
		resourceResolverName = "INVOKE_SERVICE_RESOURCE_RESOLVER", ctxAttributeNames="totalCnt")
	public String list(User user, HttpServletRequest request, HttpServletResponse response, Model model) {
		
		Page<User> page = systemService.find(new PageAdapter<User>(new ViewPage<User>(request, response)), user); 
		if ( page != null ) {
        	model.addAttribute("page", new PageAdapter<User>(page));
            model.addAttribute("totalCnt", page.getTotalElements());
        }
		
		model.addAttribute("page", new PageAdapter<User>(page));

		return "modules/sys/userList";
	}

	@RequiresPermissions("sys:user:view")
	@RequestMapping("form")
	public String form(User usr, Model model) {
		
		User currentUser = contextService.<User>getCurrentUser();
		
/*		if (user.getCompany() != null || user.getCompany().getId() != null) {
			user.setCompany(currentUser.getCompany());
		}
		if (user.getOffice() != null || user.getOffice().getId() != null) {
			user.setOffice(currentUser.getOffice());
		}*/

		// 判断显示的用户是否在授权范围内
		Office office = usr.getOffice();
		String officeId= new String();
		if(office!=null){
			officeId = usr.getOffice().getId();
		}
		
		
		if (!currentUser.isAdmin()) {
			String dataScope = systemService.getDataScope(currentUser);
			// System.out.println(dataScope);
			if (dataScope.indexOf("office.id=") != -1) {
				String AuthorizedOfficeId = dataScope.substring(dataScope.indexOf("office.id=") + 10, dataScope.indexOf(" or"));
				if (!AuthorizedOfficeId.equalsIgnoreCase(officeId)) {
					return "error/403";
				}
			}
		}
		String roleIdStr =  StringUtils.join(usr.getRoleIdList(), ",");
		
		if ( StringUtils.isBlank(roleIdStr)){
			roleIdStr = "";
		}
		
		model.addAttribute("chan",","+usr.getChannelType()+",");
		model.addAttribute("user", usr);
		model.addAttribute("roles", "," + roleIdStr + ",");
		model.addAttribute("allRoles", systemService.findAllRole());
		return "modules/sys/userForm";
	}

	@RequiresPermissions("sys:user:edit")
	@RequestMapping("save")
	@AuditLog(action = "保存用户", actionResolverName = "INVOKE_SERVICE_RESOLVER", 
		resourceResolverName = "INVOKE_SERVICE_RESOURCE_RESOLVER", ctxAttributeNames="changed-user")
	public String save(User user, String oldLoginName, String newPassword, HttpServletRequest request, Model model, RedirectAttributes redirectAttributes) {
		
		// 如果新密码为空，则不更换密码
		if (StringUtils.isNotBlank(newPassword)) {
			user.setPassword(systemService.entryptPassword(newPassword));
		}
		if (!beanValidator(model, user)) {
			return form(user, model);
		}
		if (!"true".equals(checkLoginName(oldLoginName, user.getLoginName()))) {
			addMessage(model, "保存用户'" + user.getLoginName() + "'失败，登录名已存在");
			return form(user, model);
		}
		String channels = request.getParameter("channelTypes");
		if(StringUtils.isNotBlank(channels)){
			channels = channels.replace(" ", "");
		}
		user.setChannelType(channels.trim());
		user.setBizType(user.getBizType());
		
		// 角色数据有效性验证，过滤不在授权内的角色
		List<String> selectedRoles = user.getRoleIdList();
		
		if ( !CollectionUtils.isEmpty(selectedRoles) ) {
			
			user.getRoleList().clear();
			for (String roleId : selectedRoles) {
				user.getRoleList().add(this.systemService.getRole(roleId));
			}
		}
		else{
			user.getRoleList().clear();
		}
		
		List<String> selectedStores=user.getStoreIdList();
		if(!CollectionUtils.isEmpty(selectedStores)){
			user.getStoreList().clear();
			for(String storeId:selectedStores){
				user.getStoreList().add(this.systemService.getStore(storeId));
			}
		}
		
		// 保存用户信息
		User currentUser = contextService.getCurrentUser();
		
		// 修正引用赋值问题，不知道为何，Company和Office引用的一个实例地址，修改了一个，另外一个跟着修改。
		user.setCompany( officeService.findByCode(request.getParameter("company.code")) );
		user.setOffice( officeService.findByCode(request.getParameter("office.code")) );
		user.setCreateBy(currentUser.getId());
		user.setUpdateBy(currentUser.getId());
		
		String dkeyStatus=user.getDkeyStatus();
		String dkeyNumber=user.getDkeyNumber();
		String resultJson = "";
		
		/*if(StringUtils.isNotEmpty(dkeyStatus)&&StringUtils.isNotEmpty(dkeyNumber)){
			String url=Global.getConfig("dkey.path");
			
			JSONObject paramJSON=new JSONObject(); 
			if(ConstantUtils.KEY_ENABLE_CODE.equals(dkeyStatus)){
				paramJSON.put("businessType", ConstantUtils.KEY_ENABLE);//验证方法
			}else if(ConstantUtils.KEY_DISABLE_CODE.equals(dkeyStatus)){
				paramJSON.put("businessType", ConstantUtils.KEY_DISABLE);//验证方法
			}
			//设置电子令牌的属性
			paramJSON.put("deviceid", ConstantUtils.KEY_DEVICE_ID);  //控制版本号
			paramJSON.put("devicesn", user.getDkeyNumber());   //用户绑定令牌的序列号
			paramJSON.put("transcont", ""); //挑战码
			paramJSON.put("pin", "");//pin码
			String jsonStr=paramJSON.toJSONString();
			logger.debug("dkey",jsonStr);
			resultJson = DKeyUtils.sendPost(url,"jsonStr="+jsonStr);
			JSONObject resultObj=JSONObject.parseObject(resultJson);
			String resType ="";
			if (resultObj != null ){
				resType=resultObj.getString("retCode");
				logger.debug("resType",resultObj.toJSONString());
			}
			//String resType = resultJson.substring(12,19);
			if(StringUtils.trim(resType).length()>0){
				user.setResType(resType);
			}
		}*/
		if(user.getId().length()>0){
			User old = this.systemService.getUser(user.getId());
			if(old != null && old.getDkeyType()!=null && !old.getDkeyType().equalsIgnoreCase(user.getDkeyType())){
				user.setDkeyStatus(null);
				user.setResType(null);
			}
		}
		user = systemService.saveUser(user);
		
		// 清除当前用户缓存
		if (user.getLoginName().equals(currentUser.getLoginName())) {
			CacheManager.getCacheMap().clear();
		}
		
		model.addAttribute("changed-user", JsonMapper.toJsonString(user));
		addMessage(redirectAttributes, "保存用户'" + user.getLoginName() + "'成功");
		return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
	}
	
	@RequiresPermissions("sys:user:edit")
	@RequestMapping("delete")
	public String delete(String id, RedirectAttributes redirectAttributes) {
		
		User currentUser = contextService.<User>getCurrentUser();
		
		if (currentUser.getId().equals(id)) {
			addMessage(redirectAttributes, "删除用户失败, 不允许删除当前用户");
		} else if (User.isAdmin(id)) {
			addMessage(redirectAttributes, "删除用户失败, 不允许删除超级管理员用户");
		} else {
			systemService.deleteVirtualUser(id);
			addMessage(redirectAttributes, "删除用户成功");
		}
		return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
	}
	
	@RequiresPermissions("sys:user:view")
    @RequestMapping(value = "export", method=RequestMethod.POST)
    public String exportFile(User user, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
		
		try {
			String fileName = "用户数据" + DateUtils.getDate("yyyyMMddHHmmss") + ".xlsx"; 
			//Page<User> page = systemService.find(new PageAdapter<User>(new ViewPage<User>(request, response, -1)), user); 
			List<User> users= systemService.findByConditions(user);
			List<UserExport> exports = Lists.newArrayList();
			for(User u : users){
				UserExport target = new UserExport();
				BeanUtils.copyProperties(u, target);
				target.setOffice(u.getOffice().getName());
				target.setCompany(u.getCompany().getName());
				String roleList = "";
				for(Role r: u.getRoleList()){
					roleList+=r.getName()+",";
				}
				target.setRoles(roleList);
				exports.add(target);
			}
    		new ExportExcel("用户数据", UserExport.class).setDataList(exports).write(response, fileName).dispose();
    		
    		return null;
		} catch (Exception e) {
			addMessage(redirectAttributes, "导出用户失败！失败信息："+e.getMessage());
		}
		return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
    }
	
	@RequiresPermissions("sys:user:edit")
	@RequestMapping(value = "import/data")
	public String importData(User user,Model model){
		model.addAttribute("user", user);
		return "modules/sys/userImport";
		
	}
	
	@RequiresPermissions("sys:user:edit")
    @RequestMapping(value = "import", method=RequestMethod.POST)
    public String importFile(MultipartFile file,User u,HttpServletRequest request,RedirectAttributes redirectAttributes) {
		
		try {
			int successNum = 0;
			int failureNum = 0;
			StringBuilder failureMsg = new StringBuilder();
			ImportExcel ei = new ImportExcel(file, 1, 0);
			List<User> list = ei.getDataList(User.class);
			for (User user : list){
				try{
					if ("true".equals(checkLoginName("", user.getLoginName()))){
						user.setPassword(systemService.entryptPassword("123456"));
						if(StringUtils.isNotBlank(u.getCompany().getCode())){
							Office company = officeService.findByCode(u.getCompany().getCode());
							if(company!=null){
								user.setCompany(company);
							}
						}else{
							failureNum++;
							continue;
						}
						if(StringUtils.isNotBlank(u.getOffice().getCode())){
							Office office = officeService.findByCode(u.getOffice().getCode());
							if(office!=null){
								user.setOffice(office);
							}
						}else{
							failureNum++;
							continue;
						}
						if(StringUtils.isNotBlank(u.getBizType())){
							user.setBizType(u.getBizType());
						}else{
							failureNum++;
							continue;
						}
						String channels = request.getParameter("channelTypes");
						if(StringUtils.isNotBlank(channels)){
							channels = channels.replace(" ", "");
							user.setChannelType(channels.trim());
						}else{
							failureNum++;
							continue;
						}
						if(StringUtils.isBlank(user.getName())){
							failureNum++;
							continue;
						}
						BeanValidators.validateWithException(validator, user);
						systemService.saveUser(user);
						successNum++;
					}else{
						failureMsg.append("<br/>登录名 " + user.getLoginName() + " 已存在; ");
						failureNum++;
					}
				}catch(ConstraintViolationException ex){
					failureMsg.append("<br/>登录名 " + user.getLoginName() + " 导入失败：");
					List<String> messageList = BeanValidators.extractPropertyAndMessageAsList(ex, ": ");
					for (String message : messageList){
						failureMsg.append(message+"; ");
						failureNum++;
					}
				}catch (Exception ex) {
					failureMsg.append("<br/>登录名 " + user.getLoginName() + " 导入失败：" + ex.getMessage());
				}
			}
			if (failureNum>0){
				failureMsg.insert(0, "，失败 "+failureNum+" 条用户，导入信息如下：");
			}
			addMessage(redirectAttributes, "已成功导入 " + successNum+" 条用户" + failureMsg);
		} catch (Exception e) {
			addMessage(redirectAttributes, "导入用户失败！失败信息：" + e.getMessage());
		}
		return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
    }
	
	
	@RequiresPermissions("sys:user:view")
    @RequestMapping("import/template")
    public String importFileTemplate(HttpServletResponse response, RedirectAttributes redirectAttributes) {
		try {
			String fileName = "用户数据导入模板.xlsx";
			List<User> list = Lists.newArrayList();
			User currentUser = contextService.<User>getCurrentUser();
			list.add(currentUser);
			new ExportExcel("用户数据", User.class,1).setDataList(list).write(response, fileName).dispose();
			return null;
		} catch (Exception e) {
			addMessage(redirectAttributes, "导入模板下载失败！失败信息：" + e.getMessage());
		}
		return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
    }

	@ResponseBody
	@RequiresPermissions("sys:user:edit")
	@RequestMapping("checkLoginName")
	public String checkLoginName(String oldLoginName, String loginName) {
		if (loginName != null && loginName.equals(oldLoginName)) {
			return "true";
		} else if (loginName != null && systemService.getUserByLoginName(loginName) == null) {
			return "true";
		}
		return "false";
	}

	@RequiresUser
	@RequestMapping("info")
	public String info(User user, Model model) {
		
		User currentUser = contextService.getCurrentUser();
		if (StringUtils.isNotBlank(user.getName())){
			if(Global.isDemoMode()){
				model.addAttribute("message", "演示模式，不允许操作！");
				return "modules/sys/userInfo";
			}
			
			currentUser = contextService.getCurrentUser(true);
			currentUser.setEmail(user.getEmail());
			currentUser.setPhone(user.getPhone());
			currentUser.setMobile(user.getMobile());
			currentUser.setRemarks(user.getRemarks());
			systemService.saveUser(currentUser);
			model.addAttribute("message", "保存用户信息成功");
		}
		model.addAttribute("currentUser", currentUser);
		return "modules/sys/userInfo";
	}

	@RequiresUser
	@RequestMapping("modifyPwd")
	@AuditLog(action = "修改用户密码", actionResolverName = "INVOKE_SERVICE_RESOLVER", 
		resourceResolverName = "INVOKE_SERVICE_RESOURCE_RESOLVER", ctxAttributeNames="user, message")
	public String modifyPwd(String oldPassword, String newPassword, Model model) {
		
		User currentUser = contextService.getCurrentUser();
		
		if (StringUtils.isNotBlank(oldPassword) && StringUtils.isNotBlank(newPassword)){

			if (systemService.validatePassword(oldPassword, currentUser.getPassword())){
				systemService.updatePasswordById(currentUser.getId(), currentUser.getLoginName(), newPassword);
				model.addAttribute("message", "修改密码成功");
			}else{
				model.addAttribute("message", "修改密码失败，旧密码错误");
			}
		}
		model.addAttribute("currentUser", currentUser);
		return "modules/sys/userModifyPwd";
	}
	
	@RequestMapping("selstore")
	public String selStore(@RequestParam Map<String, Object> paramMap,String type,Store store,String bizIds, HttpServletRequest request, HttpServletResponse response, Model model) {
		List<String> bizTypes=new ArrayList<String>();
		if(StringUtils.isNotEmpty(bizIds)&&bizIds.split(",").length>0){
			bizTypes=DictUtils.getDictValuesByValueStr(bizIds);
		}
		Page<Store> page =  storeService.find(new PageAdapter<Store>(new ViewPage<Store>(request, response)), paramMap,store, bizTypes);
		//model.addAttribute("type",type);
		model.addAttribute("page", new PageAdapter<Store>(page));
		return "modules/sys/selectStore"; 
	}

	/***
	 * 口令编辑页面的跳转
	 * @param id
	 * @param user
	 * @param request
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("dkeyInfo")
	public String dkeyInfo(String id,User user,HttpServletRequest request,Model model,RedirectAttributes redirectAttributes) {
		model.addAttribute("user",user);
		return "modules/sys/dialog/selectDKey"; 
	}
	
	
	/**
	*判断电子口令是否会重复 
	*@param dkeyNumber
	*@param request
	*@param model
	*@param redirectAttributes
	*@return
	*/
	@RequestMapping("checkDKeyNumber")
	@ResponseBody
	public Map<String,Object> checkDKeyNumber(String dkeyNumber,HttpServletRequest request,Model model,RedirectAttributes redirectAttributes){
		Map<String, Object> resultMap = new HashMap<String, Object>();
		User user=this.systemService.getUserByDkeyNumber(dkeyNumber);
		if(user!=null)
			resultMap.put("result",1);
		else
			resultMap.put("result",0);
		
		return resultMap;
	}
	
	
	/**
	*判断电子初始码是否会重复 
	*@param dkeyCode
	*@param request
	*@param model
	*@param redirectAttributes
	*@return
	*/
	@RequestMapping("checkDKeyCode")
	@ResponseBody
	public Map<String,Object> checkDKeyCode(String dkeyInitial,HttpServletRequest request,Model model,RedirectAttributes redirectAttributes){
		User user=this.systemService.getUserByDkeyCode(dkeyInitial);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if(user!=null)
			resultMap.put("result",1);
		else
			resultMap.put("result",0);
		return resultMap;
	}
	
	/***
	 * 二维码扫描
	 * @param id
	 * @param request
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("qrcode")
	public String qrcode(String id,HttpServletRequest request,Model model,RedirectAttributes redirectAttributes){
		User user = systemService.getUser(id);
		model.addAttribute("user",user);
		return "modules/sys/dialog/qrcode"; 
	}
	
	/***
	 * 查询电子口令状态
	 * @param id
	 * @param request
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("query")
	public String query(String id,HttpServletRequest request,Model model,RedirectAttributes redirectAttributes){
		User user = systemService.getUser(id);
		String resultJson="";
		JSONObject paramJSON=new JSONObject();
		if(user.getDkeyType().equals("1")){
			paramJSON.put("deviceid",ConstantUtils.KEY_DEVICE_APP_ID);  //控制版本号
		}else{
			paramJSON.put("deviceid",ConstantUtils.KEY_DEVICE_ID);  //控制版本号
		}
		paramJSON.put("devicesn",user.getDkeyNumber());
		paramJSON.put("businessType",ConstantUtils.KEY_QUERY);//验证方法
		String jsonStr=paramJSON.toJSONString();
		logger.debug("dkey",jsonStr);
		//resultJson=DKeyUtils.sendPost(Global.getConfig("dkey.path"),"jsonStr="+jsonStr.trim());
		//System.out.println(resultJson);
		return "modules/sys/dialog/query"; 
	}
	
	/***
	 * 软件注册时初始化序列号
	 * @param dkeyInitial
	 * @param request
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("initKeyNumber")
	@ResponseBody
	public Map<String,Object> initKeyNumber(String dkeyInitial,HttpServletRequest request,Model model,RedirectAttributes redirectAttributes){
		String resultJson="";
		Map<String, Object> resultMap = new HashMap<String, Object>();
		if(StringUtils.isNotEmpty(dkeyInitial)){
			JSONObject paramJSON=new JSONObject();
			paramJSON.put("deviceid",ConstantUtils.KEY_DEVICE_APP_ID);  //控制版本号
			paramJSON.put("initcode",dkeyInitial);
			paramJSON.put("businessType",ConstantUtils.KEY_MOBILEBIND);//验证方法
			String jsonStr=paramJSON.toJSONString();
			logger.debug("dkey",jsonStr);
			resultJson=DKeyUtils.sendPost(Global.getConfig("dkey.path"),"jsonStr="+jsonStr.trim());
			resultMap.put("resultJson",resultJson);
			return resultMap;
		}
		return null;
	}
	
	/***
	 * 清除用户电子口令相关信息
	 * @param id
	 * @param user
	 * @param request
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("dkeyEmpty")
	public String dkeyEmpty(String id,User user,HttpServletRequest request,Model model,RedirectAttributes redirectAttributes) {
		
		boolean rt =systemService.updateUserDkeyNumber(id);
		String message="";
		if(rt){
			message=ConstantUtils.KEY_EMPTY_SUCCESS;
		}else{
			message=ConstantUtils.KEY_EMPTY_ERROR;
		}
		addMessage(redirectAttributes,message);
		return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
	}

	/***
	 * 电子口令编辑
	 * 1 -为软体 1008 2-为硬体1101
	 * 操作：激活 禁用 挂失 解挂 解锁 同步
	 * @param id
	 * @param dkeyStatus
	 * @param user
	 * @param request
	 * @param model
	 * @param redirectAttributes
	 * @return
	 */
	@RequestMapping("editDKey")
	public String editDKey(String id,String dkeyType,String dkeyStatus,User user,HttpServletRequest request,Model model,RedirectAttributes redirectAttributes) {
		String resultJson="";
		String message="";
		String actionPassword = request.getParameter("actionPassword");
		String firstDyPassword = request.getParameter("firstDyPassword");
		String secondDyPassword = request.getParameter("secondDyPassword");
		User myUser=systemService.getUser(id);
		String dkeyNumber=myUser.getDkeyNumber();
		//激活验证
		if(!StringUtils.isNotEmpty(actionPassword)&&ConstantUtils.KEY_ENABLE_CODE.equals(dkeyStatus)){
			message="激活口令必须有动态口令";
			if(actionPassword.length()!=6){
				message="动态口令长度有误";
			}
			addMessage(redirectAttributes,message);
			return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
		}//解挂验证
		else if(ConstantUtils.KEY_UNLOST_CODE.equals(dkeyStatus)&&!StringUtils.isNotEmpty(actionPassword)){
			message="解挂口令必须有动态口令";
			if(actionPassword.length()!=6){
				message="动态口令长度有误";
			}
			addMessage(redirectAttributes,message);
			return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
		}//同步验证
		else if(ConstantUtils.KEY_SYNC_CODE.equals(dkeyStatus)&&!StringUtils.isNotEmpty(firstDyPassword)&&!StringUtils.isNotEmpty(secondDyPassword)){
			message="同步口令必须两条动态口令";
			if(firstDyPassword.length()!=6 && secondDyPassword.length()!=6){
				message="两条动态口令长度有误";
			}
			addMessage(redirectAttributes,message);
			return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
		}
		
		if(StringUtils.isNotEmpty(dkeyNumber)&&StringUtils.isNotEmpty(dkeyStatus)){
			String url=Global.getConfig("dkey.path");
			JSONObject paramJSON=new JSONObject(); 
			if(ConstantUtils.KEY_ENABLE_CODE.equals(dkeyStatus)){
				//激活方法
				if(dkeyType.equals("1")){//手机激活
					//{"activatecode":"211956492361850811112736014269","dyPassword":"968798","devicesn":"2119564923618508","businessType":"mobileEnable","deviceid":"1008"}
					paramJSON.put("businessType",ConstantUtils.KEY_MOBILEENABLE);//激活方法
					paramJSON.put("dyPassword",actionPassword);
					paramJSON.put("activatecode",user.getDkeyActivation().substring(16));
				}else{//硬体激活
					//{"businessType":"enable","dyPassword":"968798","pin":"","devicesn":"32372745977361410099672736014269","deviceid":"1101"}
					paramJSON.put("businessType",ConstantUtils.KEY_ENABLE);//激活方法
					paramJSON.put("dyPassword",actionPassword);
					paramJSON.put("pin","");
				}
			}else if(ConstantUtils.KEY_DISABLE_CODE.equals(dkeyStatus)){
				//{"businessType":"disable","devicesn":"32372745977361410099672736014269","deviceid":"1008"}
				paramJSON.put("businessType",ConstantUtils.KEY_DISABLE);//禁用方法
			}else if(ConstantUtils.KEY_LOST_CODE.equals(dkeyStatus)){
				//{"businessType":"lost","devicesn":"32372745977361410099672736014269","deviceid":"1008"}
				paramJSON.put("businessType",ConstantUtils.KEY_LOST);//挂失方法
			}else if(ConstantUtils.KEY_UNLOST_CODE.equals(dkeyStatus)){
				//{"businessType":"unlost","dyPassword":"968798","pin":"","devicesn":"32372745977361410099672736014269","deviceid":"1008"}
				paramJSON.put("businessType",ConstantUtils.KEY_UNLOST);//解挂方法
				paramJSON.put("dyPassword",actionPassword);
				paramJSON.put("pin","");//pin码
			}else if(ConstantUtils.KEY_UNLOCK_CODE.equals(dkeyStatus)){
				//{"businessType":"unlock","devicesn":"32372745977361410099672736014269","deviceid":"1008"}
				paramJSON.put("businessType",ConstantUtils.KEY_UNLOCK);//解锁方法
			}else if(ConstantUtils.KEY_SYNC_CODE.equals(dkeyStatus)){
				//硬体同步方法
				//{"businessType":"sync","devicesn":"000000000047","firstDyPassword":"123123","secondDyPassword":"234234","deviceid":"1101"}
				paramJSON.put("businessType",ConstantUtils.KEY_SYNC);
				paramJSON.put("firstDyPassword",firstDyPassword);
				paramJSON.put("secondDyPassword",secondDyPassword);
			}
			
			//设置电子令牌的属性
			if(dkeyType.equals("1")){
				paramJSON.put("deviceid",ConstantUtils.KEY_DEVICE_APP_ID);  //控制版本号 软体
			}else{
				paramJSON.put("deviceid",ConstantUtils.KEY_DEVICE_ID);  //控制版本号 硬体
			}
			paramJSON.put("devicesn",dkeyNumber);   //用户绑定令牌的序列号
			paramJSON.put("transcont",""); //挑战码
			String jsonStr=paramJSON.toJSONString();
			logger.debug("dkey",jsonStr);
			try{
				resultJson=DKeyUtils.sendPost(url,"jsonStr="+jsonStr.trim());
			}catch(Exception e){
				e.printStackTrace();
				addMessage(redirectAttributes,"动态口令服务异常");
				return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
			}
		}
		if(StringUtils.isNotEmpty(resultJson)){
				JSONObject resultObj=JSONObject.parseObject(resultJson);
				String retCode ="";
				if (resultObj != null ){
					retCode=resultObj.getString("retCode");
					logger.debug("retCode",resultObj.toJSONString());
				}
				//String retCode =  resultJson.substring(12,19);
				message=ConstantUtils.getKeyResultCode(retCode);
				systemService.updateResType(id,message);
				if(ConstantUtils.KEY_RESULT_SUCCESS.equals(retCode)){
					systemService.updateDKeyInfo(id,dkeyStatus);
				}
		}else{
			message="电子口令更改状态异常";
		}
		addMessage(redirectAttributes,message);
		return "redirect:" + Global.getAdminPath() + "/sys/user/?repage";
	}
	
}
