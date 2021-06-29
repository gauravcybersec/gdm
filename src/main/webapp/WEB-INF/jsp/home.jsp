<!DOCTYPE html>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html lang="en"><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>GDM</title>
<meta name="viewport" content="width=device-width, initial-scale=1">

<head>
<style>
.widget-content {
    width: 500px;
    height: 500px;
    border: 1px solid #000;
}
.btn-minimize {
    background-color: #ADD8E6;
}
.btn-minimize.btn-plus {
    background-color: #0F0;
}
button {
    background-color: Transparent;
    background-repeat:no-repeat;
    cursor:pointer;
    overflow: hidden;
    outline:none;
}



</style>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"></script>
<script>

$(document).ready(function(){
    // Get value on button click and show alert
    $("#fetchFromBtn").click(function(event){
    	event.preventDefault();
        var user = $("#fromAccount").val();
        $('#fromAccountDetails').empty();
        var htmltext = fetchFromGDrive(user, "fromAccountDetails", 1, null, null);
        $('#fromAccountDetails').html(htmltext);
    });
    
    
    
});


function fetchFromGDrive(accountAddress, divid, root, mimeType, objName){
		event.preventDefault();
	
		var prefix = "-";
		var counter = root+1;
		while(counter>0){
			prefix = prefix + "-";
			counter--;
		}
		//get root files
		if(root==1){
			
			makeAjaxCall(accountAddress, divid, root, prefix, "all", objName);

			
		}else{
	
			makeAjaxCall(accountAddress, divid, root, prefix, "file", objName);
			makeAjaxCall(accountAddress, divid, root, prefix, "folder", objName);
		}
		
		
			
}

function makeAjaxCall(accountAddress, divid, root, prefix, mimeType, objName){
	
	var cellHtml = "";
	var toAccountAddress = "interviewtest2062521@gmail.com";
	
	var gdriveobject = {
			objectid: divid,
			objectname:objName,
			isroot:root,
			requestingowner:accountAddress,
			objmimetype:mimeType
        };
	
	//get files first to display files first
    $.ajax({
        type: "POST",
        url: "/GDM/fetch",
        async: false,//async because we want to display files first and then folders
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(gdriveobject),
        statusCode: {
            204: function(responseObject, textStatus, jqXHR) {
            	cellHtml =  "<div>"+prefix+"No Files in this folder</div>";
        		$("#"+divid).html(cellHtml);    

            	
            }},
        success: function (result, status, xhr) {
        	
        	$.each(result, function(propName,propVal) {  
        		
        		root++;
			    
			    if(propVal.type=='folder'){

					cellHtml = cellHtml + prefix+"<button class='btn-minimize' id='b-"+propName+"' onClick=\"toggle(this.id,'"+accountAddress+"','"+propName+"','"+root+"','folder','"+ propVal.name+"')\";>"+propVal.name+"</button>&nbsp;<img src=\"images/change_owner.jpg\" alt=\"Change Owner\" onClick=\"changeOwner('"+accountAddress+"','"+ propName+"','"+ toAccountAddress+"','"+ propVal.name+"','folder')\"><div id='"+propName+"' class='widget-b-"+propName+"'></div>";
				}else{
					
					cellHtml = cellHtml + "<div id='"+propName+"'>"+prefix+propVal.name+"&nbsp;<img src=\"images/change_owner.jpg\" alt=\"Change Owner\" onClick=\"changeOwner('"+accountAddress+"','"+ propName+"','"+ toAccountAddress+"','"+ propVal.name+"','file')\"></div>";

					
				}
			    
			    
			});
        	
        	if(divid==""){
        		return cellHtml;
        	}else{
        		var currenthtml = $("#"+divid).html();
        		$("#"+divid).html(currenthtml+cellHtml);    
        	}
        	
            
        },
        error: function (xhr, status, error) {
            alert("Result: " + status + " " + error + " " + xhr.status + " " + xhr.statusText);
        }
    });
}

function toggle(id,accountAddress, divid, root, mimeType, objName){

	event.preventDefault();
	//$("#"+id).toggleClass('btn-plus');
    //$(".widget-"+id).slideToggle();
    fetchFromGDrive(accountAddress, divid, root, mimeType, objName);
    $("#"+id).attr("onclick","justToggle(this.id)");

}

function justToggle(id){
	event.preventDefault();
	$("#"+id).toggleClass('btn-plus');
    $(".widget-"+id).slideToggle();
}

function changeOwner(fromAccountAddress, divid, toAccountAddress, objName, mimeType){
	
	event.preventDefault();

	var cellHtml = "";
	
	var gdriveobject = {
			objectid: divid,
			objectname:objName,
			newowner:toAccountAddress,
			requestingowner:fromAccountAddress,
			objmimetype:mimeType
        };
	
	//get files first to display files first
    $.ajax({
        type: "POST",
        url: "/GDM/changeOwner",
        async: false,//async because we want to display files first and then folders
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify(gdriveobject),
        statusCode: {
            204: function(responseObject, textStatus, jqXHR) {
            	alert("Ownership changed successfully!");
            	
            },
        	503 : function(responseObject, textStatus, jqXHR) {
        		alert("Ownership change failed with error :"+responseObject.responseText);
        	}
        },
        success: function (result, status, xhr) {
        	console.log(result);
        	
        },
        error: function (xhr, status, error) {
            //alert("Result: " + status + " " + error + " " + xhr.status + " " + xhr.statusText);
        }
    });
}



</script>

</head>
<header class="header header--large">
	<span><h2>Google Drive Manager Demo (GDM)</h2></a>
</header>

<body>

<h2>This tool provides the ability to Navigate into Google Drive folders or files for a given Google Account</h2>

<form>
  <table style="width:100%" border=1>
  <tr>
    <th><label for="fromAccount">Google Account:</label>
  <input type="text" id="fromAccount" name="fromAccount" value="interviewtest1062521@gmail.com" size="50">
  <button id="fetchFromBtn">Fetch Gdrive Files</button>
 <label for="fromAccount">Transfer Ownership Google Account:</label>
  <input type="text" id="toAccount" name="toAccount" value="interviewtest2062521@gmail.com" size="50">
	</th>
    
  </tr>
  <tr>
    <td><div id=fromAccountDetails></div></td>
  </tr>
  
</table>
</form>



<p>Note that the Google Account Owner will have to provide permissions to GDM application. The user will be presented with a screen to provide these permissions.</p>

</body>
</html>

