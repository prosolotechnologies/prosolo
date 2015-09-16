(function($) {
	var NewCommentPlugin = function (element, options) {
	
		var opts = $.extend({}, $.fn.newcomment.defaults, options);
		
		var core = {
			users: [],
		};
		
		var $this = $(element);
		var obj = this;
		
		core.activateUserSuggestion = function(text) {
        	core.userSuggestionsActive = true;
        };
        
        core.invokeUserSuggestSearch = function(text) {
        	if (text == null || text.length == 0) {
        		return;
        	}
        	
        	var userSearchQuery = text.substring(text.lastIndexOf('@') + 1, text.length);
        	
        	$this.find('.expandableInputBox .userSuggestions ' + opts.userSearchInput).val(userSearchQuery);
        	
        	if (userSearchQuery.length > 0) {
        		$this.find('.expandableInputBox .userSuggestions').show();
        		opts.userSearchAction();
        	} else {
        		$this.find('.expandableInputBox .userSuggestions').hide();
        	}
        };
        
        core.clearUserSuggestion = function() {
        	core.userSuggestionsActive = false;
        	$this.find('.expandableInputBox .userSuggestions').hide();
        	$this.find('.expandableInputBox .userSuggestions ' + opts.userSearchInput).val('');
        };

        var init = function() {
        	core.watermark = $this.find('.expandableInputBox .watermark');
			core.inputTextField = $this.find('.expandableInputBox .inputTextField');
			
			// set placehoders
			$this.find('.inactiveBox .watermark').html(opts.watermark);
			core.watermark.html(opts.watermark);
			
			core.inputTextField.attr('placeholder', opts.watermark);
			
			// hide some elements
			reset();
			
			$this.find('.inactiveBox').on('click', function (e) {
				e.stopPropagation();
				$(this).hide();
			
				var expandableInputBox = $(this).next('.expandableInputBox');
				expandableInputBox.toggleClass('collapsed').toggleClass('expanded');
				
				core.inputTextField.val('');
				core.inputTextField.focus();
				
				if (typeof opts.startEditing === 'function') opts.startEditing.call(this);
			});
			
			core.inputTextField.on('keyup', function(e) {
				if ($(this).html() == '' || $(this).html() == '<br>') {
					core.watermark.show();
				} else {
					core.watermark.hide();
				}
				
				// copy from the editable div to textfield
                $this.find('.expandableInputBox .inputTextFieldHidden').val($(this).html());
                
                window.clearInterval(core.textTimer);
                
                // check if user suggest should be activated
                var last = core.inputTextField.html().charAt(core.inputTextField.html().length - 1);
                
                var code = (e.keyCode ? e.keyCode : e.which);
                
                clearTimeout(core.suggestUserSearchTimeout);
                core.suggestUserSearchTimeout = setTimeout(function(){
                	 if (core.userSuggestionsActive) {
                     	if (code == 32 ) {
                     		core.clearUserSuggestion();
                     	} else {
                     		core.invokeUserSuggestSearch(core.inputTextField.html());
                     	}
                     }
				}, 200);
                
                if (last == '@') {
                	core.activateUserSuggestion(core.inputTextField.html());
                }
			}).on('paste', function(e) {
				if ($(this).html() == '' || $(this).html() == '<br>') {
					core.watermark.show();
				} else {
					core.watermark.hide();
				}
					
			    // strip all html tags except <br>
			    $(this).html(stripTagsExceptBr($(this).html()));

			    // copy from the editable div to textfield
			    $this.find('.expandableInputBox .inputTextFieldHidden').val($(this).html());
			}).on('click', function() {
				this.focus();
			});
			
			$this.find('.expandableInputBox .hideInputBox').on('click', function () {
				var parent = $(this).parent();
				
				if(parent.hasClass('expanded')) {
					close();
				}
			});
			
			$this.find('.expandableInputBox .postButtonBox .postButton, .expandableInputBox .postButtonBox .cancelButton').on('click', function () {
				close();
				
				if (typeof opts.stopEditing === 'function') opts.stopEditing.call(this);
			});
	
			function close() {
				$this.find('.expandableInputBox').toggleClass("collapsed").toggleClass('expanded');
				core.inputTextField.val('');
				core.watermark.show();
				$this.find('.inactiveBox').show();
				
				reset();
			}
			
			function reset() {
				//$this.find('.expandableInputBox .inputTextField').hide();
			}
        };
        
        this.selectSuggestedUser = function(elem) {
			var id = $(elem).attr('data-id');
			var name = $(elem).find('.infoContainer .name').html();
			
			// convert into a span
			var text = core.inputTextField.html();
			var indexOfAt = text.lastIndexOf('@');
			var newText = text.substring(0, indexOfAt);
			core.inputTextField.html(newText);
			
			var userSpanTag = $('<a />').attr({'class': 'userLink userLinkInline', 'data-id': id}).html(name);
			userSpanTag.click(function(e){
				e.stopPropagation();
			});
			core.inputTextField.append(userSpanTag);
			core.inputTextField.html(core.inputTextField.html() + '&nbsp;');
			
			// set cursor at the end
			setCaretAtEndOfEditableDiv(core.inputTextField[0]);
			
			// hide user suggestions
			core.clearUserSuggestion();
			
			// add to user array
			core.users.push(id);
			$this.find('.expandableInputBox .selectedUsers').val(returnAsCSV(core.users));
			
			// copy from the editable div to textfield
            $this.find('.expandableInputBox .inputTextFieldHidden').val($(this).html());
			
			return obj;
		};
        
		// actual code
		init();
	};
		
	$.fn.newcomment = function(options) {

		return this.each(function() {
			var element = $(this);
	          
			// Return early if this element already has a plugin instance
			if (element.data('newcomment')) return;
			
			// pass options to plugin constructor
			var newCommentPlugin = new NewCommentPlugin(this, options);
			
			// Store plugin object in this element's data
			element.data('newcomment', newCommentPlugin);
		});
	};
	
	$.fn.newcomment.defaults = {
		watermark: "Add a comment...",
		startEditing: $.noop,
		stopEditing: $.noop,
	};
	
})(jQuery);