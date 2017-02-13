<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>菜单管理</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<style type="text/css">.table td i{margin:0 2px;}</style>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#treeTable").treeTable({expandLevel : 1});
		});
    	function updateSort() {
			loading('正在提交，请稍等...');
	    	$("#listForm").attr("action", "${ctx}/sys/menu/updateSort");
	    	$("#listForm").submit();
    	}
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li class="active"><a href="${ctx}/sys/menu/">菜单列表</a></li>
		<shiro:hasPermission name="sys:menu:edit"><li><a href="${ctx}/sys/menu/form">菜单添加</a></li></shiro:hasPermission>
	</ul>
	<tags:message content="${message}"/>
	<form id="listForm" method="post">
	
		<shiro:hasPermission name="sys:menu:edit">
			<c:set var="menuEditAble" value="1"/>
		</shiro:hasPermission>
		<table id="treeTable" class="table table-striped table-bordered table-condensed">
			<tr>
				<th>名称</th><th>链接</th><th style="text-align:center;">排序</th><th>可见</th><th>权限标识</th>
				
				<c:if test="${ menuEditAble==1 }" >
					<th>操作</th>
				</c:if>
				<%-- <shiro:hasPermission name="sys:menu:edit"></shiro:hasPermission> --%>
			</tr>
			<c:forEach items="${list}" var="menu">
				<tr id="${menu.id}" pId="${menu.parent.id ne '1' ? menu.parent.id : '0'}">
					<td>
						<i class="icon-${not empty menu.icon?menu.icon:' hide'}"></i>
						<a href="${ctx}/sys/menu/form?id=${menu.id}">${menu.name}</a>
					</td>
					<td>${menu.href}</td>
					<%-- <shiro:hasPermission name="sys:menu:edit"> --%>
						<c:if test="${ menuEditAble==1 }" >
							<td style="text-align:center;">
								<input type="hidden" name="ids" value="${menu.id}"/>
								<input name="sorts" type="text" value="${menu.sort}" style="width:50px;margin:0;padding:0;text-align:center;">
							</td>
							<td>${menu.isShow eq '1'?'显示':'隐藏'}</td>
							<td>${menu.permission}</td>
							<td>
								<a href="${ctx}/sys/menu/form?id=${menu.id}">修改</a>
								<a href="${ctx}/sys/menu/delete?id=${menu.id}" onclick="return confirmx('要删除该菜单及所有子菜单项吗？', this.href)">删除</a>
								<a href="${ctx}/sys/menu/form?parent.id=${menu.id}">添加下级菜单</a> 
							</td>
						</c:if>
					<%-- </shiro:hasPermission> --%>
<%-- 					<shiro:lacksPermission name="sys:menu:edit">
						<td style="text-align:center;">${menu.sort}</td>
						<td>${menu.isShow eq '1'?'显示':'隐藏'}</td>
						<td>${menu.permission}</td>
					</shiro:lacksPermission> --%>
				</tr>
			</c:forEach>
		</table>
		<c:if test="${ menuEditAble==1 }" ><div class="form-actions pagination-left">
			<input id="btnSubmit" class="btn btn-primary" type="button" value="保存排序" onclick="updateSort();"/>
		</div></c:if>
	 </form>
</body>
</html>
