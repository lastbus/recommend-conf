<%@ page language="java" errorPage="/error.jsp" pageEncoding="UTF-8" contentType="text/html;charset=utf-8" %>
<%@ page import="org.apache.shiro.web.filter.authc.FormAuthenticationFilter"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<!--[if IE 8]> <html lang="en" class="ie8"> <![endif]-->
<!--[if IE 9]> <html lang="en" class="ie9"> <![endif]-->

<!--[if !IE]><!-->
<html lang="zh-cn">
<!--<![endif]-->

<head>
	<meta charset="utf-8" />
	<title>${fns:getConfig('productName')} | 登录</title>
	<meta content="width=device-width, initial-scale=1.0" name="viewport" />
	<meta content="" name="description" />
	<meta content="" name="author" />
	<!-- BEGIN GLOBAL MANDATORY STYLES -->
	<link href="${ctxStatic}/media/css/bootstrap.min.css" rel="stylesheet" type="text/css"/>
	<%-- <link href="${ctxStatic}/media/css/bootstrap-responsive.min.css" rel="stylesheet" type="text/css"/> --%>
	<link href="${ctxStatic}/media/css/font-awesome.min.css" rel="stylesheet" type="text/css"/>
	<link href="${ctxStatic}/media/css/style.css" rel="stylesheet" type="text/css"/>
	<link href="${ctxStatic}/media/css/style-metro.css" rel="stylesheet" type="text/css"/>
	<link href="${ctxStatic}/media/css/style-responsive.css" rel="stylesheet" type="text/css"/>
	<link href="${ctxStatic}/media/css/default.css" rel="stylesheet" type="text/css" id="style_color"/>
	<link href="${ctxStatic}/media/css/uniform.default.css" rel="stylesheet" type="text/css"/>
	<!-- END GLOBAL MANDATORY STYLES -->
	<!-- BEGIN PAGE LEVEL STYLES -->
	<link href="${ctxStatic}/media/css/login.css" rel="stylesheet" type="text/css"/>
	<link href="${ctxStatic}/common/typica-login.css" rel="stylesheet" type="text/css"/>
	<!-- END PAGE LEVEL STYLES -->
	<link rel="shortcut icon" href="${ctxStatic}/media/image/favicon.ico" />
</head>
<!-- END HEAD -->

