$(function () {

});

function linkCategoryLabels() {
    $('input[data-category-order]').on('input', function (e) {
        var order = $(e.target).data('category-order');
        var name = $(e.target).val();
        setCategoryName(name, order);
    });
}

function linkLevelLabels() {
    $('input[data-level-order]').on('input', function (e) {
        var order = $(e.target).data('level-order');
        var field = $(e.target).data('field');
        var text = $(e.target).val();

        setLevelFieldValue(text, field, order);
    });
}

function checkNumberOfDecimals(obj, maxNumberOfDecimals) {
    var jqObj = $(obj);
    var number = jqObj.val();
    var intPartLength = number.indexOf('.');
    //if it is -1 it means there are no decimals
    if (intPartLength > -1) {
        var decimalNo = number.length - intPartLength - 1;
        if (decimalNo > maxNumberOfDecimals) {
            jqObj.val(jqObj.val().slice(0, intPartLength + 1 + maxNumberOfDecimals));
        }
    }
}

function copyCategoryName(order) {
    var name = $('input[data-category-order=' + order + ']').val();
    setCategoryName(name, order);
}

function setCategoryName(name, order) {
    if (!name || name.length === 0) {
        name = '<Category Name>';
    }
    $('h3[data-category-order=' + order + ']').text(name);
}

function copyLevelName(order) {
    var name = $('input[data-level-order=' + order + '][data-field=name]').val();
    setLevelFieldValue(name, 'name', order);
}

function copyLevelWeight(order) {
    var weight = $('input[data-level-order=' + order + '][data-field=weight]').val();
    setLevelFieldValue(weight, 'weight', order);
}

function setLevelFieldValue(value, field, order) {
    var classSelector = field === 'name' ? '.col01' : '.col02';
    var newValue = field === 'name'
        ? (!value || value.length === 0 ? '<Level Name>' : value)
        : (!value || value.length === 0 ? '' : value + '%');
    $('td' + classSelector + '[data-level-order=' + order + ']').text(newValue);
}