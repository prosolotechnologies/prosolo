var prosolo = $.extend({}, prosolo, prosolo || {});

prosolo.bigTooltipControl = {
	create: function(linkContainer, linkSelector, refreshCommandSelector, tooltipContentDiv, tooltipPosition, loaderPath, userId) {
		function getOppositePosition(position) {
			if (position == 'bottom') {
				return 'top';
			} else if (position == 'top') {
				return 'bottom';
			} else if (position == 'left') {
				return 'right';
			} else if (position == 'right') {
				return 'left';
			}
		}
		
		var timeout, timeout1;
		
		var newInstance = {
			mouseInside: false,	
			displayed: false,
			linkContainer: linkContainer,
			linkSelector: linkSelector,
			refreshCommandSelector: refreshCommandSelector,
			show: function() {
				if (this.mouseInside && !this.displayed) {
					$(linkContainer+' '+linkSelector).qtip('option', 'content.text', $(tooltipContentDiv).html()).qtip('show');
				}
			}
		};
		
		$(linkContainer+' '+linkSelector).qtip({
			content: {
				text: " ",
				title: {
					text: "",
					button: false
				}
			},
			position: {
				at: tooltipPosition + ' center', // Position the tooltip above the link
				my: getOppositePosition(tooltipPosition) + ' center',
				viewport: $(window), // Keep the tooltip on-screen at all times
				effect: false
			},
			show: {
				solo: true,
				delay: 300
			},
			hide: {
				when: {
					event:'mouseout'
				}, 
				fixed: true, 
				delay: 500
			},
			style: {
				classes: 'ui-tooltip-wiki ui-tooltip-light ui-tooltip-rounded ui-tooltip-shadow ui-tooltip-width-unbounded no-close-button'
			},
		});
		
		$(linkContainer).hover(
			function() {
				timeout = window.setTimeout(
					function(){
						$(linkContainer+' '+linkSelector).qtip('option', 'content.text', '<img src="'+loaderPath+'" style="vertical-align:middle;"/>').qtip('show');
						newInstance.mouseInside = true;
						$(linkContainer+' '+refreshCommandSelector).trigger('click');
						sendServiceUse('USER_DIALOG', {context: $(linkContainer+' '+linkSelector).attr('context'), userId: userId});
					}, 300);
			},
			function(){
    			newInstance.mouseInside = false;
    			window.clearTimeout(timeout);
			}
		);
		
		return newInstance;
	}
};
