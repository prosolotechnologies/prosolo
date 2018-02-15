function sumAndDisplayPoints() {
    var sum = 0;
    $('input[type=radio][data-points]:checked').each(function () {
        sum += $(this).data('points');
    });
    $('#spanTotalPoints').text(sum);
}

$(function(){
    $(document).keyup(function(e) {
        if (e.keyCode == 27) { // esc keycode
            hidePopupSidebar('#cbp-spmenu-s2');
        }
    });
});