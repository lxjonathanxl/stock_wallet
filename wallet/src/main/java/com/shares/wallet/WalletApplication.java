package com.shares.wallet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WalletApplication {

	private static final Logger log = LoggerFactory.getLogger(WalletApplication.class);
	public static void main(String[] args) {

		SpringApplication.run(WalletApplication.class, args);
		log.info("Application Started de boa");
	}

}
