package com.jamigo.shop.platform_order.service.impl;

import com.jamigo.member.member_coupon.dao.MemberCouponDao;
import com.jamigo.member.member_coupon.entity.MemberCoupon;
import com.jamigo.member.member_data.dao.MemberDataDAO;
import com.jamigo.promotion.CouponType.Dao.CouponTypeDao;
import com.jamigo.promotion.CouponType.Entity.CouponType;
import com.jamigo.shop.cart.dto.CartDTO;
import com.jamigo.shop.cart.service.CartService;
import com.jamigo.shop.order_detail_coupon.entity.OrderDetailCoupon;
import com.jamigo.shop.order_detail_coupon.repo.OrderDetailCouponRepository;
import com.jamigo.shop.platform_order.dto.*;
import com.jamigo.member.member_data.entity.MemberData;
import com.jamigo.member.member_level.dao.MemberLevelDetailRepository;
import com.jamigo.member.member_level.model.MemberLevelDetail;
import com.jamigo.shop.counter_order.entity.CounterOrder;
import com.jamigo.shop.counter_order.repo.CounterOrderRepository;
import com.jamigo.shop.counter_order_detail.entity.CounterOrderDetail;
import com.jamigo.shop.counter_order_detail.entity.CounterOrderDetailId;
import com.jamigo.shop.counter_order_detail.repo.CounterOrderDetailRepository;
import com.jamigo.shop.platform_order.entity.PlatformOrder;
import com.jamigo.shop.platform_order.repo.PlatformOrderRepository;
import com.jamigo.shop.platform_order.service.PlatformOrderService;
import com.jamigo.shop.product.entity.Product;
import com.jamigo.shop.product.repo.ProductRepository;
import com.jamigo.shop.product_picture.service.ProductPictureService;
import ecpay.payment.integration.AllInOne;
import ecpay.payment.integration.domain.AioCheckOutALL;
import freemarker.template.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.mail.internet.MimeMessage;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
public class PlatformOrderServiceImpl implements PlatformOrderService {

    @Autowired
    private MemberDataDAO memberDataDAO;
    @Autowired
    private PlatformOrderRepository platformOrderRepository;
    @Autowired
    private CounterOrderRepository counterOrderRepository;
    @Autowired
    private CounterOrderDetailRepository counterOrderDetailRepository;
    @Autowired
    private MemberLevelDetailRepository memberLevelDetailRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private Configuration configuration;
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private CartService cartService;
    @Autowired
    private ProductPictureService productPictureService;
    @Autowired
    private MemberCouponDao memberCouponDao;
    @Autowired
    private CouponTypeDao couponTypeDao;
    @Autowired
    private OrderDetailCouponRepository orderDetailCouponRepository;

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;


    @Override
    public MemberDataForCheckoutDTO getMemberDataForCheckout(Integer memberNo) {

        // 取得該會員的全部資料
        MemberData memberData = memberDataDAO.selectById(memberNo);

        if (memberData != null) {

            // 取得該會員的會員等級編號，透過會員等級編號查詢會員等級資訊
            MemberLevelDetail memberLevelDetail = memberLevelDetailRepository.findById(Integer.valueOf(memberData.getLevelNo())).orElse(null);

            if (memberLevelDetail != null) {

                // 將結帳會用到的資料封裝起來
                MemberDataForCheckoutDTO memberDataForCheckoutDTO = new MemberDataForCheckoutDTO();

                memberDataForCheckoutDTO.setMemberName(memberData.getMemberName());
                memberDataForCheckoutDTO.setMemberPhone(memberData.getMemberPhone());
                memberDataForCheckoutDTO.setMemberEmail(memberData.getMemberEmail());
                memberDataForCheckoutDTO.setMemberAddress(memberData.getMemberAddress());
                memberDataForCheckoutDTO.setMemberLevelDetail(memberLevelDetail);

                return memberDataForCheckoutDTO;
            } else
                return null;
        } else
            return null;
    }


