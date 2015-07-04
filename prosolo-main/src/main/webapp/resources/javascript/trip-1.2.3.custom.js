/*
 *  Trip.js - A jQuery plugin that can help you customize your tutorial trip easily
 *  Version : 1.2.3
 *
 *  Author : EragonJ <eragonj@eragonj.me>
 *  Blog : http://eragonj.me
 *
 *  Customized by Nikola Milikic. Code inside 'bindResizeEvents' function is commented out as it reruns the trip on screen resize.
 */
(function (e, t) {
    function r(t, n) {
        var r, i, s = n;
        this.pause = function () {
            e.clearTimeout(r);
            s -= new Date - i;
        };
        this.resume = function () {
            i = new Date;
            r = e.setTimeout(t, s);
            return s;
        };
        this.stop = function () {
            e.clearTimeout(r);
        };
        this.resume();
    }
    var n = function (n, r) {
        this.settings = t.extend({
            tripIndex: 0,
            tripTheme: "black",
            backToTopWhenEnded: false,
            overlayZindex: 99999,
            delay: 1e3,
            enableKeyBinding: true,
            showCloseBox: false,
            skipUndefinedTrip: false,
            showNavigation: false,
            canGoNext: true,
            canGoPrev: true,
            nextLabel: "Next",
            prevLabel: "Back",
            finishLabel: "Dismiss",
            closeBoxLabel: "&#215;",
            onTripStart: t.noop,
            onTripEnd: t.noop,
            onTripStop: t.noop,
            onTripChange: t.noop
        }, r);
        this.tripData = n;
        this.$tripBlock = null;
        this.$overlay = null;
        this.$bar = null;
        this.$root = t("body, html");
        this.tripIndex = this.settings.tripIndex;
        this.tripDirection = "next";
        this.timer = null;
        this.progressing = false;
        this.hasExpose = false;
        this.CONSTANTS = {
            LEFT_ARROW: 37,
            UP_ARROW: 38,
            RIGHT_ARROW: 39,
            DOWN_ARROW: 40,
            ESC: 27,
            SPACE: 32
        };
        this.console = e.console || {};
    };
    n.prototype = {
        preInit: function () {
            if (typeof this.console === "undefined") {
                var e = this,
                    n = ["log", "warn", "debug", "info", "error"];
                t.each(n, function (n, r) {
                    e.console[r] = t.noop;
                });
            }
        },
        showExpose: function (e) {
            this.hasExpose = true;
            var t, n;
            t = {
                position: e.css("position"),
                zIndex: e.css("z-Index")
            };
            n = {
                position: "relative",
                zIndex: this.settings.overlayZindex + 1
            };
            e.data("trip-old-css", t).css(n).addClass("trip-exposed");
            this.$overlay.show();
        },
        hideExpose: function () {
            this.hasExpose = false;
            var e = t(".trip-exposed"),
                n = e.data("trip-old-css");
            e.css(n).removeClass("trip-exposed");
            this.$overlay.hide();
        },
        bindResizeEvents: function () {
            var n = this;
            t(e).on("resize", function () {});
        },
        bindKeyEvents: function () {
            var e = this;
            t(document).on({
                "keydown.Trip": function (t) {
                    e.keyEvent.call(e, t);
                }
            });
        },
        unbindKeyEvents: function () {
            t(document).off("keydown.Trip");
        },
        keyEvent: function (e) {
            switch (e.which) {
            case this.CONSTANTS["ESC"]:
                this.stop();
                break;
            case this.CONSTANTS["SPACE"]:
                e.preventDefault();
                this.pause();
                break;
            case this.CONSTANTS["LEFT_ARROW"]:
            case this.CONSTANTS["UP_ARROW"]:
                this.prev();
                break;
            case this.CONSTANTS["RIGHT_ARROW"]:
            case this.CONSTANTS["DOWN_ARROW"]:
                this.next();
                break;
            }
        },
        stop: function () {
            if (this.timer) {
                this.timer.stop();
            }
            if (this.hasExpose) {
                this.hideExpose();
            }
            this.tripIndex = this.settings.tripIndex;
            this.hideTripBlock();
            this.settings.onTripStop();
        },
        pauseAndResume: function () {
            if (this.progressing) {
                this.timer.pause();
                this.pauseProgressBar();
            } else {
                var e = this.timer.resume();
                this.resumeProgressBar(e);
            }
            this.progressing = !this.progressing;
        },
        pause: function () {
            this.pauseAndResume();
        },
        resume: function () {
            this.pauseAndResume();
        },
        next: function () {
            this.tripDirection = "next";
            if (!this.canGoNext()) {
                return this.run();
            }
            if (this.hasCallback()) {
                this.callCallback();
            }
            if (this.isLast()) {
                this.doLastOperation();
            } else {
                this.increaseIndex();
                this.run();
            }
        },
        prev: function () {
            this.tripDirection = "prev";
            if (!this.isFirst() && this.canGoPrev()) {
                this.decreaseIndex();
            }
            this.run();
        },
        showCurrentTrip: function (e) {
            if (typeof e.sel === "string") {
                e.sel = t(e.sel);
            }
            if (this.timer) {
                this.timer.stop();
            }
            if (this.hasExpose) {
                this.hideExpose();
            }
            if (this.progressing) {
                this.hideProgressBar();
                this.progressing = false;
            }
            this.setTripBlock(e);
            this.showTripBlock(e);
            if (e.expose) {
                this.showExpose(e.sel);
            }
        },
        doLastOperation: function () {
            if (this.timer) {
                this.timer.stop();
            }
            if (this.settings.enableKeyBinding) {
                this.unbindKeyEvents();
            }
            this.hideTripBlock();
            if (this.hasExpose) {
                this.hideExpose();
            }
            if (this.settings.backToTopWhenEnded) {
                this.$root.animate({
                    scrollTop: 0
                }, "slow");
            }
            this.tripIndex = this.settings.tripIndex;
            this.settings.onTripEnd();
            return false;
        },
        showProgressBar: function (e) {
            var t = this;
            this.$bar.animate({
                width: "100%"
            }, e, "linear", function () {
                t.$bar.width(0);
            });
        },
        hideProgressBar: function () {
            this.$bar.width(0);
            this.$bar.stop(true);
        },
        pauseProgressBar: function () {
            this.$bar.stop(true);
        },
        resumeProgressBar: function (e) {
            this.showProgressBar(e);
        },
        run: function () {
            var e = this,
                t = this.getCurrentTripObject(),
                n = t.delay || this.settings.delay;
            if (!this.isTripDataValid(t)) {
                if (this.settings.skipUndefinedTrip === false) {
                    this.console.error("Your tripData is not valid at index : " + this.tripIndex);
                    this.stop();
                    return false;
                } else {
                    return this[this.tripDirection]();
                }
            }
            this.showCurrentTrip(t);
            this.showProgressBar(n);
            this.progressing = true;
            if (n >= 0) {
                this.timer = new r(e.next.bind(e), n);
            }
            this.settings.onTripChange(this.tripIndex, t);
        },
        isFirst: function () {
            return this.tripIndex === 0 ? true : false;
        },
        isLast: function () {
            return this.tripIndex === this.tripData.length - 1 ? true : false;
        },
        isTripDataValid: function (e) {
            if (typeof e.content === "undefined" || typeof e.sel === "undefined" || e.sel === null || e.sel.length === 0 || t(e.sel).length === 0) {
                return false;
            }
            return true;
        },
        hasCallback: function () {
            return typeof this.tripData[this.tripIndex].callback !== "undefined";
        },
        callCallback: function () {
            this.tripData[this.tripIndex].callback(this.tripIndex);
        },
        canGoPrev: function () {
            var e = this.tripData[this.tripIndex],
                t = e.canGoPrev || this.settings.canGoPrev;
            if (typeof t === "function") {
                t = t.call(e);
            }
            return t;
        },
        canGoNext: function () {
            var e = this.tripData[this.tripIndex],
                t = e.canGoNext || this.settings.canGoNext;
            if (typeof t === "function") {
                t = t.call(e);
            }
            return t;
        },
        increaseIndex: function () {
            if (this.tripIndex >= this.tripData.length - 1) {} else {
                this.tripIndex += 1;
            }
        },
        decreaseIndex: function () {
            if (this.tripIndex <= 0) {} else {
                this.tripIndex -= 1;
            }
        },
        getCurrentTripObject: function () {
            return this.tripData[this.tripIndex];
        },
        setTripBlock: function (e) {
            var t = this.$tripBlock,
                n = e.showCloseBox || this.settings.showCloseBox,
                r = e.showNavigation || this.settings.showNavigation,
                i = e.closeBoxLabel || this.settings.closeBoxLabel,
                s = e.prevLabel || this.settings.prevLabel,
                o = e.nextLabel || this.settings.nextLabel,
                u = e.finishLabel || this.settings.finishLabel;
            t.find(".trip-content").html(e.content);
            t.find(".trip-prev").html(s).toggle(r && !this.isFirst());
            t.find(".trip-next").html(this.isLast() ? u : o).toggle(r);
            t.find(".trip-close").html(i).toggle(n);
            var a = e.sel,
                f = a.outerWidth(),
                l = a.outerHeight(),
                c = t.outerWidth(),
                h = t.outerHeight(),
                p = 10,
                d = 10;
            t.removeClass("e s w n");
            switch (e.position) {
            case "e":
                t.addClass("e");
                t.css({
                    left: a.offset().left + f + d,
                    top: a.offset().top - (h - l) / 2
                });
                break;
            case "s":
                t.addClass("s");
                t.css({
                    left: a.offset().left + (f - c) / 2,
                    top: a.offset().top + l + p
                });
                break;
            case "w":
                t.addClass("w");
                t.css({
                    left: a.offset().left - (d + c),
                    top: a.offset().top - (h - l) / 2
                });
                break;
            case "n":
            default:
                t.addClass("n");
                t.css({
                    left: a.offset().left + (f - c) / 2,
                    top: a.offset().top - p - h
                });
                break;
            }
        },
        showTripBlock: function (n) {
            this.$tripBlock.css({
                display: "inline-block",
                zIndex: this.settings.overlayZindex + 1
            });
            var r = t(e).height(),
                i = t(e).scrollTop(),
                s = this.$tripBlock.offset().top,
                o = 100;
            if (s < i + r && s >= i) {} else {
                this.$root.animate({
                    scrollTop: s - o
                }, "slow");
            }
        },
        hideTripBlock: function () {
            this.$tripBlock.fadeOut("slow");
        },
        create: function () {
            this.createTripBlock();
            this.createOverlay();
        },
        createTripBlock: function () {
            if (typeof t(".trip-block").get(0) === "undefined") {
                var e = ['<div class="trip-block">', '<a href="#" class="trip-close"></a>', '<div class="trip-content"></div>', '<div class="trip-progress-wrapper">', '<div class="trip-progress-bar"></div>', '<a href="#" class="trip-prev"></a>', '<a href="#" class="trip-next"></a>', "</div>", "</div>"].join("");
                var n = this,
                    r = t(e).addClass(this.settings.tripTheme);
                t("body").append(r);
                r.find(".trip-close").on("click", function (e) {
                    e.preventDefault();
                    n.stop();
                });
                r.find(".trip-prev").on("click", function (e) {
                    e.preventDefault();
                    n.prev();
                });
                r.find(".trip-next").on("click", function (e) {
                    e.preventDefault();
                    n.next();
                });
            }
        },
        createOverlay: function () {
            if (typeof t(".trip-overlay").get(0) === "undefined") {
                var n = ['<div class="trip-overlay">', "</div>"].join("");
                var r = t(n);
                r.height(t(e).height()).css({
                    zIndex: this.settings.overlayZindex
                });
                t("body").append(r);
            }
        },
        cleanup: function () {
            t(".trip-overlay, .trip-block").remove();
        },
        init: function () {
            this.preInit();
            if (this.settings.enableKeyBinding) {
                this.bindKeyEvents();
            }
            this.bindResizeEvents();
            this.$tripBlock = t(".trip-block");
            this.$bar = t(".trip-progress-bar");
            this.$overlay = t(".trip-overlay");
        },
        start: function () {
            this.cleanup();
            this.settings.onTripStart();
            this.create();
            this.init();
            this.run();
        },
        getSettings: function() {
        	return this.settings;
        },
    };
    e.Trip = n;
})(window, jQuery);