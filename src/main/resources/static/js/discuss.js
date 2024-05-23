$(function (){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete)
})

function like(btn, entityType, entityId, entityUserId, postId){
    $.post(
        "/like",
        {"entityType":entityType, "entityId":entityId, "entityUserId":entityUserId, "postId":postId},
        function (data){
            data = $.parseJSON(data);
            if (data.code == 0){
                $(btn).children("i").text(data.entityLikeCount);
                $(btn).children("b").text(data.entityLikeStatus == 1 ? '已赞':'赞');
            }else {
                alert(data.msg);
            }
        }
    )
}

function setTop(){
    $.post(
        "/discuss/top",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data)
            if (data.code == 0){
                // 改变按钮的可用性
                $("#topBtn").attr("disabled", "disabled");
            }else {
                alert(data.msg);
            }
        }
    );
}

function setWonderful(){
    $.post(
        "/discuss/wonderful",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data)
            if (data.code == 0){
                // 改变按钮的可用性
                $("#wonderfulBtn").attr("disabled", "disabled");
            }else {
                alert(data.msg);
            }
        }
    );
}

function setDelete(){
    $.post(
        "/discuss/delete",
        {"id":$("#postId").val()},
        function (data){
            data = $.parseJSON(data)
            if (data.code == 0){
                // 改变按钮的可用性
                // $("#deleteBtn").attr("disabled", "disabled");
                location.href = "/index";
            }else {
                alert(data.msg);
            }
        }
    );
}