    @Override
    public Map<String, List<CartDTO>> getCartInfoByMemberNo(Integer memberNo) {

        List<CartDTO> cartDTOList = cartService.findAllCartItem(memberNo);

        return cartDTOList.stream()
                .collect(Collectors.groupingBy(CartDTO::getCounterName));
    }


    @Override
    public List<PlatformOrder> getAllPlatformOrder() {
        return platformOrderRepository.findAll();
    }


    @Override
    public PlatformOrder getPlatformOrderById(Integer platformOrderNo) {
        return platformOrderRepository.findById(platformOrderNo).orElse(null);
    }


    @Override
    public Map<String, CounterOrderForPlatformOrderDTO> getPlatformOrderDetailById(Integer platformOrderNo) {

        List<PlatformOrderDetailDTO> orderDetailList = platformOrderRepository.getOrderDetailByPlatformOrderNo(platformOrderNo);

        Map<String, CounterOrderForPlatformOrderDTO> orderDetailMap = new HashMap<>();

        for (PlatformOrderDetailDTO detail : orderDetailList) {
            String counterName = detail.getCounterName();
            CounterOrderForPlatformOrderDTO counterOrder = orderDetailMap.get(counterName);

            if (counterOrder == null) {
                counterOrder = new CounterOrderForPlatformOrderDTO();
                counterOrder.setDisbursementStat(detail.getDisbursementStat());
                orderDetailMap.put(counterName, counterOrder);
            }

            ProductDetailForPlatformOrderDTO productDetail = new ProductDetailForPlatformOrderDTO();
            productDetail.setProductNo(detail.getProductNo());
            productDetail.setProductName(detail.getProductName());
            productDetail.setProductPrice(detail.getProductPrice());
            productDetail.setAmount(detail.getAmount());
            productDetail.setOrderDetailStat(detail.getOrderDetailStat());

            counterOrder.getProduct().add(productDetail);
        }

        return orderDetailMap;
    }

