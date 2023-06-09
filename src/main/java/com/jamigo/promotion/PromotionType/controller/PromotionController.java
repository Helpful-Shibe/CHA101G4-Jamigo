package com.jamigo.promotion.PromotionType.controller;

import com.jamigo.promotion.PromotionType.Entity.Promotion;
import com.jamigo.promotion.PromotionType.Service.PromotionTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PromotionController {
    private static final long serialVersionUID = 1L;
    @Autowired
    private PromotionTypeService SERVICE;

    @PostMapping("promotion/promotion/editpromotion")
    public Promotion editPromotion(@RequestBody Promotion promotionRequest) {
        Promotion promotion = SERVICE.edit(promotionRequest);
        System.out.println("修改controller");
        return promotion;
    }

    ;

    @PostMapping("promotion/promotion4counter/editpromotion")
    public Promotion editPromotion2(@RequestBody Promotion promotionRequest) {
        Promotion promotion = SERVICE.edit(promotionRequest);
        System.out.println("修改controller");
        return promotion;
    }

    ;

    @PostMapping("promotion/promotion/newpromotion")
    public Promotion newPromotion(@RequestBody Promotion promotionRequest) {
        Promotion promotion = SERVICE.add(promotionRequest);
        System.out.println("新增controller");

        return promotion;
    }

    ;

    @PostMapping("promotion/promotion4counter/newpromotion")
    public Promotion newPromotion2(@RequestBody Promotion promotionRequest) {
        Promotion promotion = SERVICE.add(promotionRequest);
        System.out.println("新增controller");

        return promotion;
    }

    ;

    @GetMapping("Promotion/GetAllPromotionType")
    public List<Promotion> findAll() {
        return SERVICE.findAll();
    }

    @PostMapping("promotion/promotion4counter/getcounterPromotionType")
    public List<Promotion> findbycountno(@RequestBody Promotion promotionRequest) {
        Integer counterNo = promotionRequest.getCounterNo();
        List<Promotion> PromotioncounterNo = SERVICE.findbycounterNo(counterNo);
        return PromotioncounterNo;
    }

    ;

    @PostMapping("promotion/promotion/deletepromotion")
    public Boolean deletePromotion(@RequestBody Promotion promotionRequest) {
        String promotionName = promotionRequest.getPromotionName();
        Boolean deletesucceed = SERVICE.remove(promotionName);
        return deletesucceed;
    }

    ;

    @PostMapping("promotion/promotion4counter/deletepromotion")
    public Boolean deletePromotion2(@RequestBody Promotion promotionRequest) {
        String promotionName = promotionRequest.getPromotionName();
        Boolean deletesucceed = SERVICE.remove(promotionName);
        return deletesucceed;
    }

    ;
}






