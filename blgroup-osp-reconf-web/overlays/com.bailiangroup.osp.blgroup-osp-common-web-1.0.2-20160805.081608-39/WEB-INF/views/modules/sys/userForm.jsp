<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html lang="en">
<head>
	<title>用户管理</title>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/custom/js/mark.js"></script>
	<script src="${ctxStatic}/custom/js/myCustomjs.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#loginName").focus();
			
			// 手机验证 
			jQuery.validator.addMethod("isMobile", function(value, element) { 
				var length = value.length; 
				var mobile = /^(((13[0-9]{1})|(15[0-9]{1}))+\d{8})$/; 
				return this.optional(element) || (length == 11 && mobile.test(value)); 
				}, "请正确填写您的手机号码"); 
			
			/* // 电话号码验证 
			jQuery.validator.addMethod("isPhone", function(value, element) { 
			var tel = /^\d{3,4}-?\d{7,9}$/; //电话号码格式010-12345678 
			return this.optional(element) || (tel.test(value)); 
			}, "请正确填写您的电话号码"); */ 
			
			$("#inputForm").validate({
				rules: {
					loginName: {remote: "${ctx}/sys/user/checkLoginName?oldLoginName=" + encodeURIComponent('${user.loginName}')},
					email:{ required:true,email:true}, 
					mobile:{required:true,isMobile:true} 
				},
				messages: {
					loginName: {remote: "用户登录名已存在"},
					confirmNewPassword: {equalTo: "输入与上面相同的密码"},
					email:{required: "请输入一个Email地址", email: "请输入一个有效的Email地址"}, 
					mobile:{required: "请输入您的手机号码",isMobile: "请输入一个有效的手机号码"} 
				}
			});
			
		});
		
		var storeMap = new Map();
		
		var roleMap = new Map();
		
		function submittion(){
			
			if($("#sela option:selected").length == 0){
				$("#channelTypes").val();
			}
			else{
				var channels = $("#sela").val();
				$("#channelTypes").val(channels);
			}
			
			if($("#selbiz option:selected").length == 0){
				$("#bizTypes").val();
			}
			else{
				var bizs = $("#selbiz").val();
				$("#bizTypes").val(bizs);
			}
			
			if(storeMap.size == 0){
				$("#storeIdList").val('');
			}else{
				$("#storeIdList").val('');
				storeMap.forEach(function (item, key, mapObj) {
				    $("#storeIdList").val($("#storeIdList").val()+","+item);
				});
			}
			
			if(roleMap.size == 0){
				$("#roleIdList").val('');
			}else{
				$("#roleIdList").val('');
				roleMap.forEach(function (item, key, mapObj) {
				    $("#roleIdList").val($("#roleIdList").val()+","+item);
				});
			}
			
			if($("#dkeyMsg").html()=='该序列号已经被使用'){
				location.hash='#keyMsg';
				return  false;
			}
			
			
			$("#inputForm").submit();
		}
		
		
		function mySelectStore()
		{
			var bizIds = $("#bizTypeId").val();
			var url='${ctx}';
			top.$.jBox.open("iframe:"+url+"/sys/user/selstore?bizIds="+bizIds,"选择门店",800,500, 
			{
				submit : function(v, h, f) 
				{
					var rgArray = h.find("iframe")[0].contentWindow.document.getElementsByName("radioGroup");
					for ( var i = 0; i < rgArray.length; i++) 
					{
						var flag = rgArray[i].checked;
						if (flag) {
							var rgv = rgArray[i].value;
							if(!storeMap.has(escape(rgv.split("#")[1]))){
							$("#storeTags").addTag(rgv.split("#")[1]);
							storeMap.set(escape(rgv.split("#")[1]),rgv.split("#")[0]);
						}
							}
						}
					},
					loaded : function(h) 
					{
						$(".jbox-content", top.document).css("overflow-y", "hidden");
					}
				});
		}
		
		function mySelectRole(){
			var url='${ctx}';
			top.$.jBox.open("iframe:"+url+"/sys/role/selectRole","选择角色",800,500, {
				submit : function(v, h, f) {
					var rgArray = h.find("iframe")[0].contentWindow.document.getElementsByName("radioGroup1");
					for ( var i = 0; i < rgArray.length; i++) {
						var flag = rgArray[i].checked;
						if (flag) {
							var rgv = rgArray[i].value;
							if(!roleMap.has(escape(rgv.split("#")[1]))){
							$("#roleTags").addTag(rgv.split("#")[1]);
							roleMap.set(escape(rgv.split("#")[1]),rgv.split("#")[0]);
							}
						}
					}
				},
				loaded : function(h) {
					$(".jbox-content", top.document).css("overflow-y", "hidden");
				}
			});
		}
		
		function onRemoveTag(tag) {
            storeMap.delete(escape(tag));
        }
		
		function onRemoveTag(tag) {
			roleMap.delete(escape(tag));
        }
		
	</script>
	<link rel="stylesheet" type="text/css" href="/blgroup-osp/static/multi-select/multiselectSrc/jquery.multiselect.css" />
	<!--压缩样式  -->
	 <link rel="stylesheet" type="text/css" href="/blgroup-osp/static/multi-select/assets/style.css" /> 
	
	<link rel="stylesheet" type="text/css" href="/blgroup-osp/static/multi-select/assets/prettify.css" />
	<link rel="stylesheet" type="text/css" href="/blgroup-osp/static/multi-select/jquery-ui.css" />
	<script type="text/javascript" src="/blgroup-osp/static/multi-select/jquery.js"></script> 
	<!--多选框样式  -->
	 <script type="text/javascript" src="/blgroup-osp/static/multi-select/ui/jquery.ui.widget.js"></script>
	<script type="text/javascript" src="/blgroup-osp/static/multi-select/ui/jquery.ui.core.js"></script>
	<script type="text/javascript" src="/blgroup-osp/static/multi-select/assets/prettify.js"></script> 
	<script type="text/javascript" src="/blgroup-osp/static/multi-select/multiselectSrc/jquery.multiselect.js"></script> 
	
	<script type='text/javascript' src='/blgroup-osp/static/taginput/jquery-ui.js'></script>
	<link rel="stylesheet" type="text/css" href="/blgroup-osp/static/taginput/jquery.tagsinput.css" />
	<script type="text/javascript" src="/blgroup-osp/static/taginput/jquery.tagsinput.js"></script>

	<script type="text/javascript">
	$(function(){
		  $("#sela").multiselect({
		        noneSelectedText: "--请选择--",
		        checkAllText: "全选",
		        uncheckAllText: '全不选',
		        selectedList:6,
		    });  
		
		    
		$('#storeTags').tagsInput({
            width:'auto',
            onRemoveTag:function(tag){
            	onRemoveTag(tag);
            },
            interactive:false
        });
		
		$('#roleTags').tagsInput({
            width:'auto',
            onRemoveTag:function(tag){
            	onRemoveTag(tag);
            },
            interactive:false
        });
		
		if('${user.dkeyNumber}'.length >0){
			if('${user.dkeyInitial}'.length>0){
				$("#dkeyInitialDiv").show();
			}
			$("#dkeyNumberDiv").show();
		}
	});
	
	
	//再调用插件初始化select对象
	</script>
	
	<script type="text/javascript">
		//校验注册类型下拉框
		function checkDkeyType(){
			$("#dkeyInitial").val("");$("#dkeyNumber").val("");
			$("#keyMsg").html("");$("#codeMsg").html("");
			var dkeyType = $("#dkeyType").val();
			if(dkeyType == 1){
				$("#btnSubmit").attr("disabled",true);
				$("#dkeyNumber").attr("readonly",true);
				$("#dkeyInitialDiv").show();
				$("#dkeyNumberDiv").show();
			}else if(dkeyType == 2){
				$("#keyMsg").html("必填")
				$("#btnSubmit").attr("disabled",true);
				$("#dkeyNumber").attr("readonly",false);
				$("#dkeyInitialDiv").hide();
				$("#dkeyNumberDiv").show();
			}else{
				$("#btnSubmit").attr("disabled",false);
				$("#dkeyInitialDiv").hide();
				$("#dkeyNumberDiv").hide();
			}
		}
	
		//校验初始码并初始化序列号
		function checkAndInitKeyNumber(){
			var dkeyInitial = $("#dkeyInitial").val();
			var reg = new RegExp("^[A-Z0-9]{10}$");
			var flag = reg.test(dkeyInitial);
			if(!flag){
				$("#codeMsg").html("<font color='red'>请输入10位初始码</font>");
				$("#dkeyNumber").val("");
				$("#btnSubmit").attr("disabled",true);
				return;
			}
			checkKeyCode(dkeyInitial);
		}
		
		//验证电子口初始码
		function checkKeyCode(dkeyInitial){
			var dkeyInitialValue="${user.dkeyInitial}";
			if(dkeyInitialValue!=dkeyInitial){
				var url = "${ctx}/sys/user/checkDKeyCode?dkeyInitial="+dkeyInitial;
		    	$.ajax({
					   type: "GET",
					   url: url,
					   data:{},
				       success:function(data){
				    	   if(data.result == 1){
				    		   $("#codeMsg").html("<font color='red'>该初始码已被使用</font>");
				    		   $("#dkeyNumber").val("");
				    		   $("#btnSubmit").attr("disabled",true);
				    	   }else{
				    		   $("#codeMsg").html("<font color='green'>校验通过</font>");
				    		   $("#btnSubmit").attr("disabled",false);
				    		   initKeyNumber(dkeyInitial);
				    	   }
					   },
					   error: function(){
						  top.$.jBox.tip('请重新输入!');
					   }
					});
			}
		}
		
		//初始化序列号
		function initKeyNumber(dkeyInitial){
			var url = "${ctx}/sys/user/initKeyNumber?dkeyInitial="+dkeyInitial;
	    	$.ajax({
				   type: "GET",
				   url: url,
				   data:{},
			       success:function(data){
			    	   var json = data.resultJson;
			    	   json = JSON.parse(json);
			    	   if(json.retCode == "3000000"){
			    		   var devicesn = json.devicesn;
				    	   var activecode = json.activecode;
				    	   $("#dkeyNumber").val(devicesn);
				    	   $("#dkeyActivation").val(devicesn+activecode);
			    	   }
				   },
				   error: function(){
					  top.$.jBox.tip('请重新输入!');
				   }
				});
		}
		
		
		//校验硬体注册
		function checkKeyNumber(){
			if($("#dkeyType").val()==1){
				return;
			}
			var dkeyNumber = $("#dkeyNumber").val();
			var reg = new RegExp("^[0-9]{12}$");
			var flag = reg.test(dkeyNumber);
			if(!flag){
				$("#keyMsg").html("<font color='red'>请输入12位序列号</font>");
				$("#btnSubmit").attr("disabled",true);
				return;
			}
			checkSameKeyNumber(dkeyNumber);
		}
		
		//验证电子口令序列号
		function checkSameKeyNumber(dkeyNumber){
			var dkeyNumberValue="${user.dkeyNumber}";
			if(dkeyNumber != dkeyNumberValue){
				var url = "${ctx}/sys/user/checkDKeyNumber?dkeyNumber="+dkeyNumber;
		    	$.ajax({
					   type: "GET",
					   url: url,
					   data:{},
				       success:function(data){
				    	   if(data.result == 1){
				    		   $("#keyMsg").html("<font color='red'>该序列号已被使用</font>");
				    		   $("#btnSubmit").attr("disabled",true);
				    	   }else{
				    		   $("#keyMsg").html("<font color='green'>校验通过</font>");
				    		   $("#btnSubmit").attr("disabled",false);
				    	   }
					   },
					   error: function(){
						  top.$.jBox.tip('请重新输入!');
					   }
					});
			}
		}

	
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/sys/user/">用户列表</a></li>
		<li class="active"><a href="${ctx}/sys/user/form?id=${user.id}">用户<shiro:hasPermission name="sys:user:edit">${not empty user.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="sys:user:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	<form:form id="inputForm" modelAttribute="user" action="${ctx}/sys/user/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<tags:message content="${message}"/>
		<div class="control-group">
			<label class="control-label" for="company">归属公司:</label>
			<input type="hidden" id="parentId">
			<div class="controls">
                <tags:treecompanycode id="company" name="company.code"  value="${user.company.code}" labelName="company.name" labelValue="${user.company.name}"
					title="公司" url="/sys/office/treeData?type=1" cssClass="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="office">归属部门:</label>
			<div class="controls">
                <tags:treeofficecode id="office" name="office.code" value="${user.office.code}" labelName="office.name" labelValue="${user.office.name}"
					title="部门" url="/sys/office/treeData?type=2" cssClass="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="oldLoginName">登录名:</label>
			<div class="controls">
				<input id="oldLoginName" name="oldLoginName" type="hidden" value="${user.loginName}">
				<form:input path="loginName" htmlEscape="false" maxlength="50" class="required userName"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="no">工号:</label>
			<div class="controls">
				<form:input path="no" htmlEscape="false" maxlength="50" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="name">姓名:</label>
			<div class="controls">
				<form:input path="name" htmlEscape="false" maxlength="50" class="required"/>
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="newPassword">密码:</label>
			<div class="controls">
				<input id="newPassword" name="newPassword" type="password" value="" maxlength="50" minlength="3" class="${empty user.id?'required':''}"/>
				<c:if test="${not empty user.id}"><span class="help-inline">若不修改密码，请留空。</span></c:if>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="confirmNewPassword">确认密码:</label>
			<div class="controls">
				<input id="confirmNewPassword" name="confirmNewPassword" type="password" value="" maxlength="50" minlength="3" equalTo="#newPassword"/>
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="dkeyType">注册类型：</label>
			<div class="controls">
				<form:select path="dkeyType" class="input-small required" onchange="checkDkeyType()">
					<form:options items="${fns:getDictList('user_osp_dkey_type')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
				</form:select>
				<span class="help-inline">此项可不选</span>
			</div>
		</div>
		
		<div id="dkeyInitialDiv" class="control-group" style="display:none;">
			<label class="control-label" for="dkeyInitial">令牌初始码:</label>
			<div class="controls">
				<form:input path="dkeyInitial" htmlEscape="false" maxlength="100"  onblur="checkAndInitKeyNumber()"/>
				<span id="codeMsg" class="help-inline"></span>
				<span class="help-inline"> 必填  此功能只对应APP令牌</span>
			</div>
		</div>
		
		<div id="dkeyNumberDiv" class="control-group" style="display:none;">
			<label class="control-label" for="dkeyNumber">令牌序列号:</label>
			<div class="controls">
				<form:input path="dkeyNumber" htmlEscape="false" maxlength="100"  onblur="checkKeyNumber()"/>
				<span id="keyMsg" class="help-inline"></span>
			</div>
		</div>
		<div class="control-group" style="display:none;">
			<label class="control-label" for="dkeyActivation">令牌激活码:</label>
			<div class="controls">
				<form:input path="dkeyActivation" htmlEscape="false" maxlength="100" />
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="email">邮箱:</label>
			<div class="controls">
				<form:input path="email" htmlEscape="false" maxlength="100" class="email"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="phone">电话:</label>
			<div class="controls">
				<form:input path="phone" htmlEscape="false" maxlength="100"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="mobile">手机:</label>
			<div class="controls">
				<form:input path="mobile" htmlEscape="false" maxlength="100"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="remarks">备注:</label>
			<div class="controls">
				<form:textarea path="remarks" htmlEscape="false" rows="3" maxlength="200" class="input-xlarge"/>
			</div>
		</div>
		<%-- <div class="control-group">
			<label class="control-label" for="channelType">所属渠道:</label>
			<div class="controls">
				<select id="sela" title="Basic example" class="input-xxlarge" name="sela" size="5" multiple="multiple">
					<c:forEach items="${fns:getDictList('sell_channel')}" var="inner">
					 	<c:set var="temp" value=",${inner.value},"></c:set>
								<c:choose>
									<c:when test="${fn:indexOf(chan,temp) ne -1}">
										<c:set var="select" value="selected"></c:set>
									</c:when>
									<c:otherwise>
										<c:set var="select" value=""></c:set>
									</c:otherwise>
								</c:choose>
								<option value='${inner.value}' ${select}>${inner.label}</option>
					</c:forEach>
				</select>
			</div>
		</div> --%>
		
		 <div class="control-group">
			<label class="control-label" for="channelType">所属渠道:</label>
			<div class="controls" >
				<select id="sela" title="Basic example" class="input-xxlarge" name="sela" size="5" multiple="multiple"  style="display:none">
					<c:forEach items="${fns:getDictList('sell_channel')}" var="inner">
					 		 <c:set var="temp" value=",${inner.value},"></c:set>
								<c:choose>
									<c:when test="${fn:indexOf(chan,temp) ne -1}">
										<c:set var="select" value="selected"></c:set>
									</c:when>
									<c:otherwise>
										<c:set var="select" value=""></c:set>
									</c:otherwise>
								</c:choose> 
								<option value='${inner.value}' ${select}>${inner.label}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		
		
		<div class="control-group">
			<label class="control-label" for="bizType">所属业态:</label>
			<div class="controls">
				<%-- <select id="selbiz" title="Basic example" class="input-xxlarge" name="selbiz" size="5" multiple="multiple">
					<c:forEach items="${fns:getDictList('sys_data_site_com_type')}" var="inner">
					 	<c:set var="temp" value=",${inner.value},"></c:set>
								<c:choose>
									<c:when test="${fn:indexOf(biztypes,temp) ne -1}">
										<c:set var="select" value="selected"></c:set>
									</c:when>
									<c:otherwise>
										<c:set var="select" value=""></c:set>
									</c:otherwise>
								</c:choose>
								<option value='${inner.value}' ${
}>${inner.label}</option>
					</c:forEach>
				</select> --%>
				<c:choose>
					<c:when test="${not empty user.id}">
						<tags:storeselect id="bizType" userTag="1" name="bizType" value="${user.bizType}" cssClass="input-xlarge"/>
					</c:when>
					<c:otherwise>
						<tags:storeselect id="bizType" userTag="1" name="bizType" cssClass="input-xlarge"/>
					</c:otherwise>
				</c:choose>
				
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="bizType">所属门店:</label>
			<div class="controls">
				<input type="button" value="选 择" class="btn" onclick="mySelectStore()">
				<input id='storeTags' type='text' class='tags'>
				<c:forEach items="${user.storeList}" var="store">
				<c:set var="storeName" value="${store.storeName}"/>
				<c:set var="storeId" value="${store.storeId}"/>
					<script>
					$(function(){
						$("#storeTags").addTag('${storeName}');
						storeMap.set(escape('${storeName}'),'${storeId}');
						
					});
					</script>
					
				</c:forEach>
				<form:hidden path="storeIdList"/>
			</div>
		</div>
		
		<!--div class="control-group">
			<label class="control-label" for="userType">用户类型:</label>
			<div class="controls">
				<form:select path="userType">
					<form:option value="" label="请选择"/>
					<form:options items="${fns:getDictList('sys_user_type')}" itemLabel="label" itemValue="value" htmlEscape="false"/>
				</form:select>
			</div>
		</div-->
		<div class="control-group">
			<label class="control-label" for="roleIdList">用户角色:</label>
			<div class="controls"> 
			<!-- old -->
			<%-- <table cellspacing="2" cellpadding="2">
			<c:forEach items="${allRoles}" var="r" varStatus="cn">
			<tr><td>
			<c:set var="temp" value=",${r.id},"></c:set>
				<c:choose>
					<c:when test="${fn:indexOf(roles,temp) ne -1}">
						<input id="${r.id}" checked class="required" type="checkbox" value="${r.id}" name="roleIdList">
						<span>${r.name}</span>
					</c:when>
					<c:otherwise>
						<input id="${r.id}" class="required" type="checkbox" value="${r.id}" name="roleIdList">
						<span>${r.name}</span>
					</c:otherwise>
				</c:choose>
				</td></tr>
			</c:forEach>
			</table> --%>
			<!-- 以下新增 -->
			<input type="button" value="选 择" class="btn" onclick="mySelectRole()">
				<input id='roleTags' type='text' class='tags'>
				<c:forEach items="${user.roleList}" var="role">
				<c:set var="roleName" value="${role.name}"/>
				<c:set var="roleId" value="${role.id}"/>
					<script>
					$(function(){
						$("#roleTags").addTag('${roleName}');
						roleMap.set(escape('${roleName}'),'${roleId}');
					});
					</script>
				</c:forEach>
				<form:hidden path="roleIdList"/>
			</div>
			<!--<form:checkboxes path="roleIdList" items="${allRoles}" itemLabel="name" itemValue="id" htmlEscape="false" class="required"/>-->
		</div>
		<c:if test="${not empty user.id}">
			<div class="control-group">
				<label class="control-label">创建时间:</label>
				<div class="controls">
					<label class="lbl"><fmt:formatDate value="${user.createDate}" type="both" dateStyle="full"/></label>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label">最后登陆:</label>
				<div class="controls">
					<label class="lbl">IP: ${user.loginIp}&nbsp;&nbsp;&nbsp;&nbsp;时间：<fmt:formatDate value="${user.loginDate}" type="both" dateStyle="full"/></label>
				</div>
			</div>
		</c:if>
		<div class="form-actions">
			<shiro:hasPermission name="sys:user:edit"><input id="btnSubmit" class="btn btn-primary" type="button" onclick="submittion()" value="保 存"/>&nbsp;</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
		
		<input type="hidden" id="channelTypes" name="channelTypes">
		<input type="hidden" id="bizTypes" name="bizTypes">
	</form:form>


</body>
</html>
