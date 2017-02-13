<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>区域管理</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#treeTable").treeTable({expandLevel : 1	});
		});
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").submit();
	    	return false;
	    }
	</script>
</head>
<body>

	<shiro:hasPermission name="sys:area:edit">
		<c:set var="areaEditAble" value="1"/>
	</shiro:hasPermission>
	<ul class="nav nav-tabs">
		<li class="active"><a href="${ctx}/sys/area/">区域列表</a></li>
		<c:if test="${ areaEditAble==1 }" ><li><a href="${ctx}/sys/area/form">区域添加</a></li></c:if>
	</ul>
	<tags:message content="${message}"/>
	<table id="treeTable" class="table table-striped table-bordered table-condensed">
		<tr>
			<th>区域名称</th><th>区域编码</th><th>区域类型</th><th>备注</th>
			<c:if test="${ areaEditAble==1 }" ><th>操作</th></c:if>
		</tr>
		<c:forEach items="${list}" var="area">
			<tr id="${area.id}" pId="${area.parent.id ne requestScope.area.id?area.parent.id:'0'}">
				<td><a href="${ctx}/sys/area/form?id=${area.id}">${area.name}</a></td>
				<td>${area.code}</td>
				<td>${fns:getDictLabel(area.type, 'sys_area_type', '无')}</td>
				<td>${area.remarks}</td>
				<c:if test="${ areaEditAble==1 }" >
				<td>
					<a href="${ctx}/sys/area/form?id=${area.id}">修改</a>
					<a href="${ctx}/sys/area/delete?id=${area.id}" onclick="return confirmx('要删除该区域及所有子区域项吗？', this.href)">删除</a>
					<a href="${ctx}/sys/area/form?parent.id=${area.id}">添加下级区域</a> 
				</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
</body>
</html>