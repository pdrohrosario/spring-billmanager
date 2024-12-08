package com.api.billmanager.application.service;

import com.api.billmanager.domain.enums.BillStatus;
import com.api.billmanager.domain.enums.Role;
import com.api.billmanager.domain.exception.*;
import com.api.billmanager.domain.model.Bill;
import com.api.billmanager.domain.model.User;
import com.api.billmanager.infrastructure.persistence.BillRepositoryInterface;
import com.api.billmanager.infrastructure.utils.CsvUtils;
import com.api.billmanager.presentation.dto.request.BillRequest;
import com.api.billmanager.presentation.dto.request.UserRequest;
import com.api.billmanager.presentation.dto.response.BillResponse;
import com.api.billmanager.presentation.dto.response.PaginatedResponse;
import com.api.billmanager.presentation.dto.response.UserResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BillServiceTest {

    @InjectMocks
    private BillService billService;

    @Mock
    private BillRepositoryInterface billRepository;

    @Mock
    private UserService userService;

    private BillRequest request;

    User mockUser;
    private Bill bill;

    @BeforeEach
    void setUp() {
        mockUser = new User("teste@gmail.com", 1L, "###", Role.USER);
        request = BillRequest.builder()
                .paymentDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .amount(BigDecimal.TEN)
                .billStatus(BillStatus.PENDING)
                .description("new bill to pay")
                .user(new UserRequest("teste@gmail.com"))
                .build();

        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create a bill successfully when everything is OK")
    void createBillCase1() {
        //Arrange
        when(userService.findByEmail("teste@gmail.com")).thenReturn(mockUser);

        Bill savedBill = Bill.builder()
                .id(1L)
                .paymentDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .amount(BigDecimal.TEN)
                .billStatus(BillStatus.PENDING)
                .user(mockUser)
                .build();

        UserResponse userResponseExpected = new UserResponse().convertUserToResponse(mockUser);
        BillResponse billResponseExpected = BillResponse.builder()
                .id(1L)
                .paymentDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .amount(BigDecimal.TEN)
                .billStatus(BillStatus.PENDING)
                .user(userResponseExpected)
                .build();

        when(billRepository.save(any(Bill.class))).thenReturn(savedBill);

        //Action
        BillResponse billResponse = billService.create(request);

        //Assert
        assertNotNull(billResponse);
        assertEquals(billResponse, billResponseExpected);
    }

    @Test
    @DisplayName("Should not create a bill when user does not exist")
    void createBillCase2() {
        // Arrange
        String userEmail = "teste@gmail.com";

        when(userService.findByEmail(userEmail))
                .thenThrow(new UserNotFoundException("User with email " + userEmail + " not exist."));

        // Action
        Exception thrown = Assertions.assertThrows(UserNotFoundException.class, () -> {
            billService.create(request);
        });

        // Assert
        Assertions.assertEquals("User with email " + userEmail + " not exist.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should not create a bill when paymentDate is after dueDate")
    void createBillCase3() {
        // Arrange
        String userEmail = "teste@gmail.com";

        request.setPaymentDate(LocalDate.now().plusDays(90));

        // Action
        Exception thrown = Assertions.assertThrows(PaymentDateException.class, () -> {
            billService.create(request);
        });

        // Assert
        Assertions.assertEquals("The field 'paymentDate' must be a date before or equal to the  field 'dueDate'.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should update a bill successfully when everything is OK")
    void updateBillCase1() {
        //Arrange
        request.setId(1L);

        Bill billFinded = Bill.builder()
                .id(1L)
                .paymentDate(LocalDate.now())
                .amount(BigDecimal.valueOf(30L))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(BillStatus.PENDING)
                .description("new bill to pay")
                .user(mockUser)
                .build();

        Bill billUpdated = request.convertRequestToBill();
        billUpdated.setUser(mockUser);

        when(billRepository.findById(1L)).thenReturn(Optional.of(billFinded));
        when(billRepository.save(any(Bill.class))).thenReturn(billUpdated);

        BillResponse billUpdatedResponseExpected = new BillResponse().convertBillToResponse(billUpdated);

        //Action
        BillResponse billUpdatedResponse = billService.update(request);

        //Assert
        assertNotNull(billUpdatedResponse);
        assertEquals(billUpdatedResponse, billUpdatedResponseExpected);
    }

    @Test
    @DisplayName("Should not update a bill successfully when bill not exist")
    void updateBillCase2() {
        //Arrange
        request.setId(1L);

        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        //Action
        Exception thrown = Assertions.assertThrows(BillNotFoundException.class, () -> {
            billService.update(request);
        });

        //Assert
        Assertions.assertEquals("Bill with id " + 1L + " not exist.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should not update a bill successfully when bill already paid")
    void updateBillCase3() {
        //Arrange
        request.setId(1L);

        Bill billFinded = Bill.builder()
                .id(1L)
                .paymentDate(LocalDate.now())
                .amount(BigDecimal.valueOf(30L))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(BillStatus.PAID)
                .description("new bill to pay")
                .user(mockUser)
                .build();

        when(billRepository.findById(1L)).thenReturn(Optional.ofNullable(billFinded));

        //Action
        Exception thrown = Assertions.assertThrows(BillAlreadyPaidException.class, () -> {
            billService.update(request);
        });

        //Assert
        Assertions.assertEquals("This bill already paid, you can't modify the status of this bill.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should update status of bill successfully when everything ok")
    void updateStatusBillCase1() {
        //Arrange
        Bill billFinded = Bill.builder()
                .id(1L)
                .paymentDate(LocalDate.now())
                .amount(BigDecimal.valueOf(30L))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(BillStatus.PENDING)
                .description("new bill to pay")
                .user(mockUser)
                .build();

        when(billRepository.findById(1L)).thenReturn(Optional.ofNullable(billFinded));

        when(billRepository.save(billFinded)).thenReturn(billFinded);
        BillResponse billUpdatedResponseExpected = new BillResponse().convertBillToResponse(billFinded);
        billUpdatedResponseExpected.setBillStatus(BillStatus.PAID);

        //Action
        BillResponse billUpdatedResponse = billService.updateStatus(1L,"PAID");

        //Assert
        assertNotNull(billUpdatedResponse);
        assertEquals(billUpdatedResponse, billUpdatedResponseExpected);
    }

    @Test
    @DisplayName("Should not update status of bill successfully when bill not founded")
    void updateStatusBillCase2() {
        //Arrange
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        //Action
        Exception thrown = Assertions.assertThrows(BillNotFoundException.class, () -> {
            billService.updateStatus(1L,"PAID");
        });

        //Assert
        Assertions.assertEquals("Bill with id " + 1L + " not exist.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should not update status of bill successfully when bill already paid")
    void updateStatusBillCase3() {
        //Arrange
        Bill billFinded = Bill.builder()
                .id(1L)
                .paymentDate(LocalDate.now())
                .amount(BigDecimal.valueOf(30L))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(BillStatus.PAID)
                .description("new bill to pay")
                .user(mockUser)
                .build();

        when(billRepository.findById(1L)).thenReturn(Optional.ofNullable(billFinded));

        //Action
        Exception thrown = Assertions.assertThrows(BillAlreadyPaidException.class, () -> {
            billService.updateStatus(1L,"PAID");
        });

        //Assert
        Assertions.assertEquals("This bill already paid, you can't modify the status of this bill.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should not update status of bill successfully when bill status is invalid")
    void updateStatusBillCase4() {
        //Arrange
        Bill billFinded = Bill.builder()
                .id(1L)
                .paymentDate(LocalDate.now())
                .amount(BigDecimal.valueOf(30L))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(BillStatus.PENDING)
                .description("new bill to pay")
                .user(mockUser)
                .build();

        when(billRepository.findById(1L)).thenReturn(Optional.ofNullable(billFinded));

        //Action
        Exception thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            billService.updateStatus(1L,"UPDATED");
        });

        //Assert
        Assertions.assertEquals("Value invalid to enum BillStatus : UPDATED",thrown.getMessage());
    }

    @Test
    @DisplayName("Should return a bill successfully when everything is ok")
    void getByIdCase1() {
        //Arrange
        Bill billFinded = Bill.builder()
                .id(1L)
                .paymentDate(LocalDate.now())
                .amount(BigDecimal.valueOf(30L))
                .dueDate(LocalDate.now().plusDays(30))
                .billStatus(BillStatus.PENDING)
                .description("new bill to pay")
                .user(mockUser)
                .build();

        when(billRepository.findById(1L)).thenReturn(Optional.ofNullable(billFinded));
        BillResponse billResponseExpected = new BillResponse().convertBillToResponse(billFinded);

        //Action
        BillResponse billResponse = billService.getById(1L);

        //Assert
        assertNotNull(billResponse);
        assertEquals(billResponse, billResponseExpected);
    }

    @Test
    @DisplayName("Should not return a bill successfully when bill not founded")
    void getByIdCase2() {
        //Arrange
        when(billRepository.findById(1L)).thenReturn(Optional.empty());

        //Action
        Exception thrown = Assertions.assertThrows(BillNotFoundException.class, () -> {
            billService.getById(1L);
        });

        //Assert
        Assertions.assertEquals("Bill with id " + 1L + " not exist.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should return bills by dueDate and description successfully when everything is ok")
    void caseGetBillsByDueDateAndDescription1() {
        // Arrange
        LocalDate dueDate = LocalDate.of(2024, 12, 1);
        String description = "Electricity";
        Pageable pageable =  PageRequest.of(0, 10);

        Bill bill = Bill.builder().id(1L).description("Electricity Bill").dueDate(dueDate).user(mockUser).build();

        Page<Bill> mockPage = new PageImpl<>(List.of(bill), pageable, 1);

        when(billRepository.findByDueDateGreaterThanEqualAndDescriptionContainingIgnoreCase(dueDate, description, pageable))
                .thenReturn(mockPage);

        // Act
        PaginatedResponse<BillResponse> result = billService.getBillsByDueDateAndDescription(dueDate, description, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    @DisplayName("Should not return bills by dueDate and description successfully when everything is ok")
    void caseGetBillsByDueDateAndDescription2() {
        // Arrange
        LocalDate dueDate = LocalDate.of(2024, 12, 1);
        String description = "Water Bill";
        Pageable pageable = PageRequest.of(0, 10);

        when(billRepository.findByDueDateGreaterThanEqualAndDescriptionContainingIgnoreCase(dueDate, description, pageable))
                .thenReturn(Page.empty());

        // Act
        Exception thrown = Assertions.assertThrows(BillNotFoundException.class, () -> {
            billService.getBillsByDueDateAndDescription(dueDate, description, pageable);
        });

        //Assert
        assertEquals("Bills with 'dueDate' 2024-12-01 and 'description' Water Bill not found.", thrown.getMessage());
    }

    @Test
    @DisplayName("Should return bills by dueDate and description successfully when description is case sensitive")
    void caseGetBillsByDueDateAndDescription3() {
        //Arrange
        LocalDate dueDate = LocalDate.of(2024, 12, 1);
        String description = "rent";
        Pageable pageable = PageRequest.of(0, 10);

        Bill bill1 = Bill.builder().id(1L).description("Rent Payment").dueDate(dueDate).user(mockUser).build();

        Bill bill2 = Bill.builder().id(2L).description("Apartment Rent").dueDate(dueDate).user(mockUser).build();

        Page<Bill> mockPage = new PageImpl<>(List.of(bill1, bill2), pageable, 2);
        when(billRepository.findByDueDateGreaterThanEqualAndDescriptionContainingIgnoreCase(dueDate, description, pageable))
                .thenReturn(mockPage);

        // Act
        PaginatedResponse<BillResponse> result = billService.getBillsByDueDateAndDescription(dueDate, description, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Should return bills by dueDate and description successfully when description is null")
    void caseGetBillsByDueDateAndDescription4() {
        // Arrange
        LocalDate dueDate = LocalDate.of(2024, 12, 31);
        String description = "";
        Pageable pageable = PageRequest.of(0, 10);

        Bill bill = Bill.builder().id(1L).description("Rent Payment").dueDate(dueDate).user(mockUser).build();

        Page<Bill> mockPage = new PageImpl<>(List.of(bill), pageable, 1);
        when(billRepository.findByDueDateGreaterThanEqualAndDescriptionContainingIgnoreCase(dueDate, description, pageable))
                .thenReturn(mockPage);

        // Act
        PaginatedResponse<BillResponse> result = billService.getBillsByDueDateAndDescription(dueDate, description, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    @DisplayName("Should import bills from csv successfully when everything ok")
    void caseImportCsvBills1() throws IOException {
        // Arrange
        String csvData = "Due Date,Payment Date,Amount,Description,User Email\n" +
                "2024-12-05,2024-12-01,100.00,Electricity,teste@gmail.com";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        BillRequest billRequest = BillRequest.builder()
                        .dueDate(LocalDate.of(2024, 12, 5))
                        .paymentDate(LocalDate.of(2024, 12, 1))
                        .amount(new BigDecimal("100.00"))
                        .description("Electricity")
                        .billStatus(BillStatus.PENDING)
                        .user(UserRequest.builder().email("teste@gmail.com").build())
                        .build();

        when(billRepository.save(any(Bill.class))).thenReturn(Bill.builder().id(1L)
                .description(billRequest.getDescription())
                .dueDate(billRequest.getDueDate())
                .paymentDate(billRequest.getPaymentDate())
                .billStatus(billRequest.getBillStatus())
                .amount(billRequest.getAmount())
                .user(mockUser)
                .build());

        when(userService.findByEmail("teste@gmail.com")).thenReturn(mockUser);

        // Act
        PaginatedResponse<BillResponse> response = billService.importCsvBill(mockFile);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(LocalDate.of(2024,12,5), response.getContent().get(0).getDueDate());
        assertEquals(LocalDate.of(2024,12,1), response.getContent().get(0).getPaymentDate());
        assertEquals(new BigDecimal("100.00"), response.getContent().get(0).getAmount());
        assertEquals("Electricity", response.getContent().get(0).getDescription());
        assertEquals(BillStatus.PENDING, response.getContent().get(0).getBillStatus());
        assertEquals("teste@gmail.com", response.getContent().get(0).getUser().getEmail());

        assertEquals(1, response.getTotalPages());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    @DisplayName("Should not import bills from csv successfully when the contentType is not text/csv")
    void caseImportCsvBills2() {
        // Arrange
        String csvData = "Due Date,Payment Date,Amount,Description,User Email\n" +
                "2024-12-05,2024-12-01,100.00,Electricity,teste@gmail.com";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.csv",
                "",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        // Act & Assert
        assertThrows(CsvFileException.class, () -> billService.importCsvBill(mockFile));
    }

    @Test
    @DisplayName("Should not import bills from csv successfully when paymentDate is not informed in csv file")
    void caseImportCsvBills3() {

        // Arrange
        String csvData = "Due Date,Payment Date,Amount,Description,User Email\n" +
                "2024-12-05,,100.00,Electricity,teste@gmail.com";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        // Act & Assert
        assertThrows(CsvParseException.class, () -> billService.importCsvBill(mockFile));
    }

    @Test
    @DisplayName("Should not import bills from csv successfully when amount is not informed in csv file")
    void caseImportCsvBills4() {

        // Arrange
        String csvData = "Due Date,Payment Date,Amount,Description,User Email\n" +
                "2024-12-05,2024-12-01,,Electricity,teste@gmail.com";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        // Act & Assert
        assertThrows(CsvParseException.class, () -> billService.importCsvBill(mockFile));
    }

    @Test
    @DisplayName("Should not import bills from csv successfully when user email is not informed in csv file")
    void caseImportCsvBills5() {

        // Arrange
        String csvData = "ID,Due Date,Payment Date,Amount,Description,User Email\n" +
                "1,,2024-12-01,100.00,Electricity, ";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvData.getBytes(StandardCharsets.UTF_8)
        );

        // Act & Assert
        assertThrows(CsvParseException.class, () -> billService.importCsvBill(mockFile));
    }

}