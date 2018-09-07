var apiURLs = {};
var serverUrl = 'https://MY_SERVER_URL';

var DanalResponseStatus = {
    SUCCESS: '1',
    FAILURE: '0',
    SWITCH_TO_FALLBACK: '2',
    ERROR: '-1'
};

$(document).ready(function () {
    $('#verifyMobileBtn').click(getAuthorization);
    $('#verifyPinBtn').click(verifySMSCode);
});

var getAuthorization = function () {
    showSpinner();
    setVerificationPassed(false);
    setVerificationFailed(false);
    var mobileNumber = $('#mobileNumberInput').val().trim();
    postJson(serverUrl, {
        mobileNumber: '+1' + mobileNumber
    }, function (err, res) {
        if (err || res.err) {
            hideSpinner();
            console.error('Failed to get authorization: ' + (err ? err : res.err));
        } else {
            apiURLs = res;
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
            handleDanalResponse(data);
        }
    });
}

function verifySMSCode() {
    showSpinner();
    var pin = $('#pinInput').val().trim();
    getJsonp(apiURLs.verifySMSCode, {
        code: pin
    }, function (err, data) {
        if (err) {
            hideSpinner();
            console.error('Failed to verify SMS Code: ' + err);
        } else {
            console.log('SUCCESS: ' + JSON.stringify(data));
            handleDanalResponse(data);
        }
    });
}

function handleDanalResponse(response) {
    hideSpinner();
    var status = response.status;
    if (status == DanalResponseStatus.SUCCESS) {
        //phone verified
        setVerificationPassed(true);
        setVerificationFailed(false);
        resetApp();
    } else if (status == DanalResponseStatus.FAILURE) {
        //phone not verified
        setVerificationPassed(false);
        setVerificationFailed(true);
        resetApp();
    } else if (status == DanalResponseStatus.SWITCH_TO_FALLBACK) {
        //sms fallback, enter pin
        setMobileNumberVisibility(false);
        setPinVisibility(true);
    } else {
        //error, try again later
        setVerificationPassed(false);
        setVerificationFailed(true);
        resetApp();
    }
}

function resetApp() {
    hideSpinner();
    $('#pinInput').val('');
    $('#mobileNumberInput').val('');
    setMobileNumberVisibility(true);
    setPinVisibility(false);
}

function setMobileNumberVisibility(isVisible) {
    if (isVisible) {
        $('#mobileNumber').show();
    } else {
        $('#mobileNumber').hide();
    }
}

function setPinVisibility(isVisible) {
    if (isVisible) {
        $('#pin').show();
    } else {
        $('#pin').hide();
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

//POST JSON body
function postJson(url, data, cb, timeout) {
    if (typeof timeout === 'undefined') {
        timeout = 10000;
    }
    console.log('API Call: ' + JSON.stringify(data));
    $.ajax({
        type: 'POST',
        data: JSON.stringify(data),
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