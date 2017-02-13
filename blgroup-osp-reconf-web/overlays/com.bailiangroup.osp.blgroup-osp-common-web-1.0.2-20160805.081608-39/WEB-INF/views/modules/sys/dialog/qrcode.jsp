<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
<title>电子口令令牌</title>
<meta name="decorator" content="default" />
<script src="${ctxStatic}/blgroup/jquery.qrcode.min.js"></script>
</head>
<body>
<table id="contentTable" class="table table-striped table-bordered table-condensed">
	<tr>
		<td><font size="3"><b>登录名</b></font></td>
		<td>${user.loginName}</td>
	</tr>
	<tr>
		<td><font size="3"><b>初始码</b></font></td>
		<td>${user.dkeyInitial}</td>
	</tr>
	<tr>
		<td><font size="3"><b>序列号</b></font></td>
		<td>${user.dkeyNumber}</td>
	</tr>
	<tr>
		<td><font size="3"><b>激活码</b></font></td>
		<td>${user.dkeyActivation}</td>
	</tr>
</table>
<div id="code" align="center"></div> 
<script type="text/javascript">
	$("#code").qrcode({ 
	    render: "table", //table方式 
	    width: 200, //宽度 
	    height:200, //高度 
	    text: "${user.dkeyActivation}" //任意内容 
	}); 
</script>
</body>
</html>