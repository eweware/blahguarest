// Supports text/index.html API Center

$(document).ready(function() {
	configFromServer()
});

function rest(method, path, dataHandler, successFunction) {
	var endpoint = document.getElementById("endpoint").value;
	if ( typeof (endpoint) == "undefined") {
		alert("Missing the hostname and optional port for the endpoint (e.g., 'localhost:8080'");
		return;
	}
	var fullUrl = 'http://' + endpoint + '/v2/' + path;
	$("#results").html("");
	$("#resultstatus").html("");
	$("#callmeth").css({'color': 'blue'}).attr("value", method);
	$("#callurl").css({'color': 'blue'}).attr("value", fullUrl);
	$.ajax({
		type : method,
		url : fullUrl,
		data : dataHandler,
		contentType : "application/json; charset=utf-8",
		dataType : "html",
		success : successFunction == null ? defaultSuccessFunction : successFunction,
		error : defaultErrorHandler
	});
	var payload = String(dataHandler);
	if (payload == 'null' || payload.indexOf("function") != -1) {
		payload = 'NO PAYLOAD';
	}
	$("#payload").css({
		'color' : 'blue'
	}).text(payload);
}

function defaultErrorHandler(theErr, err, thrown) {
	$("#resultstatus").css({
		'color' : 'red'
	}).html("<span style='color: red'>Http Status: " + theErr.status + ": " + theErr.statusText + "</span>");
	$("#resultsArea").html('<textarea id="results" cols="94" rows="10"/>');
	$("#results").css({
		'color' : 'red'
	}).html(theErr.responseText);
}

function defaultSuccessFunction(results, successOrNot, theStatus) {
	if (theStatus) {
		$("#resultstatus").html("<span style='color:green'>Http Status: " + theStatus.status + "</span>");
	} else {
		$("#resultstatus").html("<span style='color:green'>Http Status: Unknown</span>");
	}
	
	$("#resultsArea").html('<textarea id="results" cols="94" rows="10"/>');
	$("#results").css({
		'color' : 'black'
	}).html(results)
}

function createChannelType() {
	var name = document.getElementById('channeltypename').value;
	if (!name) {alert('Missing Channel Type Name'); return;}
	rest('POST', 'groupTypes', JSON.stringify({'displayName': name}), setChannelTypeData);
}

function updateChannelTypeName() {
	var name = document.getElementById('channeltypename').value;
	var ctId = document.getElementById('channeltypeid').value;
	if (!name || !ctId) {alert('Missing Channel Type Id and/or Name'); return;}
	rest('PUT', 'groupTypes/'+ctId, JSON.stringify({'displayName': name}));
}

function getChannelTypeById() {
	var ctId = document.getElementById('channeltypeid').value;
	if (!ctId) {alert('Missing Channel Type Id'); return;}
	rest('GET', 'groupTypes/'+ctId);
}

function createChannel(descriptor) {
	var ctId = document.getElementById('channeltypeid').value;
	var name = document.getElementById('channelname').value;
	var validationMethod = 'n'; // normal validation
	if (!ctId || !name) {alert('Missing Channel Type Id and/or Name'); return;}
	rest('POST', 'groups', JSON.stringify({'displayName': name, 'groupTypeId': ctId, 's': descriptor, 'vmeth': validationMethod}), setChannelData);
}

function createUser() {

	var username = getUsername();
	var password = getPassword();
	if (!username || username.length == 0) {
		alert("Missing User Name");
		return;
	}
	if (!password || password.length == 0) {
		alert("Missing Password");
		return;
	}
	rest("POST", "users", '{"displayName": "' + username + '", "pwd": "' + password + '"}', setUserData);
}

function checkUsername() {
	var username = getUsername();
	if (!username) {
		alert("Missing User Name");
		return;
	}
	rest("GET", "users/check/username/" + username)
}


function changePassword() {
	var userId = getUserId();
	var password = getPassword();
	if (username.length == 0) {
		alert("Missing User Name");
		return;
	}
	rest("PUT", "users/update/password/"+password);
}

function loginUser() {
	var username = getUsername();
	var password = getPassword();
	if (username.length == 0 || password.length == 0) {
		alert("Missing username and/or password");
		return;
	}
	rest("POST", "users/login", '{"displayName": "' + username + '", "pwd": "' + password + '"}');
}

