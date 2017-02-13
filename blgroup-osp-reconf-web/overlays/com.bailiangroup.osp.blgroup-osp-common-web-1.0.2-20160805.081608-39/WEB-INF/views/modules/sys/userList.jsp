<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>用户管理</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/dialog.jsp" %>
	<style type="text/css">.sort{color:#0663A2;cursor:pointer;}</style>
	<script type="text/javascript">
		$(document).ready(function() {
			// 表格排序
			tableSort({callBack : page});
			$("#btnExport").click(function(){
				top.$.jBox.confirm("确认要导出用户数据吗？","系统提示",function(v,h,f){
					if(v == "ok"){
						$("#searchForm").attr("action","${ctx}/sys/user/export").submit();
					}
				},{buttonsFocus:1});
				top.$('.jbox-body .jbox-icon').css('top','55px');
			});
			
			$("#btnImport").click(function(){
				$.jBox($("#importBox").html(), {title:"导入数据", buttons:{"关闭":true}, 
					bottomText:"导入文件不能超过5M，仅允许导入“xls”或“xlsx”格式文件！"});
			});
		});
		
		function shrink(str,maxLength) {
			if(str.length>maxLength) {
				var temp = str.substr(0,maxLength)+"...";
				var length = (str.length)*13;
				document.write(temp+"<div class='tips' style='width:"+length.toString()+"px;'>"+str+"</div>");
			}
			else {
				document.write(str);
			}				 
		}
		
		function page(n,s){
			$("#pageNo").val(n);
			$("#pageSize").val(s);
			$("#searchForm").attr("action","${ctx}/sys/user/").submit();
	    	return false;
	    }
		
		function  open_dialog(id){
			var locationUri='${ctx}/sys/user/dkeyInfo?id='+id;
			top.$.jBox.open('iframe:'+locationUri, "电子口令信息", 500, 300, {
			buttons:{"确定":"ok","关闭":true}, submit:function(v, h, f){
			if (v=="ok"){
				var contents=h.find("iframe").contents();
				$('#id').val(contents.find('#id').val());
				$('#dkeyType').val(contents.find('#dkeyType').val());
				$('#dkeyInitial').val(contents.find('#dkeyInitial').val());
				$('#dkeyNumber').val(contents.find('#dkeyNumber').val());
				$('#dkeyActivation').val(contents.find('#dkeyActivation').val());
				$('#actionPassword').val(contents.find('#actionPassword').val());
				$('#firstDyPassword').val(contents.find('#firstDyPassword').val());
				$('#secondDyPassword').val(contents.find('#secondDyPassword').val());
				$('#dkeyStatus').val(contents.find("[name='dkeyStatus']:checked").val());
				$('#dkeyForm').submit();
				return true;
			}
			}, loaded:function(h){
			$(".jbox-content", top.document).css("overflow-y","hidden");
			}
			});
		}
		
		//二维码扫描
		function open_qrcode(id,dkeyInitial){
			if(dkeyInitial == null || dkeyInitial.length == 0){
				top.$.jBox.tip("硬件注册无需扫码");
				return;
			}
			var locationUri='${ctx}/sys/user/qrcode?id='+id;
			top.$.jBox.open('iframe:'+locationUri, "电子口令激活码", 420, 420,{buttons:{"关闭":true}});
		}
		
	/* 	//查看状态
		function open_type(id){
			if(dkeyInitial == null || dkeyInitial.length == 0){
				top.$.jBox.tip("硬件注册无需扫码");
				return;
			}
			var locationUri='${ctx}/sys/user/query?id='+id;
			top.$.jBox.open('iframe:'+locationUri, "电子口令状态", 350, 350,{buttons:{"关闭":true}});
		} */
	</script>
	<style type="text/css">
		.fix{position:relative;}
		.fix .tips{z-index:999;height:20px;overflow:visible; position:absolute;top:20px;left:100px;display:none;border:1px solid #000}
		.fix:hover .tips{display:block;background-color:#fff}
	</style>
</head>
<body>
	<div id="importBox" class="hide">
		<form id="importForm" action="${ctx}/sys/user/import" method="post" enctype="multipart/form-data"
			style="padding-left:20px;text-align:center;" class="form-search" onsubmit="loading('正在导入，请稍等...');"><br/>
			<input id="uploadFile" name="file" type="file" style="width:330px"/><br/><br/>　　
			<input id="btnImportSubmit" class="btn btn-primary" type="submit" value="   导    入   "/>
			<a href="${ctx}/sys/user/import/template">下载模板</a>
		</form>
	</div>
	
	<shiro:hasPermission name="sys:user:edit">
		<c:set var="usrEditAble" value="1"/>
	</shiro:hasPermission>
	
	<ul class="nav nav-tabs">
		<li class="active"><a href="${ctx}/sys/user/">用户列表</a></li>
		<c:if test="${ usrEditAble==1 }" ><li><a href="${ctx}/sys/user/form">用户添加</a></li></c:if>
	</ul>
	
	<form:form id="searchForm" modelAttribute="user" action="${ctx}/sys/user/" method="post" class="breadcrumb form-search">
		<input id="pageNo" name="pageNo" type="hidden" value="${page.pageNo}"/>
		<input id="pageSize" name="pageSize" type="hidden" value="${page.pageSize}"/>
		<input id="orderBy" name="orderBy" type="hidden" value="${page.orderBy}"/>
		<div>
			<label>归属公司：</label><tags:treeselect id="company" name="company.id" value="${user.company.id}" labelName="company.name" labelValue="${user.company.name}" 
				title="公司" url="/sys/office/treeData?type=1" cssClass="input-small" allowClear="true"/>
			<label>登录名：</label><form:input path="loginName" htmlEscape="false" maxlength="50" class="input-small"/>
			
		</div><div style="margin-top:8px;">
			<label>归属部门：</label><tags:treeselect id="office" name="office.id" value="${user.office.id}" labelName="office.name" labelValue="${user.office.name}" 
				title="部门" url="/sys/office/treeData?type=2" cssClass="input-small" allowClear="true"/>
			<label>姓&nbsp;&nbsp;&nbsp;名：</label><form:input path="name" htmlEscape="false" maxlength="50" class="input-small"/>
			<label>口令序列号：</label><form:input path="dkeyNumber" htmlEscape="false" maxlength="50" class="input-small"/>
			&nbsp;<input id="btnSubmit" class="btn btn-primary" type="submit" value="查询" onclick="return page();"/>
			&nbsp;<input id="btnExport" class="btn btn-primary" type="button" value="导出"/>
			&nbsp;<input id="btnImport" class="btn btn-primary" type="button" value="导入"/>
		</div>
	</form:form>
	
	<tags:message content="${message}"/>
	<table id="contentTable" class="table table-striped table-bordered table-condensed">
		 <thead>
			<tr>
				<th>归属公司</th>
				<th>归属部门</th>
				<th class="sort loginName">登录名</th>
				<th class="sort name">姓名</th>
				<th>电话</th><th>手机</th>
				<th>角色</th>
				<th>口令序列号</th>
				<th>口令状态</th>
				<!-- <th>口令实时状态</th> -->
				<th>口令操作</th>
				<c:if test="${ usrEditAble==1 }" >
					<th>操作</th>
				</c:if>
			</tr>
		  </thead>
		<tbody>
		<c:forEach items="${page.list}" var="user">
			<tr>
				<td>${user.company.name}</td>
				<td>${user.office.name}</td>
				<td><a href="${ctx}/sys/user/form?id=${user.id}">${user.loginName}</a></td>
				<td>${user.name}</td>
				<td>${user.phone}</td>
				<td>${user.mobile}</td>
				<td class="fix"><script>shrink("${user.roleNames}", 32);</script></td>
				<td>${user.dkeyNumber}</td>
				<td>${fns:getDictLabel(user.dkeyStatus, 'sys_data_osp_dkey', '')}</td> 
				<%-- <td>${user.resType}</td> --%>
				<td>
					<c:if test="${not empty user.dkeyNumber}">
						<a href="#" onclick="open_dialog('${user.id}')">编辑</a>
						<%--<a href="${ctx}/sys/user/dkeyUnLock?id=${user.id}">解锁</a> --%>
						<a href="${ctx}/sys/user/dkeyEmpty?id=${user.id}">清除</a>
						<a href="#" id="open_qrcode" onclick="open_qrcode('${user.id}','${user.dkeyInitial}')">扫码</a>
						<%-- <a href="#" id="open_type" onclick="open_type('${user.id}')">状态</a> --%>
					</c:if>
				</td>
				<c:if test="${ usrEditAble==1 }" ><td>
    				<a href="${ctx}/sys/user/form?id=${user.id}">修改</a>
					<a href="${ctx}/sys/user/delete?id=${user.id}" onclick="return confirmx('确认要删除该用户吗？', this.href)">删除</a>
				</td></c:if>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	<div class="pagination">${page}</div>
	
<form:form id="dkeyForm" modelAttribute="user" action="${ctx}/sys/user/editDKey" method="post" class="breadcrumb form-search">	
	<input type="hidden" id="dkeyNumber" name="dkeyNumber"/>
	<input type="hidden" id="dkeyActivation" name="dkeyActivation"/>
	<input type="hidden" id="dkeyStatus" name="dkeyStatus"/>
	<input type="hidden" id="actionPassword" name="actionPassword"/>
	<input type="hidden" id="firstDyPassword" name="firstDyPassword"/>
	<input type="hidden" id="secondDyPassword" name="secondDyPassword"/>
	<input type="hidden" id="id" name="id"/>
	<input type="hidden" id="dkeyType" name="dkeyType"/>
	<input type="hidden" id="dkeyInitial" name="dkeyInitial"/>
</form:form>
	
</body>
</html>