(function() {
	function parseForm(event) {
    	var form = this;
		// make sure form points to the surrounding form object if a custom button was used
		if (this.tagName.toLowerCase() != 'form')
        	form = this.form;
        var data = '';
        if (!form.method) form.method = 'get';
        data += 'method=' + form.method;
		data += '&action=' + form.action;
        var inputs = form.getElementsByTagName('input'); // Get "input" type inputs
        for (var i = 0; i < inputs.length; i++) {
        	var field = inputs[i];
        	if (field.type != 'submit' && field.type != 'reset' && field.type != 'button')
        		data += '&' + field.name + '=' + field.value;
        }
        var selects = form.getElementsByTagName('select');  // Get "select" type inputs
        for (var i = 0; i < selects.length; i++) {
        	var field = selects[i];
       		data += '&' + field.name + '=' + field.value;
        }		
        Android.processFormData(data);
	}
    
	for (var form_idx = 0; form_idx < document.forms.length; ++form_idx)
    	document.forms[form_idx].addEventListener('submit', parseForm, false);
    var inputs = document.getElementsByTagName('input');
    for (var i = 0; i < inputs.length; i++) {
    	if (inputs[i].getAttribute('type') == 'button')
        	inputs[i].addEventListener('click', parseForm, false);
    }
})();