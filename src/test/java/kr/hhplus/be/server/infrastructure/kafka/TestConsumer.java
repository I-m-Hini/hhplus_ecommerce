package kr.hhplus.be.server.infrastructure.kafka;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Getter
@Component
public class TestConsumer {

    private final List<String> messageList = new ArrayList<>();

    @KafkaListener(topics = "test-topic", groupId = "hhplus-consumer-group")
    public void receive(String message) {
        log.info("Received message: {}", message);
        messageList.add(message);
    }
}
