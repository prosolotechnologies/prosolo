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
			plugins: 'link media code image fullscreen paste lists placeholder',
			paste_as_text: true,
			menubar: false,
			statusbar: false,
			style_formats: [
	            { title: 'Heading 1', block: 'h1' },
	            { title: 'Heading 2', block: 'h2' },
	            { title: 'Heading 3', block: 'h3' }
            ],
            toolbar: 'undo redo | styleselect | bold italic | alignleft aligncenter alignright alignfull | bullist numlist |  outdent indent | link image media  | code | fullscreen',
			placeholder_attrs: {
				style: {
					position: 'absolute',
					top:'5px',
					left:0,
					color: '#888',
					padding: '1%',
					width:'98%',
					overflow: 'hidden',
					'white-space': 'pre-wrap',
					font: '400 14px/22px prosolo_regular, \'Helvetica Neue\', Helvetica, Arial, sans-serif',
					color: '#859095',
					opacity: '0.5'
				}
			}
		});
	}
}

function copyTextFromTinyMCEToTextarea(textarea) {
	if ($(textarea).length>0) {
		$(textarea).val(tinymce.get(textarea.substring(1).replace(new RegExp(/\\/, 'g'),'')).getContent());
	}
}