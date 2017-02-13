/**
 * Copyright &copy; 2014-2020 <a href="https://www.bailiangroup.com/osp">Bailian Group OSP</a> All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.bailiangroup.osp.modules.sys.web;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresUser;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bailiangroup.osp.base.core.service.ctx.ContextService;
import com.bailiangroup.osp.base.core.util.AppConfig;
import com.bailiangroup.osp.base.core.util.CacheUtils;
import com.bailiangroup.osp.base.core.util.CookieUtils;
import com.bailiangroup.osp.common.CacheManager;
import com.bailiangroup.osp.common.config.Global;
import com.bailiangroup.osp.common.domain.entity.User;
import com.bailiangroup.osp.common.web.BaseController;
import com.google.common.collect.Maps;

/**
 * 登录Controller
 * @author IBM Consultant Team
 * @version 2013-5-31
 */
@Controller
public class LoginController extends BaseController{
	
	@Inject
	private MessageSource messageSource;
	
	@Inject
	private ContextService contextService;
	
	@Inject
	private AppConfig configBean;
	/**
	 * 管理登录
	 */
	@RequestMapping(value = "${adminPath}/login", method = RequestMethod.GET)
	public String login(HttpServletRequest request, HttpServletResponse response, Model model) {
		
		User user = contextService.getCurrentUser();
		// 如果已经登录，则跳转到管理首页
		if(user.getId() != null){
			return "redirect:" + Global.getAdminPath();
		}
		return "modules/sys/sysLogin";
	}

	/**
	 * 登录失败，真正登录的POST请求由Filter完成
	 */
	@RequestMapping(value = "${adminPath}/login", method = RequestMethod.POST)
	public String login(@RequestParam(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM) String username, HttpServletRequest request, HttpServletResponse response, Model model) {
		
		User user = contextService.getCurrentUser();
		// 如果已经登录，则跳转到管理首页
		if(user.getId() != null){
			return "redirect:"+Global.getAdminPath();
		}
		model.addAttribute(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM, username);
		model.addAttribute("isValidateCodeLogin", isValidateCodeLogin(username, true, false));
		return "modules/sys/sysLogin";
	}

	/**
	 * 登录成功，进入管理首页
	 */
	@RequiresUser
	@RequestMapping(value = "${adminPath}")
	public String index(HttpServletRequest request, HttpServletResponse response) {
		
		User user = contextService.getCurrentUser();
		// 未登录，则跳转到登录页
		if(user.getId() == null){
			return "redirect:"+Global.getAdminPath()+"/login";
		}
		// 登录成功后，验证码计算器清零
		isValidateCodeLogin(user.getLoginName(), false, true);
		// 登录成功后，获取上次登录的当前站点ID
		CacheManager.putCache("siteId", CookieUtils.getCookie(request, "siteId"));
		return "modules/sys/sysIndex";
	}
	
	/**
	 * 获取主题方案
	 */
	@RequestMapping(value = "/theme/{theme}")
	public String getThemeInCookie(@PathVariable String theme, HttpServletRequest request, HttpServletResponse response){
		if (StringUtils.isNotBlank(theme)){
			CookieUtils.setCookie(response, "theme", theme);
		}else{
			theme = CookieUtils.getCookie(request, "theme");
		}
		return "redirect:"+request.getParameter("url");
	}
	
	/**
	 * 是否是验证码登录
	 * @param useruame 用户名
	 * @param isFail 计数加1
	 * @param clean 计数清零
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static boolean isValidateCodeLogin(String useruame, boolean isFail, boolean clean){
		Map<String, Integer> loginFailMap = (Map<String, Integer>)CacheUtils.get("loginFailMap");
		if (loginFailMap==null){
			loginFailMap = Maps.newHashMap();
			CacheUtils.put("loginFailMap", loginFailMap);
		}
		Integer loginFailNum = loginFailMap.get(useruame);
		if (loginFailNum==null){
			loginFailNum = 0;
		}
		if (isFail){
			loginFailNum++;
			loginFailMap.put(useruame, loginFailNum);
		}
		if (clean){
			loginFailMap.remove(useruame);
		}
		return loginFailNum >= 3;
	}
	

	@SuppressWarnings("resource")
	@RequestMapping("${adminPath}/download")
	public String download(@RequestParam String filePath,HttpServletResponse response) {
		File file = new File(filePath);
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(filePath);
			response.reset();
			response.setContentType("application/octet-stream;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
			OutputStream outputStream = new BufferedOutputStream(
					response.getOutputStream());
			byte data[] = new byte[1024];
			while (inputStream.read(data, 0, 1024) >= 0) {
				outputStream.write(data);
			}
			outputStream.flush();
			outputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping(value = "${adminPath}/logout/{spath}")
	public String loginForm(@PathVariable String spath, HttpServletRequest request, ModelMap model) {

		if (StringUtils.isNotBlank(spath)){
			
			Locale currLocale = request.getLocale();
			// 表示正常退出
			if (StringUtils.equalsIgnoreCase(spath, "do")) {
				model.addAttribute("message", messageSource.getMessage("user.logout.success", null, currLocale));
			}
			//表示用户被管理员强制退出
			if (StringUtils.equalsIgnoreCase(spath, "forcelogout")) {
				model.addAttribute("error", messageSource.getMessage("user.forcelogout", null, currLocale));
			}
			//表示未知退出
			if (StringUtils.equalsIgnoreCase(spath, "unknown")) {
				model.addAttribute("error", messageSource.getMessage("user.unknown.error", null, currLocale));
			}
		}

        //登录失败了 提取错误消息
        Exception shiroLoginFailureEx =
                (Exception) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
        if (shiroLoginFailureEx != null) {
            model.addAttribute("error", shiroLoginFailureEx.getMessage());
        }
		
        //如果用户直接到登录页面 先退出一下
        //原因：isAccessAllowed实现是subject.isAuthenticated()---->即如果用户验证通过 就允许访问
        // 这样会导致登录一直死循环
        Subject subject = SecurityUtils.getSubject();
        if (subject != null && subject.isAuthenticated()) {
            subject.logout();
        }

        //如果同时存在错误消息 和 普通消息  只保留错误消息
        if (model.containsAttribute("error")) {
            model.remove("message");
        }
        
        String isCasEnabled = configBean.getProperty("osp.cas.enabled");
        if ( !StringUtils.equalsIgnoreCase("true", isCasEnabled) ) {
        	
        	return "${adminPath}/logout";
        }
        else{
        	String logoutUrl = configBean.getProperty("osp.cas.logoutUrl");
    		return "redirect:" + logoutUrl;
        }
        
	}
}
