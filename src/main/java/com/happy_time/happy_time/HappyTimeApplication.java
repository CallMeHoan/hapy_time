package com.happy_time.happy_time;

import com.happy_time.happy_time.common.DateTimeUtils;
import com.happy_time.happy_time.common.HAPStringUtils;
import com.happy_time.happy_time.ddd.job.executor.JobExecutor;
import com.happy_time.happy_time.ddd.job.executor.ShiftScheduleExecutor;
import com.happy_time.happy_time.ddd.tool.JobTool;
import com.happy_time.happy_time.ddd.tool.ShiftResultTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@ComponentScan("com.happy_time.happy_time.*")
public class HappyTimeApplication {

	public static void main(String[] args) {
		System.setProperty("user.timezone", "Asia/Ho_Chi_Minh");
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
		SpringApplication.run(HappyTimeApplication.class, args);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return NoOpPasswordEncoder.getInstance();
	}
}
