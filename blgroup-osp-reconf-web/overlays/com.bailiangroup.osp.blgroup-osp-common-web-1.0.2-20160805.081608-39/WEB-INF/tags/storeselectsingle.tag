<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<%@ attribute name="id" type="java.lang.String" required="true" description="编号"%>
<%@ attribute name="name" type="java.lang.String" required="false" description="输入框名称"%>
<%@ attribute name="value" type="java.lang.String" required="false" description="输入框值"%>
<%@ attribute name="cssClass" type="java.lang.String" required="false" description="css样式"%>
<%@ attribute name="storeValueId" type="java.lang.String" required="false" description="门店ID信息"%>
<%@ attribute name="storeNameId"  type="java.lang.String" required="false" description="门店名称信息"%>
<%@ attribute name="storeCodeId"  type="java.lang.String" required="false" description="门店编码信息"%>
<div class="input-append">
    <input id="${id}Id" name="${name}" class="${cssClass}" type="hidden" value="${value}"${disabled eq 'true' ? ' disabled=\'disabled\'' : ''}/>
	<input id="${id}Name" readonly="readonly" type="text" 
		value="${fns:getDictKeysByCodes('sys_data_site_com_type', value)}" 
		maxlength="50"${disabled eq "true"? " disabled=\"disabled\"":""}" class="${cssClass}"/>
	<a id="${id}Button" href="javascript:" class="btn${disabled eq 'true' ? ' disabled' : ''}">
		<i class="icon-search"></i></a>&nbsp;&nbsp;
</div>
<script type="text/javascript">
$("#${id}Button").click(function(){
	var typeStr = "sys_data_site_com_type";
	var url="iframe:${ctx}/sys/omni/selectStoreSingle?typeStr="+typeStr+"&ids="+$("#${id}Id").val();
	top.$.jBox.open(url, "选择业态", 300, 420, {
		buttons:{"确定":"ok","关闭":true}, submit:function(v, h, f){
			if (v=="ok"){
				var contents=h.find("iframe").contents();
				contents.find('#btnSubmit').click();
				$("#${id}Id").val(contents.find('#omniIds').val());
				$("#${id}Name").val(contents.find('#omniNames').val());
				if('${storeNameId}'!=''){
					$('#${storeNameId}').val('');
				}
				if('${storeValueId}'!=''){
					$('#${storeValueId}').val('');
				}
				if('${storeCodeId}'!=''){
					$('#${storeCodeId}').val('');
				}
				return true;
			}
		}, loaded:function(h){
			$(".jbox-content", top.document).css("overflow-y","hidden");
		}
	});
});

</script>
