let urlParams = new URLSearchParams(window.location.search);
let productCatNo = urlParams.get('productCatNo');
let page = 1;
let orderBy = 1;
let perPage = 12; // 每頁顯示的商品數
let productCount;

let cartItems = [];

$(document).ready(function () {
    printCartItemsCount();
    product_category_name_render();
    getProductCountAndRender();

    change_sort_method();
});

function change_sort_method() {
    $('.list').on('click', '.option', function() {

        orderBy = $(this).attr("data-value");
        get_products_by_category_render(1, orderBy);

        $('.pagination li.current').removeClass('current');
        $('.pagination li').first().addClass('current');
    });
}

function product_category_name_render() {

    $.ajax({
        type: 'GET',
        url: `/Jamigo/shop/product_category/${productCatNo}`,
        success: function (response) {

            let html_str = `<h3>${response.productCatName}</h3>`;

            $("div.breadcrumbs_area div.breadcrumb_content").html(html_str);
        }
    })
}

function get_products_by_category_render(page, orderBy) {

    return $.ajax({
        type: 'GET',
        url: `/Jamigo/shop/product_category/${productCatNo}/products?page=${page}&orderBy=${orderBy}`,
        success: function (response) {


            $('html, body').animate({scrollTop:0}, 'slow');

            let num = 0;

            let html_str = "";

            for (let item of response) {

                num++;

                let avg_rate = 0;

                if (item['evalTotalPeople'] !== 0)
                    avg_rate = Math.round(item['evalTotalScore'] / item['evalTotalPeople'] * 10) / 10;

                html_str += `
                    <div class="col-lg-3 col-md-4 col-sm-6 col-12">
                        <article class="single_product">
                            <figure>
                                <div class="product_thumb">
                                    <a class="primary_img" href="/Jamigo/shop/shopping/product_detail_page.html?productNo=${item['productNo']}">
                                        <img src="/Jamigo/shop/product_picture/product/${item['productNo']}"alt="">
                                    </a>
                                    <div class="action_links">
                                        <ul>
                                            <li class="wishlist"><a onclick="addWish(this, ${item['productNo']});"><i class="fa-solid fa-heart"></i></a>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                                <div class="product_content grid_content">

                                    <p class="product_no" style="display: none">${item['productNo']}</p>
                                    
                                    <div class="product_rating">
                                        <ul>
                                            <div class="Stars" style="--rating: ${avg_rate};"><span>${avg_rate} (${item['evalTotalPeople']})</span>
                                            </div>
                                        </ul>
                                    </div>
                                    <!-- 櫃位名稱 -->
                                    <h5 class="counter_name">
                                        <a href="/Jamigo/shop/counter/counter_mainPage.html?counterNo=${item['counterNo']}">${item['counterName']}</a>
                                    </h5>
                                    <!-- 商品名稱 -->
                                    <h4 class="product_name">
                                        <a href="/Jamigo/shop/shopping/product_detail_page.html?productNo=${item['productNo']}">${item['productName']}</a>
                                    </h4>
                                    <div class="price_box">
                                        <span class="current_price">$${item['productPrice']}</span>
                                    </div>
                                    <div class="add_to_cart">
                                        <a href="#" data-toggle="modal" data-target="#modal_box">加入購物車</a>
                                    </div>
                                </div>
                                <div class="product_content list_content">

                                    <p class="product_no" style="display: none">${item['productNo']}</p>
                                    
                                    <div class="product_rating">
                                        <ul>
                                            <div class="Stars" style="--rating: ${avg_rate};"><span>${avg_rate} (${item['evalTotalPeople']})</span>
                                            </div>
                                        </ul>
                                    </div>
                                    <h5 class="counter_name">
                                        <a href="/Jamigo/shop/counter/counter_mainPage.html?counterNo=${item['counterNo']}">${item['counterName']}</a>
                                    </h5>
                                    <h4 class="product_name">
                                        <a href="/Jamigo/shop/shopping/product_detail_page.html?productNo=${item['productNo']}">${item['productName']}</a>
                                    </h4>
                                    <div class="product_desc">
                                        <p>
                                            ${item['productInfo']}
                                        </p>
                                    </div>
                                    <div class="price_box">
                                        <span class="current_price2">$${item['productPrice']}</span>
                                    </div>

                                    <div class="action_links list_action_right">
                                        <ul>
                                            <li class="add_to_cart"><a href="#" data-toggle="modal"
                                                                       data-target="#modal_box">加入購物車</a></li>
                                            <li class="wishlist"><a onclick="addWish(this, ${item['productNo']});"><i class="fa-solid fa-heart"></i></a>
                                            </li>
                                        </ul>
                                    </div>
                                </div>
                            </figure>
                        </article>
                    </div>
                `;
            }

            $("div.shop_wrapper").html(html_str);

            let viewMode = $("div.shop_wrapper").prev().find("button.active").data('role');

            if (viewMode === 'grid_3')
                $("div.shop_wrapper").children().addClass('col-lg-4 col-md-4 col-sm-6').removeClass('col-lg-3 col-12');
            else if (viewMode === 'grid_4')
                $("div.shop_wrapper").children().addClass('col-lg-3 col-md-4 col-sm-6').removeClass('col-lg-4 col-12');
            else if (viewMode === 'grid_list')
                $("div.shop_wrapper").children().addClass('col-12').removeClass('col-lg-3 col-lg-4 col-md-4 col-sm-6');

            $("div.page_amount").html(`<p>顯示第 ${1 + 12 * (page - 1)}-${num + 12 * (page - 1)} 件商品，共有 ${productCount} 件商品</p>`);
        }
    })
}

