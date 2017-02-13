<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>省市区管理</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#name").focus();
			$("#inputForm").validate();
		});
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/sys/areaInfo/">区域列表</a></li>
		<li class="active"><a href="form?id=${area.id}&parent.id=${area.parent.id}">区域<shiro:hasPermission name="sys:areaInfo:edit">${not empty area.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="sys:areaInfo:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	
	<form:form id="inputForm" modelAttribute="areaInfo" action="${ctx}/sys/areaInfo/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="countryId"/>
		<form:hidden path="parentIds"/>
		<tags:message content="${message}"/>
		<div class="control-group">
			<label class="control-label">上级区域:</label>
			<div class="controls">
				<tags:treeselect id="area" name="parent.id" value="${areaInfo.parent.id}" labelName="parent.name" labelValue="${areaInfo.parent.areaNameS}"
					title="区域" url="/sys/areaInfo/treeData" extId="${areaInfo.id}" cssClass="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="areaNameS">区域名称:</label>
			<div class="controls">
				<form:input path="areaNameS" htmlEscape="false" maxlength="50" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="areaCode">区域编码:</label>
			<div class="controls">
				<form:input path="areaCode" htmlEscape="false" maxlength="50" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for=levelId>区域类型:</label>
			<div class="controls">
				<form:select path="levelId">
					<form:options items="${fns:getDictList('sys_area_level')}" itemLabel="label" itemValue="value" htmlEscape="false" class="required"/>
				</form:select>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="pinyinSm">区域拼音:</label>
			<div class="controls">
				<form:input path="pinyinSm" htmlEscape="false" maxlength="200" class="required"/>
			</div>
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="sys:areaInfo:edit">
				<input id="btnSubmit" class="btn btn-primary" type="submit" value="保 存"/>&nbsp;
			</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
</body>
</html>