$(function (){
    // 点击提交按钮，触发上传事件
    $("#uploadForm").submit(upload);

})

function upload(){
    // 异步请求
    $.ajax({
        url : "http://up-z0.qiniup.com",
        method : "post",
        // 不要把表单数据转换为字符串
        processData : false,
        // 不让jquery设置上传类型，浏览器自动设置，浏览器设置边界
        contentType : false,
        data : new FormData($("#uploadForm")[0]),
        success : function (data){
            if (data && data.code == 0){
                // 更新头像访问路径
                $.post(
                    "/user/header/url",
                    {"fileName": $("input[name='key']").val()},
                    function (data){
                        data = $.parseJSON(data);
                        if (data.code == 0){
                            window.location.reload();
                        }else {
                            alert(data.msg);
                        }
                    }
                );
            }else {
                alert("上传失败");
            }
        }
    });

    // 事件到此为止
    return false;
}