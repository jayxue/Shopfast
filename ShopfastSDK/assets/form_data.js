/*
 * Copyright 2015 Waterloo Mobile Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
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