package com.happy_time.happy_time.ddd.schedule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;

@Component
public class ScheduleApplication {
    private static final Logger LOGGER = LogManager.getLogger();

    @Scheduled(cron = "0 0 0 ? * *")
    public void scheduleTaskWithInitialDelay() {
        LOGGER.info("Chào Ngày Mới <3");
    }
}
