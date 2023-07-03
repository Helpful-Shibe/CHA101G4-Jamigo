( ()=> {
    const memberAccount = document.querySelector('#memberAccount');
    const password = document.querySelector('#password');
    const errMsg = document.querySelector('#errMsg');
    const btn1 = document.querySelector('#btn1');
    console.log("login.js啟動");

    const currentPageUrl = localStorage.getItem('currentPageUrl');
    let newlocation;
    if (currentPageUrl) {
        newlocation = currentPageUrl;
    }else{
        newlocation = '../../frontEnd/前台會員管理首頁.html';
    }

    btn1.addEventListener('click', function () {
        console.log("登入按鈕啟動");
        fetch('login', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({
                memberAccount: memberAccount.value,
                memberPassword: password.value
            }),
        })

            .then(resp => resp.json())
            .then(body => {
                errMsg.textContent = '';
                const {successful, message} = body;
                if (successful) {
                    const {memberNo, memberAccount} = body;
                    localStorage.setItem('memberNo', memberNo);
                    localStorage.setItem('memberAccount', memberAccount);
                    localStorage.setItem('memberorcount', '0');
                    Swal.fire({
                        position: 'center',
                        icon: 'success',
                        title: '登入成功!',
                        showConfirmButton: false,
                        timer: 1500
                    })
                    setTimeout(function() {
                        console.log("等1.5秒");
                        location = newlocation;
                    }, 1600);

                } else {
                    errMsg.textContent = message;
                    Swal.fire({
                        icon: 'error',
                        title: '登入失敗',
                        text: `${message}`,
                        footer: '<a href="">Why do I have this issue?</a>'
                    })
                }
            });
    });
})();