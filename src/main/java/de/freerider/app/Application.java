package de.freerider.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;

import de.freerider.datamodel.*;
import de.freerider.repository.*;

@SpringBootApplication
@ComponentScan(basePackages = { "de.freerider.restapi", "de.freerider.repository" })

public class Application {

	@Autowired // Spring auto‐wires reference to CustomerRepository instance
	private CustomerRepository customerRepository;

	public static void main(String[] args) {
		System.out.println("Hello, freerider.de");
		SpringApplication.run(Application.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void runAfterSpringStartup() { // runs when Spring is ready
		customerRepository.save(new Customer().setId(1).setName("Eric", "Meyer").addContact("eric98@yahoo.com")
				.addContact("(030) 7000‐640000") // updated phone number
		);
		customerRepository.save(new Customer().setId(2).setName("Anne", "Bayer").addContact("anne24@yahoo.de")
				.addContact("(030) 3481‐23352"));
		customerRepository.save(new Customer().setId(3).setName("Tim", "Schulz‐Mueller").addContact("tim2346@gmx.de"));
		long count = customerRepository.count(); // 3 customers added to repository
		System.out.println("repository<Customer> with: " + count + " entries");
	}

}