function setHttpCode(a, successOrNot, theStatus) {
	alert(successOrNot + ": " + theStatus.status);
}

function userLoggedIn() {
	rest("GET", "users/login/check");
}

function setUserData(user) {
	defaultSuccessFunction(user);
	var obj = jQuery.parseJSON(user);
	//  alert(user);
	document.getElementById("username").value = obj.displayName;
	document.getElementById("userid").value = obj._id;
}

function createDummyProfile() {
	var email = document.getElementById("email").value;
	var userId = getUserId();
	if (!email || !userId) {alert("Missing UserId and/or Email Address"); return;}
	rest('POST', 'users/profiles', JSON.stringify({"_id": userId, "e": email, "a": "hi"}));
}


function recoverUser() {
	var username = getUsername();
	var email = document.getElementById("email").value;
	if (!username || !email) {alert("Missing User Name and/or Email Address"); return;}
	var challengeAnswer = "hi";
	rest('POST', "users/recover/user", JSON.stringify({"u": username, "e": email, "a": "hi"}));
}

function setChannelTypeData(channelType) {
	defaultSuccessFunction(channelType);
	var obj = jQuery.parseJSON(channelType);
	document.getElementById("channeltypename").value = obj.displayName;
	document.getElementById("channeltypeid").value = obj._id;
}

function setChannelData(channel) {
	defaultSuccessFunction(channel);
	var obj = jQuery.parseJSON(channel);
	document.getElementById("channelname").value = obj.displayName;
	document.getElementById("channelid").value = obj._id;
}

function getUserById() {
	var userId = getUserId();
	if (userId.length == 0) {
		alert("Missing User Id");
		return;
	}
	rest("GET", "users/" + userId, setUserData);
}

function getUserByName() {
	var username = getUsername();
	if (username.length == 0) {
		alert("Missing User Name");
		return;
	}
	rest("GET", "users/" + username + "?u=true", setUserData);
}

function joinChannel() {
	var channelId = getChannelId();
	var userId = getUserId();
	if (channelId.length == 0 || userId.length == 0) {
		alert("Missing Channel Id and/or User Id");
		return;
	}
	var data = '{"userId": "' + userId + '", "groupId": "' + channelId + '"}';
	rest("POST", "userGroups", data);
}

function getChannel() {
	var channelId = getChannelId();
	if (!channelId) {
		alert("Missing Channel Id");
		return;
	}
	rest("GET", "groups/"+channelId, JSON.stringify(null), setChannelData);
}

function createBlahType() {
	var name = document.getElementById('blahtypename').value;
	if (!name) {alert("Missing Blah Type Name"); return;}
	alert('Create Blah Type not implemented!)');
}

function createBlah() {
	var userId = getUserId();
	var typeId = getTypeId();
	var channelId = getChannelId();
	var text = getBlahText();
	if (userId.length == 0 || typeId.length == 0 || channelId.length == 0 || text.length == 0) {
		alert("Missing User Id, and/or Blah Type Id, and/or Channel Id and/or Blah Text");
		return;
	}
	var data = '{"authorId": "' + userId + '", "groupId": "' + channelId + '", "typeId": "' + typeId + '", "text": "' + text + '"}';
	rest("POST", "blahs", data, setBlahData1);
}

function getBlah() {
	var blahId = getBlahId();
	if (blahId.length == 0) {
		alert("Missing Blah Id parameter");
	}
	rest("GET", "blahs/" + blahId, null, setBlahData2);
}

function setBlahData1(blahinfo) {
	defaultSuccessFunction(blahinfo);
	var obj = jQuery.parseJSON(blahinfo);
	document.getElementById("blahid").value = obj._id;
}

function setBlahData2(blahinfo) {
	defaultSuccessFunction(blahinfo);
	var obj = jQuery.parseJSON(blahinfo);
	var img = obj.img;
	if (img && img.length > 0) {
		var url = "http://blahguaimages.s3-website-us-west-2.amazonaws.com/image/";
		//$("#imgs").css({"visibility", "visible"});
		for (var i = 0; i < img.length; i++) {
			var im = img[i];
			$("#img_a").attr("src", url + im + "-A.jpg");
			$("#img_b").attr("src", url + im + "-B.jpg");
			$("#img_c").attr("src", url + im + "-C.jpg");
			$("#img_d").attr("src", url + im + "-D.jpg");
		}
	}
}

