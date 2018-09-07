var crypto = require('crypto');
var moment = require('moment');

exports.generate = function (options) {
    //setup cipher
    var decodedKey = Buffer.from(options.aesKey, 'base64');
    var cipherSalt = getCipherSaltString(16);
    var iv = new Buffer.from(cipherSalt);
    var cipher = crypto.createCipheriv(options.encryptionAlgo, decodedKey, iv);
    cipher.setAutoPadding(options.cipherPadding);

    //get and encrypt the payload
    var payload = generatePayload();
    console.log("payload=" + payload);
    var encPayload = cipher.update(payload, 'utf8', 'base64');
    encPayload += cipher.final('base64');

	//cipher salt should also be Base64 encoded in the authorization
    var encCipherSalt = Buffer.from(cipherSalt).toString('base64')
    console.log("developerId=" + options.developerId + ", encCipherSalt=" + encCipherSalt + ", encPayload=" + encPayload);

    //create and encode the authorization
    var auth = options.developerId + ":" + encCipherSalt + ":" + encPayload;
    console.log("authorization=" + auth);
    var encodedAuthorization = Buffer.from(auth).toString('base64')
    console.log("encodedAuthorization=" + encodedAuthorization);

    return encodedAuthorization;
};

function generatePayload() {
    var timeStamp = moment.utc().format("YYYYMMDDHHmmss");
    var nonce = Math.floor(Math.random() * 90000) + 10000;
    var payload = timeStamp + nonce;
    return payload;
}

function getCipherSaltString(length) {
    var chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    var salt = "";
    for (var x = 0; x < length; x++) {
        var i = Math.floor(Math.random() * chars.length);
        salt += chars.charAt(i);
    }
    return salt;
}