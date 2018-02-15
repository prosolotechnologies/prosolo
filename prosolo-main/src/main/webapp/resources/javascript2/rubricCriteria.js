$(function () {

});

function linkCriteriaLabels() {
    $('input[data-criterion-order]').on('input', function (e) {
        var order = $(e.target).data('criterion-order');
        var name = $('input[data-criterion-order=' + order + '][data-field=name]').val();
        var weight = $('input[data-criterion-order=' + order + '][data-field=weight]').val();
        //var name = $(e.target).val();
        setCriterionNameAndWeight(name, weight, order);
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

function copyCriterionNameAndWeight(order) {
    var name = $('input[data-criterion-order=' + order + '][data-field=name]').val();
    var weight = $('input[data-criterion-order=' + order + '][data-field=weight]').val();
    setCriterionNameAndWeight(name, weight, order);
}

function setCriterionNameAndWeight(name, weight, order) {
    if (!name || name.length === 0) {
        name = '<Criterion Name>';
    }
    var weight = !weight || weight.length === 0 ? '' : ' - ' + weight + '%';
    $('h3[data-criterion-order=' + order + ']').text(name + weight);
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
    $('td' + classSelector + '[data-level-order=' + order + ']' + (field === 'name' ? ' .name' : '')).text(newValue);
}