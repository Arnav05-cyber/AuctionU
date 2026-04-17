package com.example.org.processor;

import com.example.org.entities.OutboxEntity;
import com.example.org.repos.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox(){
        List<OutboxEntity> messages = outboxRepository.findByProcessedFalse();
        for (OutboxEntity message : messages) {
            try{
                kafkaTemplate.send("product-events", message.getAggregateId(), message.getPayload());
                message.setProcessed(true);
                outboxRepository.save(message);
                log.info("Successfully published event from outbox: {}", message.getId());
            }
            catch (Exception e){
                log.error("Failed to publish event from outbox: {}", message.getId(), e);
            }
        }
    }

}
