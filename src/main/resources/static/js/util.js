// ----- 이전 입력 자동 채우기 (localStorage, 최대 10개) -----
var STORAGE_KEY_SEARCH = "smartInventory.recentSearches";
var STORAGE_KEY_TITLES = "smartInventory.recentTitles";
var MAX_RECENT = 10;

function getRecentList(key) {
	try {
		var json = localStorage.getItem(key);
		return json ? JSON.parse(json) : [];
	} catch (e) { return []; }
}

function saveRecentList(key, value, max) {
	if (!value || (typeof value === 'string' && value.trim() === '')) return;
	var list = getRecentList(key);
	var val = typeof value === 'string' ? value.trim() : value;
	list = list.filter(function(item) { return item !== val; });
	list.unshift(val);
	list = list.slice(0, max || MAX_RECENT);
	try { localStorage.setItem(key, JSON.stringify(list)); } catch (e) {}
}

function saveRecentSearch(term) { saveRecentList(STORAGE_KEY_SEARCH, term); }
function saveRecentTitle(title) { saveRecentList(STORAGE_KEY_TITLES, title); }

function applyAutocomplete() {
	var searches = getRecentList(STORAGE_KEY_SEARCH);
	var titles = getRecentList(STORAGE_KEY_TITLES);

	var $searchContent = $("#searchContent");
	if ($searchContent.length) {
		var dlId = "datalist-search";
		var $dl = $("#" + dlId);
		if (!$dl.length) { $dl = $("<datalist id='" + dlId + "'></datalist>").appendTo("body"); }
		$dl.empty();
		searches.forEach(function(s) { $dl.append($("<option>").attr("value", s)); });
		$searchContent.attr("list", dlId);
	}

	var $dbTitle = $("#dbTitle");
	if ($dbTitle.length) {
		var dlId = "datalist-title";
		var $dl = $("#" + dlId);
		if (!$dl.length) { $dl = $("<datalist id='" + dlId + "'></datalist>").appendTo("body"); }
		$dl.empty();
		titles.forEach(function(t) { $dl.append($("<option>").attr("value", t)); });
		$dbTitle.attr("list", dlId);
	}
}

$(document).ready(function(){
		init();
		applyAutocomplete();
		 $('#preview').hide();
		 $('#previewVideo').hide();
	$('#findSearchMemoCount2').hide();

		 
		 
		   $(window).scroll(function() {
      if ($(this).scrollTop() > 100) {
        $('#scroll-to-top').fadeIn();
      } else {
        $('#scroll-to-top').fadeOut();
      }
    });

    
	})

	
	    // 이미지 삭제하는 기능. 
function imageDel(filename2){
	 var shouldDeleteImage = confirm("정말 삭제 할까요?  " + filename2 + "?");
	  if (shouldDeleteImage){
	var token = $("meta[name='_csrf']").attr("content");
            var header = $("meta[name='_csrf_header']").attr("content");
	$.ajax({
		type:"delete",
		beforeSend : function(xhr){
                    /* 데이터를 전송하기 전에 헤더에 csrf값을 설정 */
                    xhr.setRequestHeader(header, token);
                },
		url:"/images/deleteImage/"+filename2,
	})
	.done(function(resp){
		alert(filename2+"번 이미지 삭제 완료");
		location.href='/admin'
	})
	.fail(function(){
		alert("삭제 실패")
	})
	}
}
	
	//스크롤 버튼 부드럽게 동작하기. 
	 $('#scroll-to-top').click(function() {
      $('html, body').animate({scrollTop : 0},100);
      return false;
    });
    
    // 파일 선택시, 선택된 이미지 미리보기 , 로드이미지 함수 출력시 해당 아이디 보여줌.
    // 평소에는 숨김.
	    function loadImage() {
		
            var input = document.getElementById("image");
            /*console.log(input.files[0].name)*/
            var fileStr = input.files[0].name;
              var str2 = fileStr.substring(fileStr.lastIndexOf('.') + 1);
              
            if (input.files && input.files[0] &&str2 !='mp4' &&str2 !='mov' &&str2 !='avi' &&str2 !='wmv' &&str2 !='MOV') {
				$('#preview').show();
				$('#previewVideo').hide();
				$('#imgId4').hide();
                var reader = new FileReader();
                reader.onload = function(e) {
                    var img = document.getElementById("preview");
                    img.src = e.target.result;
                };
                reader.readAsDataURL(input.files[0]);
            } else {
				$('#previewVideo').show();
				$('#preview').hide();
				$('#imgId4').hide();
                var reader = new FileReader();
                reader.onload = function(e) {
                    var img = document.getElementById("previewVideo");
                    img.src = e.target.result;
                };
                reader.readAsDataURL(input.files[0]);
}
        }
	
	// 파일이미지 이름 모두 가져오기. 
	// 가져와서, innerHTML 으로 테이블 만들기. 
