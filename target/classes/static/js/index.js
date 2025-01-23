$(document).ready(function() {

	$(document).on('click', '#sendMessageButton', function(event) {
		sendMessage();
	});
	
	

	function sendMessage() {

		let message = $("#messageWindow").val();
		

		$.ajax({
			'url': '/sendMessage',
			'contentType': 'application/json; charset=utf-8',
			'method': 'POST',
			'data': message,
			'success': function(data) {
				addText(data);
				
			},
			'error': function(request, status, error) {

			}
		});
	}
	
	function addText(newText) {

		const textarea = document.getElementById('czatWindow');
		textarea.value += newText + '\n'; // Dodaj tekst do textarea
	}

});