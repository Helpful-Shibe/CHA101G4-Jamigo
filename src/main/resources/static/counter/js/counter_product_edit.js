$(function () {

    let urlParams = new URLSearchParams(window.location.search);
    let productNo = urlParams.get(`productNo`);

    getProductCatOptions();

    //圖片預覽
    $("#imageUpload1").change(function () {
            previewPicture(this, "imagePreview1");
        }
    );
    $("#imageUpload2").change(function () {
            previewPicture(this, "imagePreview2");
        }
    );
    $("#imageUpload3").change(function () {
            previewPicture(this, "imagePreview3");
        }
    );
    $("#imageUpload4").change(function () {
            previewPicture(this, "imagePreview4");
        }
    );

    $.ajax({
        url: `/Jamigo/products/getOneProduct/${productNo}`,
        type: "GET",
        success: function (productWithPics) {
            console.log(productWithPics);
            // $("#categoryEdit").val(productWithPics.productCategory.productCatNo);
            let categoryEdit = document.getElementById("categoryEdit");
            for (let i = 0; i < categoryEdit.length; i++) {
                if (categoryEdit.options[i].value == productWithPics.productCategory.productCatNo) {
                    categoryEdit.selectedIndex = i;
                    break;
                }
            }
            // $("#counterNo").val(productWithPics.counterNo);
            let transProductStat = productWithPics.productStat === true ? 1 : 0;
            $("#productName").val(productWithPics.productName);
            $("#productPrice").val(productWithPics.productPrice);
            $("#productDescription").val(productWithPics.productInfo);
            $("input[name='productStatus'][value='" + transProductStat + "']").prop("checked", true);
            $("#productSaleNum").val(productWithPics.productSaleNum);
            $("#reportNumber").val(productWithPics.reportNumber);
            // $("#evalTotalPeople").val(productWithPics.evalTotalPeople);
            // $("#evalTotalScore").val(productWithPics.evalTotalScore);
            // $("#evalAvg").innerText(productWithPics.evalTotalScore / productWithPics.evalTotalPeople);
            // console.log(productWithPics.productPics[0].productPic);
            if (productWithPics.productPics && productWithPics.productPics.length >= 1) {
                $("#imagePreview1").attr('src', 'data:image/*;base64,' + productWithPics.productPics[0].productPic).show();
            }
            if (productWithPics.productPics && productWithPics.productPics.length >= 2) {
                $("#imagePreview2").attr('src', 'data:image/*;base64,' + productWithPics.productPics[1].productPic).show();
            }
            if (productWithPics.productPics && productWithPics.productPics.length >= 3) {
                $("#imagePreview3").attr('src', 'data:image/*;base64,' + productWithPics.productPics[2].productPic).show();
            }
            if (productWithPics.productPics && productWithPics.productPics.length >= 4) {
                $("#imagePreview4").attr('src', 'data:image/*;base64,' + productWithPics.productPics[3].productPic).show();
            }
        },
        error: function (xhr, textStatus, errorThrown) {
            console.error(xhr);
        }
    });

    sendUpdateData(productNo);
});

function getProductCatOptions() {
    $.ajax({
        url: `/Jamigo/products/getAllCategories`,
        method: "GET",
        success: function (resp) {
            console.log(resp);
            let options_html = ``;
            for (let e in resp) {
                options_html += `
                        <option value="${resp[e].productCatNo}">${resp[e].productCatName}</option>
                    `;
            }
            $("#categoryEdit").html(options_html);
        },
        error: function (xhr, errorThrown) {
            console.log(xhr, errorThrown);
        }
    });
}

//預覽圖片用函式
function previewPicture(input, previewElement) {
    if (input.files && input.files[0]) {
        let reader = new FileReader();
        reader.onload = function (e) {
            $(`#${previewElement}`).attr("src", e.target.result).show();
        }
        reader.readAsDataURL(input.files[0]); // convert to base64 string
    }
}

function sendUpdateData(productNo){
    $("#confirm-edit").click(function (e){
        let formData = new FormData($("#product-detail")[0]);
        $.ajax({
            url: `/Jamigo/products/updateProduct/${productNo}`,
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function (resp) {
                alert("更新成功");
                window.location = `/Jamigo/counter/counter_product.html`;
            }
        });
    });
}