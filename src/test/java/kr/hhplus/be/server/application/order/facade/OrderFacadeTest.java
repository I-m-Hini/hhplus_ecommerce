package kr.hhplus.be.server.application.order.facade;

import kr.hhplus.be.server.application.order.dto.request.OrderFacadeRequest;
import kr.hhplus.be.server.application.order.dto.request.OrderProductDto;
import kr.hhplus.be.server.application.order.dto.response.OrderFacadeResponse;
import kr.hhplus.be.server.application.order.event.OrderCreatedEvent;
import kr.hhplus.be.server.domain.coupon.dto.CouponIssuanceResult;
import kr.hhplus.be.server.domain.coupon.enums.CouponStateType;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @InjectMocks
    private OrderFacade orderFacade;

    @Mock
    private OrderService orderService;

    @Mock
    private CouponService couponService;

    @Mock
    private UserService userService;

    @Mock
    private ProductService productService;

//    @Mock
//    private OrderEventDataPlatformSender orderDataPlatformSender;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("쿠폰을 사용하여 주문을 성공적으로 생성한다.")
    void createOrderWithCoupon() {
        // Given
        Long userId = 1L;
        Long issuedCouponId = 10L;

        // Mock UserResult
        UserResult mockUser = UserResult.create(userId, "황지수");
        when(userService.getUserByUserId(userId)).thenReturn(mockUser);

        // Mock ProductResults
        List<OrderProductDto> productDtos = Arrays.asList(
                new OrderProductDto(101L, 2L),
                new OrderProductDto(102L, 1L)
        );

        ProductResult mockProduct1 = new ProductResult(101L, "고구마", 5000L, 100L);
        ProductResult mockProduct2 = new ProductResult(102L, "감자", 3000L, 50L);

        when(productService.getProductByProductId(101L)).thenReturn(mockProduct1);
        when(productService.getProductByProductId(102L)).thenReturn(mockProduct2);

        CouponIssuanceResult mockCoupon = CouponIssuanceResult.create(issuedCouponId, userId, "2000원 할인 쿠폰", DiscountType.AMOUNT, 2000L, 2000L, CouponStateType.UNUSED, null, LocalDateTime.now().plusDays(2));
        when(couponService.useUserIssuedCoupon(userId, issuedCouponId)).thenReturn(mockCoupon);

        Long totalPrice = 13000L; // 5000*2 + 3000*1
        Long discountPrice = 2000L;
        OrderResult mockOrderResult = OrderResult.create(1L, userId, issuedCouponId, totalPrice, discountPrice, totalPrice - discountPrice, OrderStateType.ORDERED);

        when(orderService.createOrder(any(OrderServiceRequest.class))).thenReturn(mockOrderResult);

        // Mock Order Items
        List<OrderItem> mockOrderItems = Arrays.asList(
                OrderItem.create(1L, 101L, "고구마", 2L, 5000L, 2L),
                OrderItem.create(1L, 102L, "감자", 1L, 3000L, 1L)
        );

        when(orderService.createOrderProduct(anyList())).thenReturn(mockOrderItems);

        OrderFacadeRequest request = new OrderFacadeRequest(userId, 1L, issuedCouponId, productDtos);

        // When
        OrderFacadeResponse response = orderFacade.createOrder(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getOriginPrice()).isEqualTo(totalPrice);
        assertThat(response.getDiscountPrice()).isEqualTo(discountPrice);
        assertThat(response.getSalePrice()).isEqualTo(totalPrice - discountPrice);
        assertThat(response.getItems()).hasSize(2);

        verify(userService, times(1)).getUserByUserId(userId);
        verify(productService, times(1)).getProductByProductId(101L);
        verify(productService, times(1)).getProductByProductId(102L);
        verify(productService, times(1)).decreaseStock(101L, 2L);
        verify(productService, times(1)).decreaseStock(102L, 1L);
        verify(couponService, times(1)).useUserIssuedCoupon(userId, issuedCouponId);
        verify(orderService, times(1)).createOrder(any(OrderServiceRequest.class));
        verify(orderService, times(1)).createOrderProduct(anyList());
        //verify(orderDataPlatformSender, times(1)).send(any(OrderResult.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderCreatedEvent.class));
    }

    @Test
    @DisplayName("쿠폰을 사용하지 않고 주문을 성공적으로 생성한다.")
    void createOrderWithNoCoupon() {
        // Given
        Long userId = 1L;

        // Mock UserResult
        UserResult mockUser = UserResult.create(userId, "김경덕");
        when(userService.getUserByUserId(userId)).thenReturn(mockUser);

        // Mock ProductResults
        List<OrderProductDto> productDtos = Arrays.asList(
                new OrderProductDto(101L, 2L),
                new OrderProductDto(102L, 1L)
        );

        ProductResult mockProduct1 = new ProductResult(101L, "지코바", 5000L, 100L);
        ProductResult mockProduct2 = new ProductResult(102L, "소주", 3000L, 50L);

        when(productService.getProductByProductId(101L)).thenReturn(mockProduct1);
        when(productService.getProductByProductId(102L)).thenReturn(mockProduct2);

        Long totalPrice = 13000L; // 5000*2 + 3000*1
        Long discountPrice = 0L;
        OrderResult mockOrderResult = OrderResult.create(1L, userId, null, totalPrice, discountPrice, totalPrice - discountPrice, OrderStateType.ORDERED);

        when(orderService.createOrder(any(OrderServiceRequest.class))).thenReturn(mockOrderResult);

        // Mock Order Items
        List<OrderItem> mockOrderItems = Arrays.asList(
                OrderItem.create(1L, 101L, "지코바", 2L, 5000L, 2L),
                OrderItem.create(1L, 102L, "소주", 1L, 3000L, 1L)
        );

        when(orderService.createOrderProduct(anyList())).thenReturn(mockOrderItems);

        OrderFacadeRequest request = new OrderFacadeRequest(userId, 1L, null, productDtos);

        // When
        OrderFacadeResponse response = orderFacade.createOrder(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(1L);
        assertThat(response.getOriginPrice()).isEqualTo(totalPrice);
        assertThat(response.getDiscountPrice()).isEqualTo(discountPrice);
        assertThat(response.getSalePrice()).isEqualTo(totalPrice);
        assertThat(response.getItems()).hasSize(2);

        verify(userService, times(1)).getUserByUserId(userId);
        verify(productService, times(1)).getProductByProductId(101L);
        verify(productService, times(1)).getProductByProductId(102L);
        verify(productService, times(1)).decreaseStock(101L, 2L);
        verify(productService, times(1)).decreaseStock(102L, 1L);
        verify(couponService, never()).useUserIssuedCoupon(anyLong(), anyLong());
        verify(orderService, times(1)).createOrder(any(OrderServiceRequest.class));
        verify(orderService, times(1)).createOrderProduct(anyList());
        //verify(orderDataPlatformSender, times(1)).send(any(OrderResult.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderCreatedEvent.class));
    }

}