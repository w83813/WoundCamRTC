(function(win) {

    var hasOwnProperty = Object.prototype.hasOwnProperty;
    var JSBridge = win.JSBridge || (win.JSBridge = {});
    var JSBRIDGE_PROTOCOL = 'JSBridge';
    //然后有一个Inner类，里面有我们的call和onFinish方法。
    var Inner = {
        callbacks: {},
        call: function(obj, method, params, callback) {
            console.log(obj + " " + method + " " + params + " " + callback);
            var port = Util.getPort();
            console.log(port);
            this.callbacks[port] = callback;
            var uri = Util.getUri(obj, method, params, port);
            console.log(uri);
            window.prompt(uri, "");
        },
        onFinish: function(port, jsonObj) {
            var callback = this.callbacks[port];
            callback && callback(jsonObj);
            delete this.callbacks[port];
        },
        onReceiving: function(port, jsonObj) {
            var callback = this.callbacks[port];
            callback && callback(jsonObj);
        },
        onStop: function(port, jsonObj) {
            var callback = this.callbacks[port];
            callback && callback(jsonObj);
            delete this.callbacks[port];
        },
    };
    //一个Util类，里面有三个方法，getPort()用于随机生成port，getParam()用于生成json字符串
    //getUri()用于生成native需要的协议uri，里面主要做字符串拼接的工作
    var Util = {
        getPort: function() {
            return Math.floor(Math.random() * (1 << 30));
        },
        getUri: function(obj, method, params, port) {
            params = this.getParam(params);
            var uri = JSBRIDGE_PROTOCOL + '://' + obj + ':' + port + '/' + method + '?' + params;
            return uri;
        },
        getParam: function(obj) {
            if (obj && typeof obj === 'object') {
                return JSON.stringify(obj);
            } else {
                return obj || '';
            }
        }
    };
    for (var key in Inner) {
        if (!hasOwnProperty.call(JSBridge, key)) {
            JSBridge[key] = Inner[key];
        }
    }
})(window);