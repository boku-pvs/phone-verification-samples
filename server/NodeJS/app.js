var http = require('http');
var jsonBody = require("body/json");
var authorization = require('./authorization.js');
var port = '3000';

//TODO: configure these values received during API signup
var aesKey = 'AES_KEY';
var developerId = 'DEVELOPER_ID';
var encryptionType = 'aes-128-ctr'; //default, check it is correct
var danalBaseUrl = 'http://DANAL_BASE_URL/api/v1/';


var server = http.createServer(function (req, res) {
    console.log('Received request: ' + req.method, req.url, req.headers);

    jsonBody(req, res, function (err, body) {
        // err is probably an invalid json error
        if (err) {
            res.statusCode = 500
            return res.end("SERVER ERROR")
        }

		//allow CORS
        res.setHeader("content-type", "application/json");
        res.setHeader('Access-Control-Allow-Origin', '*');
        res.setHeader('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE');
        res.setHeader('Access-Control-Allow-Headers', 'Content-Type');
        handleRequest(req, res, body);
    });
});

console.log('Listening on port=' + port);
server.listen(port);


var handleRequest = function (req, res, body) {
    var mobileNumber = body.mobileNumber;
    console.log('Received request mobileNumber=' + mobileNumber);

    var msg;
    if (mobileNumber !== null && mobileNumber.trim().length === 12) {
        try {
            var auth = authorization.generate({
                aesKey: aesKey,
                developerId: developerId,
                encryptionAlgo: encryptionType,
                cipherPadding: false
            });

            var responseMap = {
                verifyPhoneNumber: danalBaseUrl + 'verifyPhoneNumber/' + mobileNumber + '/?authToken=' + auth,
                verifySMSCode: danalBaseUrl + 'verifySMSCode/' + mobileNumber + '/?authToken=' + auth,
                resendCode: danalBaseUrl + 'resendCode/' + mobileNumber + '/?authToken=' + auth
            };
            return res.end(JSON.stringify(responseMap));
        } catch (e) {
            msg = 'AuthorizationServlet: ERROR in handleRequest e=' + e;
        }
    } else {
        msg = 'AuthorizationServlet: ERROR in handleRequest, invalid mobileNumber=' + mobileNumber;
    }
    console.error(msg);
    res.end(JSON.stringify({err: msg}));
};




//
//request({
//    url: url,
//    headers: {
//        'Accept': 'application/json',
//        'Content-Type': 'application/json',
//        'Authorization': authorization.generate({
//            aesKey: 'Xg8/C5qRfXL+U1/d11W2gA==',
//            developerId: '00CA44300099',
//            encryptionAlgo: 'aes-128-ctr',
//            cipherPadding: false
//        })
//    },
//    json: {
//        appName: 'Danal Phone Verification',
//        fallback: '1',
//        smsMessage: ''
//    },
//    method: 'POST'
//}, function (err, res, body) {
//    if (err) {
//        console.log('err=' + err);
//    }
//    if (res) {
//        console.log('res=' + JSON.stringify(res));
//    }
//    if (!err && res.statusCode == 200) {
//        var info = JSON.parse(body);
//        console.log(info);
//    }
//});
//var danalUrl = 'http://35.199.146.35:8080/devqa/api/v1/verifyPhoneNumber/+15555550002';