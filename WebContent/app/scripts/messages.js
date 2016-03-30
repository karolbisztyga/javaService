$(document).ready(function() {

	var lastMessageId = {
		sent: null,
		received: null
	};
	
	$(".category-title").click(function(e) {
		var type =  $(e.target).attr('id');
		$("#messages").empty();
		new BBLoader( 60 , 'loader' , "#000000" , "#FFFFFF" , 7 , 30 , 1 );
		$.ajax({
			url:"getMessages",
			type:"post",
			data: {
				query: type,
				lastMessageId: lastMessageId[type],
			},
			success: function(data) {
				deleteBBLoader('loader');
				if(type === "sent") {
					addMessageEntry("date", "target", "message");
				} else if(type === "received") {
					addMessageEntry("date", "author", "message");
				}
				if(typeof data.result !== 'undefined') {
					var result = data.result[0];
					for(var i in result) {
						var message = result[i][0];
						var messageContent = message.message[0];
						var sendDate = message.sendDate[0];
						var user;
						if(type === "sent") {
							user = message.target[0];
						} else if(type === "received") {
							user = message.author[0];
						}
						addMessageEntry(dateFormat(sendDate), user, messageContent);
					}
				}
				if(typeof data.lastMessageId !== 'undefined') {
					type = data.lastMessageId[0];
				}
			},
			error: function(err) {
				console.log(err);
				deleteBBLoader('loader');
			}
		});
	});

	function addMessageEntry(date, user, message) {
		var newContent = "<div class='row message'>";
		newContent += "<div class='col-xs-3'>"+date+"</div>";
		newContent += "<div class='col-xs-2'>"+user+"</div>";
		newContent += "<div class='col-xs-7'>"+message+"</div>";
		newContent += "</div>";
		$("#messages").append(newContent);
	}

	function dateFormat(milliseconds) {
        var date = new Date(milliseconds);
        var yyyy = date.getFullYear();
        var mm = addZero(date.getMonth()+1);
        var dd = addZero(date.getDate());
        var hh = addZero(date.getHours());
        var ii = addZero(date.getMinutes());
        var ss = addZero(date.getSeconds());
        return yyyy + "-" + mm + "-" + dd + " " + hh + ":" + ii + ":" + ss;
	}
    
    function addZero(x) {
        return (x<10) ? '0'+x : x ;
    }

});