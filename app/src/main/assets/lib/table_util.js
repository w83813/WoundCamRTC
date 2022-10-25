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

function generatePager(divclass, roleId, ptNo){
	var pages = 3
	var searching = false;
	var lengthChange = false;
	var info = false;
	var remainingRetry = 5;
	
	if(divclass === ".patient-table"){  //table 名稱記得改
		pages = 10;
		searching = true;
		lengthChange = false;
		info = false;
	}

	$(divclass).dataTable({
		ajax: {

		  //  url: "http://192.168.50.75:8080/woundcare/wound/tsgh/v2/api/qryPatientListByApp?roleId=" + roleId + "&ptNo=" + ptNo ,
		    //url: "https://icare.itri.org.tw/woundcare/wound/tsgh/v3/api/qryPatientListByApp?roleId=" + roleId + "&ptNo=" + ptNo + "&location=TC",

		    //url: "http://192.168.1.157:8080/wgnursing/wound/tsgh/v3/api/qryPatientListByApp?roleId=" + roleId + "&ptNo=" + ptNo + "&location=TC",
		    url: "https://icare.itri.org.tw/woundcare/wound/tsgh/v3/api/qryPatientListByApp?roleId=" + roleId + "&ptNo=" + ptNo + "&location=TC",

		  //  url: "http://192.168.1.157:8080/wgnursing/wound/tsgh/v3/api/qryPatientListByApp?roleId=" + roleId + "&ptNo=" + ptNo + "&location=TC",
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
        paging: true,
        searching: searching,
        info: info,
        stateSave: true,
        lengthChange: lengthChange,
        pageLength: pages,
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
        initComplete: function(){
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