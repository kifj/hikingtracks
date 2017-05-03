/*!
 * jQuery UI Image Carousel v1.0.0
 * http://www.ferretarmy.com/files/jQuery/ImageCarousel/ImageCarousel.html
 *
 * Copyright (c) 2009 Jon Neal
 * Dual licensed under the MIT and GPL licenses, using the same terms as jQuery.
 * Refer to http://docs.jquery.com/License
 *
 * Date: 2009-09-29 (Tue, 29 September 2009)
 * Revision: 1.0.0
 */
(function($) {

	$.fn.ImageCarousel = function(options) {

		// Options.
		var options = $.extend({}, $.fn.ImageCarousel.defaults, options);

		return this.each(function() {

			// Private variables.
			var gallery = $(this);
			var currentItem = 0;
			var currentPosition = parseInt($("ul", gallery).css("marginLeft")
			    .replace(/px/g, ""));

			// Support for metadata plugin.
			options = $.metadata ? $.extend({}, options, $(gallery).metadata())
			    : options;

			AddjQueryUIClassesToCarousel(gallery);
			SetupButtonBehavior(gallery);

			// Setup button behavior.
			$(".btnNext", gallery).click(
			    function(event) {
				    event.preventDefault();				    
				    if (AreImagesRemaining(gallery, currentItem)) {
					    currentPosition = currentPosition - $("li:eq(" + currentItem + ")", gallery).outerWidth();
					    currentItem = currentItem + 1;
					    MoveGallery(gallery, currentPosition, currentItem);
				    }
			    });

			$(".btnPrevious", gallery)
			    .click(
			        function(event) {
				        event.preventDefault();
				        if (currentItem > 0) {
					        currentPosition = currentPosition + $("li:eq(" + (currentItem - 1) + ")", gallery).outerWidth();
					        currentItem = currentItem - 1;
					        MoveGallery(gallery, currentPosition, currentItem);
				        }
			        });

			$(".btnHide", gallery).click(
			    function(event) {
				    event.preventDefault();
				    if ($(".carouselBody", gallery).css("display") == "none") {
					    $(".carouselHeader", gallery).removeClass("ui-corner-all").addClass("ui-corner-top");
				    } else {
					    // For some reason, the FG buttons don't hide until the slide is
							// complete in IE, so hide them
					    // first to prevent ugly UI behavior.
					    $(".carouselBody .btnPrevious, .carouselBody .btnNext", gallery).hide();
				    }

				    $(".carouselBody", gallery).slideToggle(
				        options.carousel_speed,
				        function() {
					        if ($(this).css("display") == "none") {
						        $(".carouselHeader", gallery).removeClass("ui-corner-top").addClass("ui-corner-all");
						        $(".btnHide", gallery).attr("title", "Show");
						        $(".btnHide span", gallery).removeClass("ui-icon-minus").addClass("ui-icon-plus");
					        } else {
						        $(".btnHide span").removeClass("ui-icon-plus").addClass("ui-icon-minus");
						        $(".btnHide", gallery).attr("title", "Hide");
						        $(".carouselBody .btnPrevious, .carouselBody .btnNext", gallery).show();
					        }
				        });
			    });

			gallery.css("width", options.carousel_width).append('<div style="clear: both; height: 0px;"></div>');
			$('ul', gallery).append('<div style="clear: both; height: 0px;"></div>');

			// Do this to assure the gallery images won't wrap to the next line, which
			// breaks the gallery. It's defaulted in CSS to a large
			// value, so it's pretty safe in any case.
			var imageListWidth = 0;
			$('li', gallery).each(
			    function(index) {
				    imageListWidth = imageListWidth + $('li:eq(' + index + ')').outerWidth();
			    });
			$('ul', gallery).outerWidth((imageListWidth * 2) + 'px');
		});

		function AddjQueryUIClassesToCarousel(gallery) {
			gallery.addClass("ui-widget");
			if (options.display_header) {
				$(".carouselHeader", gallery).addClass("ui-widget-header ui-corner-top ui-helper-clearfix");
				$(".carouselBody", gallery).addClass("ui-widget-content ui-corner-bottom").css("borderTop", "0px");

				$(".carouselHeader", gallery).html(
				    '<span class="headerTitle">' + $(".carouselHeader", gallery).html() + '</span>');

				if (options.allow_minimize) {
					$(".carouselHeader", gallery)
					    .append(
					        '<a href="#" class="fg-button ui-state-default fg-button-icon-solo ui-corner-all btnHide" title="Hide"><span class="ui-icon ui-icon-minus"></span></a>');
				}
			} else {
				$(".carouselHeader", gallery).hide();
				$(".carouselBody", gallery).addClass("ui-widget-content ui-corner-all");
			}
			$(".btnNext, .btnPrevious", gallery).addClass("fg-button ui-state-default fg-button-icon-solo ui-corner-all");
			$(".btnNext", gallery).prepend('<span class="ui-icon ui-icon-carat-1-e"></span>');
			$(".btnPrevious", gallery).prepend('<span class="ui-icon ui-icon-carat-1-w"></span>');
		}
		
		function AreImagesRemaining(gallery, currentItem) {
			var width = gallery.innerWidth();
			var remaining = 0;
			var count = $("li", gallery).size();
			for ( var i = currentItem; i < count; i++) {
				remaining = remaining + $($("li", gallery)[i]).outerWidth();
			}
			return (remaining > width);
		}

		function SetupButtonBehavior(gallery) {
			// All hover and click logic for buttons.
			// Adapted from
			// http://www.filamentgroup.com/lab/styling_buttons_and_toolbars_with_the_jquery_ui_css_framework/
			$(".fg-button:not(.ui-state-disabled)", gallery).hover(function() {
				$(this).addClass("ui-state-hover");
			}, function() {
				$(this).removeClass("ui-state-hover");
			}).mousedown(function() {
				$(this).addClass("ui-state-active");
			}).mouseup(function() {
				$(this).removeClass("ui-state-active");
			});

			$(".btnPrevious", gallery).addClass("ui-state-disabled");
			/*if (!AreImagesRemaining(gallery, 0)) {
				$(".btnNext", gallery).addClass("ui-state-disabled");
			}*/
		}
		;

		function MoveGallery(gallery, currentPosition, currentItem) {
			$("ul", gallery).stop().animate({
				marginLeft : currentPosition + "px"
			}, options.carousel_speed);

			if (currentItem == 0) {
				$(".btnPrevious", gallery).addClass("ui-state-disabled");
			} else {
				$(".btnPrevious", gallery).removeClass("ui-state-disabled");
			}

			if (!AreImagesRemaining(gallery, currentItem)) {
				$(".btnNext", gallery).addClass("ui-state-disabled");
			} else {
				$(".btnNext", gallery).removeClass("ui-state-disabled");
			}
		}
		;
	};

	$.fn.ImageCarousel.defaults = {
	  allow_minimize : true,
	  display_header : true,
	  carousel_speed : 'normal',
	  carousel_width : '500px'
	};

})(jQuery);