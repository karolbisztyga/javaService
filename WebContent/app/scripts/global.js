$(document).ready(function() {

	$.ajax({
		url:"status",
		type:"post",
		success: function(data) {
			if(typeof data.userName === 'undefined') {
				createMenu({
					"home":"home",
					"login":"login",
					"register":"register",
				});
			} else {
				var unreadMessages = data.unreadMessages[0];
				var avatar = getAvatar(data.avatar) ;
				$("#status-bar").append("<div class='status'>logged in as <strong>"+ 
						data.userName[0] +"</strong><br>"+avatar+"</div>");
				createMenu({
					"home":"home",
					"messages":"messages("+ unreadMessages +")",
					"editProfile":"editProfile",
					"findUser":"findUser",
					"logout":"logout"
				});

			}
			if(typeof data.alerts !== 'undefined') {
				var alerts = JSON.parse(data.alerts)[0];
				for(var i in alerts) {
					var alert = alerts[i];
					$("#status-bar").append("<div class='status'>"+ alert +"</div>");
				}
			}
		},
		error: function(err) {
			alert(data);
			console.log(err);
		}
	});

	function createMenu(items) {
		for(var i in items) {
			$("#menu").append("<a href='"+ i +"'>"+ items[i] +"</a>");
		}
	}

});

function getAvatar(avatar) {
	return (typeof avatar === 'undefined') ?
			"<div class='avatar fa fa-user'></div>" :
			"<img class='avatar' src='"+avatar[0]+"' />";
}