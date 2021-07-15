package com.iftm.client.tests.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iftm.client.dto.ClientDTO;
import com.iftm.client.services.ClientService;
import com.iftm.client.services.exceptions.ResourceNotFoundException;
import com.iftm.client.tests.factory.ClientFactory;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientResourceTests {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ClientService service; 
	
	@Autowired
	private ObjectMapper objectMapper; 
	
	private Long existingId; 
	private Long nonExistingId;
	private Long dependentId;
	private ClientDTO clientDTO;
	private ClientDTO newClientDTO;
	private List<ClientDTO> list;
	private PageImpl<ClientDTO> page;
	
	@BeforeEach
	void setUp() throws Exception {
		existingId = 1L; 
		nonExistingId = 1000L;
		dependentId = 4L;
		clientDTO = ClientFactory.createClientDTO(existingId);
		newClientDTO = ClientFactory.createClientDTO(null); 
		list = new ArrayList<ClientDTO>();
		page = new PageImpl<>(List.of(clientDTO));

		when(service.findById(existingId)).thenReturn(clientDTO);
		when(service.findById(nonExistingId)).thenThrow(ResourceNotFoundException.class); 
		
		when(service.findAll()).thenReturn(list);
		when(service.findAllPaged(any())).thenReturn(page);
		
		when(service.insert(any())).thenReturn(clientDTO);
		
		when(service.update(eq(existingId), any())).thenReturn(clientDTO);
		when(service.update(eq(nonExistingId), any())).thenThrow(ResourceNotFoundException.class);
		
		doNothing().when(service).delete(existingId);
		doThrow(ResourceNotFoundException.class).when(service).delete(nonExistingId);
		doThrow(DataIntegrityViolationException.class).when(service).delete(dependentId);
	}
	
	@Test
	public void findByIdShouldReturnClientDTOWhenIdExists() throws Exception {
		mockMvc.perform(get("/clients/{id}", existingId)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()); 
		ResultActions result = 	mockMvc.perform(get("/clients/{id}", existingId)
											.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.id").value(existingId));
	}
	
	@Test
	public void findByIdShouldReturnResourceNotFoundExceptionWhenIdDoesNotExists() throws Exception {
		ResultActions result =
		mockMvc.perform(get("/clients/{id}", nonExistingId)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isNotFound());
	}
	
	@Test
	public void findAllShoudReturnList() throws Exception {
		ResultActions result =
		mockMvc.perform(get("/clients/findAll")
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
	}
	
	@Test 
	public void findAllShouldReturnPage() throws Exception {
		ResultActions result =
				mockMvc.perform(get("/clients/")
						.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.content").exists());
	}
	
	@Test 
	public void updateShouldReturnNotFoundWhenIdDoesNotExists() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(newClientDTO);
		
		
		ResultActions result =
				mockMvc.perform(put("/clients/{id}", nonExistingId)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isNotFound());
	}
	
	public void updateShouldReturnClientDTOWhenIdExists() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(newClientDTO);
		
		String expectedName = newClientDTO.getName(); 
		Double expectedIncome = newClientDTO.getIncome();
		
		ResultActions result =
				mockMvc.perform(put("/clients/{id}", existingId)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.id").value(existingId));
		result.andExpect(jsonPath("$.name").value(expectedName));
		result.andExpect(jsonPath("$.income").value(expectedIncome));
	}
	
	
	/*------------------------------- Atividade: testes da camada web com MockMvc --------------------------------*/
	@Test
	public void insertShouldReturnCodeCreatedWhenCreateProductWithValidData() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(newClientDTO);
		
		String expectedName = newClientDTO.getName();
		Double expectedIncome = newClientDTO.getIncome();
		Instant expectedBirthDate = newClientDTO.getBirthDate();
		Integer expectedChildren = newClientDTO.getChildren();
		String expectedCPF = newClientDTO.getCpf();
		
		ResultActions result = mockMvc.perform(post("/clients")
				.content(jsonBody)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").value(existingId));
		result.andExpect(jsonPath("$.name").value(expectedName));
		result.andExpect(jsonPath("$.income").value(expectedIncome));
		result.andExpect(jsonPath("$.birthDate").value(expectedBirthDate.toString()));
		result.andExpect(jsonPath("$.children").value(expectedChildren));
		result.andExpect(jsonPath("$.cpf").value(expectedCPF));
	}
	
	@Test
	public void deleteShouldReturnCodeNoContentWhenIdDoesExists() throws Exception {
		ResultActions result =
				mockMvc.perform(delete("/clients/{id}", existingId));
		
		result.andExpect(status().isNoContent());
	}
	
	@Test
	public void deleteShouldReturnCodeNotFoundWhenIdDoesNotExists() throws Exception {
		ResultActions result =
				mockMvc.perform(delete("/clients/{id}", nonExistingId));
		
		result.andExpect(status().isNotFound());
	}
	
	@Test
	public void updateShouldReturnCodeIsOkAndClientWithUpdatedDataWhenIdExists() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(newClientDTO);

		String expectedName = newClientDTO.getName();
		Double expectedIncome = newClientDTO.getIncome();
		Instant expectedBirthDate = newClientDTO.getBirthDate();
		Integer expectedChildren = newClientDTO.getChildren();
		String expectedCpf = newClientDTO.getCpf();

		ResultActions result =
				mockMvc.perform(put("/clients/{id}", existingId)
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.id").value(existingId));
		result.andExpect(jsonPath("$.name").value(expectedName));
		result.andExpect(jsonPath("$.income").value(expectedIncome));
		result.andExpect(jsonPath("$.income").value(expectedIncome));
		result.andExpect(jsonPath("$.birthDate").value(expectedBirthDate.toString()));
		result.andExpect(jsonPath("$.children").value(expectedChildren));
		result.andExpect(jsonPath("$.cpf").value(expectedCpf));
	}
	
	@Test 
	public void updateShouldReturnNotFoundAndReturnErrorMessageWhenIdDoesNotExists() throws Exception {
		String jsonBody = objectMapper.writeValueAsString(newClientDTO);
		String expectedErrorMessage = "Resource not found";
		
		
		ResultActions result =
				mockMvc.perform(put("/clients/{id}", nonExistingId)
						.content(jsonBody)
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
				
		result.andExpect(status().isNotFound());
		result.andExpect(jsonPath("$.error").value(expectedErrorMessage));
	}
	
}