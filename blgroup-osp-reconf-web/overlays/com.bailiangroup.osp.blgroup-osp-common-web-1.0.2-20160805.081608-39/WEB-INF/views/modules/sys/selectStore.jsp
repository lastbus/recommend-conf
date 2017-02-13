<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
<title>门户管理</title>
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
	function page(n, s) {
		$("#pageNo").val(n);
		$("#pageSize").val(s);
		$("#searchForm").attr("action","${ctx}/sys/user/selstore?type=${type}").submit();
		return false;
	}
</script>
</head>
<body>
	<form:form id="searchForm" modelAttribute="store"
		action="${ctx}/sys/user/selstore?type=${type}" method="post"
		class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}" />
		<input id="pageSize" name="pageSize" type="hidden"
			value="${page.pageSize}" />
		<input id="orderBy" name="orderBy" type="hidden"
			value="${page.orderBy}" />
		<div>
			<table cellspacing="3" cellpadding="3">
				<tr>
					<td><label>门店编号：</label></td>
					<td><form:input path="storeCode" htmlEscape="false"
							maxlength="50" class="input-small" /></td>
					<td><label>门店名称：</label></td>
					<td><form:input path="storeName" htmlEscape="false"
							maxlength="50" class="input-small" /> <input id="btnSubmit"
						class="btn btn-primary" type="submit" value="查询"
						onclick="return page();" /></td>
				</tr>
				<tr>
					<td colspan="8"></td>
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
								<th class="sort storeCode">门店编码</th>
								<th class="sort storeName">门店名称</th>
								<th class="sort state">门店状态</th>
								<th class="sort storeType">门店类型</th>
								<th class="sort openTime">开业时间</th>
							</tr>
						</thead>
						<tbody>
							<c:forEach items="${page.list}" var="store" varStatus="sta">
								<tr>
									<td><input type="checkbox" name="radioGroup"
										value="${store.storeId}#${store.storeName}"></td>
									<td>${store.storeCode}</td>
									<!-- 门店编码 -->
									<td>${store.storeName}</td>
									<!-- 门店名称 -->
									<td>${fns:getDictLabel(store.state,'sys_data_site_store_state',
										'')}</td>
									<!-- 门店状态 -->
									<td>${fns:getDictLabel(store.storeType,
										'sys_data_site_com_type', '')}</td>
									<!-- 门店类型 -->
									<td><fmt:formatDate value="${store.openTime}"
											pattern="yyyy-MM-dd" /></td>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</form>
				<div class="pagination">${page}</div>
</body>
</html>