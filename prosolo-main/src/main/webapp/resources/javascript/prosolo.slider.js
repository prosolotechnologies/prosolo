var prosolo = $.extend({}, prosolo, prosolo || {});

prosolo.slider = {
	enable: function(sliderInput, sliderDiv, dis) {
		$(sliderDiv).slider({
			disabled: dis,
			animate: true,
			range: "min",
			value:10,
			min: 0,
			max: 100,
			step: 10,
			slide: function(event, ui) {
				if (!dis) {
					$(sliderInput).val(ui.value);
				}
			}
		});
		if (dis) {
			$(sliderDiv).slider( "value" , [$(sliderInput).html()] );
		} else {
			$(sliderDiv).slider( "value" , [$(sliderInput).val()] );
			
			$(sliderInput).change(function() {
				if (dis) {
					$(sliderDiv).slider( "value" , [$(this).html()] );
				} else {
					$(sliderDiv).slider( "value" , [$(this).val()] );
				}
			});
		}
	}
};