function FindAllFileName () {
            $.ajax({
                url: "/images/findFileNameAll",
                type: "GET",
                })
                .done (function(data) {
					var html = '<div class="row">';
					for (var i = 0; i < data.length; i++) {
						html += '<div class="col-md-2 margin"> ';
						html += '<div class="card"> ';
					  var filename = data[i];
					  var str = filename.substring(filename.lastIndexOf('.') + 1);
					  if(str=='mp4' || str=='mov' || str=='MOV' || str=='avi' || str=='wmv'){
						html += '<video controls src='+'/images/'+filename+'></video>';
					}
					else {
					  html += '<img src='+'/images/'+filename+'>';
					  }
					  html +=  '<div class="card-body">';
					  html +=  '<h4 class="card-title">'+filename+'</h4>';
					  html +=  '<p class="card-text">강의설명 준비중</p>';
					  html+="<a href=javascript:imageDel('"+filename+"')>삭제</a>";
					  html += '</div>';
					  html += '</div>';
					  html += '</div>';
}
               html += '</div>';
               $('#ImageTest').html(html);
                })
                };

		//이미지 하나 가져오기.
function FindOneFileName (imageName1) {
	$.ajax({
		url: "/images/"+imageName1,
		type: "GET",
	})
		.done (function(data) {
			var html = '<div class="row">';

				html += '<div class="col-md-2 margin"> ';
				html += '<div class="card"> ';
				console.log("data : " + data)
				var filename = data;
				var str = filename.substring(filename.lastIndexOf('.') + 1);
				if(str=='mp4' || str=='mov' || str=='MOV' || str=='avi' || str=='wmv'){
					html += '<video controls src='+'/images/'+filename+'></video>';
				}
				else {
					html += '<img src='+'/images/'+filename+'>';
				}
				html +=  '<div class="card-body">';
				html +=  '<h4 class="card-title">'+filename+'</h4>';
				html +=  '<p class="card-text">강의설명 준비중</p>';
				html+="<a href=javascript:imageDel('"+filename+"')>삭제</a>";
				html += '</div>';
				html += '</div>';
				html += '</div>';

			html += '</div>';
			$('#ImageOne').html(html);
		})
};
        
        
        // 메인 목록: 커서 기반 무한 스크롤 (10개씩)
var allLastId = null, allHasNext = true, allLoading = false, allTotalCount = 0;
var searchLastId = null, searchHasNext = true, searchLoading = false, searchTotalCount = 0, currentSearchData = null;
/** 현재 선택된 카테고리 ID (탭별 메모 필터) */
var currentCategoryId = null;

/** 유통기한 년월일만 표기 (yyyy-MM-dd) */
function formatExpiryDate(str) {
	if (!str) return "-";
	var s = String(str);
	return s.length >= 10 ? s.substring(0, 10) : s;
}