function getUserBlahs() {
	var userId = getUserId();
	if (userId.length == 0) {
		alert("Missing User id");
		return;
	}
	rest("GET", "blahs?authorId=" + userId);
}

function getUserDescriptor() {
	var userId = getUserId();
	if (!userId) {
		alert("Missing User Id");
		return;
	}
	rest('GET', 'users/profiles/descriptor/' + userId);
}

function getInbox() {
	var channelId = getChannelId();
	if (channelId.length == 0) {
		alert("Missing Channel Id");
		return;
	}
	rest("GET", "users/inbox?groupId=" + channelId);
}

// function getUserInbox() {
// var channelId = getChannelId();
// var userId = getUserId();
// if (channelId.length == 0 || userId.length == 0) {
// alert("Get User Inbox: Missing Channel Id and/or User Id");
// return;
// }
// rest("GET", "users/" + userId + "/inbox?groupId=" + channelId);
//
// }

function clearResults() {
	$("#results").css({
		'color' : 'black'
	}).html("");
	$("#payload").html("");
	$("#callmeth").attr("value", "");
	$("#callurl").attr("value", '');
	//$("#imgs").css({"visibility", "hidden"});
}

function clearParameters() {
	var obj = $(".param").val("");
}

function help() {
	$("#resultsArea").html('<div id="results"/>');
	$("#results").css({
		'color' : 'blue'
	}).html("<h4>Help</h4><div>Create User: needs a User Name. If password is given, this will be used to later authenticate.</div><div>Login User: requires User Name and Password.</div><div>Get Users: needs no parameters</div><div>Get User By Id: needs User Id parameter -- returns users when not provided</div><div>Get User By Name: needs the User Name parameter.</div><div>Get User's Blahs: needs User Id parameter</div><div>Get Channels: needs no parameter</div><div>Join User To Channel: needs User Id and Channel Id parameters</div><div>Create Blah: Needs User Id parameter</div><div>Get Blah: Needs the Blah Id parameter. Returns any images.</div><div>Get User Blahs:: Needs User Id parameter</div><br/><div>Clear All Parameters: Clears parameter values</div><div>Clear All Results: clears results text</div>");
}

// Getters --------------------------------------
function getUsername() {
	return document.getElementById("username").value;
}

function getPassword() {
	return document.getElementById("password").value;
}

function getUserId() {
	return document.getElementById("userid").value;
}

function getChannelId() {
	return document.getElementById("channelid").value;
}

function getTypeId() {
	return document.getElementById("blahtypeid").value;
}

function getBlahText() {
	return document.getElementById("blahtext").value;
}

function getBlahId() {
	return document.getElementById("blahid").value;
}

// Special
function configFromServer() {
	help();
	var endpoint = document.getElementById("endpoint").value;
	if ( typeof (endpoint) == "undefined") {
		alert("Config Error: missing the hostname and optional port for the endpoint (e.g., 'localhost:8080'");
		return;
	}// Get blah types for drop down menu
	var getBlahTypesUrl = "http://" + endpoint + "/v2/blahs/types";
	$.ajax({
		type : "GET",
		url : getBlahTypesUrl,
		data : null,
		contentType : "application/json; charset=utf-8",
		dataType : "html",
		success : configHandler,
		error : configErrorHandler
	});
}

function configHandler(result) {
	var items = $.parseJSON(result);
	var ar = Array.prototype.slice.call(items, 0);
	var selectHtml = "<select id='blahtypeid'>";
	for (var i = 0; i < ar.length; i++) {
		var obj = ar[i];
		var theId = "";
		var theName = "";
		for (var p in obj) {
			if (p == '_id')
				theId = obj[p];
			if (p == 'name')
				theName = obj[p];
		}

		selectHtml += "<option value='" + theId + "'>" + theName + "</option>";
	}
	selectHtml += "</select>";
	$("#blahTypes").html(selectHtml);
}

function configErrorHandler(result, error, thrown) {
	alert("Error: Failed to configure page:\n" + thrown + "\n" + result);
}
