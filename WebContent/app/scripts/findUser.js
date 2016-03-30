$(document).ready(function() {

	$("#search-button").click(function() {
		$("#results").empty();
		new BBLoader( 60 , 'find-user-loader' , "#000000" , "#FFFFFF" , 7 , 30 , 1 );
		var name = $("#search-name").val();
		if(!name.length) {
			showAlert("no name provided");
			return;
		}
		$.ajax({
			url:"getUserInfo",
			type:"post",
			data: {
				field: "name",
				name: name
			},
			success: function(data) {
				if(typeof data.error !== 'undefined') {
					showAlert(data.error);
				}
				if(typeof data.result[0] !== 'undefined') {
					var result = data.result[0];
					var muted = result.muted[0];
					var muteText = (muted) ? "unmute" : "mute" ;
					var muteButton = 
							"<form method='POST' action='muteUser'>"+
							"<input class='hidden' name='userName' value='"+result.name+"' />"+
							"<input class='hidden' name='muted' value='"+muted+"' />"+
							"<input type='submit' value='"+ muteText +"' /></form>";
					$("#results").append(
						"<span>name: "+ result.name +"</span><br>"+
						"<span>email "+ result.email +"</span><br>"+
						muteButton+
						"<span>avatar:<br>"+getAvatar(result.avatar)+"<br>"+
						"<h3>send message</h3>"+
						"<form method='POST' action='sendMessage'><input type='hidden' name='target' value='"+ result.name +"' />"+
						"<textarea name='message' /><br><input type='submit' /></form>"
					);
				}
				deleteBBLoader('find-user-loader');
			},
			error: function(err) {
				showAlert("an error occured");
				console.log(err);
			}
		});
	});

	function showAlert(info) {
		$("#results").empty();
		$("#results").append("<span>"+ info +"</span><br>");
	}
		

});