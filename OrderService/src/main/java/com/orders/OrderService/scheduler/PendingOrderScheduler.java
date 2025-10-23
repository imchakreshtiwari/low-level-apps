package com.orders.OrderService.scheduler;

import com.orders.OrderService.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PendingOrderScheduler {
    private static final Logger log = LoggerFactory.getLogger(PendingOrderScheduler.class);
    private final OrderService orderService;

    public PendingOrderScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 300000) // every 5 minutes
    public void promotePending() {
        int updated = orderService.promotePendingToProcessing();
        if (updated > 0) {
            log.info("Updated {} pending orders to PROCESSING", updated);
        }
    }
}

