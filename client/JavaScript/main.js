var apiURLs = {};
var serverUrl = 'https://MY_SERVER_URL';

var BokuResponseStatus = {
    SUCCESS: '1',
    FAILURE: '0',
    SWITCH_TO_FALLBACK: '2',
    ERROR: '-1'
};

$(document).ready(function () {
    $('#verifyMobileBtn').click(getAuthorization);
    $('#verifySMSCodeBtn').click(verifySMSCode);
    $('#resendCodeBtn').click(resendCode);
});

var getAuthorization = function () {
    clearMessages();
    setVerificationPassed(false);
    setVerificationFailed(false);
    var mobileNumber = $('#mobileNumberInput').val().trim();
    if (mobileNumber.length !== 10) {
        return;
    }
    showSpinner();
    getUrl(serverUrl + '?mobileNumber=' + encodeURIComponent('+1' + mobileNumber), function (err, res) {
        if (err || res.err) {
            hideSpinner();
            console.error('Failed to get authorization: ' + (err ? err : res.err));
        } else {
            for (var key in res) {
                var value = res[key];
                apiURLs[key] = decodeURIComponent(value);
            }
            verifyMobileNumber();
        }
    });
};

function verifyMobileNumber() {
    getJsonp(apiURLs.verifyPhoneNumber, {
        appName: "My Application",
        fallback: "1",
        smsMessage: "Please visit www.example.com for assistance."
    }, function (err, data) {
        if (err) {
            hideSpinner();
            console.error('Failed to verify Mobile Number: ' + err);
        } else {
            console.log('SUCCESS: ' + JSON.stringify(data));
            handleBokuResponse(data);
        }
    });
}

function verifySMSCode() {
    showSpinner();
    var code = $('#codeInput').val().trim();
    getJsonp(apiURLs.verifySMSCode, {
        code: code
    }, function (err, data) {
        if (err) {
            hideSpinner();
            console.error('Failed to verify SMS Code: ' + err);
        } else {
            console.log('SUCCESS: ' + JSON.stringify(data));
            handleBokuResponse(data);
        }
    });
}

function resendCode() {
    showSpinner();
    getJsonp(apiURLs.resendCode, {}, function (err, data) {
        hideSpinner();
        if (err) {
            console.error('Failed to resend SMS Code: ' + err);
        } else {
            console.log('SUCCESS: ' + JSON.stringify(data));
            var status = data.status;
            if (status == BokuResponseStatus.SUCCESS) {
                setMessage('Resent SMS Code');
            } else {
                setMessage('Failed to resend SMS Code: ' + err);
            }
        }
    });
}

function handleBokuResponse(response) {
    hideSpinner();
    var status = response.status;
    if (status == BokuResponseStatus.SUCCESS) {
        //phone verified
        setVerificationPassed(true);
        setVerificationFailed(false);
        resetApp();
    } else if (status == BokuResponseStatus.FAILURE) {
        //phone not verified
        setVerificationPassed(false);
        setVerificationFailed(true);
        resetApp();
    } else if (status == BokuResponseStatus.SWITCH_TO_FALLBACK) {
        //sms fallback, enter code
        setMobileNumberVisibility(false);
        setCodeVisibility(true);
        setMessage('Please enter SMS Code');
    } else {
        //error, try again later
        setVerificationPassed(false);
        setVerificationFailed(true);
        resetApp();
        setMessage('Error identifying phone');
    }
}

function resetApp() {
    hideSpinner();
    clearMessages();
    $('#codeInput').val('');
    $('#mobileNumberInput').val('');
    setMobileNumberVisibility(true);
    setCodeVisibility(false);
}

function setMobileNumberVisibility(isVisible) {
    if (isVisible) {
        $('#mobileNumber').show();
    } else {
        $('#mobileNumber').hide();
    }
}

function setCodeVisibility(isVisible) {
    if (isVisible) {
        $('#code').show();
    } else {
        $('#code').hide();
    }
}

function setVerificationPassed(isVisible) {
    if (isVisible) {
        $('#verificationPassed').show();
    } else {
        $('#verificationPassed').hide();
    }
}

function setVerificationFailed(isVisible) {
    if (isVisible) {
        $('#verificationFailed').show();
    } else {
        $('#verificationFailed').hide();
    }
}

function showSpinner() {
    $('#overlay').show();
}

function hideSpinner() {
    $('#overlay').hide();
}

function setMessage(msg) {
    $('#messages p').html(msg);
}

function clearMessages() {
    $('#messages p').html('');
}

function getUrl(url, cb, timeout) {
    if (typeof timeout === 'undefined') {
        timeout = 10000;
    }
    console.log('GET on url=' + url);
    $.ajax({
        type: 'GET',
        dataType: 'json',
        url: url,
        timeout: timeout,
        success: function (data, textStatus, xhr) {
            cb(null, data);
        },
        error: function (xhr, ajaxOptions, thrownError) {
            if (!thrownError) {
                thrownError = 'Unknown Error';
            }
            cb(thrownError, 'unknown');
        }
    });
}

//GET on URL using JSONP
function getJsonp(url, parameters, cb, timeout) {
    if (typeof timeout === 'undefined') {
        timeout = 10000;
    }
    for (var key in parameters) {
        url += '&' + key + '=' + encodeURIComponent(parameters[key]);
    }
    $.ajax(url, {
        type: 'GET',
        dataType: 'jsonp',
        timeout: timeout,
        success: function (data, textStatus, xhr) {
            cb(null, data);
        },
        error: function (jxhr, ajaxOptions, thrownError) {
            cb('AJAX ERROR: ajaxOptions=' + JSON.stringify(ajaxOptions) + ', thrownError=' + JSON.stringify(thrownError));
        }
    });
}