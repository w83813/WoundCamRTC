function getEditTime(){ //取得編輯時間
    var timeNow = new Date();
    var editYear = timeNow.getFullYear();
    var editMonth = (timeNow.getMonth()+1<10 ? '0' : '') + (timeNow.getMonth()+1);
    var editDate = (timeNow.getDate()<10 ? '0' : '') + timeNow.getDate();
    var editHour = (timeNow.getHours()<10 ? '0' : '') + timeNow.getHours();
    var editMin = (timeNow.getMinutes()<10 ? '0' : '') + timeNow.getMinutes();
    var editSec = (timeNow.getSeconds()<10 ? '0' : '') + timeNow.getSeconds(); 
    var editTime = editYear +'-'+ editMonth +'-'+ editDate +' '+ editHour +':'+ editMin +':'+ editSec;
    return editTime;
}

function formatDate(date) {
	var str = date.toLocaleDateString() + " " + date.getHours() + "時" + date.getMinutes() + "分" + date.getSeconds() + "秒";
	return str;
}

function cleanDataTables() {
    var tables = $.fn.dataTable.fnTables(true);
    $(tables).each(function() {
        $(this).dataTable().fnClearTable();
        $(this).dataTable().fnDestroy();
    });
}

function generatePager(divclass, page_num, roleId, ptNo){
	var limit = 3
	var searching = false;
	var lengthChange = false;
	var info = false;
	var remainingRetry = 5;
	
	if(divclass === ".patient-table"){  //table 名稱記得改
		limit = 10;
		//searching = true;
		lengthChange = false;
		info = false;
	}

	$(divclass).dataTable({
		ajax: {
		    //url: "http://192.168.1.157:8080/wgnursing/wound/tsgh/v3/api/qryPatientListByApp?roleId=" + roleId + "&ptNo=" + ptNo + "&location=TC",
		    url: "http://52.10.24.93:8080/woundcare/connect/v3/api/qryPatientListByApp?page_num="+page_num+"&limit="+limit+"&roleId=" + roleId + "&ptNo=" + ptNo + "&location=YT",
		    //url: "https://icare.itri.org.tw/woundcare/wound/tsgh/v3/api/qryPatientListByApp?roleId=" + roleId + "&ptNo=" + ptNo + "&location=TC",
		    //url: "http://192.168.50.12:8080/wgnursing/wound/tsgh/v2/api/qryPatientListByApp?roleId=" + roleId + "&ptNo=" + ptNo,
		    type: "POST",
		    error: function (jqXHR, textStatus, errorThrown) {
                if (remainingRetry > 0){
                     remainingRetry--;
                     console.log("錯誤:" + jqXHR);
                     console.log("錯誤:" +jqXHR.responseText);
                     console.log("錯誤:" +jqXHR.status);
                     console.log("錯誤:" +jqXHR.readyState);
                     console.log("錯誤:" +jqXHR.statusText);
                     setTimeout(function(){
                        generatePager(divclass, roleId, ptNo);
                     },500);
                }
            }
		},
        responsive: true,
        sort: false,
        paging: false,
        searching: searching,
        info: info,
        stateSave: true,
        lengthChange: lengthChange,
        pageLength: limit,
        autoWidth: false,
        destroy: true,
        columns: [
            {
                className:      'details-control',
                orderable:      false,
                data:           null,
                defaultContent: ''
            },
            { data: "charNo"},
            { data: "name" }
        ],
        initComplete: function( data, type, row, meta ){
        	$('.patient-table tbody').off('click').on('click', 'td.details-control', function () {
        	    var tr = $(this).closest('tr');
        	    var keyNo = tr.attr("keyNo");
        	    var table = $('.patient-table').DataTable();
        	    var row = table.row( tr );

        	    if ( row.child.isShown() ) {
        	        row.child.hide();
        	        tr.removeClass('shown');
        	    }
        	    else {
        	    	if ( table.row( '.shown' ).length ) {
        	    		$('.details-control', table.row( '.shown' ).node()).click();
        	    	}
        	        row.child( format(row.data()) ).show();
        	        tr.addClass('shown');
        	    }
        	});
			$('.pagination').html('');
			CreatePaging(gCurPage,data.json.num,data.json.data.length);
			console.log("num: "+data.json.num);
        },
        language: {
            search:         "搜尋:&nbsp;&nbsp;",
            info:           "第 _START_ 筆 - 第 _END_ 筆 總共 _TOTAL_ 筆",
            infoEmpty:      "總共 _TOTAL_ 筆",
            loadingRecords: "載入中...",
            emptyTable:     "無資料",
            zeroRecords: "無法找到相符條件的資料",
            infoFiltered: "(搜尋共 _MAX_ 筆資料)",
            paginate: {
                first:      "首頁",
                previous:   "上頁",
                next:       "下頁",
                last:       "末頁"
            },
            aria: {
                sortAscending:  ": 升冪",
                sortDescending: ": 降冪"
            }
        }
    });
	
	
}