    @Override
    public String createPlatformOrder(CreatePlatformOrderDTO newCreatePlatformOrderDTO) {

        newCreatePlatformOrderDTO.setOrderTime(new Timestamp(System.currentTimeMillis()));

        Integer memberNo = newCreatePlatformOrderDTO.getMemberNo();

        // 透過 JSON 資料中的會員編號，取得該會員的購物車資料
        List<CartDTO> cartDTOList = cartService.findAllCartItem(memberNo);

        // 價錢計算必須放在後端
        // 計算原總金額
        int totalPaid = cartDTOList.stream()
                .mapToInt(cartItem -> cartItem.getProductPrice() * cartItem.getQuantity())
                .sum();

        int totalCoupon = 0;



        PlatformOrder newPlatformOrder = new PlatformOrder();
        newPlatformOrder.setMemberNo(memberNo);
        newPlatformOrder.setBuyerName(newCreatePlatformOrderDTO.getBuyerName());
        newPlatformOrder.setBuyerPhone(newCreatePlatformOrderDTO.getBuyerPhone());
        newPlatformOrder.setBuyerEmail(newCreatePlatformOrderDTO.getBuyerEmail());
        newPlatformOrder.setPaymentMethod(newCreatePlatformOrderDTO.getPaymentMethod());
        newPlatformOrder.setPickupMethod(newCreatePlatformOrderDTO.getPickupMethod());
        newPlatformOrder.setDeliveryCountry(newCreatePlatformOrderDTO.getDeliveryCountry());
        newPlatformOrder.setDeliveryAddress(newCreatePlatformOrderDTO.getDeliveryAddress());
        newPlatformOrder.setInvoiceMethod(newCreatePlatformOrderDTO.getInvoiceMethod());
        newPlatformOrder.setInvoiceGui(newCreatePlatformOrderDTO.getInvoiceGui());
        newPlatformOrder.setTotalPaid(totalPaid);
        newPlatformOrder.setTotalCoupon(totalCoupon);
        newPlatformOrder.setTotalPoints(newCreatePlatformOrderDTO.getTotalPoints());
//        newPlatformOrder.setActuallyPaid(actuallyPaid);
        newPlatformOrder.setActuallyPaid(0);
//        newPlatformOrder.setRewardPoints(rewardPoints);
        newPlatformOrder.setRewardPoints(0);
        newPlatformOrder.setOrderTime(newCreatePlatformOrderDTO.getOrderTime());

        switch (newCreatePlatformOrderDTO.getPaymentMethod()) {

            case 1:
                newPlatformOrder.setPlatformOrderStat((byte) 10);
                newPlatformOrder.setPaymentStat((byte) 0);
                break;

            case 2:
                newPlatformOrder.setPlatformOrderStat((byte) 20);
                newPlatformOrder.setPaymentStat((byte) 0);
        }

        // 更新訂單資訊
        PlatformOrder savedPlatformOrder = platformOrderRepository.save(newPlatformOrder);

        // 取得平台訂單編號
        Integer platformOrderNo = savedPlatformOrder.getPlatformOrderNo();

        for (var id : newCreatePlatformOrderDTO.getMemberCouponIdList()) {

            MemberCoupon memberCoupon = memberCouponDao.findById(id).orElse(null);

            if (memberCoupon != null) {

                CouponType couponType = couponTypeDao.selectById(memberCoupon.getMemberCouponId().getCouponTypeNo());

                if (couponType.getCounterNo() == null) {
                    totalCoupon += couponType.getCouponPrice();

                    OrderDetailCoupon newOrderDetailCoupon = new OrderDetailCoupon();
                    newOrderDetailCoupon.setPlatformOrderNo(platformOrderNo);
                    OrderDetailCoupon savedOrderDetailCoupon = orderDetailCouponRepository.save(newOrderDetailCoupon);

                    memberCoupon.setCouponUsedStat((byte) 1);
                    memberCoupon.setCouponUsedTime(newCreatePlatformOrderDTO.getOrderTime());
                    memberCoupon.setOrderDetailCouponNo(savedOrderDetailCoupon.getOrderDetailCouponNo());

                    memberCouponDao.save(memberCoupon);
                }

            }
        }


        // 拆單
        Map<Integer, List<CartDTO>> cartMap = cartDTOList.stream()
                .collect(Collectors.groupingBy(CartDTO::getCounterNo));

        for (var entry : cartMap.entrySet()) {

            Integer counterNo = entry.getKey();
            List<CartDTO> productList = entry.getValue();

            CounterOrder newCounterOrder = new CounterOrder();
            newCounterOrder.setPlatformOrderNo(platformOrderNo);
            newCounterOrder.setCounterNo(counterNo);
            newCounterOrder.setTotalPaid(0);  // 先設定櫃位訂單的 totalPaid 為 0，等等再更新
            newCounterOrder.setActuallyPaid(0);
            newCounterOrder.setCounterOrderStat(savedPlatformOrder.getPlatformOrderStat());
            newCounterOrder.setDisbursementStat((byte) 0);

            CounterOrder savedCounterOrder = counterOrderRepository.save(newCounterOrder);

            // 取得櫃位訂單編號
            Integer counterOrderNo = savedCounterOrder.getCounterOrderNo();

            int counterTotalPaid = 0;

            for (var product : productList) {
                CounterOrderDetail newCounterOrderDetail = new CounterOrderDetail();

                CounterOrderDetailId id = new CounterOrderDetailId();
                id.setCounterOrderNo(counterOrderNo);
                id.setProductNo(product.getProductNo());

                newCounterOrderDetail.setId(id);
                newCounterOrderDetail.setAmount(product.getQuantity());
                newCounterOrderDetail.setOrderDetailStat(savedPlatformOrder.getPlatformOrderStat());

                counterOrderDetailRepository.save(newCounterOrderDetail);

                counterTotalPaid += product.getProductPrice() * product.getQuantity();

                Product p = productRepository.findById(product.getProductNo()).orElse(null);

                if (p != null)
                    p.setProductSaleNum(p.getProductSaleNum() + product.getQuantity());

            }

            savedCounterOrder.setTotalPaid(counterTotalPaid);

            int counterActuallyPaid = counterTotalPaid;

            for (var id : newCreatePlatformOrderDTO.getMemberCouponIdList()) {

                MemberCoupon memberCoupon = memberCouponDao.findById(id).orElse(null);

                if (memberCoupon != null) {

                    CouponType couponType = couponTypeDao.selectById(memberCoupon.getMemberCouponId().getCouponTypeNo());

                    if (Objects.equals(couponType.getCounterNo(), counterNo)) {
                        totalCoupon += couponType.getCouponPrice();
                        counterActuallyPaid -= couponType.getCouponPrice();

                        OrderDetailCoupon newOrderDetailCoupon = new OrderDetailCoupon();
                        newOrderDetailCoupon.setCounterOrderNo(counterOrderNo);
                        OrderDetailCoupon savedOrderDetailCoupon = orderDetailCouponRepository.save(newOrderDetailCoupon);

                        memberCoupon.setCouponUsedStat((byte) 1);
                        memberCoupon.setCouponUsedTime(newCreatePlatformOrderDTO.getOrderTime());
                        memberCoupon.setOrderDetailCouponNo(savedOrderDetailCoupon.getOrderDetailCouponNo());

                        memberCouponDao.save(memberCoupon);
                    }

                }
            }


            savedCounterOrder.setActuallyPaid(counterActuallyPaid);

            counterOrderRepository.save(savedCounterOrder);
        }

        int actuallyPaid = totalPaid - totalCoupon - newCreatePlatformOrderDTO.getTotalPoints();

        // 查詢會員等級資訊，計算回饋點數時會用到
        MemberData memberData = memberDataDAO.selectById(newCreatePlatformOrderDTO.getMemberNo());
        memberData.setMemberPoints(memberData.getMemberPoints() - newCreatePlatformOrderDTO.getTotalPoints());

        MemberLevelDetail memberLevelDetail = memberLevelDetailRepository.findById(Integer.valueOf(memberData.getLevelNo())).orElse(null);
        // 計算回饋點數
        int levelFeedback = (memberLevelDetail != null) ? memberLevelDetail.getLevelFeedback() : 1;
        int rewardPoints = Math.round(actuallyPaid / 10.0f * levelFeedback);

        savedPlatformOrder.setTotalCoupon(totalCoupon);
        savedPlatformOrder.setActuallyPaid(actuallyPaid);
        savedPlatformOrder.setRewardPoints(rewardPoints);
        PlatformOrder finalPlatformOrder = platformOrderRepository.save(savedPlatformOrder);


        // 刪除會員所有存放在購物車的商品
        for (var cartItem : cartDTOList) {
            cartService.deleteOneInCart(cartItem, memberNo);
        }

        if (finalPlatformOrder.getPaymentMethod() == 1)
            return ecpayCheckout(finalPlatformOrder);
        else {
            taskExecutor.execute(() -> {
                try {
                    sendEmail(finalPlatformOrder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return null;
        }
    }

    public String ecpayCheckout(PlatformOrder newPlatformOrder) {

        String uuId = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);

        Timestamp timestamp = newPlatformOrder.getOrderTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String strTimestamp = sdf.format(timestamp);

        AllInOne all = new AllInOne("");

        AioCheckOutALL obj = new AioCheckOutALL();
        obj.setMerchantTradeNo(uuId);
        obj.setMerchantTradeDate(strTimestamp);
        obj.setTotalAmount(String.valueOf(newPlatformOrder.getActuallyPaid()));

        obj.setTradeDesc("Jamigo Mall 購物測試");
        obj.setItemName("Jamigo Mall 商品");
        String orderResultURL = "http://localhost:8080/Jamigo/shop/platform_order/" + newPlatformOrder.getPlatformOrderNo().toString() + "/paidResult";
        obj.setOrderResultURL(orderResultURL);
        obj.setReturnURL("http://211.23.128.214:5000");
        obj.setNeedExtraPaidInfo("N");
        obj.setClientBackURL("http://localhost:8080/Jamigo/shop/main_page/shopping_main_page.html");
        String form = all.aioCheckOut(obj, null);

        return form;
    }

    @Override
    public void changePaidStat(Integer platformOrderNo, String formData) {

        Map<String, String> map = new HashMap<String, String>();

        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            map.put(pair.substring(0, idx), pair.substring(idx + 1));
        }

        if ("1".equals(map.get("RtnCode"))) {
            PlatformOrder platformOrder = platformOrderRepository.findById(platformOrderNo).orElse(null);

            if (platformOrder != null) {
                platformOrder.setPaymentStat((byte) 1);
                platformOrder.setPlatformOrderStat((byte) 20);
                platformOrderRepository.save(platformOrder);

                List<CounterOrder> counterOrderList = counterOrderRepository.findAllByPlatformOrderNo(platformOrderNo);

                for (var counterOrder : counterOrderList) {

                    List<CounterOrderDetail> counterOrderDetailList = counterOrderDetailRepository.findAllByIdCounterOrderNo(counterOrder.getCounterOrderNo());

                    for (var counterOrderDetail : counterOrderDetailList) {
                        counterOrderDetail.setOrderDetailStat((byte) 20);
                        counterOrderDetailRepository.save(counterOrderDetail);
                    }

                    counterOrder.setCounterOrderStat((byte) 20);
                    counterOrderRepository.save(counterOrder);
                }

                taskExecutor.execute(() -> {
                    try {
                        sendEmail(platformOrder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    @Override
    public void sendEmail(PlatformOrder platformOrder) throws Exception {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        helper.setSubject("[Jamigo Mall 線上商城] 您的訂單正在準備中");
        helper.setFrom("jamigo.contact@gmail.com", "Jamigo Mall");
        helper.setTo(platformOrder.getBuyerEmail());

        Map<String, byte[]> images = new HashMap<>();
        String emailContent = getEmailContent(platformOrder, images);
        helper.setText(emailContent, true);

        // 將所有的圖片添加到電子郵件中
        for (Map.Entry<String, byte[]> image : images.entrySet()) {
            String id = image.getKey();
            byte[] bytes = image.getValue();
            helper.addInline(id, new ByteArrayResource(bytes), "image/gif");
        }

        javaMailSender.send(mimeMessage);
    }

    @Override
    public String getEmailContent(PlatformOrder platformOrder, Map<String, byte[]> images) throws Exception {

        StringWriter stringWriter = new StringWriter();
        Map<String, Object> model = new HashMap<>();

        // 將你的資料添加到模型中
        Map<String, CounterOrderForPlatformOrderDTO> orderDetailMap = getPlatformOrderDetailById(platformOrder.getPlatformOrderNo());
        model.put("orderDetailMap", orderDetailMap);
        model.put("platformOrder", platformOrder);

        // 遍歷所有的訂單，獲取每個商品的圖片
        for (CounterOrderForPlatformOrderDTO counterOrder : orderDetailMap.values()) {
            for (ProductDetailForPlatformOrderDTO product : counterOrder.getProduct()) {
                byte[] image = productPictureService.getFirstProductPicByProductNo(product.getProductNo());
                images.put("image" + product.getProductNo(), image);
            }
        }

        configuration.getTemplate("order_confirm.ftlh").process(model, stringWriter);
        return stringWriter.getBuffer().toString();
    }

    @Override
    public List<PlatformOrder> getAllPlatformOrderByMemberNo(Integer memberNo) {
        return platformOrderRepository.findAllByMemberNo(memberNo);
    }

    @Transactional
//    @Scheduled(fixedRate = 6000) // 每六秒執行一次
    @Scheduled(cron = "0 0 0 * * *")  // 每天 0 點執行一次
    public void updatePlatformOrderStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = now.minusDays(4);
        LocalDateTime endDateTime = now.minusDays(2);
        Timestamp end = Timestamp.valueOf(endDateTime);
        Timestamp start = Timestamp.valueOf(startDateTime);

        List<PlatformOrder> platformOrderList = platformOrderRepository.findAllOrdersBetween(start, end);
        for (var platformOrder : platformOrderList) {

            LocalDateTime orderDateTime = platformOrder.getOrderTime().toLocalDateTime();
            long hours = ChronoUnit.HOURS.between(orderDateTime, now);

            if (hours >= 72 && platformOrder.getPlatformOrderStat() == 10 && platformOrder.getPaymentMethod() == 1) {

                platformOrder.setPlatformOrderStat((byte) 0);
                platformOrder.setPaymentStat((byte) 2);
                platformOrderRepository.save(platformOrder);

                List<CounterOrder> counterOrderList = counterOrderRepository.findAllByPlatformOrderNo(platformOrder.getPlatformOrderNo());

                for (var counterOrder : counterOrderList) {

                    counterOrder.setCounterOrderStat((byte) 0);
                    counterOrder.setDisbursementStat((byte) 2);
                    counterOrderRepository.save(counterOrder);

                    List<CounterOrderDetail> counterOrderDetailList = counterOrderDetailRepository.findAllByIdCounterOrderNo(counterOrder.getCounterOrderNo());

                    for (var counterOrderDetail : counterOrderDetailList) {

                        counterOrderDetail.setOrderDetailStat((byte) 0);
                        counterOrderDetailRepository.save(counterOrderDetail);
                    }
                }
            }
        }

//        System.out.println("執行取消訂單的排程器");
    }

    @Override
    public void editPlatformOrderStat(Integer platformOrderNo, EditPlatformOrderDTO editPlatformOrderDTO) {

        byte stat = editPlatformOrderDTO.getPlatformOrderStat();

        if (stat == 60) {

            PlatformOrder platformOrder = platformOrderRepository.findById(platformOrderNo).orElse(null);

            if (platformOrder != null) {

                platformOrder.setPlatformOrderStat(stat);
                platformOrder.setPaymentStat((byte) 1);
                platformOrderRepository.save(platformOrder);

                List<CounterOrder> counterOrderList = counterOrderRepository.findAllByPlatformOrderNo(platformOrder.getPlatformOrderNo());

                for (var counterOrder : counterOrderList) {

                    counterOrder.setCounterOrderStat(stat);
                    counterOrderRepository.save(counterOrder);

                    List<CounterOrderDetail> counterOrderDetailList = counterOrderDetailRepository.findAllByIdCounterOrderNo(counterOrder.getCounterOrderNo());

                    for (var counterOrderDetail : counterOrderDetailList) {

                        counterOrderDetail.setOrderDetailStat(stat);
                        counterOrderDetailRepository.save(counterOrderDetail);
                    }
                }
            }
        }
        else if (stat == 70) {
            PlatformOrder platformOrder = platformOrderRepository.findById(platformOrderNo).orElse(null);


            if (platformOrder != null) {

                MemberData memberData = memberDataDAO.selectById(platformOrder.getMemberNo());

                platformOrder.setPlatformOrderStat(stat);
                platformOrderRepository.save(platformOrder);

                memberData.setMemberPoints(memberData.getMemberPoints() + platformOrder.getRewardPoints());
                memberDataDAO.update(memberData);

                List<CounterOrder> counterOrderList = counterOrderRepository.findAllByPlatformOrderNo(platformOrder.getPlatformOrderNo());

                for (var counterOrder : counterOrderList) {

                    counterOrder.setCounterOrderStat(stat);
                    counterOrder.setDisbursementStat((byte) 1);
                    counterOrderRepository.save(counterOrder);

                    List<CounterOrderDetail> counterOrderDetailList = counterOrderDetailRepository.findAllByIdCounterOrderNo(counterOrder.getCounterOrderNo());

                    for (var counterOrderDetail : counterOrderDetailList) {

                        counterOrderDetail.setOrderDetailStat(stat);
                        counterOrderDetailRepository.save(counterOrderDetail);
                    }
                }
            }
        }

    }

}
