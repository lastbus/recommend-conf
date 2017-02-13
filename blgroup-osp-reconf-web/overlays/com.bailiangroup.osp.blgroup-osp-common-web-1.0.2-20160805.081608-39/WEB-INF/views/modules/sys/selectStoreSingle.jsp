<%@ page contentType="text/html;charset=UTF-8" %>
<%@ include file="/WEB-INF/views/include/taglib.jsp"%>
<html>
<head>
	<title>选择业态</title>
	<meta name="decorator" content="default"/>
	<%@include file="/WEB-INF/views/include/treeview.jsp" %>
	<script type="text/javascript">
		var zTree;  
	    var demoIframe;  
	  
	    var setting = {  
	        view : {  
	            dblClickExpand : false,  
	            showLine : true,  
	            selectedMulti : false  
	        },  
	        data : {  
	            simpleData : {  
	                enable : true,  
	                idKey : "id",  
	                pIdKey : "pId",  
	                rootPId : ""  
	            }  
	        },  
	        callback: {
	        	beforeClick: zTreeBeforeClick
	        }
	       /*  callback : {  
	            beforeClick : function(treeId, treeNode) {  
	                var zTree = $.fn.zTree.getZTreeObj("tree");  
	                if (treeNode.isParent) {  
	                    zTree.expandNode(treeNode);  
	                    return false;  
	                } else {  
	                    demoIframe.attr("src", treeNode.file);  
	                    return true;  
	                }  
	            }  
	        }   */
	    };  
	  
	    function zTreeBeforeClick(treeId, treeNode, clickFlag) {
	    	return !treeNode.isParent;//当是父节点 返回false 不让选取
	    }; 
	    	
	   
	    var zNodes=[
			<c:forEach items="${dictList}" var="dict">
				{id:'${dict.id}', pId:'${not empty dict.parentId?dict.parentId:-1}', name:"${dict.label}", value:"${dict.value}"},
		     </c:forEach>
		];
	    $(document).ready(function() {  
	    	
	    	
	    	$("#inputForm").validate({
				submitHandler: function(form){
					
					var omniIds= [], omniNames =[], nodes = zTree.getSelectedNodes();
					
					for(var i=0; i<nodes.length; i++) {
						omniIds.push(nodes[i].value);
						omniNames.push(nodes[i].name); 
					}
					$("#omniIds").val(omniIds);
					$("#omniNames").val(omniNames);
				}
			});
	    	
	        var t = $("#tree");  
	        t = $.fn.zTree.init(t, setting, zNodes);  
	        demoIframe = $("#testIframe");  
	        demoIframe.bind("load", loadReady);  
	        var zTree = $.fn.zTree.getZTreeObj("tree");  
	        /* zTree.selectNode(zTree.getNodeByParam("id", 101));   */
	        zTree.expandAll(true);
	  
	    });  
	  
	    function loadReady() {  
	        var bodyH = demoIframe.contents().find("body").get(0).scrollHeight,   
	        htmlH = demoIframe.contents().find("html").get(0).scrollHeight,  
	        maxH = Math.max(bodyH, htmlH),  
	        minH = Math.min(bodyH, htmlH),   
	        h = demoIframe.height() >= maxH ? minH : maxH;  
	        if (h < 530)  
	            h = 530;  
	        demoIframe.height(h);  
	    }  
	    
	    
	    
	</script>
</head>
<body>

	<form id="inputForm" action="${ctx}/sys/omni/selectStoreSingle" method="post">
	
	    <div class="control-group">
			<label class="control-label"></label>
			<div class="controls">
				<div id="tree" class="ztree" style="margin-left:20px;"></div>
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