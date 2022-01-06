package de.freerider.repository;

import java.util.HashSet;
import java.util.Optional;

import org.springframework.stereotype.Component;

import de.freerider.datamodel.Customer;

@Component("customerRepository")
public class CustomerRepository implements CrudRepository<Customer, Long> {

	// Hashset of Customer entities as database
	private HashSet<Customer> customerSet = new HashSet<Customer>();

	@Override
	public <S extends Customer> S save(S entity) {
		if (entity != null) {
			customerSet.add(entity);
			return entity;
		} else {
			throw new IllegalArgumentException("Customer cannot be null.");
		}
	}

	@Override
	public <S extends Customer> Iterable<S> saveAll(Iterable<S> entities) {
		if (entities != null) {
			for (S e : entities) {
				if (e != null) {
					save(e);
				} else {
					throw new IllegalArgumentException();
				}
			}
			return entities;
		} else {
			throw new IllegalArgumentException("List of Customers cannot be null.");
		}
	}

	@Override
	public boolean existsById(Long id) {
		if (id != null) {
			boolean returnBoolean = false;
			for (Customer c : customerSet) {
				if (c.getId() == id) {
					returnBoolean = true;
					break; // stop after first hit
				}
			}
			return returnBoolean;
		} else {
			throw new IllegalArgumentException("Id can not be null.");
		}
	}

	@Override
	public Optional<Customer> findById(Long id) {
		Optional<Customer> opt = Optional.empty();
		if (id != null) {
			for (Customer c : customerSet) {
				if (c.getId() == id) {
					opt = Optional.of(c);
					break; // stop after first hit
				}
			}
			return opt;
		} else {
			throw new IllegalArgumentException("Id can not be null.");
		}
	}

	@Override
	public Iterable<Customer> findAll() {
		return customerSet;
	}

	@Override
	public Iterable<Customer> findAllById(Iterable<Long> ids) {
		if (ids != null) {
			HashSet<Customer> hits = new HashSet<Customer>();
			for (Long id : ids) {
				if (findById(id).isPresent()) {
					hits.add(findById(id).get());
				}
			}
			return hits;
		} else {
			throw new IllegalArgumentException("Ids can not be null.");
		}
	}

	@Override
	public long count() {
		if (customerSet != null) {
			return customerSet.size();
		} else {
			throw new IllegalArgumentException("Set of Customers cannot be null.");
		}
	}

	@Override
	public void deleteById(Long id) {
		if (id != null) {
			if (findById(id).isPresent()) {
				customerSet.remove(findById(id).get());
			}
		} else {
			throw new IllegalArgumentException("Id cannot be null.");
		}
	}

	@Override
	public void delete(Customer entity) {
		if (entity != null) {
			customerSet.remove(entity);
		} else {
			throw new IllegalArgumentException("Customer cannot be null.");
		}
	}

	@Override
	public void deleteAllById(Iterable<? extends Long> ids) {
		if (ids != null) {
			for (Long id : ids) {
				deleteById(id);
			}
		} else {
			throw new IllegalArgumentException("Ids cannot be null.");
		}
	}

	@Override
	public void deleteAll(Iterable<? extends Customer> entities) {
		if (entities != null) {
			for (Customer c : entities) {
				delete(c);
			}
		} else {
			throw new IllegalArgumentException("Ids cannot be null.");
		}
	}

	@Override
	public void deleteAll() {
		customerSet.clear();
	}

}
