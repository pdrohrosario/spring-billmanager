package com.api.billmanager.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.api.billmanager.application.service.BillService;
import com.api.billmanager.application.service.UserService;
import com.api.billmanager.domain.enums.BillStatus;
import com.api.billmanager.domain.exception.BillNotFoundException;
import com.api.billmanager.domain.model.Bill;
import com.api.billmanager.infrastructure.persistence.BillRepositoryInterface;
import com.api.billmanager.presentation.dto.request.BillRequest;
import com.api.billmanager.presentation.dto.response.BillResponse;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BillServiceTest {

    @InjectMocks
    private BillService billService;

    @Mock
    private BillRepositoryInterface billRepository;

    @Mock
    private UserService userService;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreate() {
        BillRequest request = new BillRequest();
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setPaymentDate(LocalDate.now());
        request.setAmount(BigDecimal.valueOf(100));

        Bill billEntity = new Bill();
        BillResponse response = new BillResponse();

        when(userService.validateUserExist(anyString())).thenReturn(true);
        when(modelMapper.map(request, Bill.class)).thenReturn(billEntity);
        when(billRepository.save(any(Bill.class))).thenReturn(billEntity);
        when(modelMapper.map(billEntity, BillResponse.class)).thenReturn(response);

        BillResponse result = billService.create(request);

        assertNotNull(result);
        verify(userService).validateUserExist(anyString());
        verify(billRepository).save(any(Bill.class));
    }


    @Test
    public void testUpdate() {
        BillRequest request = new BillRequest();
        request.setId(1L);
        request.setDueDate(LocalDate.now().plusDays(5));
        request.setPaymentDate(LocalDate.now());
        request.setAmount(BigDecimal.valueOf(100));
        request.setBillStatus(BillStatus.PENDING);

        Bill existingBill = new Bill();
        existingBill.setBillStatus(BillStatus.PENDING);

        BillResponse response = new BillResponse();

        when(billRepository.findById(1L)).thenReturn(Optional.of(existingBill));
        when(billRepository.save(any(Bill.class))).thenReturn(existingBill);
        when(modelMapper.map(existingBill, BillResponse.class)).thenReturn(response);

        BillResponse result = billService.update(request);

        assertNotNull(result);
        verify(billRepository).findById(1L);
        verify(billRepository).save(any(Bill.class));
    }

    @Test
    public void testGetByIdNotFound() {
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BillNotFoundException.class, () -> billService.getById(1L));
        verify(billRepository).findById(1L);
    }

    // @Test
    // public void testImportCsvBill() throws IOException {
    //     MultipartFile mockFile = mock(MultipartFile.class);
    //     when(mockFile.getInputStream()).thenReturn(mock(InputStream.class));
    //     when(mockFile.getOriginalFilename()).thenReturn("test.csv");
    //     when(billRepository.saveAll(anyList())).thenReturn(List.of());

    //     billService.importCsvBill(mockFile);

    //     verify(billRepository).saveAll(anyList());
    // }

}
