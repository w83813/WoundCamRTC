// Cookie在跨不同的頁面時會有問題..
//
function getCookie(key) {
    var c_value = document.cookie;
    var c_start = c_value.indexOf(" " + key + "=");
    if (c_start == -1) {
    	c_start = c_value.indexOf(key + "=");
    }
    if (c_start == -1) {
    	c_value = null;
    } else {
    	c_start = c_value.indexOf("=", c_start) + 1;
    	var c_end = c_value.indexOf(";", c_start);
    	if (c_end == -1) {
    	    c_end = c_value.length;
    	}
    	c_value = unescape(c_value.substring(c_start, c_end));
    }
    return c_value;
}

function setCookie(key, value) {
    var exdate = new Date();
    exdate.setDate(exdate.getDate() + 1); // 設定一日後失效
    if (value == null) {
    	document.cookie = key + "="; // 空白值
    } else {
    	var c_value = escape(value) + "; expires=" + exdate.toUTCString();
    	document.cookie = key + "=" + c_value;
    }
}

/**
 * 由url字串取得參數值
 * @returns 參數值
 */
function getUrlValues() {
	var _json = {};
    var searchString = window.location.search.substring(1);
    //
    searchString = decodeURIComponent(searchString);
    if (searchString.length == 0) {
    	return _json;
    }
    //
    var data = searchString.split('&');
    if (data.length > 0) {
    	for ( var i = 0; i < data.length; i++) {
    	    var temp = data[i].split('=');
    	    if (temp[0] != "") {
    	    	_json[temp[0]] = temp[1];
    	    }
    	}
    }
    return _json;
}

// 開啟新對話窗視
function openDialog(url, title, width, height) {
    if (title == null) {
    	title = '';
    }
    if (width == undefined) {
    	width = 480;
    }
    if (height == undefined) {
    	height = 400;
    }
    var win = window.open(url, title, 'width='+width+',height='+height+',toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=yes,copyhistory=no,resizable=no');
    return win;
}

function initQueryDate(from, to, days) {
    var today = new Date();
    if (to) {
        to.value = today.toISOString().substring(0, 10);
        today.setTime(today.getTime() - (86400000 * (days - 1))); // 往前推days - 1天
    }
    if (from) {
        from.value = today.toISOString().substring(0, 10);    
    }
}

//
function parseDate(str) {
    if (!/^(\d){14}$/.test(str)) {
    	return "invalid date";
    }
    //
    var year   = str.substr(0, 4),
        month  = str.substr(4, 2),
        day    = str.substr(6, 2),
        hour   = str.substr(8, 2),
        minute = str.substr(10, 2),
        second = str.substr(12, 2);
    return new Date(year, month-1, day, hour, minute, second);
}

function formatDate(date) {
	var str = date.toLocaleDateString() + " " + date.getHours() + "時" + date.getMinutes() + "分" + date.getSeconds() + "秒";
	return str;
}

// 每頁顯示幾筆資料
var _datatable_page_size = 10;

// 語言設定
var _datatable_page_language = {
	"sLengthMenu": "每頁顯示 _MENU_ 筆資料",
	"sSearch": "搜尋",
	"sZeroRecords": "沒有找到資料",
	"sInfo": "目前資料為從第 _START_ 到第 _END_ 筆資料；總共有 _TOTAL_ 筆記錄",
	"sInfoEmpty": "無資料",
	"sProcessing": "資料載入中...",
	"oPaginate": {"sFirst": "首頁", "sPrevious": "前一頁", "sNext": "下一頁", "sLast": "尾頁"}
 };

