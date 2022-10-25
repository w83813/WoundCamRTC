/* 定義共通的執行動作(JS --> APP) */
function APP() {
    this.code_version = 'v1.0.20180801';
    this.descript = '通用版本';
    this.for = 'Android/iOS';
};

APP.iOSDevice = function () {
    var platform = window.navigator.platform;
    if (platform != null && (typeof(platform) == 'string') && (platform.includes('iPhone') || platform.includes('iPad')) ) {
        return true;
    } else {
        return false;
    }
};

APP.inputOnBlur = function (e) {
	try {
        if (APP.iOSDevice()) {
			document.body.scrollTop = 0;
        }
	} catch (e) {
		console.log(e);
	}
};

APP.postMessage = function (data) {
	try {
        if (APP.iOSDevice()) {
            webkit.messageHandlers.app.postMessage(JSON.stringify(data));
        } else {
    		app.postMessage(JSON.stringify(data));
        }
	} catch (e) {
		console.log(e);
	}
};

/* Log */
APP.log = function (message) {
    if (typeof(message) == 'object') {
        message = JSON.stringify(message);
    }
    var data = {
        'method': 'console',
        'msg': message
    };
    if (APP.iOSDevice()) {
        APP.postMessage(data);
    } else {
        console.info(message);
    }
};

APP.vibrating = function () {
    var data = {
        'method': 'vibrating'
    };
    APP.postMessage(data);
};

APP.getPhotoList = function () {
    var data = {
        'method': 'getPhotoList'
    };
    APP.postMessage(data);
};

APP.delPhotoList = function (photos) {
    var data = {
        'method': 'delPhotoList',
        'photos': JSON.stringify(photos)
    };
    APP.postMessage(data);
};

APP.saveTxtData = function (params) {
    var data = {
        'method': 'saveTxtData',
        'params': JSON.stringify(params)
    };
    APP.postMessage(data);
};

APP.goback = function () {
    var data = {
        'method': 'goback'
    };
    APP.postMessage(data);
};

APP.singleUpload = function () {
    var data = {
        'method': 'singleUpload'
    };
    APP.postMessage(data);
};

APP.gotoAnalysis = function (params) {
    var data = {
        'method': 'gotoAnalysis',
        'params': JSON.stringify(params)
    };
    APP.postMessage(data);
};

APP.gotoBodyPartPicker = function (params) {
    var data = {
        'method': 'gotoBodyPartPicker',
        'params': JSON.stringify(params)
    };
    APP.postMessage(data);
};

APP.checkpart = function (params) {
    var data = {
        'method': 'checkpart',
        'params': params
    };
    APP.postMessage(data);
};

APP.getSensingValueWithRGBCoord = function (params) {
    var data = {
        'method': 'getSensingValueWithRGBCoord',
        'params': JSON.stringify(params)
    };
    APP.postMessage(data);
};

APP.getGen3DColorImage = function (params) {
    var data = {
            'method': 'getGen3DColorImage',
            'params': JSON.stringify(params)
        };
    APP.postMessage(data);
};

APP.getGen3DThermalImage = function (params) {
    var data = {
            'method': 'getGen3DThermalImage',
            'params': JSON.stringify(params)
        };
    APP.postMessage(data);
};