function stripTags(text) {
	return text.replace(/<\/?[^>]+>|&nbsp;/gi, '');
};

function stripTagsExceptBr(html) {
	var html = html.replace("<br>","||br||");  
	var tmp = document.createElement("DIV");
	tmp.innerHTML = html;
	html = tmp.textContent||tmp.innerText;
	return html.replace("||br||","<br>");  
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
		
		core.resolvePostButtonStatus = function() {
			if (stripTags(core.inputTextField.html()).trim() == '' && !core.fileUploaded && !core.linkPreviewSet) {
				$this.find(opts.postButtonEnabledSelector).hide();
				$this.find(opts.postButtonDisabledSelector).show();
			} else {
				$this.find(opts.postButtonDisabledSelector).hide();
				$this.find(opts.postButtonEnabledSelector).show();
			}
		};
		
		core.textUpdate = function(text) {  
            // read all links 
            var links = $.urlHelper.getLinks(text);
       
            if (links != null) {
                if (!core.fileUploaded && !core.linkPreviewSet) {
                    core.selectedLink = links[links.length - 1];
                    $this.find(opts.urlFieldSelector).val(core.selectedLink);
                    $this.find(opts.addUrlButtonSelector).trigger('click');
                }
            }
        };
        	
		var init = function() {
			core.postOptions = $this.find(opts.postOptionsSelector);
			core.inputTextField = $this.find(opts.editableDivSelector);
			core.inputHiddenTextarea = $this.find(opts.textAreaSelector);
			$(document).click(function() {
				if (core.inputTextField.html() == '' && !core.fileUploaded && !core.linkPreviewSet) {
					core.postOptions.hide();
				}
			});
			$(opts.newPostContainerSelector).click(function(ev) {
				ev.stopPropagation();
			});
			$(opts.uploadFileModalSelector).click(function(ev) {
				ev.stopPropagation();
			});
			// hide some elements
			//reset();
			
			// events on inputTextField
			core.inputTextField.focus(function(){
				if ($(this).html() == '') {
					core.postOptions.show();
			    }
			}).on('keyup', function(e) {
                //window.clearInterval(core.textTimer);           
                // copy from the editable div to textfield
				core.inputHiddenTextarea.text($(this).html());
                
                var code = (e.keyCode ? e.keyCode : e.which);
                
                if (code == 13 || code == 32) { //Enter or space keycode
                    core.textUpdate(core.inputTextField.html());
                }  
                core.resolvePostButtonStatus();
			}).on('paste', function(e) {
				var inputTextField = $(this);
				
				setTimeout(function () {
					inputTextField.focus();
				    // strip all html tags except <br>
				    $this.find(opts.editableDivSelector).html(stripTagsExceptBr(inputTextField.html()));

				    // copy from the editable div to textfield
				    core.inputHiddenTextarea.val(inputTextField.html());
				    
				    if (e.originalEvent.clipboardData) {
				    	core.textUpdate(inputTextField.html());
				    }
				}, 150);
			});
			
			$this.find(opts.addUrlButtonSelector).on('click', function () {
				obj.showLinkLoader();
			});

			// process inputed text after every keyup to detect links
//			$this.find('.expandableInputBox .linkInput').on('keyup', function(e) {
//				$this.find('.expandableInputBox .linkInput').html(jQuery($this.find('.expandableInputBox .linkInput').html()).text());
//                
//                var code = (e.keyCode ? e.keyCode : e.which);
//                
//                if (code == 13 || code == 32) { //Enter or space keycode
//                    core.textUpdate($this.find('.expandableInputBox .linkInput').val());
//                }
//			}).bind('paste', function(e) {
//				setTimeout(function () {
//				    if (e.originalEvent.clipboardData){
//				    	core.textUpdate($this.find('.expandableInputBox .linkInput').val());
//				    }
//				}, 10);
//			});
			
			
//			$this.find('.expandableInputBox .hideInputBox').on('click', function () {
//				if($this.find('.expandableInputBox').hasClass('expanded')) {
//					obj.close();
//				}
//			});
//			
//			$this.find('.expandableInputBox .actionsContaioner .showLinkForm').on('click', function () {
//				resetActionContainerForm();
//				
//				$this.find('.expandableInputBox .actionsContaioner').hide();
//				$this.find('.expandableInputBox .linkContainer').show();
//				$this.find('.expandableInputBox .linkInput').focus();
//			});
//			
//			$this.find('.expandableInputBox .linkContainer .hideLinkForm').on('click', function () {
//				$this.find('.expandableInputBox .linkContainer').hide();
//				$this.find('.expandableInputBox .actionsContaioner').show();
//				core.selectedLink = null;
//				reset();
//			});
//			
//			$this.find('.expandableInputBox .linkContainer .addLinkBtn').on('click', function () {
//				core.selectedLink = null;
//				obj.showLinkLoader();
//			});
//			
//			$this.find('.expandableInputBox .actionsContaioner .browseFile').on('click', function () {
//				$this.find('.expandableInputBox .uploadContainer').find('.browseFile').trigger('click');
//			});
//
//			$this.find('.expandableInputBox .postButtonBox .postButton').on('click', function () {
//				$this.find('.expandableInputBox .inputTextFieldTitle').val('');
//				$this.find('.expandableInputBox .inputTextField').val('');
//				obj.close();
//			});
//			
//			$this.find('.expandableInputBox .uploadDetails .hideUploadDetails').on('click', function () {
//				$this.find('.expandableInputBox .uploadDetails').hide();
//			});
		};
		
		this.hideLinkPreview = function() {
			core.linkPreviewSet = false;
			core.fileUploaded = false;
			return obj;
		};
		
		this.showLinkLoader = function() {
			showLoader($this.find(opts.linkLoaderContainerSelector));
			return obj;
		};

		this.hideLinkLoader = function() {
			hideLoader($this.find(opts.linkLoaderContainerSelector));
			return obj;
		};
		
		this.afterAddLinkBtnCallback = function(linkPreviewInitialized) {
			this.hideLinkLoader();
			core.linkPreviewSet = linkPreviewInitialized;
			return obj;
		};
		
		this.afterUploadFileBtnCallback = function(filePreviewInitialized) {
			core.fileUploaded = filePreviewInitialized;
			if(filePreviewInitialized) {
				core.linkPreviewSet = false;
			}
			
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
            var expression = /((https?:\/\/?)?[\w-]+(\.[\w-]+)+\.?(:\d+)?(\/\S*)?)/gi;
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