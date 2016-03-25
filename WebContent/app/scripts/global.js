$(document).ready(function() {

	$.ajax({
		url:"status",
		type:"post",
		success: function(data) {
			if(typeof data.userName === 'undefined') {
				createMenu(["home","login","register"]);
			} else {
				var avatar = getAvatar(data.avatar) ;
				$("#status-bar").append("<div class='status'>logged in as <strong>"+ 
						data.userName[0] +"</strong><br>"+avatar+"</div>");
				createMenu(["home","editProfile","findUser","logout"]);

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
			var item = items[i];
			$("#menu").append("<a href='"+ item +"'>"+ item +"</a>");
		}
	}

});

function getAvatar(avatar) {
	return (typeof avatar === 'undefined') ?
			"<div class='avatar fa fa-user'></div>" :
			"<img class='avatar' src='"+avatar[0]+"' />"
}