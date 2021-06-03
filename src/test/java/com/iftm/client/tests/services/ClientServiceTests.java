package com.iftm.client.tests.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.iftm.client.dto.ClientDTO;
import com.iftm.client.entities.Client;
import com.iftm.client.repositories.ClientRepository;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.DatabaseException;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import com.iftm.client.tests.factory.ClientFactory;

@ExtendWith(SpringExtension.class)
public class ClientServiceTests {
	
	@InjectMocks
	private ClientService service;
	
	@Mock
	private ClientRepository repository;
	
	private long existingId;
	private long existentId;
	private long nonExistingId;
	private long inexistentId;
	private long depedentId;
	private long depedentId2;
	private double income; 
	private PageRequest validPageRequest;
	private Client client;
	private ClientDTO clientDTO;
	private Page<Client> pageMock;
	private List<Client> fakeList; 
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1;
		existentId = 2;
		nonExistingId = 1000L;
		inexistentId = 1001L;
		depedentId = 4L;
		depedentId2 = 5L;
		client = ClientFactory.createClient();
		clientDTO = ClientFactory.createClientDTO();
		validPageRequest = PageRequest.of(0, 12, Direction.valueOf("ASC"), "name");
		fakeList = new ArrayList<>();
		fakeList.add(client);
		pageMock = new PageImpl<Client>(fakeList);
		income = 4500.0; 
		
		// Configurando comportamento para mock
		Mockito.doNothing().when(repository).deleteById(existingId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(nonExistingId);
		Mockito.doThrow(DatabaseException.class).when(repository).deleteById(depedentId);

		Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(client));
		Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());
		
		Mockito.doThrow(ResourceNotFoundException.class).when(repository).findById(nonExistingId);
		
		Mockito.when(repository.getOne(existingId)).thenReturn(client);
		Mockito.doThrow(ResourceNotFoundException.class).when(repository).getOne(nonExistingId);
		Mockito.doThrow(EmptyResultDataAccessException.class).when(repository).deleteById(inexistentId);
		Mockito.doThrow(DataIntegrityViolationException.class).when(repository).deleteById(depedentId2);
		Mockito.when(repository.save(client)).thenReturn(client);
		Mockito.when(repository.findAll(validPageRequest)).thenReturn(pageMock);
		Mockito.when(repository.findByIncome(income, validPageRequest)).thenReturn(pageMock);
	}
	
	@Test
	public void deleteShouldDoNothingWhenIdExists() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existingId);
		});
	
		Mockito.verify(repository, Mockito.times(1)).deleteById(existingId);
	}
	
	@Test
	public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(nonExistingId);
		});
	
		Mockito.verify(repository, Mockito.times(1)).deleteById(nonExistingId);
	}
	
	@Test
	public void deleteShouldThrowDatabaseExceptionWhenHasDependecyIntegrity() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(depedentId);
		});
	
		Mockito.verify(repository, Mockito.times(1)).deleteById(depedentId);
	}
	
	/*------------------------------- Atividade: Testes de service com Mockito ---------------------------------------*/
	
	
	@Test
	public void deleteShouldDoNothingWhenIdExistent() {
		Assertions.assertDoesNotThrow(() -> {
			service.delete(existentId);
		});
	
		Mockito.verify(repository, Mockito.times(1)).deleteById(existentId);
	}
	
	@Test
	public void deleteShouldThrowEmptyResultDataAccessExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.delete(inexistentId);
		});
	
		Mockito.verify(repository, Mockito.times(1)).deleteById(inexistentId);
	}
	
	@Test
	public void deleteShouldThrowDataIntegrityViolationExceptionWhenHasDependecyIntegrity() {
		Assertions.assertThrows(DatabaseException.class, () -> {
			service.delete(depedentId2);
		});
	
		Mockito.verify(repository, Mockito.times(1)).deleteById(depedentId2);
	}
	
	@Test
	public void findAllPagedShouldReturnAPageAndCallRepositoryFindAll() {
		Page<ClientDTO> page = service.findAllPaged(validPageRequest);
		
		Assertions.assertFalse(page.isEmpty());
		
		Mockito.verify(repository, Mockito.times(1)).findAll(validPageRequest);
	}
	
	@Test
	public void findByIncomeShouldReturnAPageAndCallRepositoryFindByIncome() {
		Page<ClientDTO> page = service.findByIncome(income, validPageRequest);
		
		Assertions.assertFalse(page.isEmpty());
		
		Mockito.verify(repository, Mockito.times(1)).findByIncome(income, validPageRequest);
	}
	
	@Test
	public void findByIdShouldReturnClientDTOWhenIdExists() {
		ClientDTO client = service.findById(existingId);
		Assertions.assertNotNull(client);
		
		Mockito.verify(repository, Mockito.times(1)).findById(existingId);
	}
	
	@Test
	public void findByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.findById(nonExistingId);
		});
		
		Mockito.verify(repository, Mockito.times(1)).findById(nonExistingId);
	}
	
	@Test
	public void UpdateShouldReturnClientDTOWhenIdExists() {
		ClientDTO clientUpdated = service.update(existingId, clientDTO);
		
		Assertions.assertNotNull(clientUpdated);
		
		Mockito.verify(repository, Mockito.times(1)).getOne(existingId);
		Mockito.verify(repository, Mockito.times(1)).save(client);
	}
	
	@Test
	public void UpdateShouldThrowResourceNotFoundExceptionWhenIdDoesNotExists() {
		Assertions.assertThrows(ResourceNotFoundException.class, () -> {
			service.update(nonExistingId, clientDTO);
		});
		
		Mockito.verify(repository, Mockito.times(1)).getOne(nonExistingId);
	}
	
	@Test
	public void InsertShouldReturnClientDTOWhenInsertNewClient() {
		ClientDTO dto = service.insert(clientDTO);
		
		Assertions.assertNotNull(dto);
		
		Mockito.verify(repository, Mockito.times(1)).save(clientDTO.toEntity());
	}
}
