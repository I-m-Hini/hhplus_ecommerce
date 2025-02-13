package kr.hhplus.be.server.application.order.facade;

import kr.hhplus.be.server.application.order.dto.request.OrderFacadeRequest;
import kr.hhplus.be.server.application.order.dto.request.OrderProductDto;
import kr.hhplus.be.server.application.order.dto.response.OrderFacadeResponse;
import kr.hhplus.be.server.application.order.event.OrderCreatedEvent;
import kr.hhplus.be.server.domain.coupon.dto.CouponIssuanceResult;
import kr.hhplus.be.server.domain.coupon.enums.DiscountType;
import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.order.dto.request.OrderServiceRequest;
import kr.hhplus.be.server.domain.order.dto.response.OrderItem;
import kr.hhplus.be.server.domain.order.dto.response.OrderResult;
import kr.hhplus.be.server.domain.order.enums.OrderStateType;
import kr.hhplus.be.server.domain.order.service.OrderService;
import kr.hhplus.be.server.domain.product.dto.ProductResult;
import kr.hhplus.be.server.domain.product.service.ProductService;
import kr.hhplus.be.server.domain.user.dto.UserResult;
import kr.hhplus.be.server.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component("orderFacade")
@RequiredArgsConstructor
public class OrderFacade {

    private final OrderService orderService;
    private final UserService userService;
    private final ProductService productService;
    private final CouponService couponService;
    // private final OrderEventDataPlatformSender orderDataPlatformSender;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderFacadeResponse createOrder(OrderFacadeRequest request) {
        Long userId = request.getUserId();
        UserResult user = userService.getUserByUserId(userId);

        Long totalPrice = 0L;
        Long discountPrice = 0L;
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderProductDto> productList = request.getProductList();
        for(OrderProductDto productDto : productList){
            ProductResult product = productService.getProductByProductId(productDto.getProductId());
            OrderItem item = OrderItem.resultToItem(product, productDto.getQuantity());
            orderItems.add(item);
            totalPrice += item.getTotalPrice();

            productService.decreaseStock(item.getProductId(), item.getQuantity());
        }

        Long issuedCouponId = request.getIssuedCouponId();

        if(ObjectUtils.isNotEmpty(issuedCouponId)) { // 사용할 쿠폰 존재
            CouponIssuanceResult couponResult = couponService.useUserIssuedCoupon(userId, issuedCouponId);
            if(couponResult.getDiscountType().equals(DiscountType.AMOUNT)) { // 정액 할인
                discountPrice = couponResult.getDiscountAmount();
            } else { // 정률 할인
                Long tempDiscountPrice = couponResult.getDiscountAmount() * totalPrice / 100;
                discountPrice = (tempDiscountPrice < couponResult.getMaxDiscountAmount()) ? tempDiscountPrice : couponResult.getMaxDiscountAmount();
            }
        }

        OrderServiceRequest orderServiceRequest = new OrderServiceRequest(userId, issuedCouponId, totalPrice, discountPrice, (totalPrice - discountPrice), OrderStateType.ORDERED);
        OrderResult orderResult = orderService.createOrder(orderServiceRequest);
        List<OrderItem> orderProductResult = orderService.createOrderProduct(orderItems);
        orderResult.setItems(orderProductResult);

        //orderDataPlatformSender.send(orderResult);
        eventPublisher.publishEvent(new OrderCreatedEvent(this, orderResult));

        return OrderFacadeResponse.toResponse(orderResult);
    }
}