function buildMemoRow(val) {
	var img = val.imageFileName ? "<img src=/images/"+val.imageFileName+">" : "-";
	var expiry = formatExpiryDate(val.expiryDate);
	return "<tr><td>"+img+"</td><td>"+(val.title||"")+"</td><td>"+(val.message||"")+"</td><td>"+expiry+"</td><td>"+(val.dateField||"")+"</td>"+
		"<td><a href=javascript:dbUpdateFormMemo('"+val.id+"')>수정</a></td>"+
		"<td><a href=javascript:dbDel('"+val.id+"','"+(val.imageFileName||"")+"')>삭제</a></td></tr>";
}

function buildTableHeader() {
	return "<table class='table table-hover mt-3' border=1><thead><tr>"+
		"<th>사진</th><th>제목</th><th>메세지</th><th>유통기한</th><th>등록일</th><th>수정</th><th>삭제</th></tr></thead><tbody id='dbResultBody'></tbody></table>";
}

function loadCategoriesAndRenderTabs() {
	$.ajax({ type:"get", url:"/categories", dataType:"JSON" })
	.done(function(categories){
		var html = '<li class="nav-item"><a class="nav-link'+(currentCategoryId==null||currentCategoryId===''?' active':'')+'" href="#" data-category-id="">전체</a></li>';
		$.each(categories || [], function(_, c) {
			var active = (currentCategoryId === c.id) ? ' active' : '';
			html += '<li class="nav-item"><a class="nav-link'+active+'" href="#" data-category-id="'+(c.id||'')+'">'+ (c.name||'') +'</a></li>';
		});
		$("#categoryTabs").html(html);
	});
}

// 탭 클릭 시 해당 카테고리로 전환
$(document).on('click', '#categoryTabs a[data-category-id]', function(e){
	e.preventDefault();
	var id = $(this).attr('data-category-id') || null;
	currentCategoryId = (id === '') ? null : id;
	init();
});

// 탭 추가 모달 (Bootstrap 5)
$("#btnAddTab").click(function(){
	var modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('modalAddTab'));
	modal.show();
});
$("#btnConfirmAddTab").click(function(){
	var name = ($("#newTabName").val() || '').trim();
	if (!name) { alert('탭 이름을 입력하세요.'); return; }
	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$.ajax({ type:"post", url:"/categories", contentType:"application/json;charset=utf-8", data: JSON.stringify({ name: name, sortOrder: 999 }),
		beforeSend: function(xhr){ xhr.setRequestHeader(header, token); } })
	.done(function(){
		var modal = bootstrap.Modal.getInstance(document.getElementById('modalAddTab'));
		if (modal) modal.hide();
		$("#newTabName").val('');
		loadCategoriesAndRenderTabs();
	})
	.fail(function(){ alert('탭 추가 실패'); });
});

var init = function(){
	allLastId = null; allHasNext = true; allLoading = false; allTotalCount = 0;
	loadCategoriesAndRenderTabs();
	$("#dbResult").html(buildTableHeader());
	loadAllNext();
};

function loadAllNext() {
	if (allLoading || !allHasNext) return;
	allLoading = true;
	var url = "/findAllMemoPage?limit=10";
	if (currentCategoryId) url += "&categoryId=" + encodeURIComponent(currentCategoryId);
	if (allLastId) url += "&lastId=" + encodeURIComponent(allLastId);
	$.ajax({ type:"get", url:url, dataType:"JSON" })
	.done(function(resp){
		allLoading = false;
		var list = resp.list || [];
		allHasNext = resp.hasNext === true;
		var $body = $("#dbResultBody");
		$.each(list, function(_, val) {
			$body.append(buildMemoRow(val));
			allLastId = val.id;
		});
		allTotalCount += list.length;
		$("#findAllMemoCount").html(allTotalCount + (allHasNext ? "+" : ""));
	})
	.fail(function(){ allLoading = false; });
}