<!-- BEGIN BODY -->
<body class="login">

	<div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
	
      </div>
    </div>

	<!-- BEGIN LOGIN -->
	
	<div class="content">
        <!-- BEGIN LOGO -->
		  <div class="logo">
          	<a class="brand" href="${ctxStatic}"><img src="${ctxStatic}/images/logo.png" alt="OSP Admin" style="height:40px;"></a>
          </div>
        <!-- END LOGO -->
        <!--[if lte IE 6]><br/><div class='alert alert-block' style="text-align:left;padding-bottom:10px;"><a class="close" data-dismiss="alert">x</a><h4>温馨提示：</h4><p>你使用的浏览器版本过低。为了获得更好的浏览体验，我们强烈建议您 <a href="http://browsehappy.com" target="_blank">升级</a> 到最新版本的IE浏览器，或者使用较新版本的 Chrome、Firefox、Safari 等。</p></div><![endif]-->
		<%String error = (String) request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);%>
		<div id="messageBox" class="alert alert-error <%=error==null?"hide":""%>"><button data-dismiss="alert" class="close"></button>
			<label id="loginError" class="error">
				<%=error=="org.apache.shiro.authc.IncorrectCredentialsException"?"用户名或者密码错误":(String)request.getAttribute(com.bailiangroup.osp.common.security.FormAuthenticationFilter.DEFAULT_ERROR_CD_KEY_ATTRIBUTE_NAME)%></label>
		</div>
		<div><span id="codeMsg"></span></div>
		<!-- BEGIN LOGIN FORM -->
		<form id="loginForm" class="form-vertical login-form" action="${ctx}/login" method="post">
			<div style="height:40px;"><span style="color:#0ac;font-size:22px;">百联运营门户(OSP)</span></div>
			<div class="alert alert-error hide">
				<button class="close" data-dismiss="alert"></button>
				<span>Enter any username and password.</span>
			</div>
			<div class="control-group">
				<!--ie8, ie9 does not support html5 placeholder, so we just show field title for that-->
				<label class="control-label visible-ie8 visible-ie9">用户名</label>
				<div class="controls">
					<div class="input-icon left">
						<i class="icon-user"></i>
						<input class="m-wrap placeholder-no-fix" type="text" placeholder="用户名" name="username"/>
					</div>
				</div>
			</div>
			<div class="control-group">
				<label class="control-label visible-ie8 visible-ie9">密码</label>
				<div class="controls">
					<div class="input-icon left">
						<i class="icon-lock"></i>
						<input class="m-wrap placeholder-no-fix" type="password" placeholder="密码" name="password"/>
					</div>
				</div>
			</div>
			
			<div class="control-group">
				<div class="controls">
					<div class="input-icon left">
						<i class="icon-key"></i>
						<select id="dkeyType" name="dkeyType"  style="width:263px;" class="m-wrap placeholder-no-fix" onchange="showDKeyActionDiv()">
							<option value="0">--登录类型--</option>
							<option value="1">软件</option>
							<option value="2">硬件</option>
						</select>
					</div>
				</div>
			</div>
			
			<div class="control-group" style="display:none;" id="dKeyActionDiv">
				<label  class="control-label visible-ie8 visible-ie9">电子口令</label>
				<div class="controls">
					<div class="input-icon left">
						<i class="icon-magnet"></i>
						<input class="m-wrap placeholder-no-fix" type="text" placeholder="电子口令" name="dkeyAction" id="dkeyAction" onblur="check()">
					</div>
				</div>
			</div>
			<div class="control-group">
				<c:if test="${isValidateCodeLogin}">
					<div class="validateCode">
						<label for="validateCode">验证码：</label>
						<tags:validateCode name="validateCode" inputCssStyle="margin-bottom:0;"/>
					</div>
				</c:if>
			</div>
			<div class="form-actions">
				<label class="checkbox">
					<input type="checkbox" name="remember" value="1"/> 记住我
				</label>
				<button type="submit" id="btnSubmit" class="btn green pull-right">Login <i class="m-icon-swapright m-icon-white"></i></button>            
			</div>
		</form>
		<!-- END LOGIN FORM -->        
	</div>
	<!-- END LOGIN -->

	<!-- BEGIN COPYRIGHT -->

	<footer class="white navbar-fixed-bottom">
		Copyright &copy; 2015-${fns:getConfig('copyrightYear')} <a href="${pageContext.request.contextPath}${fns:getFrontPath()}">${fns:getConfig('productName')}</a> - Powered By <a href="https://www.bailiangroup/ops" target="_blank">BailianGroup</a> ${fns:getConfig('version')}
   	</footer>

	<!-- END COPYRIGHT -->

	<!-- BEGIN JAVASCRIPTS(Load javascripts at bottom, this will reduce page load time) -->
	<!-- BEGIN CORE PLUGINS -->
	<script src="${ctxStatic}/media/js/jquery-1.10.1.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/media/js/jquery-migrate-1.2.1.min.js" type="text/javascript"></script>
	<!-- IMPORTANT! Load jquery-ui-1.10.1.custom.min.js before bootstrap.min.js to fix bootstrap tooltip conflict with jquery ui tooltip -->
	<script src="${ctxStatic}/media/js/jquery-ui-1.10.1.custom.min.js" type="text/javascript"></script>      
	<script src="${ctxStatic}/media/js/bootstrap.min.js" type="text/javascript"></script>

	<!--[if lt IE 9]>
	<script src="${ctxStatic}/media/js/excanvas.min.js"></script>
	<script src="${ctxStatic}/media/js/respond.min.js"></script>  
	<![endif]-->   

	<script src="${ctxStatic}/media/js/jquery.slimscroll.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/media/js/jquery.blockui.min.js" type="text/javascript"></script>  
	<script src="${ctxStatic}/media/js/jquery.cookie.min.js" type="text/javascript"></script>
	<script src="${ctxStatic}/media/js/jquery.uniform.min.js" type="text/javascript" ></script>
	<!-- END CORE PLUGINS -->
	<!-- BEGIN PAGE LEVEL PLUGINS -->

	<script src="${ctxStatic}/media/js/jquery.validate.min.js" type="text/javascript"></script>
	<!-- END PAGE LEVEL PLUGINS -->
	<!-- BEGIN PAGE LEVEL SCRIPTS -->
	<script src="${ctxStatic}/media/js/app.js" type="text/javascript"></script>
	<script src="${ctxStatic}/common/backstretch.min.js"></script>	
	<!-- END PAGE LEVEL SCRIPTS --> 
	<script>

		 //校验电子口令是否为6位数
		function check(){
			var dkeyType = $("#dkeyType").val();
			var dkeyAction = $("#dkeyAction").val();
			if(dkeyType == 0){
				return;
			}
			var reg = new RegExp("^[0-9]{6,}$");
			var flag = reg.test(dkeyAction);
			if(!flag){
				$("#codeMsg").html("<font color='red'>请输入至少6位电子口令</font>");
				return;
			}
		} 
			
		function showDKeyActionDiv(){
			$("#codeMsg").html("");
			$("#dKeyAction").val("");
			var dkeyType = $("#dkeyType").val();
			if(dkeyType == 0){
				$("#dKeyActionDiv").hide();
			}else {
				$("#dKeyActionDiv").show();
			}
		}
		jQuery(document).ready(function() {     
		  App.init();
		  Login.init();
		  $.backstretch([
    		      "${ctxStatic}/images/bg1.jpg", 
    		      "${ctxStatic}/images/bg2.jpg",
    		      "${ctxStatic}/images/bg3.jpg"
    		  	], {duration: 10000, fade: 2000});
   			
    			$("#loginForm").validate({
   				rules: {
   					validateCode: {remote: "${pageContext.request.contextPath}/servlet/validateCodeServlet"},
   					dkeyAction:{ required:true, stringCheck:true}
   				},
   				messages: {
   					username: {required: "请填写用户名."},
   					password: {required: "请填写密码."},
   					dkeyAction: {required: "请填写电子口令",stringCheck:"请输入至少6位电子口令"},
   					validateCode: {remote: "验证码不正确.", required: "请填写验证码."}
   				},
   				errorLabelContainer: "#messageBox",
   				errorPlacement: function(error, element) {
   					error.appendTo($("#loginError").parent());
   				} 
   			});
		});
		
		//电子口令长度 
		jQuery.validator.addMethod("stringCheck", function(value, element) { 
			var length = value.length; 
			var reg = /^[0-9]{6,}$/; 
			return this.optional(element) || (6 <= length && reg.test(value)); 
		}, "请输入至少6位电子口令");
		
		// 如果在框架中，则跳转刷新上级页面
		if(self.frameElement && self.frameElement.tagName=="IFRAME"){
			parent.location.reload();
		}
	</script>
	<!-- END JAVASCRIPTS -->
</body>
<!-- END BODY -->

</html>