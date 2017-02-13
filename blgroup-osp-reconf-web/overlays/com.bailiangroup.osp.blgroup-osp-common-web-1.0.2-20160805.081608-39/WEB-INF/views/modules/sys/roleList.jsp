]<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>角色管理</title>
	<meta name="decorator" content="default"/>
		<script type="text/javascript">
		$(document).ready(function() {
			// 表格排序
			tableSort({callBack : page});
			$("#btnDel").click(function(){
				top.$.jBox.confirm("确认要删除该角色吗吗？","系统提示",function(v,h,f){
					if(v == "ok"){
						$("#detailForm").attr("action","${ctx}/sys/role/deletesome").submit();
					}
				},{buttonsFocus:1});
				top.$('.jbox-body .jbox-icon').css('top','55px');
			});
			
		});
		
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").attr("action","${ctx}/sys/role/").submit();
	    	return false;
	    }
		
		function selectAll(){  
		    if ($("#SelectAll").attr("checked")) {  
		        $(":checkbox").attr("checked", true);  
		    } else {  
		        $(":checkbox").attr("checked", false);  
		    }  
		}  
		//子复选框的事件  
		function setSelectAll(){  
		    //当没有选中某个子复选框时，SelectAll取消选中  
		    if (!$("#subcheck").checked) {  
		        $("#SelectAll").attr("checked", false);  
		    }  
		    var chsub = $("input[type='checkbox'][id='subcheck']").length; //获取subcheck的个数  
		    var checkedsub = $("input[type='checkbox'][id='subcheck']:checked").length; //获取选中的subcheck的个数  
		    if (checkedsub == chsub) {  
		        $("#SelectAll").attr("checked", true);  
		    }  
		} 
	</script>
</head>
<body>
	<shiro:hasPermission name="sys:role:edit">
		<c:set var="roleEditAble" value="1"/>
	</shiro:hasPermission>
	<ul class="nav nav-tabs">
		<li class="active"><a href="${ctx}/sys/role/">角色列表</a></li>
		<c:if test="${ roleEditAble==1 }" ><li><a href="${ctx}/sys/role/form">角色添加</a></li></c:if>
	</ul>
		<form:form id="searchForm" modelAttribute="role" action="${ctx}/site/role/" method="post" class="breadcrumb form-search">
					<input type="hidden" name="operation" id="operation">
					<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
					<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
					<input id="orderBy" name="orderBy" type="hidden" value="${page.orderBy}"/>
		<div>
		<table cellspacing=5 cellpadding=5>
			<tr>
				<td align="right">
					<label>角色名称：</label>
				</td>
				<td>
					<form:input path="name" htmlEscape="false" maxlength="50" class="input-large"/>
				</td>
				<td align="right">
					<label>角色类型：</label>
				</td>
				<td>
					<form:select path="roleType"  class="input-small">
						<form:option value="" label=""/>
						<form:options items="${fns:getDictList('user_osp_role_type')}" itemValue="value" itemLabel="label" htmlEscape="false"/>
					</form:select>
				</td>
				<td align="right">
					<label>归属机构：</label>
				</td>
				<td>
					<form:select path="dataScope"  class="input-small">
						<form:option value="" label=""/>
						<form:options items="${fns:getDictList('sys_data_scope')}" itemValue="value" itemLabel="label" htmlEscape="false"/>
					</form:select>
				</td>
			<tr>
				<td colspan="6">
					&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" onclick="return page();"/>
					&nbsp;<input id="btnDel" class="btn btn-primary" type="button" value="删除"/>
				</td>
			</tr>
			</table>
		</div>
	</form:form>
	<tags:message content="${message}"/>
	<form:form id="detailForm" modelAttribute="role" action="${ctx}/sys/role/" method="post" class="breadcrumb form-search">
		<table id="contentTable" class="table table-striped table-bordered table-condensed">
			<thead><tr>
				<th><input type="checkbox" id="SelectAll"  value="全选" onclick="selectAll();"/></th>
				<th class="sort name">角色名称</th>
				<th class="sort roleType">角色类型</th>
				<th class="sort office.name">归属机构</th>
				<th class="sort dataScope">数据范围</th>	
				<c:if test="${ roleEditAble==1 }" ><th>操作</th></c:if>
			</tr></thead>
			<tbody>
			<c:forEach items="${page.list}" var="role">
				<tr>
					<td><input type="checkbox" id="subcheck" name="subcheck" value="${role.id}" onclick="setSelectAll();"/></td>
					<td><a href="form?id=${role.id}">${role.name}</a></td>
					<td>${fns:getDictLabel(role.roleType, 'user_osp_role_type', '无')}</td>
					<td>${role.office.name}</td>
					<td>${fns:getDictLabel(role.dataScope, 'sys_data_scope', '无')}</td>
					<c:if test="${ roleEditAble==1 }" >
					<td>
						<a href="${ctx}/sys/role/assign?id=${role.id}">分配</a>
						<a href="${ctx}/sys/role/form?id=${role.id}">修改</a>
						<a href="${ctx}/sys/role/delete?id=${role.id}" onclick="return confirmx('确认要删除该角色吗？', this.href)">删除</a>
					</td></c:if>
				</tr>
			</c:forEach>
			</tbody>
		</table>
	</form:form>
	<div class="pagination">${page}</div>
</body>
</html>