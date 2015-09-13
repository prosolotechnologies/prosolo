/**
 * prosolo.autosaveForm v1.0
 * A plugin that calls a function that should autosave after every change
 * @author Nikola Milikic <nikola.milikic@gmail.com>
 *
 * Copyright (c) Nikola Milikic - released under MIT License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:

 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.

 */

(function($) {

    $.widget("prosolo.autosaveForm", {

        options: {
        	elemSelector: '',			// selector of elements to apply autosaveFunction for
            autosaveFunction: $.noop,	// function to be called to save the form. Usually, this will call some backend service that performs saving.
            loaderPath: '',
            loaderClass: 'autosaveLoader',
        },

        _create: function() {
        	var self = this;
        	
            $(this.options.elemSelector).each(function() {
            
				if (this.nodeName.toLowerCase() === 'input' ) {
					if (this.type.toLowerCase() === 'text' ) {
						$(this).focus( function() {
							$(this).data('value', $(this).val());
						}).blur( function() {
							var newVal = $(this).val(),
								oldVal = $(this).data('value');
			
							if (oldVal != newVal) {
								self.addLoader(this);
								self.autosave();
							}
						});
					} else if (this.type.toLowerCase() === 'checkbox' ) {
						$(this).click(function() {
						    var $this = $(this);
						    
						    self.addLoaderNextAndAutosave(this);
						});
					}
				} else if (this.nodeName.toLowerCase() === 'textarea' ) {
					$(this).focus( function() {
						$(this).data('value', $(this).val());
					}).blur( function() {
						var newVal = $(this).val(),
							oldVal = $(this).data('value');
		
						if (oldVal != newVal) {
							self.addLoader(this);
							self.autosave();
						}
					});
				}
            })
        },
        
        autosave: function() {
        	this.options.autosaveFunction();
        },
        
        addLoader: function(elem, loaderClass) {
        	$(elem).after('<img src="' + this.options.loaderPath + '" class="'+this.options.loaderClass + ' ' +loaderClass+'" />');
		},
		
		addLoaderAndAutosave: function(elem) {
			this.addLoader(elem, '');
			this.autosave();
		},
		
		addLoaderNextAndAutosave: function(elem) {
			this.addLoader(elem, 'nextToElem');
			this.autosave();
		},
		
		removeAutosaveLoader: function() {
			$('.' + this.options.loaderClass).remove();
		},
		
        element: function() {
        	return this.element;
        }

    });

})(jQuery);