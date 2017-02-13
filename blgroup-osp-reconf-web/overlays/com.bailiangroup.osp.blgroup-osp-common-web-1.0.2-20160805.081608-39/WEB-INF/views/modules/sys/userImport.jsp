<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
	<title>导入数据</title>
	<meta name="decorator" content="default"/>
	<script src="${ctxStatic}/custom/js/mark.js"></script>
	<script src="${ctxStatic}/custom/js/myCustomjs.js"></script>
	
	<meta name="decorator" content="default"/>
	<link rel="stylesheet" type="text/css" href="/blgroup-osp/static/multi-select/multiselectSrc/jquery.multiselect.css" />
	<link rel="stylesheet" type="text/css" href="/blgroup-osp/static/multi-select/assets/style.css" />
	<link rel="stylesheet" type="text/css" href="/blgroup-osp/static/multi-select/assets/prettify.css" />
	<link rel="stylesheet" type="text/css" href="/blgroup-osp/static/multi-select/jquery-ui.css" />
	<script type="text/javascript" src="/blgroup-osp/static/multi-select/jquery.js"></script>
	
	<script type="text/javascript" src="/blgroup-osp/static/multi-select/ui/jquery.ui.core.js"></script>
	<script type="text/javascript" src="/blgroup-osp/static/multi-select/ui/jquery.ui.widget.js"></script>
	<script type="text/javascript" src="/blgroup-osp/static/multi-select/assets/prettify.js"></script>
	<script type="text/javascript" src="/blgroup-osp/static/multi-select/multiselectSrc/jquery.multiselect.js"></script>
	
	<script type='text/javascript' src='/blgroup-osp/static/taginput/jquery-ui.js'></script>
	<link rel="stylesheet" type="text/css" href="/blgroup-osp/static/taginput/jquery.tagsinput.css" />
	<script type="text/javascript" src="/blgroup-osp/static/taginput/jquery.tagsinput.js"></script>
	
	<script type="text/javascript">
	function submittion(){
		if($("#sela option:selected").length == 0){
			$("#channelTypes").val();
		}
		else{
			var channels = $("#sela").val();
			$("#channelTypes").val(channels);
		}
		
		if($("#selbiz option:selected").length == 0){
			$("#bizTypes").val();
		}
		else{
			var bizs = $("#selbiz").val();
			$("#bizTypes").val(bizs);
		}
		
		$("#importForm").submit();
	}
	
	$(function(){
	    $("select").multiselect({
	        noneSelectedText: "--请选择--",
	        checkAllText: "全选",
	        uncheckAllText: '全不选',
	        selectedList:6
	    });
	
		$('#storeTags').tagsInput({
            width:'auto',
            onRemoveTag:function(tag){
            	onRemoveTag(tag);
            },
            interactive:false
        });
		
	});
	
		//再调用插件初始化select对象
	</script>
</head>
<body>
	<div class="container-fluid">
		<div class="row-fluid">
			<div class="span12">
				<form:form id="importForm" modelAttribute="user" action="${ctx}/sys/user/import" method="post" enctype="multipart/form-data"
				style="padding-left:20px;text-align:center;" class="form-search" onsubmit="loading('正在导入，请稍等...');"><br/>
				<input id="uploadFile" name="file" type="file" style="width:330px"/><br/><br/>　
				<table>
						<tr>
							<td><label class="control-label">归属公司：</label></td>
							<td>
								<tags:treeselectcode id="company" name="company.code" value="${user.company.code}" labelName="company.name" labelValue="${user.company.name}"
								title="公司" url="/sys/office/treeData?type=1" cssClass="required"/>
							</td>
						</tr>
						<tr>
							<td><label class="control-label">归属部门：</label></td>
							<td>
								<tags:treeselectcode id="office" name="office.code" value="${user.office.code}" labelName="office.name" labelValue="${user.office.name}"
								title="部门" url="/sys/office/treeData?type=2" cssClass="required"/>
							</td>
						</tr>
						<tr>
							<td><label class="control-label">所属渠道：</label></td>
							<td>
								<select id="sela" title="Basic example" class="input-xlarge" name="sela" size="5" multiple="multiple">
									<c:forEach items="${fns:getDictList('sell_channel')}" var="inner">
									 	<c:set var="temp" value=",${inner.value},"></c:set>
											<c:choose>
												<c:when test="${fn:indexOf(chan,temp) ne -1}">
													<c:set var="select" value="selected"></c:set>
												</c:when>
												<c:otherwise>
													<c:set var="select" value=""></c:set>
												</c:otherwise>
											</c:choose>
											<option value='${inner.value}' ${select}>${inner.label}</option>
									</c:forEach>
								</select>
							</td>
						</tr>
						<tr>
							<td><label class="control-label">所属业态：</label></td>
							<td>
								<c:choose>
									<c:when test="${not empty user.id}">
										<tags:storeselect id="bizType" name="bizType" value="${user.bizType}" cssClass="input-xlarge"/>
									</c:when>
									<c:otherwise>
										<tags:storeselect id="bizType" name="bizType" cssClass="input-xlarge"/>
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
					</table> 
					<input id="btnImportSubmit" class="btn btn-primary" type="button" value="   导    入   " onclick="submittion()"/>
					<a href="${ctx}/sys/user/import/template">下载模板</a> 
					
					<input type="hidden" id="channelTypes" name="channelTypes">
					<input type="hidden" id="bizTypes" name="bizTypes">
				</form:form>
			</div>
		</div>
	</div>
</body>
</html>