function loadSearchNext() {
	if (!currentSearchData || searchLoading || !searchHasNext) return;
	searchLoading = true;
	var url = "/searchDbPage?limit=10";
	if (searchLastId) url += "&lastId=" + encodeURIComponent(searchLastId);
	var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$.ajax({ type:"post", url:url, dataType:"JSON", contentType:"application/json;charset=utf-8",
		data: JSON.stringify(currentSearchData),
		beforeSend: function(xhr){ xhr.setRequestHeader(header, token); }
	})
	.done(function(resp){
		searchLoading = false;
		var list = resp.list || [];
		searchHasNext = resp.hasNext === true;
		var $target = $("#searchResult tbody");
		if ($target.length === 0) {
			var tbl = "<table class='table table-hover mt-3' border=1><thead><tr><th>사진</th><th>제목</th><th>메세지</th><th>유통기한</th><th>등록일</th><th>수정</th><th>삭제</th></tr></thead><tbody></tbody></table>";
			$("#searchResult").html(tbl);
			$target = $("#searchResult tbody");
		}
		$.each(list, function(_, val) {
			$target.append(buildMemoRow(val).replace("dbDel(","dbDel2("));
			searchLastId = val.id;
		});
		searchTotalCount += list.length;
		$("#findSearchMemoCount").html(searchTotalCount + (searchHasNext ? "+" : ""));
	})
	.fail(function(){ searchLoading = false; });
}

$(window).on("scroll", function() {
	if ($(document).height() - $(window).height() - $(window).scrollTop() < 300) {
		if (currentSearchData) loadSearchNext();
		else loadAllNext();
	}
});
	


function dbUpdateFormMemo(id){
	location.href='/updateFormMemo/'+id;
}


//메모 with 이미지
$("#uploadDBWithImageBtn").click(function(){
	$('#my-form').on('submit', function(e) {
		e.preventDefault();

		var token = $("meta[name='_csrf']").attr("content");
		var header = $("meta[name='_csrf_header']").attr("content");


		// var formData = new FormData(this);

		var formData = new FormData();

		var data={
			"title":$("#dbTitle").val(),
			"message":$("#dbMessage").val(),
			"expiryDate":$("#dbExpiryDate").val() || null,
			"categoryId": currentCategoryId || null
		}

            var input = document.getElementById("image");
            /*console.log(input.files[0].name)*/
            var file = input.files[0];
		
		formData.append('file',file);
		formData.append('key', new Blob([ JSON.stringify(data) ], {type : "application/json"}));

		$.ajax({
			url: '/insertMemoWithImage',
			type: 'POST',
			data: formData,

			processData: false,
			contentType: false,
			beforeSend : function(xhr){
				/* 데이터를 전송하기 전에 헤더에 csrf값을 설정 */
				xhr.setRequestHeader(header, token);
			},
			success  : function(result, status){
				var title = ($("#dbTitle").val() || "").trim();
				if (title) saveRecentTitle(title);
				alert('업로드 성공');
				location.href='/admin'
			},
			error : function(jqXHR, status, error){

				if(jqXHR.status == '401'){
					alert('로그인 후 이용해주세요');
					location.href='/members/login';
				} else{
					alert(jqXHR.responseText);
				}

			}
		});
	});
});




//검색 버튼 클릭 (커서 기반 무한 스크롤)
$("#dbSearchBtn").click(function(){
	var term = ($("#searchContent").val() || "").trim();
	if (term) { saveRecentSearch(term); applyAutocomplete(); }
	$('#findSearchMemoCount2').show();
	currentSearchData = {
		searchContent: term || $("#searchContent").val(),
		searchDB: $("#searchDB option:selected").val(),
		categoryId: currentCategoryId || undefined
	};
	searchLastId = null; searchHasNext = true; searchLoading = false; searchTotalCount = 0;
	$("#searchResult").html("<table class='table table-hover mt-3' border=1><thead><tr><th>사진</th><th>제목</th><th>메세지</th><th>유통기한</th><th>등록일</th><th>수정</th><th>삭제</th></tr></thead><tbody></tbody></table>");
	loadSearchNext();
});

