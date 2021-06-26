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
        var htmltext = fetchFromGDrive(user, "fromAccountDetails", 1);
        $('#fromAccountDetails').html(htmltext);
    });
    
    
    
});


function fetchFromGDrive(accountAddress, divid, root, mimeType){
		event.preventDefault();
	
		var prefix = "-";
		var counter = root+1;
		while(counter>0){
			prefix = prefix + "-";
			counter--;
		}
		//get root files
		if(root==1){
			
			makeAjaxCall(accountAddress, divid, root, prefix, "all");

			
		}else{
	
			makeAjaxCall(accountAddress, divid, root, prefix, "file");
			makeAjaxCall(accountAddress, divid, root, prefix, "folder");
		}
		
		
			
}

function makeAjaxCall(accountAddress, divid, root, prefix, mimeType){
	
	var cellHtml = "";
	var toAccountAddress = "interviewtest2062521@gmail.com";
	
	//get files first to display files first
    $.ajax({
        type: "POST",
        url: "/GDM/fetch?user="+accountAddress+"&objectid="+divid+"&root="+root+"&mimeType="+mimeType,
        async: false,//async because we want to display files first and then folders
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        statusCode: {
            204: function(responseObject, textStatus, jqXHR) {
            	cellHtml =  "<div>"+prefix+"No Files in this folder</div>";
        		$("#"+divid).html(cellHtml);    

            	
            }},
        success: function (result, status, xhr) {
        	console.log(result);
        	
        	$.each(result, function(propName,propVal) {  
        		
        		root++;
			    
			    if(propVal.type=='folder'){

					cellHtml = cellHtml + prefix+"<button class='btn-minimize' id='b-"+propName+"' onClick=\"toggle(this.id,'"+accountAddress+"','"+propName+"','"+root+"','folder')\";>"+propVal.name+"</button>&nbsp;<img src=\"images/change_owner.jpg\" alt=\"Change Owner\" onClick=\"changeOwner('"+accountAddress+"','"+ propName+"','"+ toAccountAddress+"')\"><div id='"+propName+"' class='widget-b-"+propName+"'></div>";
				}else{
					
					cellHtml = cellHtml + "<div id='"+propName+"'>"+prefix+propVal.name+"&nbsp;<img src=\"images/change_owner.jpg\" alt=\"Change Owner\" onClick=\"changeOwner('"+accountAddress+"','"+ propName+"','"+ toAccountAddress+"')\"></div>";

					
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

function toggle(id,accountAddress, divid, root, mimeType){

	event.preventDefault();
	//$("#"+id).toggleClass('btn-plus');
    //$(".widget-"+id).slideToggle();
    fetchFromGDrive(accountAddress, divid, root, mimeType);
    $("#"+id).attr("onclick","justToggle(this.id)");

}

function justToggle(id){
	event.preventDefault();
	$("#"+id).toggleClass('btn-plus');
    $(".widget-"+id).slideToggle();
}

function changeOwner(fromAccountAddress, divid, toAccountAddress){
	
	event.preventDefault();

	var cellHtml = "";
	
	//get files first to display files first
    $.ajax({
        type: "POST",
        url: "/GDM/changeOwner?fromUser="+fromAccountAddress+"&objectid="+divid+"&toUser="+toAccountAddress,
        async: false,//async because we want to display files first and then folders
        dataType: "json",
        contentType: "application/json; charset=utf-8",
        statusCode: {
            204: function(responseObject, textStatus, jqXHR) {
            	alert("Ownership changed successfully. Please refresh screen to reflect changes.");
            	
            },
        	503 : function(responseObject, textStatus, jqXHR) {
        	alert("Ownership change failed, sorry!");
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
  <input type="text" id="fromAccount" name="fromAccount" value="">
  <button id="fetchFromBtn">Fetch Gdrive Files</button>
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