function CreatePaging(gCurPage,total_record,length) {
	var total_page = 1 ;
	var display_count = 10;

	if ( display_count == "all" )
	  total_page = 2;
    else
	  total_page = Math.ceil(total_record / display_count) + 1 ;
  
    var li, a ;
	var current_page ;
	var pagination = document.getElementsByClassName("pagination")[0];
	
	if ( gCurPage >= total_page-1 )
	  gCurPage = total_page-1;
  
	li = document.createElement("li");                     // Pre
	li.setAttribute("class","paginate_button previous");
	li.setAttribute("aria-controls","DataTables_Table_0");
	li.setAttribute("id","DataTables_Table_0_previous");
	li.setAttribute("onclick","click_page("+(gCurPage-1)+")");
	a = document.createElement("a");
	a.setAttribute("href","#");
	a.innerHTML = "上頁";
	li.appendChild(a);
	pagination.appendChild(li);
	
	if ( gCurPage <= 1 ) {
	  $('#DataTables_Table_0_previous').addClass('disabled');
      li.removeAttribute("onclick");
	}
	
	if ( total_page <= 7 ) {
	  for ( var i = 1 ; i < total_page ; i++ ) {
	    li = document.createElement("li");                     // page
	    li.setAttribute("class","paginate_button");
	    li.setAttribute("aria-controls","DataTables_Table_0");
	    li.setAttribute("id","page_"+i);
	    li.setAttribute("onclick","click_page("+i+")");
	    a = document.createElement("a");
	    a.setAttribute("href","#");
	    a.innerHTML = i;
	    li.appendChild(a);
	    pagination.appendChild(li);
	  }
		
	}
	else {
	  li = document.createElement("li");                     // page 1
	  li.setAttribute("class","paginate_button");
	  li.setAttribute("aria-controls","DataTables_Table_0");
	  li.setAttribute("id","page_1");
	    li.setAttribute("onclick","click_page(1)");
	  a = document.createElement("a");
	  a.setAttribute("href","#");
	  a.innerHTML = "1";
	  li.appendChild(a);
	  pagination.appendChild(li);
	  
	  if ( gCurPage <= 4 ) {
	    for ( var i = 2 ; i <= 5 ; i++ ) {
	      li = document.createElement("li");                     // page 2~5
	      li.setAttribute("class","paginate_button");
	      li.setAttribute("aria-controls","DataTables_Table_0");
	      li.setAttribute("id","page_"+i);
	      li.setAttribute("onclick","click_page("+i+")");
	      a = document.createElement("a");
	      a.setAttribute("href","#");
	      a.innerHTML = i;
	      li.appendChild(a);
	      pagination.appendChild(li);
	    }
		
	    li = document.createElement("li");                     // page ...
	    li.setAttribute("class","paginate_button disabled");
	    li.setAttribute("aria-controls","DataTables_Table_0");
	    li.setAttribute("id","page_dot");
	    a = document.createElement("a");
	    a.setAttribute("href","#");
	    a.innerHTML = "...";
	    li.appendChild(a);
	    pagination.appendChild(li);
	  }
	  
	  else if ( gCurPage >= total_page - 4 ) {
	    li = document.createElement("li");                     // page ...
	    li.setAttribute("class","paginate_button disabled");
	    li.setAttribute("aria-controls","DataTables_Table_0");
	    li.setAttribute("id","page_dot");
	    a = document.createElement("a");
	    a.setAttribute("href","#");
	    a.innerHTML = "...";
	    li.appendChild(a);
	    pagination.appendChild(li);
		  
	    for ( var i = total_page-5 ; i < total_page-1 ; i++ ) {
	      li = document.createElement("li");                     // page total_page-5 ~ total-1
	      li.setAttribute("class","paginate_button");
	      li.setAttribute("aria-controls","DataTables_Table_0");
	      li.setAttribute("id","page_"+i);
	      li.setAttribute("onclick","click_page("+i+")");
	      a = document.createElement("a");
	      a.setAttribute("href","#");
	      a.innerHTML = i;
	      li.appendChild(a);
	      pagination.appendChild(li);
	    }
	  }
	  
	  else {
	    li = document.createElement("li");                     // page ...
	    li.setAttribute("class","paginate_button disabled");
	    li.setAttribute("aria-controls","DataTables_Table_0");
	    li.setAttribute("id","page_dot");
	    a = document.createElement("a");
	    a.setAttribute("href","#");
	    a.innerHTML = "...";
	    li.appendChild(a);
	    pagination.appendChild(li);
		
	    for ( var i = gCurPage-1 ; i <= gCurPage+1 ; i++ ) {
	      li = document.createElement("li");                     // page gCurPage-1 ~ gCurPage+1
	      li.setAttribute("class","paginate_button");
	      li.setAttribute("aria-controls","DataTables_Table_0");
	      li.setAttribute("id","page_"+i);
	      li.setAttribute("onclick","click_page("+i+")");
	      a = document.createElement("a");
	      a.setAttribute("href","#");
	      a.innerHTML = i;
	      li.appendChild(a);
	      pagination.appendChild(li);
	    }
		
	    li = document.createElement("li");                     // page ...
	    li.setAttribute("class","paginate_button disabled");
	    li.setAttribute("aria-controls","DataTables_Table_0");
	    li.setAttribute("id","page_dot");
	    a = document.createElement("a");
	    a.setAttribute("href","#");
	    a.innerHTML = "...";
	    li.appendChild(a);
	    pagination.appendChild(li);
	  }
	  
	  li = document.createElement("li");                     // page total
	  li.setAttribute("class","paginate_button");
	  li.setAttribute("aria-controls","DataTables_Table_0");
	  li.setAttribute("id","page_"+(total_page-1));
	  li.setAttribute("onclick","click_page("+(total_page-1)+")");
	  a = document.createElement("a");
	  a.setAttribute("href","#");
	  a.innerHTML = total_page-1;
	  li.appendChild(a);
	  pagination.appendChild(li);
		
	}
	
	$('#page_'+gCurPage).addClass('active').removeAttr("onclick");
	
	li = document.createElement("li");                     // next
	li.setAttribute("class","paginate_button next");
	li.setAttribute("aria-controls","DataTables_Table_0");
	li.setAttribute("id","DataTables_Table_0_next");
	li.setAttribute("onclick","click_page("+(gCurPage+1)+")");
	a = document.createElement("a");
	a.setAttribute("href","#");
	a.innerHTML = "下頁";
	li.appendChild(a);
	pagination.appendChild(li);
	  
	if ( gCurPage == total_page-1 ) {
	  $('#DataTables_Table_0_next').addClass('disabled');
	  li.removeAttribute("onclick");
	}
}