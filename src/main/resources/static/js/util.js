$(document).ready(function(){
		init()
	})
	
function dbUpdateForm(id){
	location.href='/updateForm/'+id;
	}

$("#dbSearchBtn").click(function(){
	
			var searchData = {
		"searchContent":$("#searchContent").val(),
		"searchDB":$("#searchDB option:selected").val()
	}
	console.log(searchData)
	$.ajax({
		type:"post",
		url:"/searchDb",
		contentType:"application/json;charset=utf-8",
		data:JSON.stringify(searchData)
	})
	.done(function(resp){
	 	 var str = "<table class='table table-hover mt-3' border=1>";
			$.each(resp,function(key,val){
				str += "<tr>"
				str += "<td>" + val.id + "</td>"
				str += "<td>" + val.title + "</td>"
				str += "<td>" + val.message + "</td>"
				str += "</tr>"
			})
			str += "</table>"
			$("#searchResult").html(str);
			})
	.fail(function(){
		alert("디비 검색 실패")
	});
	});

$("#listBtn").click(function(){
	location.href='/hello'
	});

$("#dbUpdateBtn").click(function(){

	var data={
			"id":$("#dbId").val(),
			"title":$("#dbTitle").val(),
			"message":$("#dbMessage").val()
	}
	
	$.ajax({
		type:"post",
		url:"/updateDb",
		contentType:"application/json;charset=utf-8",
		data:JSON.stringify(data)
	})
	.done(function(resp){
			location.href='/hello/'
			})
	.fail(function(){
		alert("디비 수정 실패")
	});
	});
	
	
function dbUpdate(id){
	$.ajax({
		type:"get",
		url:"/updateForm/"+id,
	})
	.done(function(resp){
		  location.href='/updateForm/id';
				var str = "<table class='table table-hover mt-3' border=1>";
			$.each(resp,function(key,val){
				str += "<tr>"
				str += "<td>" + val.id + "</td>"
				str += "<td>" + val.title + "</td>"
				str += "<td>" + val.message + "</td>"
				str+= "<td><a href='javascript:dbUpdateGo("+val.id+")'>수정</a></td>"
				
				str += "</tr>"
			})
			str += "</table>"
			$("#dbResult").html(str);
		
	})
	.fail(function(){
		
	})
}
	
function dbDel(id){
	$.ajax({
		type:"delete",
		url:"/dbDelete/"+id,
	})
	.done(function(resp){
		alert(id+"번 글 삭제 완료");
		init();
	})
	.fail(function(){
		alert("삭제 실패")
	})
}
	
var init = function(){
		$.ajax({
			type:"get",
			url:"/findAll",
			dataType:"JSON",
			contentType:"application/json;charset=utf-8",
		})
		.done(function(resp){
			//alert("resp"+resp)
			var str = "<table class='table table-hover mt-3' border=1>";
			$.each(resp,function(key,val){
				str += "<tr>"
				str += "<td>" + val.id + "</td>"
				str += "<td>" + val.title + "</td>"
				str += "<td>" + val.message + "</td>"
				str+= "<td><a href='javascript:dbUpdateForm("+val.id+")'>수정</a></td>"
				str+= "<td><a href='javascript:dbDel("+val.id+")'>삭제</a></td>" 
				
				
				str += "</tr>"
			})
			str += "</table>"
			$("#dbResult").html(str);
		})
	};

$("#dbInsertBtn").click(function(){
	

	var data={
			"id":$("#dbId").val(),
			"title":$("#dbTitle").val(),
			"message":$("#dbMessage").val()
			
	}
	
	$.ajax({
		type:"post",
		url:"/insertDb",
		contentType:"application/json;charset=utf-8",
		data:JSON.stringify(data)
	})
	.done(function(resp){
				init();
			})
	.fail(function(){
		alert("디비 추가 실패")
	});
});

$("#dbFindAllBtn").click(function(){

	$.ajax({
		type:"post",
		url:"/insertDb",
		contentType:"application/json;charset=utf-8",
		data:JSON.stringify(data)
	})
	.done(function(resp){
		alert("디비 추가 성공");
			})
	.fail(function(){
		alert("디비 추가 실패")
	});
});   