function sumAndDisplayPoints() {
    var sum = 0;
    $('input[type=radio][data-points]:checked').each(function () {
        sum += $(this).data('points');
    });
    $('#spanTotalPoints').text(sum);
}