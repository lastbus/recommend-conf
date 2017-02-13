<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>

	<title>省市区管理</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treetable.jsp" %>
	<script type="text/javascript">
		$(document).ready(function() {
			function createDOM(area, pid, plevel,gid) {
				var updateHref="${ctx}/sys/areaInfo/form?id="+area.id;
				var deleteHref="${ctx}/sys/areaInfo/delete?id="+area.id;
				var parentHref="${ctx}/sys/areaInfo/form?pid="+area.id;
				var span="<span class='default_node'>&#12288;&#12288;</span>";
				if(area.levelId!=3){
					span += "<span class='collpse"+plevel+"' style='cursor:pointer;'>＋</span>";
				} else if(area.levelId==3) {
					span+=span;
				}
				var level = "<input type='hidden' value='"+area.levelId+"' class='level'>";
				return "<tr gid='"+gid+"-"+area.id+"' id='"+area.id+"' pid='"+pid+"'><td>"+span+level+"<a href='"+updateHref+"'>"+area.name+"</td><td>"+area.code+"</td>"+
				"<td>"+area.level+"</td>"+"<td> "+area.pinyin+" </td>"+"<td><a href='"+updateHref+"'>修改 "
				+"</a><a href='"+deleteHref+"' onclick='return confirmx('要删除该区域及所有子区域项吗？', this.href)'>删除 "
				+"</a><a href='"+parentHref+"'>添加下级区域</a></td></tr>";
			} 
			var collapseCallback = function(){
				var level = $(this).siblings("input").val();
				var id = $(this).parent().parent().attr("id");
				var gid= $(this).parent().parent().attr("gid");
				if(gid==null) {
					gid=id;
				}
				
				if($(this).html()=="＋") {
					$(this).html("－");
					
					var thisHtml = $(this).parent().parent();
					$.ajax({
						url: "${ctx}/sys/areaInfo/children?id="+id,
						type:"get",
						dataType:'json',
						success: function(data) {
							if(data.data.length == 0){
						  		top.$.jBox.tip('该节点下没有子节点');
						  	}
							var html="";
							$.each(data.data, function(index, area) {
								html+=createDOM(area, id, level, gid);
							});
							thisHtml.after(html);
							$("tr").on("click", ".collpse"+level, collapseCallback);
						}
					});
					
				} else if($(this).html()=="－") {
					$(this).html("＋");
					var thisHtml = $(this).parent().parent();
					var nextNode = thisHtml.next();
					while(nextNode.attr("gid").indexOf(gid+"-")==0) {
						nextNode.unbind("click");
						nextNode.remove();
						nextNode = thisHtml.next();
					}
				}
			}
			$("table tr").on("click", ".collpse", collapseCallback);
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

	<shiro:hasPermission name="sys:areaInfo:edit">
		<c:set var="areaEditAble" value="1"/>
	</shiro:hasPermission>
	<ul class="nav nav-tabs">
		<li class="active"><a href="${ctx}/sys/areaInfo/">区域列表</a></li>
		<c:if test="${ areaEditAble==1 }" ><li><a href="${ctx}/sys/areaInfo/form">区域添加</a></li></c:if>
	</ul>
	<tags:message content="${message}"/>
	<table class="table table-striped table-bordered table-condensed">
		<tr>
			<th>区域名称</th><th>区域编码</th><th>区域类型</th><th>区域拼音</th>
			<c:if test="${ areaEditAble==1 }" ><th>操作</th></c:if>
		</tr>
		<c:forEach items="${list}" var="area">
			<tr id="${area.id}" pId="${area.parent.id ne requestScope.area.id?area.parent.id:'0'}">
				<td>
					<span class="collpse" style='cursor:pointer;'><c:choose><c:when test="${area.levelId ne 3}">＋</c:when><c:otherwise></c:otherwise></c:choose></span>
					<input type="hidden" value="${area.levelId}" class="level">
					<a href="${ctx}/sys/areaInfo/form?id=${area.id}">${area.areaNameS}</a>
				</td>
				<td>${area.areaCode}</td>
				<td>${fns:getDictLabel(area.levelId, 'sys_area_level', '无')}</td>
				<td>${area.pinyinSm}</td>
				<c:if test="${ areaEditAble==1 }" >
				<td>
					<a href="${ctx}/sys/areaInfo/form?id=${area.id}">修改</a>
					<a href="${ctx}/sys/areaInfo/delete?id=${area.id}" onclick="return confirmx('要删除该区域及所有子区域项吗？', this.href)">删除</a>
					<a href="${ctx}/sys/areaInfo/form?pid=${area.id}">添加下级区域</a> 
				</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
</body>
</html>