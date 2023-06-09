const $table = $('table#main_table');
const memberNo = parseInt(localStorage.getItem("memberNo"));

$(function () {

    var urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('redirected') === 'true') {
        Swal.fire({
            title: '付款成功',
            icon: 'success',
            confirmButtonText: 'OK'
        })
    }

    $table.bootstrapTable('destroy').bootstrapTable({
        url: `/Jamigo/shop/platform_order/all/memberData/${memberNo}`,
        columns: [
            {
                field: 'platformOrderNo',
                title: '平台訂單編號',
                align: 'center',
                valign: 'middle',
                formatter: platformOrderNoFormatter
            },
            {
                field: 'platformOrderStat',
                title: '訂單狀態',
                align: 'center',
                valign: 'middle',
                width: 120,
                formatter: platformOrderStatFormatter
            },
            {
                field: 'paymentStat',
                title: '付款狀態',
                align: 'center',
                valign: 'middle',
                width: 80,
                formatter: paymentStatFormatter
            },
            {
                field: 'paymentMethod',
                title: '付款方式',
                align: 'center',
                valign: 'middle',
                width: 80,
                formatter: paymentMethodFormatter
            },
            {
                field: 'actuallyPaid',
                title: '訂單實付金額',
                align: 'center',
                valign: 'middle',
                formatter: actuallyPaidFormatter
            },
            {
                field: 'pickupMethod',
                title: '取貨方式',
                align: 'center',
                valign: 'middle',
                width: 80,
                formatter: pickupMethodFormatter
            },
            {
                field: 'orderTime',
                title: '下單時間',
                align: 'center',
                valign: 'middle'
            },
            {
                field: 'operation',
                title: '操作',
                align: 'center',
                valign: 'middle',
                formatter: '<button type="button" class="btn btn-primary full-info" data-bs-toggle="modal" data-bs-target="#orderDetailModal" data-bs-whatever="@mdo">其他資訊</button>' +
                    '<button type="button" class="btn btn-primary evaluate">完成訂單</button>'
            }
        ],
    });
})

function platformOrderNoFormatter(value) {
    return '#' + value;
}

function platformOrderStatFormatter(value) {
    if (value === 0) {
        return '<span class="badge rounded-pill text-bg-secondary">訂單取消</span>'
    }
    else if (value === 10) {
        return '<span class="badge rounded-pill text-bg-warning">等待付款</span>'
    }
    else if (value === 20) {
        return '<span class="badge rounded-pill text-bg-warning">揀貨中</span>'
    }
    else if (value === 30) {
        return '<span class="badge rounded-pill text-bg-danger">等待包裝</span>'
    }
    else if (value === 60) {
        return '<span class="badge rounded-pill text-bg-warning">等待訂單完成</span>'
    }
    else if (value === 70) {
        return '<span class="badge rounded-pill text-bg-success">訂單完成</span>'
    }
}

function paymentStatFormatter(value) {
    if (value === 0) {
        return '<span class="badge rounded-pill text-bg-danger">未付款</span>'
    }
    else if (value === 1) {
        return '<span class="badge rounded-pill text-bg-success">已付款</span>'
    }
}

function paymentMethodFormatter(value) {
    if (value === 1) {
        return '<span class="badge rounded-pill text-bg-info">綠界</span>';
    }
    else if (value === 2) {
        return '<span class="badge rounded-pill text-bg-info">貨到付款</span>';
    }
}

function actuallyPaidFormatter(value) {
    return '<b>$' + value + '</b>';
}

function pickupMethodFormatter(value) {
    if (value === 1) {
        return '<span class="badge rounded-pill text-bg-dark">店取</span>'
    }
    else if (value === 2) {
        return '<span class="badge rounded-pill text-bg-dark">宅配到府</span>'
    }
}

