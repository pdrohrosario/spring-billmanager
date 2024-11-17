package com.api.billmanager.presentation;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.api.billmanager.application.service.BillService;
import com.api.billmanager.domain.enums.BillStatus;
import com.api.billmanager.presentation.controller.BillController;
import com.api.billmanager.presentation.dto.request.BillRequest;
import com.api.billmanager.presentation.dto.response.BillResponse;
import com.api.billmanager.presentation.dto.response.PaginatedResponse;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@WebMvcTest(BillController.class)
public class BillControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private final BillService billService;

    public BillControllerTest(MockMvc mockMvc, BillService billService) {
        this.mockMvc = mockMvc;
        this.billService = billService;
    }

    @Test
    public void testCreateBill() throws Exception {
        BillResponse mockResponse = new BillResponse();
        mockResponse.setId(1L);
        mockResponse.setDescription("Test Bill");

        Mockito.when(billService.create(any(BillRequest.class))).thenReturn(mockResponse);

        String requestBody = """
            {
                "description": "Test Bill",
                "amount": 100.50,
                "dueDate": "2024-11-30"
            }
        """;

        mockMvc.perform(post("/api/bill/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Bill"));
    }

    @Test
    public void testUpdate() throws Exception {
        BillResponse mockResponse = new BillResponse();
        mockResponse.setId(1L);
        mockResponse.setDescription("Updated Test Bill");

        Mockito.when(billService.update(any(BillRequest.class))).thenReturn(mockResponse);

        String requestBody = """
            {
                "description": "Updated Test Bill",
                "amount": 200.00,
                "dueDate": "2024-12-01"
            }
        """;

        mockMvc.perform(put("/api/bill/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Updated Test Bill"));
    }

    @Test
    public void testUpdateStatus() throws Exception {
        BillResponse mockResponse = new BillResponse();
        mockResponse.setId(1L);
        mockResponse.setBillStatus(BillStatus.PAID);

        Mockito.when(billService.updateStatus(eq(1L), eq("PAID"))).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/bill/status/1/PAID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    public void testGetById() throws Exception {
        BillResponse mockResponse = new BillResponse();
        mockResponse.setId(1L);
        mockResponse.setDescription("Test Bill");

        Mockito.when(billService.getById(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/bill/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Bill"));
    }

    @Test
    public void testGetBillByDueDateAndDescription() throws Exception {
        PaginatedResponse<BillResponse> mockResponse = new PaginatedResponse<>();
        mockResponse.setContent(List.of(new BillResponse()));
        mockResponse.setPageNumber(0);
        mockResponse.setPageSize(10);
        mockResponse.setTotalElements(1);
        mockResponse.setTotalPages(1);

        Mockito.when(billService.getBillsByDueDateAndDescription(
                eq(LocalDate.of(2024, 11, 30)),
                eq("description"),
                any()
        )).thenReturn(mockResponse);

        mockMvc.perform(get("/api/bill/search-bills")
                        .param("dueDate", "2024-11-30")
                        .param("description", "description")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    public void testGetAmountByPeriod() throws Exception {
        BigDecimal mockAmount = new BigDecimal("500.00");

        Mockito.when(billService.getAmountByPeriod(
                eq(LocalDate.of(2024, 11, 01)),
                eq(LocalDate.of(2024, 11, 30))
        )).thenReturn(mockAmount);

        mockMvc.perform(get("/api/bill/amount-by-period")
                        .param("startDate", "2024-11-01")
                        .param("endDate", "2024-11-30"))
                .andExpect(status().isOk())
                .andExpect(content().string("Total amount between 2024-11-01 and 2024-11-30 : 500.00"));
    }

    @Test
    public void testUploadFile() throws Exception {
        Mockito.doNothing().when(billService).importCsvBill(any());

        mockMvc.perform(multipart("/api/bill/csv/import")
                        .file("file", "test,csv,content".getBytes()))
                .andExpect(status().isOk())
                .andExpect(content().string("Bills imported from file csv!"));
    }
}
