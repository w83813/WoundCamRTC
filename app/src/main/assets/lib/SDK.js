function SDK() {
    this.code_version = 'v1.0.20200322';
};

SDK.MIME_JSON = 'application/json';
SDK.MIME_FORM = 'application/x-www-form-urlencoded';
SDK.version = '1.0';
SDK.ServerURL = 'http://172.20.201.100:8080/wgnursing';

/**
 * HTTP Request Call
 * 
 * @param {string} url: API網址
 * @param {string} method: GET/POST
 * @param {string} contentType: 內容MIME格式
 * @param {string} paramString: params
 * @param {function} onSuccess: 當呼叫API成功時執行
 * @param {function} onError: 當呼叫API失敗時執行
 * @return {undefined}
 */
SDK.common_HTTPMethod = function (url, method, contentType, paramString, onSuccess, onError) {
    if (SDK.ServerURL == null) {
        return;
    }
    //
    method = method.toUpperCase();
    url = SDK.ServerURL + url;
    //
    var http_request = new XMLHttpRequest();
    //
    http_request.onreadystatechange = function () {
        if (http_request.readyState == 4) {
            if (http_request.status == 0) {
                console.log('common_HTTPMethod Error: ' + url + ', ' + http_request.statusText);
                //onError('HTTP ' + method + ' Error: ' + http_request.status);
                onError('伺服器無回應: ' +http_request.statusText);
            } else if (http_request.status == 200) {
                try {
                    var content = http_request.responseText;
                    var rsp = JSON.parse(content);
                    if (onSuccess != undefined && onSuccess != null) {
                        onSuccess(rsp);
                    }
                } catch (ex) {
                    if (onError != undefined && onError != null) {
                        onError(ex);
                    }
                }
            } else {
                APP.log('common_HTTPMethod Error: ' + url + ', ' + http_request.statusText);
                //onError('HTTP ' + method + ' Error: ' + http_request.status);
                onError('伺服器錯誤: ' + http_request.statusText);
            }
        }
    };
    //
    if (method == 'GET') {
        if (paramString != null) {
            url += '?' + paramString;
        }
        paramString = '';
    }
    if (method == 'POST') {
        //
    }
    if (method == 'DELETE') {
        url += '?' + paramString;
        paramString = '';
    }
    //console.info(method + ' ' + url + ' ' + contentType);
    http_request.timeout = 15000; // 15秒
    http_request.ontimeout = function (e) { onError('連線逾時'); }
    http_request.open(method, url, true);
    http_request.setRequestHeader('Content-Type', contentType + "; charset=UTF-8");
    http_request.send(paramString);
};

SDK.VERSION_CHECK_URL = "/baby/ovoRefreshJson?app=room.refresh.m1&location=HY";
// 取得所有的用戶資料列表
SDK.GET_USER_LIST = "/device/api/deviceMeasure/owner?location=HY";
// 下載護士資料列表
SDK.GET_NURSE_LIST = "/device/api/deviceMeasure/nurse?location=HY";
//

SDK.checkVersion = function (zone, onSuccess, onError) {
    SDK.common_HTTPMethod(SDK.VERSION_CHECK_URL + zone, 'GET', SDK.MIME_JSON, JSON.stringify(loginInfo), function (rsp) {
    }, function (ex) {
        onError("錯誤!檢查版本失敗:" + ex.message);
    });
};

/**
 * 取得所有的用戶資訊
 */
SDK.getOwnerList = function (onSuccess, onError) {
    var users = [
        {"roomName":"302","ownerName":"張迪迪","owner":10229,"roleName":"baby"},
        {"roomName":"106","ownerName":"陳梅梅","owner":10230,"roleName":"baby"},
        {"roomName":"106","ownerName":"陳梅梅","owner":10177,"roleName":"mom"},
        {"roomName":"302","ownerName":"張迪迪","owner":10198,"roleName":"mom"},

        {"roomName":"302","ownerName":"張迪迪","owner":10198,"roleName":"mom"},
        {"roomName":"302","ownerName":"張迪迪","owner":10198,"roleName":"mom"},
        {"roomName":"302","ownerName":"張迪迪","owner":10198,"roleName":"mom"},
        {"roomName":"302","ownerName":"張迪迪","owner":10198,"roleName":"mom"},
        {"roomName":"302","ownerName":"張迪迪","owner":10198,"roleName":"mom"}
    ];
    onSuccess(users);
    /*
    SDK.common_HTTPMethod(SDK.LOGIN, 'POST', SDK.MIME_JSON, JSON.stringify(loginInfo), function (rsp) {
        if (rsp.retCode == 0 || rsp.retCode == 1 || rsp.retCode == 3) { // 0:未審核(完成註冊 未完成詳細資料填寫) 1:審核通過 2.停用 3.審核中(完成填寫詳細資訊但還沒過審核)
            onSuccess();
        } else {
            onError(rsp.retMsg);
        }
    }, function (ex) {
        onError("錯誤!無法登入:" + ex.message);
    });
    */
};

// 取得所有的護士資料
SDK.getNurseList = function (onSuccess, onError) {
    var mngrs = [
        {"nurse":2,"nurseName":"系統管理員","babyNurse":"1","momNurse":"1"},
        {"nurse":10043,"nurseName":"測試人員","babyNurse":"1","momNurse":"0"}
    ];
    onSuccess(mngrs);
};

/**
 * 由url字串取得參數值
 * @returns 參數值
 */
SDK.getUrlValues = function () {
    var searchString = window.location.search.substring(1);
    //
    searchString = decodeURIComponent(searchString);
    if (searchString.length == 0) {
        return undefined;
    }
    //
    var data = searchString.split('&');
    if (data.length > 0) {
    var params = "{";
    for ( var i = 0; i < data.length; i++) {
        var temp = data[i].split('=');
        if (temp[0] != "") {
          params += "\"" + temp[0] + "\":" + "\"" + temp[1] + "\",";
        }
    }
    params += "}";
    var json = eval('(' + params + ')');
       return json;
    } else {
       return undefined;
    }
};

//
function MeasureItem() {
    this.barcodeType; // 97703
    this.ownerId;
    this.nurseId;
    this.evalDate;
    this.valueType; // 體溫: TP, 體重: WG, 脈搏: BG_PULSE, 收縮壓: BG_SYSTOLIC, 舒張壓: BG_DISTOLIC, 飯前血糖: AC, 飯後血糖: PC,
    this.value = "";
};
