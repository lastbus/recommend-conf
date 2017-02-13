<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
<title>角色管理</title>
<meta name="decorator" content="default" />
<%@include file="/WEB-INF/views/include/dialog.jsp"%>
<script src="${ctxStatic}/custom/js/mark.js"></script>
<script src="${ctxStatic}/custom/js/myCustomjs.js"></script>
<style type="text/css">
.sort {
	color: #0663A2;
	cursor: pointer;
}
</style>
<script type="text/javascript">
	$(document).ready(function() {
		tableSort({
			callBack : page
		}); // 表格排序
	});
	function page(n,s){
		$("#pageNo").val(n);
		$("#pageSize").val(s);
		$("#searchForm").attr("action","${ctx}/sys/role/selectRole").submit();
    	return false;
    }
</script>
</head>
<body>
	<form:form id="searchForm" modelAttribute="role" action="${ctx}/site/role/selectRole" method="post" class="breadcrumb form-search">
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

				<tags:message content="${message}" />
				<form>
					<table id="contentTable"
						class="table table-striped table-bordered table-condensed">
						<thead>
							<tr>
								<th>选择</th>
								<th class="sort name">角色名称</th>
								<th class="sort roleType">角色类型</th>
								<th class="sort office.name">归属机构</th>
								<th class="sort dataScope">数据范围</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${page.list}" var="role" varStatus="sta">
								<tr>
									<td><input type="checkbox" name="radioGroup1" value="${role.id}#${role.name}"></td>
									<td>${role.name}</td><!-- 角色名称 -->
									<td>${fns:getDictLabel(role.roleType, 'user_osp_role_type', '无')}</td><!-- 角色类型 -->
									<td>${role.office.name}</td><!-- 归属机构 -->
									<td>${fns:getDictLabel(role.dataScope, 'sys_data_scope', '无')}</td><!-- 数据范围 -->
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</form>
				<div class="pagination">${page}</div>
</body>
</html>