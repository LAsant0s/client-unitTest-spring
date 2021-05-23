package com.iftm.client.tests.repositories;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.iftm.client.entities.Client;
import com.iftm.client.repositories.ClientRepository;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import com.iftm.client.tests.factory.ClientFactory;

@DataJpaTest
public class ClientRepositoryTests {

	@Autowired
	private ClientRepository repository;
	
	private long existingId, nonExistingId, countTotalClients, countClientsByIncome; 
	private String name, emptyName;
	Calendar nonExistingBirthDate;
	private Integer newChildrenNumber;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = Long.MAX_VALUE;
		countTotalClients = 12L;
		countClientsByIncome = 5L;
		nonExistingBirthDate = Calendar.getInstance();
		nonExistingBirthDate.set(1955, Calendar.SEPTEMBER, 24);
		name = "Clarice";
		emptyName = "";
		newChildrenNumber = 3;
	}
	
	@Test
	public void deleteShouldDeleteObjectWhenIdExists() {
		repository.deleteById(existingId);
		
		Optional<Client> result = repository.findById(existingId);
		
		Assertions.assertFalse(result.isPresent());
	}
	
	@Test
	public void deleteShouldThrowExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(EmptyResultDataAccessException.class, () -> {
			repository.deleteById(nonExistingId);
		});
	}
	
	@Test
	public void saveShouldPersistsWithAutoIncrementWhenIdIsNull() {
		Client client = ClientFactory.createClient();
		client.setId(null);
		
		client = repository.save(client);
		Optional<Client> result = repository.findById(client.getId());
		
		
		Assertions.assertNotNull(client.getId());
		Assertions.assertEquals(countTotalClients + 1, client.getId());
		Assertions.assertTrue(result.isPresent());
		Assertions.assertSame(result.get(), client);
	}
	
	@Test
	public void findByIncomeShouldReturnClientsWhenClientsIncomeIsGreaterThanOrEqualsToValue() {
		
		Double income = 4000.0; 
		PageRequest pageRequest = PageRequest.of(0,10);
		
		Page<Client> result = repository.findByIncome(income, pageRequest);
		Assertions.assertFalse(result.isEmpty());
		Assertions.assertEquals(countClientsByIncome, result.getTotalElements());
	}
	
	/* Atividade 3 - Testes JPA Repository */
	
	// Testar find para nome existente
	@Test
	public void findByNameContainingIgnoreCaseShouldReturnClientWhenInformedClientNameExists() {
		List<Client> clients = repository.findByNameContainingIgnoreCase(name);
		
		Assertions.assertFalse(clients.isEmpty());
	}
	
	// Testar find para nome existente ignorando case
	@Test
	public void findByNameContainingIgnoreCaseShouldReturnClientIgnoringClientNameCase() {
		List<Client> clients = repository.findByNameContainingIgnoreCase(name.toUpperCase());
		
		Assertions.assertFalse(clients.isEmpty());
	}
	
	// Testar find para nome vazio (Neste caso teria que retornar todos os clientes);
	@Test
	public void findByNameContainingIgnoreCaseShouldReturnAllClientsWhenNameIsEmpty() {
		List<Client> clients = repository.findByNameContainingIgnoreCase(emptyName);
		
		Assertions.assertEquals(countTotalClients, clients.size());
	}
	
	// Testar find para data de nascimento maior que determinado data de referência
	@Test
	public void findByBirthDateShouldNotReturnAnyData() {
		Date bDate = nonExistingBirthDate.getTime();
		List<Client> clients = repository.findByBirthDate(bDate.toInstant());
		
		Assertions.assertTrue(clients.isEmpty());
	}
	
	// Na classe ClientRepositoryTests, criar teste para testar o update de um cliente. Teste pelo menos dois cenários diferentes.
	@Test
	public void clientChildrenShouldUpdateWhenClientExist() {
		Client client = repository.findById(existingId).orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		client.setChildren(newChildrenNumber);
		repository.save(client);
		Client alteredDataClient = repository.findById(existingId).orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
		Assertions.assertEquals(newChildrenNumber, alteredDataClient.getChildren());
	}
	
	@Test
	public void clientRepositoryUpdateShouldThrowExceptionWhenClientDoesNotExist() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			Client client = repository.findById(nonExistingId).orElseThrow(() -> new ResourceNotFoundException("Entity not found"));
			client.setChildren(newChildrenNumber);
			repository.save(client);
		}); 
	}
}
