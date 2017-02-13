<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
<title>电子口令令牌</title>
<meta name="decorator" content="default" />
<%@include file="/WEB-INF/views/include/dialog.jsp"%>
<script src="${ctxStatic}/custom/js/mark.js"></script>
<script src="${ctxStatic}/custom/js/myCustomjs.js"></script>
<style type="text/css">
.sort {
	color: #0663A2;
	cursor: pointer;
}

div{
	margin-top:10px;
}
</style>
<script type="text/javascript">
	 $(document).ready(function() {
		tableSort({
			callBack : user
		}); // 表格排序
	});
	/*function page(n, s) {
		$("#pageNo").val(n);
		$("#pageSize").val(s);
		$("#searchForm").attr("action","${ctx}/sys/user/selstore?type=${type}").submit();
		return false;
	} */
	
	function submittion(){
		$("#inputForm").submit();
	}
	
	function change(){
		var dkeyStatus = $('input[name="dkeyStatus"]:checked').val();
		var dkeyType = '${user.dkeyType}';
		if(dkeyStatus == 6){
			if(dkeyType == 1){
				top.$.jBox.tip("手机令牌无需同步！");
				return;
			}
			$("#actionPasswordDiv").hide();
			$("#firstDyPasswordDiv").show();
			$("#secondDyPasswordDiv").show();
		}else{
			$("#actionPasswordDiv").show();
			$("#firstDyPasswordDiv").hide();
			$("#secondDyPasswordDiv").hide();
		}
			
		/* if(dkeyStatus == '6' && dkeyType == "1"){
			//当为手机令牌同步时 弹出手机令牌同步码和二维码页面
			function open_SysnCode(id,dkeyInitial){
				if(dkeyInitial == null || dkeyInitial.length == 0){
					return;
				}
				var locationUri='${ctx}/sys/user/qrcode?id='+id;
				top.$.jBox.open('iframe:'+locationUri, "电子口令序列号", 400, 400,{buttons:{"关闭":true}});
			}
		} */
	}
</script>
</head>
<body>
		<form:form id="inputForm" modelAttribute="user" action="${ctx}/sys/user/save" method="post" class="form-horizontal">
		<form:hidden path="id"/>
		<form:hidden path="dkeyType"/>
		<form:hidden path="dkeyActivation"/>
		<form:hidden path="dkeyInitial"/>
			<tags:message content="${message}" />
		<div class="form-group">
    		<label for="name">用户姓名:</label>
    		<input disabled="disabled"  value="${user.name}" type="text"  maxlength="50"/>
  		</div>
  		
  		<div class="form-group">
    		<label for="dkeyNumber">电子口令:</label>
    		<input disabled="disabled"  value="${user.dkeyNumber}" type="text" maxlength="50" />
  		</div>
  		
  		<div class="form-group">
    		<label for="dkeyStatus">口令状态:</label>
    		<form:radiobuttons  path="dkeyStatus" name="dkeyStatus" items="${fns:getDictList('sys_data_osp_dkey')}" onchange="change()" itemLabel="label" itemValue="value" htmlEscape="false" class="required" />
  		</div>
  		
  		<div class="form-group" id ="actionPasswordDiv" >
  		<div style="color:gray;font-size:12px">----口令禁用/挂失/解锁状态不需要输入动态口令-----</div>
    		<label for="actionPassword">动态口令:</label>
    		<input  id="actionPassword" name="actionPassword" type="text" maxlength="50" />
  		</div>
  		
  		<div class="form-group" id ="firstDyPasswordDiv" style="display:none;" >
  		<div style="color:gray;font-size:12px">----请输入两条相邻的动态口令-----</div>
    		<label for="firstDyPasswordDiv">第一个动态口令:</label>
    		<input  id="firstDyPassword" name="firstDyPassword" type="text" maxlength="50" />
  		</div>
  		
  		<div class="form-group" id ="secondDyPasswordDiv" style="display:none;" >
    		<label for="secondDyPassword">第二个动态口令:</label>
    		<input id="secondDyPassword" name="secondDyPassword" type="text" maxlength="50" />
  		</div>
		</form:form>	
</body>
</html>