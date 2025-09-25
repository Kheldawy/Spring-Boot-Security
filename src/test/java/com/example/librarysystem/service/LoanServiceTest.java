package com.example.librarysystem.service;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.Loan;
import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Aktiverar Mockito för denna testklass
@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        // Skapar testdata före varje test
        user = new User();
        user.setId(1L);
        user.setFirstName("Khaled");
        user.setLastName("Ibrahim");
        user.setEmail("khaled.ibrahim@example.com");

        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAvailableCopies(1);
        book.setTotalCopies(2);
    }

@Test
void createLoan_setsCorrectDueDate() {
    // Arrange
    Long userId = 1L;
    Long bookId = 1L;
    LocalDateTime fixedTime = LocalDateTime.of(2024, 5, 14, 14, 49);

    try (MockedStatic<LocalDateTime> mockedStatic = mockStatic(LocalDateTime.class)) {
        mockedStatic.when(LocalDateTime::now).thenReturn(fixedTime);

        when(userService.getUserById(userId)).thenReturn(Optional.of(user));
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(book));
        when(bookService.createBook(any(Book.class))).thenReturn(book);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> {
            Loan loan = invocation.getArgument(0);
            loan.setId(1L);
            return loan;
        });

        // Act
        Loan createdLoan = loanService.createLoan(userId, bookId);

        // Assert
        assertNotNull(createdLoan, "Created loan should not be null");
        assertEquals(user, createdLoan.getUser(), "User should match");
        assertEquals(book, createdLoan.getBook(), "Book should match");
        assertNotNull(createdLoan.getBorrowedDate(), "Borrowed date should be set");
        assertNotNull(createdLoan.getDueDate(), "Due date should be set");
        assertNull(createdLoan.getReturnedDate(), "Returned date should be null");

        LocalDateTime expectedDueDate = fixedTime.plusDays(14);
        assertEquals(expectedDueDate, createdLoan.getDueDate(), "Due date should be 14 days after borrowed date");

        // Verifiera att boken är tillgänglig
        assertEquals(0, book.getAvailableCopies(), "Available copies should be decremented");
        verify(bookService, times(1)).createBook(book);
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

}
    @Test
    void createLoan_throwsExceptionWhenNoAvailableCopies() {
        // Arrange
        Long userId = 1L;
        Long bookId = 1L;
        book.setAvailableCopies(0);

        when(userService.getUserById(userId)).thenReturn(Optional.of(user));
        when(bookService.getBookById(bookId)).thenReturn(Optional.of(book));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loanService.createLoan(userId, bookId),
                "Should throw IllegalArgumentException when no copies are available"
        );

        assertEquals("There are no copies of the book available.", exception.getMessage(),
                "Exception message should match");

        // Verifiera att inga sparoperationer gjordes
        verify(bookService, never()).createBook(any(Book.class));
        verify(loanRepository, never()).save(any(Loan.class));
    }
}