$table.on("click", "button.full-info", function () {

    // 獲得平台訂單編號
    platformOrderNo = parseInt($(this).closest("td").siblings().eq(0).text().substring(1));

    // 找到燈箱內容元素
    let modal_body = document.querySelector("div#orderDetailModal div.modal-body");

    let totalPaid;
    let totalCoupon;
    let totalPoints;
    let actuallyPaid;
    let rewardPoints;

    $.ajax({
        method: "GET",
        url: `/Jamigo/shop/platform_order/${platformOrderNo}`,
        success: function(res) {

            totalPaid = res.totalPaid;
            totalCoupon = res.totalCoupon;
            totalPoints = res.totalPoints;
            actuallyPaid = res.actuallyPaid;
            rewardPoints = res.rewardPoints;

            // 動態新增訂購人資訊
            modal_body.innerHTML = `
               <div>
                   <p><b>訂購人姓名：</b>${res.buyerName}</p>
                   <p><b>訂購人手機號碼：</b>${res.buyerPhone}</p>
                   <p><b>訂購人Email：</b>${res.buyerEmail}</p>
               </div>
            `;

            // 動態新增付款資訊
            if (res.paymentMethod === 1) {
                modal_body.innerHTML += `
                    <div>
                        <p><b>付款方式：</b>綠界</p>
                `;
            }
            else if (res.paymentMethod === 2) {
                modal_body.innerHTML += `
                    <div>
                        <p><b>付款方式：</b>貨到付款</p>
                `;
            }

            // 動態新增取貨資訊
            if (res.pickupMethod === 1) {
                modal_body.innerHTML += `
                        <p><b>取貨方式：</b>店取</p>
                        <p><b>店別：</b>中壢館</p>
                    </div>
                `;
            }
            else if (res.pickupMethod === 2) {
                modal_body.innerHTML += `
                        <p><b>取貨方式：</b>宅配到府</p>
                        <p><b>宅配國家：</b>${res.deliveryCountry}</p>
                        <p><b>宅配住址：</b>${res.deliveryAddress}</p>
                   </div>
                `;
            }

            // 動態新增開立發票資訊
            if (res.invoiceMethod === 1) {
                modal_body.innerHTML += `
                    <div>
                        <p><b>開立發票方式：</b>個人發票</p>
                    </div>
                `;
            }
            else if (res.invoiceMethod === 2) {
                modal_body.innerHTML += `
                    <div>
                        <p><b>開立發票方式：</b>公司發票</p>
                        <p><b>發票統一編號：</b>${res.invoiceGui}</p>
                    </div>
                `;
            }



            $.ajax({
                method: "GET",
                url: `/Jamigo/shop/platform_order/${platformOrderNo}/detail`,
                success: function(response) {

                    tableContent  = `
                        <div class="order_table table-responsive">
                            <table>
                    `;

                    for (let counter_name in response) {
                        tableContent += `
                            <tbody>
                                <tr>
                                    <td class="counter" colspan="3">
                                        ${counter_name}
                        `;

                        for (let item of response[counter_name]["product"]) {
                            tableContent += `
                                    </td>
                                </tr>
                                <tr>
                                    <td class="cart_img">
                                        <img src="/Jamigo/shop/product_picture/product/${item['productNo']}" alt="">
                                    </td>
                            `;

                            tableContent += `
                                    <td class="cart_info" colspan="2">
                                        <h5>${item["productName"]}</h5>
                                        <p>單價: $${item["productPrice"]} / 數量: ${item["amount"]}</p>
                                    </td>
                                </tr>
                            `;
                        }

                        tableContent += `
                            </tbody>
                        `;
                    }

                    tableContent += `
                            <tfoot>
                                <tr>
                                    <th colspan="2">原總金額</th>
                                    <th>$${totalPaid}</th>
                                </tr>
                                <tr>
                                    <th colspan="2">折價券折抵</th>
                                    <th style="color: red">-$${totalCoupon}</th>
                                </tr>
                                <tr>
                                    <th colspan="2">點數折抵</th>
                                    <th style="color: red">-$${totalPoints}</th>
                                </tr>
                                <tr class="order_total">
                                    <th colspan="2">訂單實付金額</th>
                                    <th>$${actuallyPaid}</th>
                                </tr>
                                <tr class="order_total">
                                    <th colspan="2">回饋點數</th>
                                    <th>
                                        <i class="fa-solid fa-coins fa-lg" style="color: #e7eb00;"></i> ${rewardPoints}
                                    </th>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                    `;

                    modal_body.innerHTML += tableContent;
                }
            })
        }
    })
});

$table.on("click", "button.evaluate", function () {

    // 獲得平台訂單編號
    platformOrderNo = parseInt($(this).closest("td").siblings().eq(0).text().substring(1));

    $.ajax({
        method: "GET",
        url: `/Jamigo/shop/platform_order/${platformOrderNo}`,
        success: function(res) {

            if (res.platformOrderStat === 60) {

                Swal.fire({
                    title: '完成訂單？',
                    text: '您的訂單尚未完成，是否直接完成訂單？',
                    icon: 'question',
                    showCancelButton: true,
                    confirmButtonColor: '#3085d6',
                    cancelButtonColor: '#d33',
                    confirmButtonText: '是的',
                    cancelButtonText: '取消'

                }).then((result) => {

                    if (result.isConfirmed) {

                        let data = {
                            platformOrderStat: 70
                        }

                        $.ajax({
                            url: `/Jamigo/shop/platform_order/${platformOrderNo}`,
                            type: 'PUT',
                            contentType: 'application/json',
                            data: JSON.stringify(data),
                            success: function(res) {
                                Swal.fire({

                                    title: '成功完成訂單',
                                    icon: 'success',
                                    text: '您已收到訂單的回饋點數',
                                    confirmButtonText: '重新載入頁面'

                                }).then(function () {
                                    location.reload();
                                })
                            },

                            error: function (err) {
                                Swal.fire({
                                    title: '修改失敗',
                                    icon: 'error',
                                    confirmButtonText: "關閉"
                                })
                            }
                        });
                    }
                })
            }
            else if (res.platformOrderStat === 70) {
                Swal.fire({
                    title: '您已經完成訂單了',
                    icon: 'warning',
                    confirmButtonText: 'OK'
                })
            }
            else {
                Swal.fire({
                    title: '不要鬧',
                    text: '您連商品都還沒拿到，怎麼完成訂單？',
                    icon: 'warning',
                    confirmButtonText: 'OK'
                })
            }
        }
    })
})