function getProductCountAndRender() {
    $.ajax({
        type: 'GET',
        url: `/Jamigo/shop/product_category/${productCatNo}/product_amount`,
        success: function (response) {
            productCount = response;
            get_products_by_category_render(page, orderBy);
            pagination_render(productCount, perPage);
        }
    });
}

function pagination_render(productCount, perPage) {
    let totalPages = Math.ceil(productCount / perPage);
    let html_str = "";
    for (let i = 1; i <= totalPages; i++) {
        if (i === page) {
            html_str += `<li class="current"><a href="javascript:void(0)">${i}</a></li>`;
        } else {
            html_str += `<li><a href="javascript:void(0)">${i}</a></li>`;
        }
    }
    $("div.pagination ul").html(html_str);
}

$(document).on('click', '.pagination li', function() {

    // 獲取被點擊的頁碼
    page = parseInt($(this).children("a").text());

    // 調用 get_products_by_category_render 函數
    get_products_by_category_render(page, orderBy);

    // 將被點擊的元素設為當前頁碼
    $('.pagination li').removeClass('current');
    $(this).addClass('current');
});

$(document).on("click", ".add_to_cart a", function () {

    productNo = parseInt($(this).closest("div.product_content").children("p.product_no").text());

    $.ajax({
        type: 'GET',
        url: `http://localhost:8080/Jamigo/products/getProductForDetailPage/${productNo}`,
        success: function (response) {

            let html_str = "";

            let avg_rate;

            if (response.evalTotalPeople === 0)
                avg_rate = 0;
            else
                avg_rate = Math.round(response.evalTotalScore / response.evalTotalPeople * 10) / 10;

            html_str += `
                <div class="col-lg-5 col-md-5 col-sm-12">
                    <div class="modal_tab">
                        <div class="tab-content product-details-large">
                            <div class="tab-pane fade show active" id="tab1" role="tabpanel">
                                <div class="modal_tab_img">
                                    <img src="/Jamigo/shop/product_picture/product/${productNo}" alt="">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-lg-7 col-md-7 col-sm-12">
                    <div class="modal_right">
                    
                        <p class="counter_no" style="display: none">${response.counterNo}</p>
                    
                        <div class="modal_counter_name mb-10">
                            <h3>${response.counterName}</h3>
                        </div>
                        <div class="modal_product_name mb-10">
                            <h2>${response.productName}</h2>
                        </div>
                        <div class="modal_price mb-10">
                            商品價格：<span class="new_price">${response.productPrice}</span>
                        </div>
                        <div class="product_rating">
                            <ul>
                                <div class="Stars" style="--rating: ${avg_rate};"><span>${avg_rate} (${response.evalTotalPeople})</span>
                                </div>
                            </ul>
                        </div>
                        
                        <div class="variants_selects">
                            <div class="modal_add_to_cart">
                                <form action="#">
                                    <label style="font-size: 16px; font-weight: 500">商品數量：</label>
                                    <input min="1" max="100" step="1" value="1" type="number" id="product_quantity">
                                    <button type="button" style="font-size: 18px; width: 200px" class="btn_add_to_cart">加入購物車</button>
                                </form>
                            </div>
                        </div>
                        
                        <div class="modal_description mb-15">
                            <p>${response.productInfo}</p>
                        </div>
                    </div>
                </div>
            `;

            $("div.modal_body div.row").html(html_str);

        }
    })
})

//取得會員編號
function getMemberNo(){

    return parseInt(localStorage.getItem("memberNo"));
    // return 1;
}

//放入購物車
$(document).on("click", "button.btn_add_to_cart", function () {

    console.log($(this).closest("div.modal_right").find("div.modal_counter_name"));

    let memberNo = getMemberNo();

    if (!memberNo) {
        localStorage.setItem('currentPageUrl', window.location.href);
        window.location = '/Jamigo/member/login/login.html';
        return;
    }

    let counterName = $(this).closest("div.modal_right").find("div.modal_counter_name").text().trim();
    let counterNo = parseInt($(this).closest("div.modal_right").find("p.counter_no").text());
    let productName = $(this).closest("div.modal_right").find("div.modal_product_name").text().trim();
    let productPrice = $(this).closest("div.modal_right").find("div.modal_price span").text();
    let quantity = parseInt($(this).closest("div.modal_right").find("input#product_quantity").val());
    let cartItem = {
        counterNo: counterNo,
        counterName: counterName,
        productNo: productNo,
        productName: productName,
        productPrice: productPrice,
        quantity: quantity
    };
    let cartData = {
        memberNo: memberNo,
        cartItem: cartItem
    };
    $.ajax({
        url: `/Jamigo/cart/addOneToCart`,
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify(cartData),
        success: function (){

            Swal.fire({
                title: '商品已加入購物車',
                icon: 'success'
            })

            printCartItemsCount();
        },
        error: function (){
            alert("商品加入購物車失敗");
        }
    });
});

function printCartItemsCount(){
    let memberNo = getMemberNo();
    if(!memberNo){
        $(".main_header .mini_cart_wrapper .item_count").text(cartItems.length);
    }else {
        $.ajax({
            url: `/Jamigo/cart/getCartList/${memberNo}`,
            method: "GET",
            async: false,
            success: function (respCartItems){
                cartItems = respCartItems;
                $(".main_header .mini_cart_wrapper .item_count").text(cartItems.length);
            }
        });
    }
}

function goToCartDetailPage(){
    let memberNo = getMemberNo();
    if (!memberNo) {
        localStorage.setItem('currentPageUrl', window.location.href);
        window.location = '/Jamigo/member/login/login.html';
        return;
    }
    window.location = `/Jamigo/shop/shopping/cart_detail_page.html`;
}