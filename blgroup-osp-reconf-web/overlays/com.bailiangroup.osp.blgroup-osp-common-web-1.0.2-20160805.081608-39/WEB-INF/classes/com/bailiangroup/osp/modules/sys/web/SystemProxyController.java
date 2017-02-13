/**
 * 
 */
package com.bailiangroup.osp.modules.sys.web;

import java.util.List;

import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.bailiangroup.osp.common.annotation.ProxyService;
import com.bailiangroup.osp.common.annotation.ProxyService.Proxy;
import com.bailiangroup.osp.common.domain.entity.Menu;
import com.bailiangroup.osp.common.domain.entity.Role;
import com.bailiangroup.osp.common.domain.entity.User;
import com.bailiangroup.osp.common.service.SystemService;
import com.bailiangroup.osp.common.web.BaseController;

/**
 * @author Administrator
 *
 */
@Controller
@RequestMapping(value = "/api/sysproxy")
public class SystemProxyController extends BaseController {

	@Inject 
	private @ProxyService(proxy = Proxy.LOCAL)  SystemService systemService;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/user/{usrId}", method = {RequestMethod.GET,RequestMethod.POST})
	public ResponseEntity<?> getUser(@PathVariable String usrId){
		
		ResponseEntity<?> result = null;
		
		User selectUser = null;
		
		try {
			selectUser = this.systemService.getUser(usrId);
			
			if ( selectUser!= null) {
				selectUser.setId(usrId);
			}
			result = new ResponseEntity(selectUser, HttpStatus.OK);
			
		} catch (Exception e) {
			
			logger.error("invoke /api/sysproxy/usr/" + usrId, e);
			result = new ResponseEntity("", HttpStatus.BAD_REQUEST);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/user/lname/{loginName}", method = {RequestMethod.GET,RequestMethod.POST})
	public ResponseEntity<?> getUserByLoginName(@PathVariable String loginName){
		
		ResponseEntity<?> result = null;
		
		User selectUser = null;
		
		try {
			selectUser = this.systemService.getUserByLoginName(loginName);
			result = new ResponseEntity(selectUser, HttpStatus.OK);
			
		} catch (Exception e) {
			logger.error("invoke /user/lname" + loginName, e);
			result = new ResponseEntity("", HttpStatus.BAD_REQUEST);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/user/update", method = {RequestMethod.GET,RequestMethod.POST})
	public ResponseEntity<?> updateUserLoginInfo(@RequestParam(value = "loginId", required = true) String loginId){
		
		ResponseEntity<?> result = null;
		
		User selectUser = null;
		
		try {
			selectUser = this.systemService.updateUserLoginInfo(loginId);
			result = new ResponseEntity(selectUser, HttpStatus.OK);
			
		} catch (Exception e) {
			logger.error("invoke /user/update" + loginId, e);
			result = new ResponseEntity("", HttpStatus.BAD_REQUEST);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/menus/{usrId}", method = {RequestMethod.GET,RequestMethod.POST})
	public ResponseEntity<?> getAuthorizedMenusByUserId(@PathVariable String usrId){
		
		ResponseEntity<?> result = null;
		
		List<Menu> menus;
		try {
			menus = this.systemService.getMenuByUserId(usrId);
			result = new ResponseEntity(menus, HttpStatus.OK);
			
		} catch (Exception e) {
			
			logger.error("invoke /menus" + usrId, e);
			result = new ResponseEntity("", HttpStatus.BAD_REQUEST);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/menus/all", method = {RequestMethod.GET,RequestMethod.POST})
	public ResponseEntity<?> findAllMenu(){
		
		ResponseEntity<?> result = null;
		
		List<Menu> menus;
		try {
			menus = this.systemService.findAllMenu();
			result = new ResponseEntity(menus, HttpStatus.OK);
			
		} catch (Exception e) {
			
			logger.error("invoke /menus/all", e);
			result = new ResponseEntity("", HttpStatus.BAD_REQUEST);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/mmenus/all", method = {RequestMethod.GET,RequestMethod.POST})
	public ResponseEntity<?> getModuleMenu(){
		
		ResponseEntity<?> result = null;
		
		List<Menu> menus;
		try {
			menus = this.systemService.getModuleMenu();
			result = new ResponseEntity(menus, HttpStatus.OK);
			
		} catch (Exception e) {
			logger.error("invoke /mmenus/all", e);
			result = new ResponseEntity("", HttpStatus.BAD_REQUEST);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/mmenus/{usrId}", method = {RequestMethod.GET,RequestMethod.POST})
	public ResponseEntity<?> getModuleMenu(@PathVariable String usrId){
		
		ResponseEntity<?> result = null;
		
		List<Menu> menus;
		try {
			menus = this.systemService.getModuleMenu(usrId);
			result = new ResponseEntity(menus, HttpStatus.OK);
			
		} catch (Exception e) {
			logger.error("invoke //mmenus/" + usrId, e);
			result = new ResponseEntity("", HttpStatus.BAD_REQUEST);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/role/lname/{roleName}", method = {RequestMethod.GET,RequestMethod.POST})
	public ResponseEntity<?> getRoleByName(@PathVariable String roleName){
		
		ResponseEntity<?> result = null;
		
		Role role = null;
		try {
			role = this.systemService.findRoleByName(roleName);
			result = new ResponseEntity(role, HttpStatus.OK);
			
		} catch (Exception e) {
			logger.error("invoke /role/lname/" + roleName, e);
			result = new ResponseEntity("", HttpStatus.BAD_REQUEST);
		}
		
		return result;
	}
}
