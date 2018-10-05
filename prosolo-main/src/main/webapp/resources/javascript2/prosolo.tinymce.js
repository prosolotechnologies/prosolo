function initTinyMCE(textArea) {
	if ($(textArea).length > 0) {
		tinymce.remove(textArea);
		tinymce.init({
			theme: "modern",
			skin: 'light',
			height : "200",
			selector: textArea,
            convert_urls : 0,
            font_formats: '14px/22px "prosolo_regular", "Helvetica Neue", Helvetica, Arial, sans-serif',
			plugins: 'link media code image fullscreen paste lists',
			paste_as_text: true,
			menubar: false,
			statusbar: false,
			style_formats: [
	            { title: 'Heading 1', block: 'h1' },
	            { title: 'Heading 2', block: 'h2' },
	            { title: 'Heading 3', block: 'h3' }
            ],
            toolbar: 'undo redo | styleselect | bold italic | alignleft aligncenter alignright alignfull | bullist numlist |  outdent indent | link image media  | code | fullscreen'
		});
	}
}

function copyTextFromTinyMCEToTextarea(textarea) {
	if ($(textarea).length>0) {
		$(textarea).val(tinymce.get(textarea.substring(1).replace(new RegExp(/\\/, 'g'),'')).getContent());
	}
}