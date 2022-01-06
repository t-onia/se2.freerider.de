package de.freerider.restapi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.freerider.app.Application;
import de.freerider.datamodel.Customer;
import de.freerider.repository.CustomerRepository;

//@RestController
public class CustomersController implements CustomersAPI {

	@Autowired
	private CustomerRepository customerRepository;
	//
	@Autowired
	private ApplicationContext context;
	//
	private final ObjectMapper objectMapper;
	//
	private final HttpServletRequest request;
	//

	/**
	 * Constructor.
	 * 
	 * @param objectMapper entry point to JSON tree for the Jackson library
	 * @param request      HTTP request object
	 */
	public CustomersController(ObjectMapper objectMapper, HttpServletRequest request) {
		this.objectMapper = objectMapper;
		this.request = request;
	}

	/**
	 * GET /customers
	 * 
	 * Return JSON Array of customers (compact).
	 * 
	 * @return JSON Array of customers
	 */
	@Override
	public ResponseEntity<List<?>> getCustomers() {
		//
		ResponseEntity<List<?>> re = null;
		System.err.println(request.getMethod() + " " + request.getRequestURI());
		try {
			ArrayNode arrayNode = customersAsJSON();
			ObjectReader reader = objectMapper.readerFor(new TypeReference<List<ObjectNode>>() {
			});
			List<String> list = reader.readValue(arrayNode);
			//
			re = new ResponseEntity<List<?>>(list, HttpStatus.OK);

		} catch (IOException e) {
			re = new ResponseEntity<List<?>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return re;
	}

	/**
	 * GET /customers/id
	 * 
	 * Return JSON of customer (compact).
	 * 
	 * @return JSON of customer
	 */
	@Override
	public ResponseEntity<?> getCustomer(long id) {
		//
		ResponseEntity<List<?>> re = null;
		System.err.println(request.getMethod() + " " + request.getRequestURI());
		try {
			ArrayNode arrayNode = customerAsJSON(id);
			ObjectReader reader = objectMapper.readerFor(new TypeReference<List<ObjectNode>>() {
			});
			List<String> list = reader.readValue(arrayNode);
			//
			re = new ResponseEntity<List<?>>(list, HttpStatus.OK);

		} catch (IOException e) {
			re = new ResponseEntity<List<?>>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return re;
	}

	private ArrayNode customerAsJSON(long id) {
		//
		ArrayNode arrayNode = objectMapper.createArrayNode();
		Optional<Customer> optCustomer = customerRepository.findById(id);
		StringBuffer sb = new StringBuffer();
		if (optCustomer.isPresent()) {
			Customer c = optCustomer.get();
			c.getContacts().forEach(contact -> sb.append(sb.length() == 0 ? "" : "; ").append(contact));
			arrayNode.add(objectMapper.createObjectNode().put("id", c.getId()).put("first name", c.getFirstName())
					.put("last name", c.getLastName()).put("contacts", sb.toString()));
		}
		return arrayNode;
	}

	private ArrayNode customersAsJSON() {
		//
		ArrayNode arrayNode = objectMapper.createArrayNode();
		//
		Iterable<Customer> customers = customerRepository.findAll();

		customers.forEach(c -> {
			StringBuffer sb = new StringBuffer();
			c.getContacts().forEach(contact -> sb.append(sb.length() == 0 ? "" : "; ").append(contact));
			arrayNode.add(objectMapper.createObjectNode().put("id", c.getId()).put("first name", c.getFirstName())
					.put("last name", c.getLastName()).put("contacts", sb.toString()));
		});
		return arrayNode;
	}

	@Override
	public ResponseEntity<List<?>> postCustomers(Map<String, Object>[] jsonMap) {
		System.err.println("POST /customers");
		List<Map<String, Object>> unaccepted = new ArrayList<Map<String, Object>>();
		if (jsonMap == null) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		for (Map<String, Object> kvpairs : jsonMap) {
			System.out.println("[{");
			kvpairs.keySet().forEach(key -> {
				Object value = kvpairs.get(key);
				System.out.println(" [" + key + ", " + value + "]");
			});
			System.out.println("}]\n");
		}

		for (Map<String, Object> map : jsonMap) {
			Optional<Customer> customer = accept(map);
			if (customer.isPresent()) {
				if (customerRepository.findById(customer.get().getId()).isPresent()) {
					unaccepted.add(map);
				} else {
					customerRepository.save(customer.get());
				}
			} else {
				return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
		}
		if (unaccepted.isEmpty()) {
			return new ResponseEntity<>(unaccepted, HttpStatus.CREATED);
		} else {
			return new ResponseEntity<>(unaccepted, HttpStatus.CONFLICT);
		}
	}

	/*
	 * Check Json Key-Value pairs and create Customer if accepted
	 */
	private Optional<Customer> accept(Map<String, Object> kvpairs) {
		Optional<Customer> opt = Optional.empty();
		// must contain first and last name
		if (kvpairs.containsKey("name") && kvpairs.containsKey("first")) {
			Long id = (long) 1;
			// check Id
			if (kvpairs.containsKey("id")) {
				id = ((Number) kvpairs.get("id")).longValue();
				if (id <= 0) {
					// HTTP Response einfügen?!
					throw new IllegalArgumentException("Id must be > 0.");
				}
			} else { // if there's no Id, create new
				for (int i = 1; i < customerRepository.count() + 2; i++) {
					if (customerRepository.findById(Long.valueOf(i)).isEmpty()) { // find empty Id
						id = Long.valueOf(i);
						break;
					}
				}
			}
			Customer c = new Customer();
			c.setId(id).setName(kvpairs.get("first").toString(), kvpairs.get("name").toString());
			// add contacts if available
			if (kvpairs.containsKey("contacts")) {
				String contacts = kvpairs.get("contacts").toString().trim();
				if (contacts.contains(";")) {
					String[] parts = contacts.split(";");
					for (String contact : parts) {
						c.addContact(contact);
					}
				} else {
					c.addContact(contacts);
				}
			}
			opt = Optional.of(c);
		}
		return opt;
	}

	@Override
	public ResponseEntity<List<?>> putCustomers(Map<String, Object>[] jsonMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<?> deleteCustomer(long id) {
		System.err.println("DELETE /customers/" + id);
		if (customerRepository.findById(id).isPresent()) {
			customerRepository.deleteById(id);
			System.out.println("Customer deleted.");
			return new ResponseEntity<>(null, HttpStatus.ACCEPTED); // status 202
		} else {
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

}
