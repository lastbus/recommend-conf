<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>字典管理</title>
	<meta name="decorator" content="default"/>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#value").focus();
			$("#inputForm").validate();
			var parentId=$('#parentId').val();//获得父字典Id
			if(parentId!=''&&parentId!=null){
				showDict(parentId);						
			}
		});
		
		
		function showDict(parentId){
			$.ajax({
				url : "${ctx}/sys/dict/showDict?parentId="+parentId,
				type : "POST",
				cache:false,
				async: false,
				dataType : "json",
				contentType : 'application/json;charset=UTF-8',
				success:function(msg){
					if(msg.status==200){
						var rt=msg.list;
						var result=[];
						for(var i=0;i<rt.length;i++){
							var rtstr='';
							if(rt[i]["id"]==msg.parentId){
								rtstr+='<input type="radio" checked="checked" value="'+rt[i]["id"]+'" name="parentBox"/>'+rt[i]["label"]+' &nbsp;';
							}else{
								rtstr+='<input type="radio" value="'+rt[i]["id"]+'" name="parentBox"/>'+rt[i]["label"]+' &nbsp;';
							}
							result.push(rtstr);
						}
						$('#parentDiv').html(result.join(''));
						$('#parentType').val(msg.type);
						$('#s2id_parentType span').html($('#parentType').find('option:selected').text());
					}
				},
				error:function(){
					top.$.jBox.alert("异常!");
				}
			});
		}
		
		
		function changeDict(id){//切换父字典表
			if(id!=''&&id!=null){
				$.ajax({
					url : "${ctx}/sys/dict/change?dictType="+id,
					type : "POST",
					cache:false,
					async: false,
					dataType : "json",
					contentType : 'application/json;charset=UTF-8',
					data :{
						dictType:id
					},
					success:function(msg){
						if(msg.status==200){
							var rt=msg.list;
							var result=[];
							for(var i=0;i<rt.length;i++){
								var rtstr='';
								rtstr+='<input type="radio" value="'+rt[i]["id"]+'_'+rt[i]["value"]+'" name="parentBox"/>'+rt[i]["label"]+' &nbsp;';
								result.push(rtstr);
							}
							$('#parentDiv').html(result.join(''));
						}
					},
					error:function(){
						top.$.jBox.alert("异常!");
					}
				});
			}else{
				$('#parentDiv').html('');
			}
		}
		
		function submitDict(){//提交submit
			var parentType=$('#parentType').val();
			if(parentType!=null&&parentType!=''){
				var count=0;
				$('input[name="parentBox"]').each(function(){
					if($(this).prop('checked')){
						count++;
					}
				});
				if(count<=0){
					top.$.jBox.alert("请选择一个父类!");
					return;
				}
				$('#parentId').val($('input[name="parentBox"]:checked').val().split('_')[0]);
				$('#parentCode').val($('input[name="parentBox"]:checked').val().split('_')[1]);
			}
			$('#inputForm').submit();
		}
		
		
	</script>
</head>
<body>
	<ul class="nav nav-tabs">
		<li><a href="${ctx}/sys/dict/">字典列表</a></li>
		<li class="active"><a href="${ctx}/sys/dict/form?id=${dict.id}">字典<shiro:hasPermission name="sys:dict:edit">${not empty dict.id?'修改':'添加'}</shiro:hasPermission><shiro:lacksPermission name="sys:dict:edit">查看</shiro:lacksPermission></a></li>
	</ul><br/>
	
	<form:form id="inputForm" modelAttribute="dict" action="${ctx}/sys/dict/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<tags:message content="${message}"/>
		
		<div class="control-group">
			<label class="control-label" for="value">键值:</label>
			<div class="controls">
				<form:input path="value" htmlEscape="false" maxlength="100" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="label">标签:</label>
			<div class="controls">
				<form:input path="label" htmlEscape="false" maxlength="50" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="type">类型:</label>
			<div class="controls">
				<form:input path="type" htmlEscape="false" maxlength="50" class="required abc"/> 
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="description">描述:</label>
			<div class="controls">
				<form:input path="description" htmlEscape="false" maxlength="50" class="required"/>
			</div>
		</div>
		<div class="control-group">
			<label class="control-label" for="sort">排序:</label>
			<div class="controls">
				<form:input path="sort" htmlEscape="false" maxlength="11" class="required digits"/>
			</div>
		</div>
		
		
		<!--选择字典表类型-->		
		<div class="control-group">
			<label class="control-label" for="parentType">父级类型:</label>
			<div class="controls">
				<select id="parentType" onchange="changeDict(this.value)">
					<option value="">请选择</option>
					<c:forEach items="${typeList}" var="parentType" >
						<option value="${parentType}">${parentType}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		
	   <!--通过字典表类型选择父字典ID-->
	   <div class="control-group">
			<div class="controls" id="parentDiv">
					<!--ajax获得字典单选框列表-->
			</div>
			<form:input path="parentId" id="parentId" type="hidden" />
			<form:input path="parentCode" id="parentCode" type="hidden" />
		</div>
		<div class="form-actions">
			<shiro:hasPermission name="sys:dict:edit">
				<input id="btnSubmit" class="btn btn-primary" type="button" value="保 存"  onclick="submitDict()" />&nbsp;
			</shiro:hasPermission>
			<input id="btnCancel" class="btn" type="button" value="返 回" onclick="history.go(-1)"/>
		</div>
	</form:form>
</body>
</html>