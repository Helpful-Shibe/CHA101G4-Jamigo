
//判斷當前頁面，導覽列按鈕停留
const currentPage = window.location.href;
const links = document.querySelectorAll('.button-01');

links.forEach(link => {
    if (link.href === currentPage) {
        link.classList.add('active1');
    }
});


//登入登出
function openmemberinfo(){
    document.getElementById('signin').style.display = 'none';
    document.getElementById('memberinfo').style.display = 'flex';
}
function opensignin(){
    document.getElementById('signin').style.display = 'flex';
    document.getElementById('memberinfo').style.display = 'none';

}
( ()=> {
    const memberimg=document.querySelector('#memberimg');
    const membername=document.querySelector('#membername');
    const memberid = localStorage.getItem('memberNo');
    const memberaccount = localStorage.getItem('memberAccount');
    checklogin();//確認登入方法

    function checklogin(){
        if(memberid){
            openmemberinfo();
            memberimg.src= `/Jamigo/member/member_data/${memberid}`;
            membername.innerText='HI!  ' +  memberaccount;
        }
    }

    const logout=document.querySelector('#logoutbutton');
    logout.addEventListener('click',()=>{
        opensignin();
        localStorage.setItem('memberNo','');
        localStorage.setItem('memberAccount','');
        window.location.href = "/Jamigo/index/首頁/index.html";
    })

})();

function check(event) {

    const memberNo = localStorage.getItem('memberNo');
    if (memberNo) {
        return;
    } else {
        event.preventDefault();
        window.location.href = '/Jamigo/member/login/login.html';
    }

}