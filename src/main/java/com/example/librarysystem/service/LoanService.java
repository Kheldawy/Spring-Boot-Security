package com.example.librarysystem.service;

import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.Loan;
import com.example.librarysystem.entity.User;
import com.example.librarysystem.repository.LoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final UserService userService;
    private final BookService bookService;

    @Autowired
    public LoanService(LoanRepository loanRepository, UserService userService, BookService bookService) {
        this.loanRepository = loanRepository;
        this.userService = userService;
        this.bookService = bookService;
    }

    // Hämtar alla lån
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    // Hämtar ett lån på ID
    public Optional<Loan> getLoanById(Long id) {
        return loanRepository.findById(id);
    }

    // Hämtar lån via användar-ID
    public List<Loan> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId);
    }

    // Skapar ett nytt lån
    public Loan createLoan(Long userId, Long bookId) throws IllegalArgumentException {

        Optional<User> userOptional = userService.getUserById(userId);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found");
        }


        Optional<Book> bookOptional = bookService.getBookById(bookId);
        if (!bookOptional.isPresent()) {
            throw new IllegalArgumentException("The book is not available");
        }

        Book book = bookOptional.get();

        if (book.getAvailableCopies() <= 0) {
            throw new IllegalArgumentException("There are no copies of the book available.");
        }

        // Skapa ett nytt lån
        Loan loan = new Loan();
        loan.setUser(userOptional.get());
        loan.setBook(book);
        loan.setBorrowedDate(LocalDateTime.now());
        // Sätt återlämningsdatum till 14 dagar framåt
        loan.setDueDate(LocalDateTime.now().plusDays(14));

        // Minska antalet tillgängliga exemplar av boken
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookService.createBook(book);


        return loanRepository.save(loan);
    }

    // Hantera återlämning av en bok via lånets ID
    public Loan returnBook(Long loanId) throws IllegalArgumentException {
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (!loanOptional.isPresent()) {
            throw new IllegalArgumentException("The loan does not exist");
        }

        Loan loan = loanOptional.get();
        if (loan.getReturnedDate() != null) {
            throw new IllegalArgumentException("The book has been previously returned.");
        }

        // Uppdatera returdatum
        loan.setReturnedDate(LocalDateTime.now());

        // Öka antalet tillgängliga kopior
        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookService.createBook(book);


        return loanRepository.save(loan);
    }

    // Förläng lånetiden för ett lån med ytterligare två veckor
    public Loan extendLoan(Long loanId) throws IllegalArgumentException {
        Optional<Loan> loanOptional = loanRepository.findById(loanId);
        if (!loanOptional.isPresent()) {
            throw new IllegalArgumentException("The loan does not exist");
        }

        Loan loan = loanOptional.get();
        if (loan.getReturnedDate() != null) {
            throw new IllegalArgumentException("A loan cannot be extended for a returned book.");
        }

        // Förläng due date med ytterligare två veckor
        loan.setDueDate(loan.getDueDate().plusWeeks(2));


        return loanRepository.save(loan);
    }
}

