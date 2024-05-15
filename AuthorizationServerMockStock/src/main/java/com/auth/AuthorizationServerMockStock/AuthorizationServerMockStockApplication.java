package com.auth.AuthorizationServerMockStock;

import com.auth.AuthorizationServerMockStock.users.Users;
import com.auth.AuthorizationServerMockStock.users.UsersRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthorizationServerMockStockApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthorizationServerMockStockApplication.class, args);
	}

	@Bean
	public ApplicationRunner userLoader(
			UsersRepository usersRepo,
			PasswordEncoder encoder
	) {
		return args -> usersRepo.save(
				new Users("username", encoder.encode("password"), "ROLE_ADMIN"));
	}
}
