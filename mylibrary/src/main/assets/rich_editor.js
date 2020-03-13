const RE = {};

RE.currentSelection = {
    "startContainer": 0,
    "startOffset": 0,
    "endContainer": 0,
    "endOffset": 0
};

RE.editor = document.getElementById('editor');

//当前文档选择进行了改变
document.addEventListener("selectionchange", function () {

    const left = document.queryCommandState('justifyLeft');
    const center = document.queryCommandState('justifyCenter');

    if (left) {
        RE.setJustifyCenter()
        RE.setJustifyLeft()
    } else if (center) {
        RE.setJustifyLeft()
        RE.setJustifyCenter()
    } else {
        RE.setJustifyCenter()
        RE.setJustifyRight()
    }

    RE.backuprange();
});

// Event Listeners
RE.editor.addEventListener("input", function () {
    const html = RE.getHtml();
    window.location.href = "re-callback://" + encodeURI(html);
});

// Initializations
RE.callback = function (e) {
    const html = RE.getHtml();
    window.location.href = "re-callback://" + encodeURI(html);

    setTimeout(function () {
        RE.enabledEditingItems(e);
    }, 50)

}

RE.setHtml = function (contents) {
    RE.editor.innerHTML = decodeURIComponent(contents.replace(/\+/g, '%20'));
}

RE.getHtml = function () {
    return RE.editor.innerHTML;
}

RE.getText = function () {
    return RE.editor.innerText;
}

RE.setBaseTextColor = function (color) {
    RE.editor.style.color = color;
}

RE.setBaseFontSize = function (size) {
    RE.editor.style.fontSize = size;
}

RE.setPadding = function (left, top, right, bottom) {
    RE.editor.style.paddingLeft = left;
    RE.editor.style.paddingTop = top;
    RE.editor.style.paddingRight = right;
    RE.editor.style.paddingBottom = bottom;
}

RE.setBackgroundColor = function (color) {
    document.body.style.backgroundColor = color;
}

RE.setBackgroundImage = function (image) {
    RE.editor.style.backgroundImage = image;
}

RE.setWidth = function (size) {
    RE.editor.style.minWidth = size;
}

RE.setHeight = function (size) {
    RE.editor.style.height = size;
}

RE.setTextAlign = function (align) {
    RE.editor.style.textAlign = align;
}

RE.setVerticalAlign = function (align) {
    RE.editor.style.verticalAlign = align;
}

RE.setPlaceholder = function (placeholder) {
    RE.editor.setAttribute("placeholder", placeholder);
}

RE.setInputEnabled = function (inputEnabled) {
    RE.editor.contentEditable = String(inputEnabled);
}

RE.undo = function () {
    document.execCommand('undo', false, null);
}

RE.redo = function () {
    document.execCommand('redo', false, null);
}

RE.setBold = function (textBold) {
    const selection = window.getSelection();
    if (window.getSelection().toString()) {
        document.execCommand('bold', false, null)
    } else {

        if (textBold) {
            selection.focusNode.parentNode.style.fontWeight = "bold"
        } else {
            selection.focusNode.parentNode.style.fontWeight = "normal"
        }
        RE.callback()
    }
}

RE.setItalic = function (textItalic) {
    const selection = window.getSelection();
    if (window.getSelection().toString()) {
        document.execCommand('italic', false, null);
    } else {
        if (textItalic) {
            selection.focusNode.parentNode.style.fontStyle = "italic"
        } else {
            selection.focusNode.parentNode.style.fontStyle = "normal"
        }
        RE.callback()
    }
}

RE.setSubscript = function () {
    document.execCommand('subscript', false, null);
}

RE.setSuperscript = function () {
    document.execCommand('superscript', false, null);
}

RE.setStrikeThrough = function () {
    document.execCommand('strikeThrough', false, null);
}

RE.setUnderline = function (textUnderline) {
    const selection = window.getSelection();
    if (window.getSelection().toString()) {
        document.execCommand('underline', false, null);
    } else {
        if (textUnderline) {
            selection.focusNode.parentNode.style.textDecoration = "underline"
        } else {
            selection.focusNode.parentNode.style.textDecoration = "none"
        }

        RE.callback()
    }
}

RE.setBullets = function () {
    document.execCommand('insertUnorderedList', false, null);
}

