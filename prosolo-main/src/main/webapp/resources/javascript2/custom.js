//tooltips
$('[data-toggle="tooltip"]').tooltip({
    'placement': 'top'
});
$('[data-toggle="popover"]').popover({
    trigger: 'hover',
        'placement': 'top'
});

//comment form hide/show
function displaySubmitButton(inputElem) {
    if ($(inputElem).val().length==0) {
        $(inputElem).next('button.btn-green').addClass('hidden');
    } else {
        $(inputElem).next('button.btn-green').removeClass('hidden');
    }
}

function toggleReplyInput(replyBtn) {
    $(replyBtn).parent().next('.replyInput').toggleClass('hidden');
    return false;
}

function showEditComment(editBtn) {
    $(editBtn).parent().prev('.editComment').toggleClass('hidden');
    $(editBtn).parent().prevAll('.commentText').toggleClass('hidden');
    $(editBtn).addClass('hidden');
    return false;
}

function hideEditComment(cancelBtn) {
    var editCommentContainer = $(cancelBtn).parent();
    var commentContainer = $(cancelBtn).parent().prev('.commentText');

    editCommentContainer.addClass('hidden');
    commentContainer.removeClass('hidden');

    var textarea1 = editCommentContainer.find('textarea').first();
    textarea1.val(commentContainer.text());

    $(cancelBtn).parent().next('.commentOptions').find('a.edit').removeClass('hidden');
    return false;
}