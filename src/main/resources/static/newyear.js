$(function () {
    // alert("hello2");
    $("#questionBtn").click(function () {
        var questionContent = $("#questionInput").val();
        if (questionContent == null || questionContent == '') {
            return;
        }
        $.ajax(
            {
                url: "http://www.tuanbaol.com:8081/websocketserverdemo/newyear/question",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({
                    content: questionContent
                }),
                // success: function (result) {
                //     alert("添加成功！");
                // }
            });
    });
});
