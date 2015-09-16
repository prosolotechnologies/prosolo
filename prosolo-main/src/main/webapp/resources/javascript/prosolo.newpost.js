function stripTags(text) {
	return text.replace(/<\/?[^>]+>/gi, '');
};

(function($) {
	var NewPostPlugin = function (element, options) {
		// Merge options with defaults
		var opts = $.extend({}, $.fn.newpost.defaults, options || {});
		var $this = $(element);
		var obj = this;
		
		var core = {
			users: [],
		};
		
		core.textUpdate = function(text) {                
            // read all links 
            var links = $.urlHelper.getLinks(text);
            core.cleanDuplicates(links);
            
            if (links != null) {
                if (core.selectedLink == null) {
                    core.selectedLink = links[0];
                    
                    $this.find('.expandableInputBox .linkInput').val(core.selectedLink);
                    $this.find('.expandableInputBox .linkContainer .addLinkBtn').trigger('click');
                }
            }
        };
        
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
        
        core.cleanDuplicates = function(urlArray) {
            var links = core.createLinks(urlArray);
            
            for(var index in core.already) {
                var strUrl  = core.already[index];
                  
                if (!core.isDuplicate(strUrl, links)){
                    var index = $.inArray(strUrl, core.already);
                    core.already.splice(index, 1);
                }
            }
        };
        
        core.isDuplicate = function(url, array) {
            var duplicate = false;
            $.each(array, function(key, val) {
                if (val == url) {
                    duplicate = true;
                } 
            }); 
            
            return duplicate;
        };
        
        core.createLinks = function(urlArray) {
            var links = [];
            
            for(var index in urlArray) {
                var strUrl  = urlArray[index];
                if (typeof strUrl == 'string') {
                    var pLink   = new $.urlHelper.UriParser(strUrl);
                   
                    if (pLink.subdomain && pLink.subdomain.length > 0 || 
                        pLink.protocol  && pLink.protocol.length  > 0 ) {
                            
                        if  (pLink.protocol.length == 0) {
                            strUrl = opts.defaultProtocol + strUrl;
                        }
                        links.push(strUrl);
                    }
                }
            }
            return links;
        };
		
		var init = function() {
			core.watermark = $this.find('.expandableInputBox .watermark');
			core.watermarkTitle = $this.find('.expandableInputBox .watermarkTitle');
			core.inputTextFieldTitle = $this.find('.expandableInputBox .inputTextFieldTitle');
			core.inputTextField = $this.find('.expandableInputBox .inputTextField');
			
			// set placehoders
			if (opts.watermarkTitle) {
				$this.find('.inactiveBox .watermark').html(opts.watermarkTitle);
				core.watermarkTitle.html(opts.watermarkTitle);
			} else {
				$this.find('.inactiveBox .watermark').html(opts.watermark);
			}
			
			core.watermark.html(opts.watermark);
			
			// hide some elements
			reset();
			
			// adding event handlers
			$this.find('.inactiveBox').on('click', function (e) {
				obj.expandPostInput();
			});
			
			core.inputTextFieldTitle.focus(function(){
				if($(this).html() == '') {
					core.watermarkTitle.hide();
			    }
			}).blur(function() {
				if($(this).html() == '') {
					core.watermarkTitle.show();
			    }
			}).on('keyup', function(e) {
				// copy from the editable div to textfield
				$this.find('.expandableInputBox .inputTextFieldTitleHidden').val($(this).html());
			});
			
			// events on inputTextField
			core.inputTextField.focus(function(){
				if ($(this).html() == '') {
					core.watermark.hide();
			    }
			}).blur(function() {
				if ($(this).html() == '') {
					core.watermark.show();
			    }
			}).keydown(function(e) {
				// disable user suggestions id space or enter are pressed
				if (e.keyCode == 13 || e.keyCode == 32) { //Enter or space keycode
					core.clearUserSuggestion();
                }
				
			    // trap the return key being pressed
			    if (e.keyCode === 13) {
			      // insert 2 br tags (if only one br tag is inserted the cursor won't go to the next line)
			      document.execCommand('insertHTML', false, '<br/><br/>');
			      // prevent the default behaviour of return key pressed
			      return false;
			    }
			}).on('keyup', function(e) {
                window.clearInterval(core.textTimer);
                
                if (core.watermark.attr('display') != 'none') {
                	core.watermark.hide();
                }
                
                // copy from the editable div to textfield
                $this.find('.expandableInputBox .inputTextFieldHidden').val($(this).html());
                
                var code = (e.keyCode ? e.keyCode : e.which);
                
                if (code == 13 || code == 32) { //Enter or space keycode
                    core.textUpdate(core.inputTextField.html());
                }
                
                // check if user suggest should be activated
                var last = $(this).html().charAt($(this).html().length - 1);
                
                var $inputTextField = $(this);
                
                clearTimeout(core.suggestUserSearchTimeout);
                core.suggestUserSearchTimeout = setTimeout(function(){
                	 if (core.userSuggestionsActive) {
                     	if (code == 32 ) {
                     		core.clearUserSuggestion();
                     	} else {
                     		core.invokeUserSuggestSearch($inputTextField.html());
                     	}
                     }
				}, 200);
                
                if (last == '@') {
                	core.activateUserSuggestion($inputTextField.html());
                }
			}).on('paste', function(e) {
				var inputTextField = $(this);
				
				setTimeout(function () {
					inputTextField.focus();
					
					if (core.watermark.attr('display') != 'none') {
						core.watermark.hide();
	                }
				    // strip all html tags except <br>
				    $this.find('.expandableInputBox .inputTextField').html(stripTagsExceptBr($this.find('.expandableInputBox .inputTextField').html()));

				    // copy from the editable div to textfield
				    $this.find('.expandableInputBox .inputTextFieldHidden').val($this.find('.expandableInputBox .inputTextField').html());
				    
				    if (e.originalEvent.clipboardData) {
				    	core.textUpdate(inputTextField.html());
				    }
				}, 150);
			});

			// when clicking on a watermark, propagate focus to the editable div
			$this.find('.expandableInputBox .watermarkTitle, .expandableInputBox .watermark').click(function(){
				$(this).next().focus();
			});

			// process inputed text after every keyup to detect links
			$this.find('.expandableInputBox .linkInput').on('keyup', function(e) {
				$this.find('.expandableInputBox .linkInput').html(jQuery($this.find('.expandableInputBox .linkInput').html()).text());
                
                var code = (e.keyCode ? e.keyCode : e.which);
                
                if (code == 13 || code == 32) { //Enter or space keycode
                    core.textUpdate($this.find('.expandableInputBox .linkInput').val());
                }
			}).bind('paste', function(e) {
				setTimeout(function () {
				    if (e.originalEvent.clipboardData){
				    	core.textUpdate($this.find('.expandableInputBox .linkInput').val());
				    }
				}, 10);
			});
			
			
			$this.find('.expandableInputBox .hideInputBox').on('click', function () {
				if($this.find('.expandableInputBox').hasClass('expanded')) {
					obj.close();
				}
			});
			
			$this.find('.expandableInputBox .actionsContaioner .showLinkForm').on('click', function () {
				resetActionContainerForm();
				
				$this.find('.expandableInputBox .actionsContaioner').hide();
				$this.find('.expandableInputBox .linkContainer').show();
				$this.find('.expandableInputBox .linkInput').focus();
			});
			
			$this.find('.expandableInputBox .linkContainer .hideLinkForm').on('click', function () {
				$this.find('.expandableInputBox .linkContainer').hide();
				$this.find('.expandableInputBox .actionsContaioner').show();
				core.selectedLink = null;
				reset();
			});
			
			$this.find('.expandableInputBox .linkContainer .addLinkBtn').on('click', function () {
				core.selectedLink = null;
				obj.showLinkLoader();
			});
			
			$this.find('.expandableInputBox .actionsContaioner .browseFile').on('click', function () {
				$this.find('.expandableInputBox .uploadContainer').find('.browseFile').trigger('click');
			});

			$this.find('.expandableInputBox .postButtonBox .postButton').on('click', function () {
				$this.find('.expandableInputBox .inputTextFieldTitle').val('');
				$this.find('.expandableInputBox .inputTextField').val('');
				obj.close();
			});
			
			$this.find('.expandableInputBox .uploadDetails .hideUploadDetails').on('click', function () {
				$this.find('.expandableInputBox .uploadDetails').hide();
			});
		};
		
		this.hidePostButton = function() {
			$this.find('.expandableInputBox .postButtonBox .postButton').hide();
			
			return obj;
		};
		
		this.selectSuggestedUser = function(elem) {
			var id = $(elem).attr('data-id');
			var name = $(elem).find('.infoContainer .title').html();
			
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
		    $this.find('.expandableInputBox .inputTextFieldHidden').val($this.find('.expandableInputBox .inputTextField').html());
			
			return obj;
		};
		
		this.hideLinkAndUploadButtons = function() {
			$this.find('.expandableInputBox .postButtonBox .actionsContaioner').hide();
			
			return obj;
		};
		
		this.expandPostInput = function() {
			$this.find('.inactiveBox').hide();
			$this.find('.expandableInputBox').removeClass('collapsed').addClass('expanded');
			
			if ($this.find('.expandableInputBox .inputTextFieldTitle').length > 0) {
				$this.find('.expandableInputBox .inputTextFieldTitle').focus();
			} else {
				$this.find('.expandableInputBox .inputTextField').focus();
			}
			//$this.find('.expandableInputBox .inputTextField').text($this.find('.expandableInputBox .inputTextFieldHidden').text()).focus();
			reset();
			return obj;
		};
		
		this.showInputTextLoader = function() {
			$this.find('.expandableInputBox .inputTextField').css('text-align', 'center');
			$this.find('.expandableInputBox .inputTextField').html('<img src="resources/images/style/ajax-loader-black.gif" class="loader"/>').blur();
			return obj;
		};
		
		this.detectLinks = function() {
			var inputText = $($this.find('.expandableInputBox .inputTextField').get(0));
			core.textUpdate(inputText.html());
			return obj;
		};
		
		this.afterAddLinkBtnCallback = function() {
			var linkUrlFull = $this.find('.expandableInputBox .linkPreview .actUrlFull').first().children('input').first().val();
			if (linkUrlFull && linkUrlFull.length > 0) {
				core.selectedLink = linkUrlFull;
			}
			return obj;
		};

		this.close = function() {
			$this.find('.expandableInputBox').addClass("collapsed").removeClass('expanded');
			$this.find('.expandableInputBox .inputTextField').html('');
			$this.find('.expandableInputBox .inputTextFieldTitle').html('');
			$this.find('.expandableInputBox .watermark').show();
			$this.find('.inactiveBox').show();
			
			reset();
			return obj;
		};
		
		this.showLinkPreview = function() {
			$this.find('.expandableInputBox .linkPreview').show();
			return obj;
		};
		
		this.hideLinkPreview = function() {
			$this.find('.expandableInputBox .linkPreview').hide();
			core.selectedLink = null;
			return obj;
		};
		
		this.showLinkLoader = function() {
			$this.find('.expandableInputBox .linkPreview').css('text-align', 'center');
			$this.find('.expandableInputBox .linkPreview').html('<img src="resources/images/style/ajax-loader-black.gif" class="loader"/>');
			$this.find('.expandableInputBox .linkPreview').show();
			return obj;
		};
		
		this.showUploadLoader = function() {
			$this.find('.expandableInputBox .uploadDetails').css('text-align', 'center');
			$this.find('.expandableInputBox .uploadDetails').html('<img src="resources/images/style/ajax-loader-black.gif" class="loader"/>');
			$this.find('.expandableInputBox .uploadDetails').show();
			return obj;
		};

		this.hideLoader = function() {
			var laoderContainer = $this.find('.expandableInputBox .loaderContainer');
			laoderContainer.hide();
			laoderContainer.remove();
			laoderContainer.css('display', 'none');
			return obj;
		};
		
		var resetActionContainerForm = function() {
			$this.find('.expandableInputBox .actionsContaioner').show();
			$this.find('.expandableInputBox .linkContainer').hide();
			$this.find('.expandableInputBox .uploadContainer').hide();
			$this.find('.expandableInputBox .linkInput').val('');
			
			//clear input fields
			$this.find('.expandableInputBox .uploadDetails .attachContent .actInfo .atcTitle').val('');
			$this.find('.expandableInputBox .uploadDetails .attachContent .actInfo .atcDesc').val('');
			//$this.find('.expandableInputBox .inputTextField').val('');
			return obj;
		};
		
		var reset = function() {
			resetActionContainerForm();
			$this.find('.expandableInputBox .linkPreview').hide();
			$this.find('.expandableInputBox .uploadDetails').hide();
			//$this.find('.expandableInputBox .inputTextFieldHidden').hide();
			core.selectedLink = null;
			return obj;
		};
		
		// actual code
		init();
   };
	
	$.fn.newpost = function(options) {

		return this.each(function() {
			var element = $(this);
	          
			// Return early if this element already has a plugin instance
			if (element.data('newpost')) return;
			
			// pass options to plugin constructor
			var newPostPlugin = new NewPostPlugin(this, options);
			
			// Store plugin object in this element's data
			element.data('newpost', newPostPlugin);
		});
	};
	
	$.fn.newpost.defaults = {
		watermark: "What are you working on",
		defaultProtocol: 'http://',
		hideActions: false,
	};
	
	
	jQuery.urlHelper = {
        UriParser :  function (uri) { 
            this._regExp      = /^((\w+):\/\/\/?)?((\w+):?(\w+)?@)?([^\/\?:]+)?(:\d+)?(.*)?/;
            this._regExpHost  = /^(.+\.)?(.+\..+)$/;
   
            this._getVal = function(r, i) {
                if(!r) return null;
                return (typeof(r[i]) == 'undefined' ? "" : r[i]);
            };
          
            this.parse = function(uri) {
                var r          = this._regExp.exec(uri);
                this.results   = r;
                this.url       = this._getVal(r,1);
                this.protocol  = this._getVal(r,2);
                this.username  = this._getVal(r,4);
                this.password  = this._getVal(r,5);
                this.domain    = this._getVal(r,6);
                this.port      = this._getVal(r,7);
                this.path      = this._getVal(r,8);
                
                var rH         = this._regExpHost.exec( this.domain );
                this.subdomain = this._getVal(rH,1);
                this.domain    = this._getVal(rH,2); 
                return r;
            };
              
            if(uri) this.parse(uri);
        },
        getLinks : function(text) {
            var expression = /(https?:\/\/?[\w-]+(\.[\w-]+)+\.?(:\d+)?(\/\S*)?)/gi;
            return (text.match(expression));
        },
        isImage : function(img, allowed) {
            //Match jpg, gif or png image  
            if (allowed == null)  allowed = 'jpg|gif|png|jpeg';
            
            var expression = /([^\s]+(?=\.(jpg|gif|png|jpeg))\.\2)/gm; 
            return (img.match(expression));
        },
        isAbsolute : function(path) {
            var expression = /^(https?:)?\/\//i;
            var value =  (path.match(expression) != null) ? true: false;
                            
            return value;
        },
        isPathAbsolute : function(path) {
            if (path.substr(0,1) == '/') return true;
        },
        hasParam : function(path) {
             return (path.lastIndexOf('?') == -1 ) ? false : true;
        },
        stripFile : function(path) {
            return path.substr(0, path.lastIndexOf('/') + 1);
        } 
    };
})(jQuery);