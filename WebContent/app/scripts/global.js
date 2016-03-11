$(document).ready(function() {

	$.ajax({
		url:"status",
		type:"post",
		success: function(data) {
			console.log(data);
			if(typeof data.userName === 'undefined') {
				createMenu(["home","login","register"]);
			} else {
				var avatar = (typeof data.avatar === 'undefined') ?
						"<div id='avatar' class='fa fa-user'></div>" :
						"<img id='avatar' src='"+data.avatar[0]+"' />" ;
				$("#status-bar").append("<div class='status'>logged in as <strong>"+ 
						data.userName[0] +"</strong>"+avatar+"</div>");
				createMenu(["home","editProfile","logout"]);

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