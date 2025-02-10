$(document).ready(function() {

	$(document).on('click', '#sendMessageButton', function(event) {
		$(this).attr('disabled', true);
		$('#loadingIcon').show();
		sendMessage();
	});
	
	

	function sendMessage() {

		let message = $('#messageWindow').val().trim();
		if(message === "") return;
		addText("Ty: " + message);
		$('#messageWindow').val('');
		
	

		$.ajax({
			'url': '/sendMessage',
			'contentType': 'application/json; charset=utf-8',
			'method': 'POST',
			'data': message,
			'success': function(data) {
				addText(data);
				$('#sendMessageButton').attr('disabled', false); //odblokowywuje przycisk
				$('#loadingIcon').hide();
			},
			'error': function(request, status, error) {
                $('#sendMessageButton').attr('disabled', false); //odblokowywuje przycisk
				$('#loadingIcon').hide();
			}
		});
	}
	
	function addText(newText) {

		const textarea = document.getElementById('czatWindow');
		textarea.value += newText + '\n'; // Dodaj tekst do textarea
	}

});