package kr.hhplus.be.server.application.order.event;

import kr.hhplus.be.server.domain.order.dto.response.OrderResult;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderCreatedEvent extends ApplicationEvent {
    private final OrderResult orderResult;

    public OrderCreatedEvent(Object source, OrderResult orderResult) {
        super(source);
        this.orderResult = orderResult;
    }
}
