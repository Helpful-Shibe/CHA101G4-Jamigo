package com.jamigo.member.member_data.controller;

import com.jamigo.member.member_data.Service.MemberService;
import com.jamigo.member.member_data.entity.MemberData;
import com.jamigo.promotion.PromotionType.Service.PromotionTypeService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Base64;

import static com.jamigo.member.member_data.util.CommonUtil.json2Pojo;
import static com.jamigo.member.member_data.util.CommonUtil.writePojo2Json;
@WebServlet("/member/member/edit")
public class editServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    @Autowired
    private MemberService SERVICE;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("application/json; charset=utf-8");

        MemberData memberData = json2Pojo(request, MemberData.class);
        if (memberData == null) {
            memberData = new MemberData();
            memberData.setMessage("無會員資訊");
            memberData.setSuccessful(false);
            writePojo2Json(response, memberData);
            return;
        }
        //把圖片從base64轉型成byte[]
        if(memberData.getMemberPic4json() != null && !memberData.getMemberPic4json().isEmpty()){
            byte[] MemberPic = Base64.getDecoder().decode(memberData.getMemberPic4json());
            memberData.setMemberPic(MemberPic);
            memberData.setMemberPic4json("有傳圖進來");
        }else{
            memberData.setMemberPic4json("沒有傳圖進來");
        }

        memberData = SERVICE.edit(memberData);

        if (memberData.isSuccessful()) {
            if (request.getSession(false) != null) {
                request.changeSessionId();
            }
            if(memberData.getMemberPic()!=null){
                memberData.setMemberPic4json("有圖");
            }else {
                memberData.setMemberPic4json("沒有圖");
            }
        }
        writePojo2Json(response, memberData);
    }
}
