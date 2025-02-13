package kr.hhplus.be.server.infrastructure.external;

import kr.hhplus.be.server.application.order.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventListener {
    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("주문 이벤트 발생: 주문 ID = {}", event.getOrderResult().getOrderId());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
