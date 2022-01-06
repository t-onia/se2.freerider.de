package de.freerider.restapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import de.freerider.datamodel.Customer;
import de.freerider.repository.CustomerRepository;
import de.freerider.restapi.dto.CustomerDTO;

@RestController
public class CustomersDTOController implements CustomersDTOAPI {

	@Autowired
	private CustomerRepository customerRepository;
	//
	@Autowired
	private ApplicationContext context;
	//
	private final HttpServletRequest request;

	/**
	 * Constructor.
	 * 
	 * @param objectMapper entry point to JSON tree for the Jackson library
	 * @param request      HTTP request object
	 */
	public CustomersDTOController(HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public ResponseEntity<List<CustomerDTO>> getCustomers() {
		ResponseEntity<List<CustomerDTO>> re = null;
		Iterable<Customer> customers = customerRepository.findAll();
		System.err.println(request.getMethod() + " " + request.getRequestURI());
		List<CustomerDTO> list = new ArrayList<CustomerDTO>();
		for (Customer c : customers) {
			CustomerDTO dto = new CustomerDTO(c);
			list.add(dto);
		}
		re = new ResponseEntity<List<CustomerDTO>>(list, HttpStatus.OK);
		return re;
	}

	@Override
	public ResponseEntity<CustomerDTO> getCustomer(@PathVariable("id") long id) {
		ResponseEntity<CustomerDTO> re = null;
		System.err.println(request.getMethod() + " " + request.getRequestURI());
		Optional<Customer> c = customerRepository.findById(id);
		if (c.isPresent()) {
			CustomerDTO dto = new CustomerDTO(c.get());
			re = new ResponseEntity<CustomerDTO>(dto, HttpStatus.OK);
		} else {
			System.err.println("No Customer with id: " + id + " was found.");
			re = new ResponseEntity<CustomerDTO>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return re;
	}

	@Override
	public ResponseEntity<List<CustomerDTO>> postCustomers(@RequestBody List<CustomerDTO> dtos) {
		System.err.println("POST /customers");
		List<CustomerDTO> unaccepted = new ArrayList<CustomerDTO>();
		if (dtos == null) {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
		}
		for (CustomerDTO dto : dtos) {
			dto.print();
			Optional<Customer> customer = dto.create();
			if (customer.isPresent()) {
				if (customerRepository.findById(customer.get().getId()).isPresent()) {
					unaccepted.add(dto);
				} else {
					customerRepository.save(customer.get());
					System.out.println("New Customer created.");
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

	/**
	 * PUT /customers
	 */
	@Override
	public ResponseEntity<List<CustomerDTO>> putCustomers(@RequestBody List<CustomerDTO> dtos) {
		// TODO: replace by logger
		System.err.println(request.getMethod() + " " + request.getRequestURI());
		//
		dtos.stream().forEach(dto -> {
			dto.print();
			Optional<Customer> customerOpt = dto.create();
			CustomerDTO.print(customerOpt);
		});
		return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
	}

	@Override
	public ResponseEntity<?> deleteCustomer(@PathVariable("id") long id) {
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
