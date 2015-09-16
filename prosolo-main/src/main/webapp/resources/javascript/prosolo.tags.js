(function($) {
	$.fn.tags = function(options) {
		
		var opts = $.extend({}, $.fn.tags.defaults, options);

		return this.each(function() {
			// tags editor is already enabled 
			if ($(this).hasClass('jq_tags_editor_input'))
				return;
				
			var $this = this;
			
			var	editor = null,
				tags = [],
				sep = opts.separator,
				tagClass = opts.tagClass;
			
			var e = $this;

			var n = $($this).next();
			if ( /jq_tags_editor/.test(n.attr('class')) ){
				editor = n.get(0);
			}
			else{	
				var tagsEditorDiv = $('<div class="jq_tags_editor"></div>');
				$('<div class="jq_tags_tokens"></div>').appendTo(tagsEditorDiv);
				
				if (opts.editMode) {
					var o = {
			            maxWidth: 1000,
			            minWidth: 0,
			            comfortZone: 70
			        };
					
					tagsEditorDiv.css('cursor', 'text').addClass('edit');
					var inputTags = $('<input type="text" class="jq_tags_editor_input"/>');
					
	                inputTags.appendTo(tagsEditorDiv);
				} else {
					tagsEditorDiv.addClass('read');
				}
				$($this).after(tagsEditorDiv);
				editor = $($this).next();
			}

			$($this).hide();
			
			$(editor)
				.unbind()
				.click(function(){
					$(editor).find('input').focus();
				})
				.find('input')
					.unbind()
					.blur(function(){
						add_tag();
					})
					.keypress(function(e){
						switch(e.which){
							case 13:	// Return is pressed
							case sep.charCodeAt(0): // separator is pressed
								e.preventDefault();
								add_tag();
								break;
						}
					}).keydown(function(e){
						var key = e.keyCode || e.charCode;
						
						if (key == 8 || key == 46) {
							var tag_txt = $(editor).find('input')
								.val().replace( new RegExp(sep, 'gi'), '');
							
							tag_txt = $.trim(tag_txt);
						
							if (tag_txt.length == 0) {
								var lastTag = tags[tags.length-1];
								
								if (lastTag)
									remove_tag(lastTag);
							}
						}
					});

			var inputText = $($this).val();
			
			// checks whether there is some other character but space
			if (inputText != "" && /\S/.test(inputText)) {
				var r = inputText.split( sep );
				tags = [];
				
				for (var i=0; i<r.length; i++){
					var t = r[i].replace( new RegExp(sep, 'gi'), '');
					if (t != '') {
						tags.push($.trim(t));
					}
				}
			}
			refresh_list();
			
			function add_tag(){
				var tag_txt = $(editor).find('input')
					.val().replace( new RegExp(sep, 'gi'), '');
				tag_txt = $.trim(tag_txt);

				if ((tag_txt != '') && (jQuery.inArray(tag_txt, tags) < 0) ){
					tags.push(tag_txt);
					refresh_list();

					opts.afterUpdateCallback($this);
				}
				$(editor).find('input').val('');
			}
			
			function remove_tag(tag_txt){
				var r = [];
				for (var j=0; j < tags.length; j++){
					var t = tags[j];
					
					if (opts.prefix != '') {
						t = opts.prefix + t;
					}
					
					if (t != tag_txt){
						r.push(tags[j]);
					}
				}
				
				tags = r;
				refresh_list();
				
				opts.afterUpdateCallback($this);
			}
			
			function refresh_list(){
				var editorElem = $(editor);
				var tokensContainer = editorElem.find('div.jq_tags_tokens');
				
				editorElem.find('div.jq_tags_tokens').html('');
				$($this).val(tags.join(sep + ' '));

				for (var i=0; i<tags.length; i++){
					var t = tags[i];
					if (!jQuery.isFunction(t)) {
						var elem = $('<'+opts.tag+'/>', {
							class: tagClass,
						})
						
						if (opts.prefix != '') {
							elem.text(opts.prefix+t);
						} else {
							elem.text(t);
						}
						
						if (opts.editMode) {
							var linkElem = $('<a/>');
							linkElem.text('x');
							
							linkElem.click(function(){
								var tag_txt = $(this).parents('.jq_tags_token:first').html().replace(/<a(.*?)<\/a>/i, '');
								remove_tag(tag_txt);
								return false;
							});
							
							elem.append(linkElem);
						}

						opts.onClick(elem, t);
						
						tokensContainer.append(elem);
					}
				}
				editorElem.find('input').val('');
			}
		});
	};
	
	$.fn.tags.defaults = {
		separator: ',',
		editMode: true,
		add: $.noop,
		remove: $.noop,
		tag: 'div',
		tagClass: "jq_tags_token",
		prefix: '',
		onClick: $.noop, // called once for each hashtag
		afterUpdateCallback: $.noop, // called only once after hashtags have been updated
	};
})(jQuery);