RE.setNumbers = function () {
    document.execCommand('insertOrderedList', false, null);
}

RE.setTextColor = function (color) {
    const selection = window.getSelection();
    if (window.getSelection().toString()) {
        document.execCommand('foreColor', false, color);
    } else {
        selection.focusNode.parentNode.style.color = color
        RE.callback()
    }
}


RE.setTextBackgroundColor = function (color) {
//    RE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('hiliteColor', false, color);
    document.execCommand("styleWithCSS", null, false);
}

RE.setFontSize = function (fontSize) {
    const selection = window.getSelection();
    if (window.getSelection().toString()) {
        document.execCommand("fontSize", false, fontSize);
    } else {
        if (fontSize === "3") {
            selection.focusNode.parentNode.style.fontSize = "medium"
        } else if (fontSize === "4") {
            selection.focusNode.parentNode.style.fontSize = "large"
        } else if (fontSize === "5") {
            selection.focusNode.parentNode.style.fontSize = "x-large"
        }
        RE.callback()
    }
}

RE.setLineHeight = function (heightInPixel) {
// 设置单行行高
//            document.execCommand('formatblock', false, 'p')
//            var selectedNodes = [];
//            var sel = rangy.getSelection();
//            for (var i = 0; i < sel.rangeCount; i++) {
//                selectedNodes = selectedNodes.concat(sel.getRangeAt(i).getNodes());
//                $(selectedNodes).css("height", heightInPixel);
//            }
//            selectedElement = window.getSelection().focusNode.parentNode;
//            $(selectedElement).css("height", heightInPixel);
    var t = document.getElementById('editor');
    t.style.lineHeight = heightInPixel;
}

RE.setHeading = function (heading) {
    document.execCommand('formatBlock', false, '<h' + heading + '>');
}

RE.setIndent = function () {
    document.execCommand('indent', false, null);
}

RE.setOutdent = function () {
    document.execCommand('outdent', false, null);
}

RE.setJustifyLeft = function () {
    document.execCommand('justifyLeft', false, null);
}

RE.setJustifyCenter = function () {
    document.execCommand('justifyCenter', false, null);
}

RE.setJustifyRight = function () {
    document.execCommand('justifyRight', false, null);
}

RE.setBlockquote = function () {
    document.execCommand('formatBlock', false, '<blockquote>');
}

RE.insertImage = function (url, alt, widthPercent) {
    var html = '<img src="' + url + '" width="' + widthPercent + '%" alt="' + alt + '" />';
    RE.insertHTML(html);
}

RE.insertImageWrapWidth = function (url, alt) {
    var html = '<img src="' + url + '" alt="' + alt + '" />';
    RE.insertHTML(html);
}

RE.insertHTML = function (html) {
    RE.restorerange();
    document.execCommand('insertHTML', false, html);
}

RE.insertText = function (text) {
    document.execCommand('insertText', false, text);
}

RE.deleteOneWord = function () {
    document.execCommand('delete', false, "1");
}


RE.insertLink = function (url, title) {
    RE.restorerange();
    const sel = document.getSelection();
    if (sel.toString().length == 0) {
        document.execCommand("insertHTML", false, "<a href='" + url + "'>" + title + "</a>");
    } else if (sel.rangeCount) {
        const el = document.createElement("a");
        el.setAttribute("href", url);
        el.setAttribute("title", title);

        const range = sel.getRangeAt(0).cloneRange();
        range.surroundContents(el);
        sel.removeAllRanges();
        sel.addRange(range);
    }
    RE.callback();
}

RE.setTodo = function (text) {
    const html = '<input type="checkbox" name="' + text + '" value="' + text + '"/> &nbsp;';
    document.execCommand('insertHTML', false, html);
}

RE.backuprange = function () {
    const selection = window.getSelection();
    const rangeCount = selection.rangeCount;
    if (rangeCount > 0) {
        const range = selection.getRangeAt(0);

        const startContainer = range.startContainer;
        const startOffset = range.startOffset;
        const endContainer = range.endContainer;
        const endOffset = range.endOffset;

        RE.currentSelection = {
            "startContainer": startContainer,
            "startOffset": startOffset,
            "endContainer": endContainer,
            "endOffset": endOffset
        };
    }
}

