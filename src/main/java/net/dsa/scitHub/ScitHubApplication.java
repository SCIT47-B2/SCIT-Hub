package net.dsa.scitHub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
// JPA Auditing 활성화: @CreatedDate, @LastModifiedDate 등 자동으로 날짜를 관리
@EnableJpaAuditing
public class ScitHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScitHubApplication.class, args);
	}

}
