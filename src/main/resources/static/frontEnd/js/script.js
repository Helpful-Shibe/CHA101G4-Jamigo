// 創建一個<link>元素
var linkElement = document.createElement("link");

// 設定<link>元素的屬性
linkElement.rel = "icon";
linkElement.href = "/Jamigo/index/首頁/images/icon.ico";
linkElement.type = "image/x-icon";

// 獲取<head>元素
var headElement = document.getElementsByTagName("head")[0];

// 添加<link>元素到<head>元素中
headElement.appendChild(linkElement);



//判斷當前頁面，導覽列按鈕停留
const currentPage = window.location.href;
const links = document.querySelectorAll('.button-01');

links.forEach(link => {
    if (link.href === currentPage) {
        link.classList.add('active');
    }
});


//登入登出
function openmemberinfo() {
    document.getElementById('signin').style.display = 'none';
    document.getElementById('memberinfo').style.display = 'flex';
}

function opensignin() {
    document.getElementById('signin').style.display = 'flex';
    document.getElementById('memberinfo').style.display = 'none';

}

(() => {
    const memberimg = document.querySelector('#memberimg');
    const membername = document.querySelector('#membername');
    let memberid = localStorage.getItem('memberNo');
    if (typeof memberid === 'undefined' || memberid === null ||memberid ==="") {
        memberid = sessionStorage.getItem('memberNo');
    }
    let memberaccount = localStorage.getItem('memberAccount');
    if (typeof memberaccount === 'undefined' || memberaccount === null|| memberaccount ==="") {
        memberaccount = sessionStorage.getItem('memberAccount');
    }
    console.log("memberaccount" + memberaccount+"ccc")
    checklogin();//確認登入方法

    function checklogin() {
        if (memberid) {
            openmemberinfo();

            let img = new Image();
            img.onload = function() {
                // 图片加载成功
                memberimg.src = `/Jamigo/member/member_data/${memberid}`;
            };
            img.onerror = function() {
                // 图片加载失败，使用默认图片路径
                memberimg.src = '/Jamigo/member/member/image/gray.jpg';
            };
            img.src = `/Jamigo/member/member_data/${memberid}`;

            membername.innerText = 'HI! ' + memberaccount;
        }
    }


    const logout = document.querySelector('#logoutbutton');
    logout?.addEventListener('click', () => {
        opensignin();
        localStorage.removeItem('memberNo');
        localStorage.removeItem('memberAccount');
        localStorage.removeItem('memberorcount');
        localStorage.removeItem('levelNo');
        sessionStorage.removeItem('memberNo');
        sessionStorage.removeItem('memberAccount');
        sessionStorage.removeItem('memberorcount');
        console.log("登出");
        fetch("log_out")
            .then(
                window.location.href = "/Jamigo/index/首頁/index.html"
                )
    })

})();

function check(event) {

    let memberNo = localStorage.getItem('memberNo');
    if (typeof memberNo === 'undefined' || memberNo === null||memberNo === "") {
        memberNo = sessionStorage.getItem('memberNo');
    }
    if (memberNo) {
        return;
    } else {
        event.preventDefault();
        window.location.href = '/Jamigo/member/login/login.html';
    }

}