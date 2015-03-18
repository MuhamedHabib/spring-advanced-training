package com.asseco.aha.training.spring_advanced.core.clr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class Hi implements CommandLineRunner {

	/** Logger. */
	private static final Logger LOG = LoggerFactory.getLogger(Hi.class);

	@Override
	public void run(String... args) throws Exception {
		LOG.info("Hi!");
	}

}