// 메인 처럼 사용중. 
$("#listBtn").click(function(){
	location.href='/admin'
	});

var init2 = function(){
	location.href='/admin'
}
	
// 메모 수정창에서, 수정시 호출되는 함수.
$("#dbUpdateBtn2").click(function(){
	var shouldUpdate = confirm("정말 수정 할까요?");
	if (shouldUpdate) {
		$('#my-form').on('submit', function(e) {
			e.preventDefault();
		var token = $("meta[name='_csrf']").attr("content");
		var header = $("meta[name='_csrf_header']").attr("content");

		var formData = new FormData();

		var data = {
			"id": $("#dbId").val(),
			"title": $("#dbTitle").val(),
			"message": $("#dbMessage").val(),
			"expiryDate": $("#dbExpiryDate").val() || null
		}
				var input = document.getElementById("image");
				var file = input.files[0];
				formData.append('file',file);
				formData.append('key', new Blob([ JSON.stringify(data) ], {type : "application/json"}));

		$.ajax({
			type: "post",
			url: "/updateWithMemo",
			data: formData,
			processData: false,
			contentType: false,
			beforeSend: function (xhr) {
				/* 데이터를 전송하기 전에 헤더에 csrf값을 설정 */
				xhr.setRequestHeader(header, token);
			},


		})
			.done(function (resp) {
				alert('디비 수정 완료');
				location.href = '/admin'
			})
			.fail(function () {
				alert("디비 수정 실패")
			});
			}
		)};
});

	
//메모 게시글 하나 삭제 기능. 	
function dbDel(id,imageFileName){
	  var shouldDelete = confirm("정말 삭제 할까요?");
	  if (shouldDelete){
	      var token = $("meta[name='_csrf']").attr("content");
            var header = $("meta[name='_csrf_header']").attr("content");
	
	$.ajax({
		type:"delete",
		url:"/dbDelete/"+id+"/"+imageFileName,
		       beforeSend : function(xhr){
                    /* 데이터를 전송하기 전에 헤더에 csrf값을 설정 */
                    xhr.setRequestHeader(header, token);
                },
	})
	.done(function(resp){
		alert("글 삭제 완료");
		init();
	})
	.fail(function(){
		alert("삭제 실패")
	})
	}
	
}

function dbDel2(id,imageFileName){
	var shouldDelete = confirm("정말 삭제 할까요?");
	if (shouldDelete){
		var token = $("meta[name='_csrf']").attr("content");
		var header = $("meta[name='_csrf_header']").attr("content");

		$.ajax({
			type:"delete",
			url:"/dbDelete/"+id+"/"+imageFileName,
			beforeSend : function(xhr){
				/* 데이터를 전송하기 전에 헤더에 csrf값을 설정 */
				xhr.setRequestHeader(header, token);
			},
		})
			.done(function(resp){
				alert("글 삭제 완료");
				init2();
			})
			.fail(function(){
				alert("삭제 실패")
			})
	}

}

//빠른 검색 버튼 (반찬, 음료 등) - 커서 기반 무한 스크롤
$("#dbSearchBtn2").on('click','button', function(){
	var v = $(this).attr("value");
	if (!v) return;
	saveRecentSearch(v);
	applyAutocomplete();
	$('#findSearchMemoCount2').show();
	currentSearchData = { searchContent: v, searchDB: "title", categoryId: currentCategoryId || undefined };
	searchLastId = null; searchHasNext = true; searchLoading = false; searchTotalCount = 0;
	$("#searchResult").html("<table class='table table-hover mt-3' border=1><thead><tr><th>사진</th><th>제목</th><th>메세지</th><th>유통기한</th><th>등록일</th><th>수정</th><th>삭제</th></tr></thead><tbody></tbody></table>");
	loadSearchNext();
});


