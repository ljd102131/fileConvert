<%@ page language="java" import="java.util.*" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
	String ctx = request.getContextPath();
	request.setAttribute("ctx", ctx);
%>
<!DOCTYPE html>
<html>
<head lang="en">
    <meta charset="UTF-8">
    <title>XXX</title>
    <link rel="stylesheet" href="${ctx }/common/css/fileConvert.css"/>
</head>
<body>
	<div>
		<%-- <form id="formWord" style="display:none" action="${ctx}/fileUpload/pluploadFile" method="post" enctype="multipart/form-data">
		     <input id="files" type="file" name="file" accept=".doc,.docx">
		     <input type="submit" value="上传Word">
		</form>
		<form id="formPDF" style="display:none" action="${ctx}/fileUpload/pluploadFile" method="post" enctype="multipart/form-data">
		     <input id="files" type="file" name="file" accept=".pdf">
		     <input type="submit" value="上传PDF">
		</form> --%>
		<div id="buttonDiv">
			<button id="btnUploadFile" class="button">选择文件</button>
			<button id="" class="button" onclick="fnUploadFile2()">开始上传</button>
			
			<button id="wordTopdf" class="button"  onclick="wordTopdf()">word转pdf</button>
			<button id="pdfTopng" class="button"  onclick="fnpdfTopng()">pdf转png</button>
		</div>
		
	</div>
	<div>
		<ul id="showInfo">
			<li id='fileNameLi'> 
				<b>文件名称：</b> 
				<span id='fileName'></span> 
			</li>
			<li id='uploading'> 
				<b>上传进度：</b>
				<span id='spanValue' style='color:#00ff00;'>0</span>
				<label>%</label>
			</li>
		</ul>
		<b>转换文件：</b>
		<ul id='savefileui'>
			<li> 
				<a id="savefile">---未转换---</a>
			</li>
		</ul>
	</div>	
</body>
<%-- <script type="text/javascript" src="${ctx }/common/js/jquery.js"></script> --%>
<jsp:include page="../base/footer.jsp" />
<script type="text/javascript">
	var filePath = "";

	function wordTopdf() {
		/* document.getElementById("formPDF").style.display = "none";
		document.getElementById("formWord").style.display = "inline"; */
		$.ajax({
			url:"${ctx}/fileConvert/toPDF",
			data:{"filePath":filePath,"fileName":$("#fileName")[0].innerHTML},
			dataType:"json",
			type:"POST",
			success:function(data){
				if (data.code == 0) {
					var saveFileName = data.data.substring(data.data.lastIndexOf("/")+1);
					var str = "<li><a href='"+data.data+"'>"+saveFileName+"</a></li>";
					$("#savefileui").children("li").remove();
					$("#savefileui").append(str);

					alert("转换成功");
					/* document.getElementById("savefile").href = data.data;
					document.getElementById("savefile").innerHTML = saveFileName; */
				}else{
					alert("转换失败");
				}
			},
			error:function(data){
				alert("异常");
			}
		});
	}
	function fnpdfTopng() {
		/* document.getElementById("formWord").style.display = "none";
		document.getElementById("formPDF").style.display = "inline"; */
		$.ajax({
			url:"${ctx}/fileConvert/toPNG",
			data:{"filePath":filePath,"fileName":$("#fileName")[0].innerHTML},
			dataType:"JSON",
			type:"POST",
			success:function(data){
				if (data.code == 0) {
					var savePath = data.data["savePath"];
					var pngSize = data.data["pngSize"];
					if(pngSize >= 0){
						var str = "";
						for (var i = 0; i < pngSize; i++) {
							var a = i+1;
							str += "<li><a id='savefile_"+a+"' href='"+savePath+a+".png'>"+a+".png</a></li>";
						}
						$("#savefileui").children("li").remove();
						$("#savefileui").append(str);
						
						alert("转换成功");
					}
					/* var saveFileName = data.data.substring(data.data.lastIndexOf("\\")+1);
					document.getElementById("savefile").href = ;
					document.getElementById("savefile").innerHTML = saveFileName; */
				}else{
					alert("转换失败");
				}
			},
			error:function(data){
				alert("异常");
			}
		});
	}
	
	///开始上传。
	function fnUploadFile2() {
		var spanValue = document.getElementById("spanValue");
		uploader.start();
	}
	
	uploader = new plupload.Uploader({ //创建实例的构造方法 
	    runtimes: 'html5,flash,silverlight,html4', //上传插件初始化选用那种方式的优先级顺序 
	    browse_button: 'btnUploadFile', // 上传按钮 
	    url:'${ctx}/fileUpload/pluploadFile', //远程上传地址
	    flash_swf_url: 'plupload-1.5.7/Moxie.swf', //flash文件地址 
	    silverlight_xap_url: 'plupload-1.5.7/Moxie.xap', //silverlight文件地址 
	    max_retries: 3,   //允许重试次数
	    max_file_size: '2000mb', //最大上传文件大小（格式100b, 10kb, 10mb, 1gb） 
	    chunk_size: '10mb',//分块大小
	    filters: {
	        mime_types: [ //允许文件上传类型 
	            { title: "files", extensions: "doc,docx,pdf" }
	        ]
	    },
	    multipart:true,//为true时将以multipart/form-data的形式来上传文件，为false时则以二进制的格式来上传文件
	    multipart_params: {}, //文件上传附加参数 
	    file_data_name: "upimg", //文件上传的名称 
	    multi_selection: false, //true:ctrl多文件上传, false 单文件上传 
	    init: {
	        FilesAdded: function(up, files) { //文件上传前 
	        	//一个文件被添加了
	        	//uploader为当前实例对象，files数组，上传队列里的文件对象
	        	//alert("add");
	        	document.getElementById("fileName").textContent = files[0].name;
	        	spanValue.textContent = 0;
	        	$("#savefileui").children("li").remove();
	        	$("#savefileui").append("<li><a>---未转换---</a></li>");
	        },
	        UploadProgress: function(up, file) { //上传中，显示进度条 
	        	//从这里我们可以动态得到控制前端的进度条显示   
	            //file.percent为当前的进度
	            //file.loaded为已经上传的大小
	            //file.size为文件的总大小
	        	spanValue.textContent = file.percent;
	        },
	        FileUploaded: function(up, file, info) { //文件上传成功的时候触发        
	            alert("success");
	            filePath = decodeURIComponent(info.response.substring(1,info.response.length-1));
	            //filePath = info.response;
	        	var fileType = file.name.substring(file.name.lastIndexOf(".")+1);
	        	if (fileType == "doc" || fileType == "docx") {
	        		$("#pdfTopng").hide();
					$("#wordTopdf").show();
				}else if (fileType == "pdf") {
					$("#wordTopdf").hide();
					$("#pdfTopng").show();
				}
	        },
	        Error: function(up, err) { //上传出错的时候触发 
	            console.log(err.message);
	            alert("error");
	        }
	    }
	});

	uploader.init();//初始化plupload
</script>
</html>