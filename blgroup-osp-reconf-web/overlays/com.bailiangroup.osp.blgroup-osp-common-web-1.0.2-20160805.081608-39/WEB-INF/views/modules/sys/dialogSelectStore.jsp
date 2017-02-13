<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>选择业态</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
	<script type="text/javascript">
		$(document).ready(function(){
			
			$("#inputForm").validate({
				submitHandler: function(form){
					
					var omniIds= [], omniNames =[], nodes = tree.getCheckedNodes(true);
					var storeIds = [], storeNames=[];
					
					for(var i=0; i<nodes.length; i++) {
						var halfCheck = nodes[i].getCheckStatus();
						if(nodes[i].isParent && !halfCheck.half){
							pushValue(omniIds, omniNames, storeIds, storeNames, nodes[i]);
						}else if(!nodes[i].isParent){
							if(nodes[i].parentTId == null){
								pushValue(omniIds, omniNames, storeIds, storeNames, nodes[i]);
							}else{
								var parentNode = nodes[i].getParentNode();
								if(parentNode.chkDisabled){
									pushValue(omniIds, omniNames, storeIds, storeNames, nodes[i]);
								}else{
									var parentHalfCheck = parentNode.getCheckStatus();
									if(parentHalfCheck.half){
										pushValue(omniIds, omniNames, storeIds, storeNames, nodes[i]);
									}
								}
							}
						}
					}
					$("#omniIds").val(omniIds);
					$("#omniNames").val(omniNames);
					
					$("#storeIds").val(storeIds);
					$("#storeNames").val(storeNames);
				}
			});
			
			function pushValue(omniIds, omniNames, storeIds, storeNames, treeNode) {
			
				//Store Nodes
				if ( treeNode.value.indexOf('omni_') == -1) {
					
					omniIds.push(treeNode.value);
					omniNames.push(treeNode.name);
				}
				else{
					storeIds.push(treeNode.value.substr(5));
					storeNames.push(treeNode.name);
				}
			}
			
			var setting = {
					check:{enable:true,nocheckInherit:true},
					view:{selectedMulti:false},
					data:{
						simpleData:{enable:true}
					},
					callback:{
						beforeClick:function(id, node){
							tree.checkNode(node, !node.checked, true, true);
							return false;
						},
						asyncSuccess: 	zTreeOnAsyncSuccess,	//异步加载成功的fun 
						asyncError: 	zTreeOnAsyncError, 		//加载错误的fun 
						// TODO: Version 2 Support , add by feelyn, 2015-11-17
						/* onMouseDown:	onMouseDown 			//捕获单击节点之前的事件回调函数  */
					}
				};
			
				function zTreeOnAsyncError(event, treeId, treeNode){ alert("异步加载失败!"); } 
				function zTreeOnAsyncSuccess(event, treeId, treeNode, msg){ } 
				
				function onMouseDown(event, treeId, treeNode) {
					
					if ( treeNode!=null && !treeNode.chkDisabled && !treeNode.isParent ) {
						
						console.log( "[ onMouseDown ] " + (treeNode?(treeNode.name + ', ' + treeNode.value) : "root") + " down" );
						
						$.ajax({
							url:"${ctx}/sys/omni/selOmniStore/"+ treeNode.value,
							type:"POST",
							async :true,
							cache:false,    
							dataType:'json', 
							success:function(data,status){
								
								var zTree = $.fn.zTree.getZTreeObj("storeTree");
								
								var array;
								if(data == null){
									array = new Array();
								}else{
									array = data.omnistore;
								}
								
								for(var i = 0;i<array.length;i++){
									zTree.addNodes(treeNode, {id:array[i].storeId, pId:treeNode.id, name:array[i].storeName, value:'omni_' + array[i].storeCode});
								}
								treeNode.Checked=false;
							},
							error : function(XMLrequest,status,thrown) {   
							 	alert(status);
							}    
						});
						return false;
					}
				}
				
				// 用户-菜单
				var zNodes=[
					<c:forEach items="${dictList}" var="dict">
						{id:'${dict.id}', pId:'${not empty dict.parentId?dict.parentId:-1}', name:"${dict.label}", value:"${dict.value}",
							<c:choose><c:when test="${fn:contains(bizType, dict.value)}">chkDisabled:false</c:when><c:otherwise>chkDisabled:true</c:otherwise></c:choose>,
							},
		            </c:forEach>];
				// 初始化树结构
				var tree = $.fn.zTree.init($("#storeTree"), setting, zNodes);
				// 默认选择节点
				/* tree.setting.check.chkboxType = { "Y" : "p", "N" : "p"}; */
				//var nodeX = tree.getNodeByParam("id","27");
				//tree.checkNode(nodeX, true, false);
				
				var nodes = tree.getNodes();
				if( typeof(nodes) != "undefined" )
				{ 
					var nodeTree = tree.transformToArray(nodes);
				    $.each(nodeTree,function(n,obj) {   
				    	
				    	var child=obj.children;
				    	
						if( !obj.chkDisabled ){
							if( typeof(child) != "undefined" ) {
								 for(var i=0;i<child.length;i++) {
									 child[i].chkDisabled = false;
									 tree.updateNode(child[i]);
								 }
							}
						} else {
							if( typeof(child) === "undefined" ){
								
								tree.hideNode(obj);
							}
						}
					}); 
				    tree.reAsyncChildNodes(nodes, "refresh");
				}
				
				var ids = "${ids}".split(",");
				for(var i=0; i<ids.length; i++) {
					$.each(zNodes,function(n,obj) {   
						if(ids[i] == obj.value){
							var node = tree.getNodeByParam("id", obj.id);
							try{
								tree.checkNode(node, true, true);
								/* var child=node.children;
								if(child!=undefined){ 
									for(var j=child.length-1;j>=0;j--){
										tree.checkNode(child[j], true, true);
									}
								} */
							}catch(e){
								
							}
						}
					});
				}
				
				// 默认展开全部节点
				tree.expandAll(true);
				
			});
	</script>
</head>
<body>

	<form id="inputForm" action="${ctx}/sys/omni/selectStore" method="post">
	
	    <div class="control-group">
			<label class="control-label"></label>
			<div class="controls">
				<div id="storeTree" class="ztree" style="margin-left:20px;"></div>
				<input type="hidden" name="storeIds"  id="storeIds" />
				<input type="hidden" name="storeNames"  id="storeNames" />
				<input type="hidden" name="omniIds"  id="omniIds" />
				<input type="hidden" name="omniNames"  id="omniNames" />
			</div>
		</div>
		
		<div class="form-actions" style="display:none">
			<input id="btnSubmit" class="btn btn-primary" type="submit" value=""/>&nbsp;
		</div>
	</form>
	
</body>
</html>