<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
	<title>${fns:getConfig('productName')}</title>
	<%@include file="/WEB-INF/views/include/dialog.jsp" %>
	<meta name="decorator" content="default"/>
	<style type="text/css">
		#main {padding:0;margin:0;} #main .container-fluid{padding:0 7px 0 10px;}
		#header {margin:0 0 10px;position:static;} #header li {font-size:14px;_font-size:12px;}
		#header .brand {font-family:Helvetica, Georgia, Arial, sans-serif, 黑体;font-size:26px;padding-left:33px;}
		#footer {margin:8px 0 0 0;padding:3px 0 0 0;font-size:11px;text-align:center;border-top:2px solid #0663A2;}
		#footer, #footer a {color:#999;} 
	</style>
	<script type="text/javascript"> 
		
		$(document).ready(function() {
			/* console.log($.getUrlParam("app")); */
			$("#menu a.menu").bind("onclick", function(){ appswitch(appUrl, menuId); });
 			$("#menu a.menu").click(function(){
				$("#menu li.menu").removeClass("active");
				$(this).parent().addClass("active");
				if(!$("#openClose").hasClass("close")){
					$("#openClose").click();
				}
			}); 
		});
		
		function appswitch(appUrl, menuId){
			
			console.log("Appurl=" + appUrl + ", MenuId=" + menuId);
			if ((!IsNullOrEmpty(appUrl)) && (!IsNullOrEmpty(menuId))){
				var targetUrl = appUrl + "?app=" + menuId;
				console.log("targetUrl=" + targetUrl);
				window.location.href = appUrl + "?app=" + menuId;
			}
			$("#menu li.menu").removeClass("active");
			$(this).parent().addClass("active");
			if(!$("#openClose").hasClass("close")){
				$("#openClose").click();
			}
		}
		
		function IsNullOrEmpty(str){   
			
            if(typeof(str)=="undefined") return true;   
            if(str==null) return true;  
            if(str=="") return true;  
            if(str.replace(/(^s*)|(s*$)/g, "").length==0) return true;
            return false;  
        }
		
/* 		(function($){
			$.getUrlParam = function(name){
				var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
				var r = window.location.search.substr(1).match(reg);
				if (r!=null) return unescape(r[2]); return null;
			}
		})(jQuery); */
		
	</script>
