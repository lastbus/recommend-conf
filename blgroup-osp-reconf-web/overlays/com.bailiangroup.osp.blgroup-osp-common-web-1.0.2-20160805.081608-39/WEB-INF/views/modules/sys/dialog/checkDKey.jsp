<%@ page contentType="text/html;charset=UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<input id="accountresult" type="hidden" value='${result}'/> 
<c:choose>
	<c:when test="${result > 0 }">
		<label>&nbsp;<span style="color:red" id="dkeyMsg">该序列号已经被使用</span></label>
	</c:when>
	<c:otherwise>
		<label>&nbsp;<span style="color:green">该序列号可以使用</span></label>
	</c:otherwise>
</c:choose>