var host = window.location.href.replace("/search", "");
var contextPath = "/homeserver";
var hostAndContextPath = host + contextPath;
// var host = "http://localhost:8087";
var locateIndex = {
// 选中的位置
    position: {},
    bindEvent: function () {
        // 鼠标松开事件(鼠标松开的时候就获取当前选中的select)
        document.onmouseup = function (e) {
            var rangeTextareaDom = document.querySelector('#addarea')
            locateIndex.position.start = rangeTextareaDom.selectionStart
            locateIndex.position.end = rangeTextareaDom.selectionEnd
        }
    },
    // 插入内容
    insertText: function (insertValue) {
        var rangeTextareaDom = document.querySelector('#addarea')
        rangeTextareaDom.setRangeText(insertValue, this.position.start, this.position.end)
        rangeTextareaDom.focus()
        var start = this.position.start + insertValue.length
        rangeTextareaDom.setSelectionRange(start, start)
    }
};

var paste = {
    bindEvent: function () {
        $("#addarea").on('paste', function (eventObj) {
            // 处理粘贴事件
            var event = eventObj.originalEvent;
            var imageRe = new RegExp(/image\/.*/);
            var fileList = $.map(event.clipboardData.items, function (o) {
                if (imageRe.test(o.type)) {
                    var blob = o.getAsFile();
                    return blob;
                }
            });
            if (fileList.length <= 0) {
                return
            }
            upload(fileList);
            //阻止默认行为即不让剪贴板内容在div中显示出来
            event.preventDefault();
        });
    }
}

$(function () {
    // alert("hello2");
    add();
    writeBtnHandle();
    search();
    searchKeyDown();
    locateIndex.bindEvent();
    paste.bindEvent();
});

function upload(fileList) {
    for (var i = 0, l = fileList.length; i < l; i++) {
        var fd = new FormData();
        var f = fileList[i];
        fd.append('file', f);
        $.ajax({
            url: "/cfind/upload",
            type: 'POST',
            // dataType: 'json',
            data: fd,
            processData: false,
            contentType: false,
            xhrFields: {withCredentials: true},
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Credentials': 'true'
            },
            success: function (res) {
                // for (var i = 0; i < len; i++) {
                //     var img = document.createElement('img');
                //     img.src = res.data[i]; //设置上传完图片之后展示的图片
                //     editor.appendChild(img);
                // }
                var img = "<div><img src=\"" + res + "\"></div>";
                locateIndex.insertText(img);
            },
            error: function (er) {
                console.log("upload failed" + er);
                alert("上传图片错误");
            }
        });
    }
}

function writeBtnHandle() {
    var writebtn = $("#writebtn");
    writebtn.click(function () {
        var adddiv = $("#adddiv");
        if (adddiv.css("display") == "none") {
            adddiv.css("display", "block");
        } else {
            adddiv.css("display", "none");
        }
    });
}

function add(type) {
    var addText = $("#addarea").val();
    if (addText != undefined && addText != '') {
        // addText=addText.replace("\n","\r\n");
        var param = {
            type: type,
            reference: addText,
            filename: $("#kwinput").val()
        };
        $.ajax(
            {
                url: hostAndContextPath + "/cfind/add",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify(param),
                success: function (result) {
                    alert("添加成功！");
                }
            });
    }
}

function searchKeyDown() {
    var inputDom = $("#kwinput");
    inputDom.bind('keypress', function (e) {
        if (event.keyCode == 13) {
            fillRes();
        }
    });
}

function search() {
    var queryBtn = $("#querybtn");
    queryBtn.click(function () {
        fillRes();
    })
}

function addHtmlListItem(htmldir) {
    keyListDiv.append("<p> +++++++++++html++++++++++ </p>");
    htmldir.forEach(function (v, k, $this) {
        var keyListDiv = $("#keyListDiv");
        keyListDiv.append("<p data-dirname=\"" + v + "\" onclick=\"getHtml('" + v + "')\">" + v + "</p>");
    });
}

function addFileListItem(htmldir) {
    keyListDiv.append("<p> +++++++++++文件++++++++++ </p>");
    htmldir.forEach(function (v, k, $this) {
        var keyListDiv = $("#keyListDiv");
        keyListDiv.append("<p data-dirname=\"" + v + "\" onclick=\"getFile('" + v + "')\">" + v + "</p>");
    });
}

function arrNotEmpty(arr) {
    return arr != undefined && arr.length > 0;
}

function fillRes() {
    $("#keyListDiv").html('');
    $("#resdiv").html('');
    $("#iframeDiv").html('');
    var queryText = $("#kwinput").val();
    if (queryText != undefined && queryText != '') {
        $.ajax(
            {
                url: hostAndContextPath + "/cfind/search/" + queryText,
                type: "GET",
                success: function (result) {
                    $("#resdiv").html(result.divContent);
                    var textareaContent = result.textareaContent;
                    if (textareaContent != undefined && textareaContent != '') {
                        $("#addarea").val(textareaContent);
                    }
                    var htmldir = result.htmldir;
                    var fileNames = result.fileNames;
                    if (arrNotEmpty(htmldir) && arrNotEmpty(fileNames)) {
                        addHtmlListItem(htmldir);
                        addFileListItem(fileNames);
                        return;
                    }
                    if (arrNotEmpty(htmldir) && htmldir.length == 1) {
                        getHtml(htmldir[0]);
                        return;
                    }
                    if (arrNotEmpty(fileNames) && fileNames.length == 1) {
                        getFile(fileNames[0]);
                        return;
                    }
                }
            });
    }
}

function getHtml(v) {
    if (v == undefined || v.length == 0) {
        return;
    }
    var iframeDiv = $("#iframeDiv");
    iframeDiv.html('');
    var src = "/cfind/getHtml/" + v + "/" + v + ".html";
    iframeDiv.append(
        "</br>" + "<iframe src=\"" + src + "\" style=\"width: 100%;height: 100%;\"></iframe>"
    );
}

function getFile(v) {
    if (v == undefined || v.length == 0) {
        return;
    }
    $.ajax(
        {
            url: hostAndContextPath + "/cfind/getFile/" + v,
            type: "GET",
            success: function (result) {
                var fileData = result.data;
                if (fileData != undefined && fileData != '') {
                    $("#resdiv").html(fileData);
                }
            }
        });
}