RE.restorerange = function () {
    const selection = window.getSelection();
    selection.removeAllRanges();
    const range = document.createRange();
    range.setStart(RE.currentSelection.startContainer, RE.currentSelection.startOffset);
    range.setEnd(RE.currentSelection.endContainer, RE.currentSelection.endOffset);
    selection.addRange(range);
}


RE.queryCommandState = function (command) {
    var items = [];
    if (document.queryCommandState(command)) {
        window.location.href = "re-state://true";
    } else {
        window.location.href = "re-state://false";
    }
}

RE.enabledEditingItems = function (e) {
    const items = [];
    if (document.queryCommandState('bold')) {
        items.push('bold');
    }
    if (document.queryCommandState('italic')) {
        items.push('italic');
    }
    if (document.queryCommandState('subscript')) {
        items.push('subscript');
    }
    if (document.queryCommandState('superscript')) {
        items.push('superscript');
    }
    if (document.queryCommandState('strikeThrough')) {
        items.push('strikeThrough');
    }
    if (document.queryCommandState('underline')) {
        items.push('underline');
    }
    if (document.queryCommandState('insertOrderedList')) {
        items.push('orderedList');
    }
    if (document.queryCommandState('insertUnorderedList')) {
        items.push('unorderedList');
    }
    if (document.queryCommandState('justifyCenter')) {
        items.push('justifyCenter');
    }
    if (document.queryCommandState('justifyFull')) {
        items.push('justifyFull');
    }
    if (document.queryCommandState('justifyLeft')) {
        items.push('justifyLeft');
    }
    if (document.queryCommandState('justifyRight')) {
        items.push('justifyRight');
    }
    if (document.queryCommandState('insertHorizontalRule')) {
        items.push('horizontalRule');
    }
    var formatBlock = document.queryCommandValue('formatBlock');
    if (formatBlock.length > 0) {
        items.push(formatBlock);
    }

    reportColourAndFontSize(items);

    window.location.href = "re-state://" + encodeURI(items.join(','));
}

RE.focus = function () {
    var range = document.createRange();
    range.selectNodeContents(RE.editor);
    range.collapse(false);
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
    RE.editor.focus();
}

RE.blurFocus = function () {
    RE.editor.blur();
}

RE.removeFormat = function () {
    document.execCommand('removeFormat', false, null);
}


RE.editor.addEventListener("keyup", function (e) {
    var KEY_LEFT = 37, KEY_RIGHT = 39;
    if (e.which == KEY_LEFT || e.which == KEY_RIGHT) {
        RE.enabledEditingItems(e);
    }
});


RE.editor.addEventListener("click", function () {
    RE.enabledEditingItems();
});


// 获取文字颜色和大小
function getComputedStyleProperty(el, propName) {
    if (window.getComputedStyle) {
        return window.getComputedStyle(el, null)[propName];
    } else if (el.currentStyle) {
        return el.currentStyle[propName];
    }
}


function reportColourAndFontSize(items) {
    let containerEl, sel;
    if (window.getSelection) {
        sel = window.getSelection();
        if (sel.rangeCount) {
            containerEl = sel.getRangeAt(0).commonAncestorContainer;
            // https://www.w3school.com.cn/jsref/prop_node_nodetype.asp
            //3代表是一个文本节点
            if (containerEl.nodeType == 3) {
                containerEl = containerEl.parentNode;
            }
        }
    } else if ((sel = document.selection) && sel.type != "Control") {
        containerEl = sel.createRange().parentElement();
    }

    if (containerEl) {
        var fontSize = containerEl.size;
        console.log("----" + fontSize)
        if (!fontSize) {
            fontSize = 4;
        }
        var color = getComputedStyleProperty(containerEl, "color");
        var backgroundColor = getComputedStyleProperty(containerEl, "background-color");
        items.push("textColor:" + colorRGB2Hex(color));
        items.push("textBgColor:" + colorRGB2Hex(backgroundColor));
        items.push(fontSize + "pt");
    }
}

/*RGB颜色转换为16进制*/
function colorRGB2Hex(color) {
    var rgb = color.split(',');
    var r = parseInt(rgb[0].split('(')[1]);
    var g = parseInt(rgb[1]);
    var b = parseInt(rgb[2].split(')')[0]);
    var hex = "#" + ((1 << 24) + (r << 16) + (g << 8) + b).toString(16).slice(1);
    return hex;
}
