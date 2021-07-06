package com.iftm.client.tests.integration;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import com.iftm.client.tests.factory.ClientFactory;

@SpringBootTest
@Transactional
public class ClientServiceIT {
	
	@Autowired
	private ClientService service;
	
	private long existingId;
	private long nonExistingId;
	private long countClientByIncome;
	private long countTotalClients;
	private long countTotalClientsAfterInsert;
	private long countClientsAfterDeletion;
	private String existingName; 
	private String existingCPF;
	private String updatedClientName;
	private PageRequest pageRequest;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L;
		nonExistingId = 1000L;	
		countClientByIncome = 5L;
		countTotalClients = 12L;
		countClientsAfterDeletion = 11L;
		countTotalClientsAfterInsert = 13L;
		existingName = "Conceição Evaristo";
		updatedClientName = "Conceição Evaristo Santos";
		existingCPF = "10619244881";
		pageRequest = PageRequest.of(0, 6);
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExistis() {
		
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
	}
	
	@Test
	public void findByIncomeShouldReturnClientsWhenClientIncomeIsGreaterThanOrEqualsToValue() {
		
		Double income = 4000.0;
		
		Page<ClientDTO> result = service.findByIncome(income, pageRequest);
		
		Assertions.assertFalse(result.isEmpty());
		Assertions.assertEquals(countClientByIncome, result.getTotalElements());
	}
	
	@Test
	public void findAllShouldReturnAllClients() {
		
		List<ClientDTO> result = service.findAll();
		
		Assertions.assertEquals(countTotalClients, result.size());
	}
	
	
	/*------------------------------- Atividade: Testes de integração ---------------------------------------*/
	
	@Test
	public void deleteShouldSubtractOneClientFromClientsList() {
		service.delete(existingId);
		List<ClientDTO> clients = service.findAll();
		
		Assertions.assertEquals(countClientsAfterDeletion, clients.size());
	}
	
	@Test
	public void findByIdShouldReturnAClientWithCorrectDataWhenIdExists() {
		ClientDTO client = service.findById(existingId); 
		
		Assertions.assertEquals(existingName, client.getName());
		Assertions.assertEquals(existingCPF, client.getCpf());
	}
	
	@Test
	public void insertShouldAddClientAndIncrementClientListSize() {
		Assertions.assertEquals(countTotalClients, service.findAll().size());
		
		ClientDTO newClient = ClientFactory.createClientDTO();
		service.insert(newClient);
		
		Assertions.assertEquals(countTotalClientsAfterInsert, service.findAll().size());
	}
	
	@Test
	public void updateShouldReturnAClientDTOWithCorrectUpdatedData() {
		ClientDTO existingClient = service.findById(existingId);
		existingClient.setName(updatedClientName);
		ClientDTO updatedClient = service.update(existingId, existingClient);
		
		Assertions.assertEquals(updatedClientName, updatedClient.getName());
	}
	
}