</head>
<body>
	<spring:eval var="profile" expression="@environment.getProperty('spring.profiles.default')" />
	<div id="main">
		<div id="header" class="navbar navbar-fixed-top">
	      <div class="navbar-inner">
	      	 <div class="brand">${fns:getConfig('productName')}</div>
	         <div class="nav-collapse">
	           <ul id="menu" class="nav">
				 <c:set var="defaultSelectedMenu" value="true"/>
				 <c:if test="${not empty fns:getModuleMenuList()}">
					 <c:forEach items="${fns:getModuleMenuList()}" var="menu" varStatus="idxStatus">
					 	<!--/# 对NAV限制长度，8个以内正常显示-->
					 	<c:if test="${idxStatus.index < 6}">
						 	<c:if test="${profile != 'development'}" >
						 		<c:choose>
									<c:when test="${!empty param.app}">
										<c:set var="defaultSelectedMenu" value="false"/>
										<c:if test="${param.app==menu.id}">
											<c:set var="selectedMenuId" value="${menu.id}"/>
											<c:set var="defaultSelectedMenu" value="true"/>
										</c:if>
										<li class="menu ${defaultSelectedMenu ? ' active' : ''}"><a class="menu" href="#" onclick="appswitch('${menu.href}', '${menu.id}')" >${menu.name}</a></li>
									</c:when>
									<c:otherwise>
										<li class="menu ${defaultSelectedMenu ? ' active' : ''}"><a class="menu" href="#" onclick="appswitch('${menu.href}', '${menu.id}')" >${menu.name}</a></li>
										<c:if test="${defaultSelectedMenu}">
											<c:set var="selectedMenuId" value="${menu.id}"/>
										</c:if>
										<c:set var="defaultSelectedMenu" value="false"/>
									</c:otherwise>
								</c:choose>
						 	</c:if>
						 	<!-- Compatitable with Development Mode with None CAS env -->
						 	<c:if test="${profile == 'development'}" >
						 		<li class="menu ${defaultSelectedMenu ? ' active' : ''}"><a class="menu" href="${ctx}/sys/menu/tree?parentId=${menu.id}" target="menuFrame" >${menu.name}</a></li>
								<c:if test="${defaultSelectedMenu}">
									<c:set var="selectedMenuId" value="${menu.id}"/>
								</c:if>
								<c:set var="defaultSelectedMenu" value="false"/>
						 	</c:if>
					 	</c:if>
					 </c:forEach>
					 <!--/# 当顶部NAV导航数量过多(8个以上)时变为：下拉控件 -->
					 <c:if test="${fn:length(fns:getModuleMenuList()) > 6}">
					 	<ul class="nav">
			 				<li><a class="dropdown-toggle" data-toggle="dropdown" href="#"><i class="icon-th-list"></i></a>
					 			<ul class="dropdown-menu">
									<c:forEach items="${fns:getModuleMenuList()}" var="menu" varStatus="idxStatus" begin="6">
										<c:if test="${profile != 'development'}" >
									 		<c:choose>
												<c:when test="${!empty param.app}">
													<c:set var="defaultSelectedMenu" value="false"/>
													<c:if test="${param.app==menu.id}">
														<c:set var="selectedMenuId" value="${menu.id}"/>
														<c:set var="defaultSelectedMenu" value="true"/>
													</c:if>
													<li class="menu ${defaultSelectedMenu ? ' active' : ''}"><a class="menu" href="#" onclick="appswitch('${menu.href}', '${menu.id}')" >${menu.name}</a></li>
												</c:when>
												<c:otherwise>
													<li class="menu ${defaultSelectedMenu ? ' active' : ''}"><a class="menu" href="#" onclick="appswitch('${menu.href}', '${menu.id}')" >${menu.name}</a></li>
													<c:if test="${defaultSelectedMenu}">
														<c:set var="selectedMenuId" value="${menu.id}"/>
													</c:if>
													<c:set var="defaultSelectedMenu" value="false"/>
												</c:otherwise>
											</c:choose>
									 	</c:if>
									 	<!-- Compatitable with Development Mode with None CAS env -->
									 	<c:if test="${profile == 'development'}" >
									 		<li class="menu ${defaultSelectedMenu ? ' active' : ''}"><a class="menu" href="${ctx}/sys/menu/tree?parentId=${menu.id}" target="menuFrame" >${menu.name}</a></li>
											<c:if test="${defaultSelectedMenu}">
												<c:set var="selectedMenuId" value="${menu.id}"/>
											</c:if>
											<c:set var="defaultSelectedMenu" value="false"/>
									 	</c:if>
									</c:forEach>
								</ul>
				 			<li>
				 		</ul>
					 </c:if>
				 </c:if>
				 <%-- <shiro:hasPermission name="cms:site:select">
					<li class="dropdown">
						<a class="dropdown-toggle" data-toggle="dropdown" href="#">${fnc:getSite(fnc:getCurrentSiteId()).name}<b class="caret"></b></a>
						<ul class="dropdown-menu">
						<c:forEach items="${fnc:getSiteList()}" var="site"><li><a href="${ctx}/cms/site/select?id=${site.id}&flag=1">${site.name}</a></li></c:forEach>
						</ul>
					</li>
				 </shiro:hasPermission> --%>
	           </ul>
	           <ul class="nav pull-right">
				 <li><a href="${pageContext.request.contextPath}" target="_blank" title="访问网站主页"><i class="icon-home"></i></a></li>
			  	 <li id="themeSwitch" class="dropdown">
			       	<a class="dropdown-toggle" data-toggle="dropdown" href="#" title="主题切换"><i class="icon-th-large"></i></a>
				    <ul class="dropdown-menu">
				      <c:forEach items="${fns:getDictList('theme')}" var="dict"><li><a href="#" onclick="location='${pageContext.request.contextPath}/theme/${dict.value}?url='+location.href">${dict.label}</a></li></c:forEach>
				    </ul>
				    <!--[if lte IE 6]><script type="text/javascript">$('#themeSwitch').hide();</script><![endif]-->
			     </li>
			  	 <li class="dropdown">
				    <a class="dropdown-toggle" data-toggle="dropdown" href="#" title="个人信息">您好, <shiro:principal property="name"/></a>
				    <ul class="dropdown-menu">
				      <li><a href="${ctx}/sys/user/info" target="mainFrame"><i class="icon-user"></i>&nbsp; 个人信息</a></li>
				      <li><a href="${ctx}/sys/user/modifyPwd" target="mainFrame"><i class="icon-lock"></i>&nbsp;  修改密码</a></li>
				    </ul>
			  	 </li>
			  	 <li><a href="${ctx}/logout" title="退出登录">退出</a></li>
			  	 <li>&nbsp;</li>
	           </ul>
	         </div><!--/.nav-collapse -->
	      </div>
	    </div>
	    <div class="container-fluid">
			<div id="content" class="row-fluid">
				<div id="left">
					<iframe id="menuFrame" name="menuFrame" src="${ctx}/sys/menu/tree?parentId=${selectedMenuId}" style="overflow:visible;"
						scrolling="yes" frameborder="no" width="100%" height="650"></iframe>
				</div>
				<div id="openClose" class="close">&nbsp;</div>
				<div id="right">
					<iframe id="mainFrame" name="mainFrame" src="" style="overflow:visible;"
						scrolling="yes" frameborder="no" width="100%" height="650"></iframe>
				</div>
			</div>
		    <div id="footer" class="row-fluid">
	            Copyright &copy; 2012-${fns:getConfig('copyrightYear')} ${fns:getConfig('productName')} - Powered By <a href="https://www.bailiangroup.com/osp" target="_blank">Bailian Group</a> ${fns:getConfig('version')}
			</div>
		</div>
	</div>
	<script type="text/javascript"> 
		var leftWidth = "240"; // 左侧窗口大小
		function wSize(){
			var minHeight = 500, minWidth = 980;
			var strs=getWindowSize().toString().split(",");
			$("#menuFrame, #mainFrame, #openClose").height((strs[0]<minHeight?minHeight:strs[0])-$("#header").height()-$("#footer").height()-32);
			$("#openClose").height($("#openClose").height()-5);
			if(strs[1]<minWidth){
				$("#main").css("width",minWidth-10);
				$("html,body").css({"overflow":"auto","overflow-x":"auto","overflow-y":"auto"});
			}else{
				$("#main").css("width","auto");
				$("html,body").css({"overflow":"hidden","overflow-x":"hidden","overflow-y":"hidden"});
			}
			$("#right").width($("#content").width()-$("#left").width()-$("#openClose").width()-5);
		}
	</script>
	<script src="${ctxStatic}/common/wsize.min.js" type="text/javascript"></script>
</body>
</html>