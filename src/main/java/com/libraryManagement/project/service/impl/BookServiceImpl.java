package com.libraryManagement.project.service.impl;

import com.libraryManagement.project.dto.requestDTO.BookRequestDTO;
import com.libraryManagement.project.dto.responseDTO.BookResponseDTO;
import com.libraryManagement.project.entity.*;
import com.libraryManagement.project.exception.BookNotFoundException;
import com.libraryManagement.project.exception.BookSearchException;
import com.libraryManagement.project.repository.*;
import com.libraryManagement.project.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final CartItemsRepository cartItemsRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final CartRepository cartRepository;

    @Override
    public List<BookResponseDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .filter(book -> book.getActive() == null || book.getActive())
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookResponseDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));
        return convertToResponseDTO(book);
    }

    @Override
    public List<BookResponseDTO> getBooksByAuthor(Long authorId) {
        if (!authorRepository.existsById(authorId)) {
            throw new BookNotFoundException("Author not found with ID: " + authorId);
        }

        List<Book> books = bookRepository.findByAuthorAuthorId(authorId);
        if (books.isEmpty()) {
            throw new BookNotFoundException("No books found for author ID: " + authorId);
        }

        return books.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<BookResponseDTO> getBooksByCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BookNotFoundException("Category not found with ID: " + categoryId);
        }

        List<Book> books = bookRepository.findByCategoryCategoryId(categoryId);
        if (books.isEmpty()) {
            throw new BookNotFoundException("No books found for category ID: " + categoryId);
        }

        return books.stream().map(this::convertToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<BookResponseDTO> findBooksByTitle(String title) {
        List<BookResponseDTO> books = bookRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        if (books.isEmpty()) {
            throw new BookSearchException("No books found with title containing: " + title);
        }

        return books;
    }

    @Override
    public List<BookResponseDTO> findBooksByAuthor(String authorName) {
        List<BookResponseDTO> books = bookRepository.findByAuthorNameContainingIgnoreCase(authorName).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        if (books.isEmpty()) {
            throw new BookSearchException("No books found by author: " + authorName);
        }

        return books;
    }

    @Override
    public List<BookResponseDTO> findBooksByCategory(String categoryName) {
        List<BookResponseDTO> books = bookRepository.findByCategoryNameContainingIgnoreCase(categoryName).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        if (books.isEmpty()) {
            throw new BookSearchException("No books found in category: " + categoryName);
        }

        return books;
    }


    @Override
    public BookResponseDTO addBook(BookRequestDTO bookRequestDTO) {
        Author author = authorRepository.findByName(bookRequestDTO.getAuthorName())
                .orElseGet(() -> {
                    Author newAuthor = new Author();
                    newAuthor.setName(bookRequestDTO.getAuthorName());
                    return authorRepository.save(newAuthor);
                });

        Category category = categoryRepository.findByName(bookRequestDTO.getCategoryName())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(bookRequestDTO.getCategoryName());
                    return categoryRepository.save(newCategory);
                });

        boolean exists = bookRepository.existsByTitleAndAuthorAndCategory(
                bookRequestDTO.getTitle(), author, category);
        if (exists) {
            throw new IllegalArgumentException("Book already exists in the database.");
        }

        Book book = convertToEntity(bookRequestDTO);
        book.setAuthor(author);
        book.setCategory(category);

        Book savedBook = bookRepository.save(book);

        Inventory inventory = new Inventory();
        inventory.setBook(savedBook);
        inventory.setStockQuantity(savedBook.getStockQuantity());
        inventoryRepository.save(inventory);

        return convertToResponseDTO(savedBook);
    }

    @Transactional
    @Override
    public BookResponseDTO updateBook(Long id, BookRequestDTO bookRequestDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));

        Author author = authorRepository.findByName(bookRequestDTO.getAuthorName())
                .orElseGet(() -> {
                    Author newAuthor = new Author();
                    newAuthor.setName(bookRequestDTO.getAuthorName());
                    return authorRepository.save(newAuthor);
                });

        Category category = categoryRepository.findByName(bookRequestDTO.getCategoryName())
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(bookRequestDTO.getCategoryName());
                    return categoryRepository.save(newCategory);
                });

        book.setTitle(bookRequestDTO.getTitle());
        book.setPrice(bookRequestDTO.getPrice());
        book.setStockQuantity(bookRequestDTO.getStockQuantity());
        book.setAuthor(author);
        book.setCategory(category);
        book.setDescription(bookRequestDTO.getDescription());
        book.setImageUrl(bookRequestDTO.getImageUrl());

        Book updatedBook = bookRepository.save(book);

        inventoryRepository.findByBook(updatedBook).ifPresent(inventory -> {
            inventory.setStockQuantity(updatedBook.getStockQuantity());
            inventoryRepository.save(inventory);
        });

        return convertToResponseDTO(updatedBook);
    }

    @Transactional
    @Override
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + id));

        boolean isInOrders = orderItemsRepository.existsByBookId(book.getBookId());
        if (isInOrders) {
            book.setActive(false);
            bookRepository.save(book);
        } else {
            removeBookFromAllCarts(id);
            inventoryRepository.findByBook(book).ifPresent(inventoryRepository::delete);
            bookRepository.delete(book);
        }
    }

    private void removeBookFromAllCarts(Long bookId) {
        List<CartItems> cartItems = cartItemsRepository.findBookByBookId(bookId);
        for (CartItems item : cartItems) {
            Cart cart = item.getCart();
            cart.removeCartItem(item);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
            cartItemsRepository.delete(item);
        }
    }

    @Override
    public BookResponseDTO addBookWithImage(BookRequestDTO bookDTO, MultipartFile imageFile) {
        try {
            // 1. Generate a unique filename
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

            // 2. Define the static image directory path
            Path uploadDir = Paths.get("uploaded-images");
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 4. Assign image URL in DTO
            bookDTO.setImageUrl("/images/" + fileName);

            // 5. Reuse normal addBook() logic
            return addBook(bookDTO);

        } catch (IOException e) {
            throw new RuntimeException("Error saving image file", e);
        }
    }

    @Override
    public BookResponseDTO updateBookWithImage(Long bookId, BookRequestDTO bookDTO, MultipartFile imageFile) {
        try {
            // 1. Generate a unique filename
            String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();

            // 2. Define and create the image directory
            Path uploadDir = Paths.get("uploaded-images");
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(fileName);

            // 3. Save the file
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 4. Update image URL in DTO
            bookDTO.setImageUrl("/images/" + fileName);

            // 5. Reuse existing update logic
            return updateBook(bookId, bookDTO);

        } catch (IOException e) {
            throw new RuntimeException("Error updating image file", e);
        }
    }

    @Override
    // Dummy conversion methods (replace with ModelMapper or actual logic)
    public BookResponseDTO convertToResponseDTO(Book book) {
        BookResponseDTO dto = new BookResponseDTO();
        dto.setBookId(book.getBookId());
        dto.setTitle(book.getTitle());
        dto.setPrice(book.getPrice());
        dto.setStockQuantity(book.getStockQuantity());
        dto.setAuthorName(book.getAuthor().getName());
        dto.setCategoryName(book.getCategory().getName());
        dto.setDescription(book.getDescription());
        dto.setImageUrl(book.getImageUrl());
        return dto;
    }

    @Override
    public Book convertToEntity(BookRequestDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setPrice(dto.getPrice());
        book.setStockQuantity(dto.getStockQuantity());
        book.setImageUrl(dto.getImageUrl());
        book.setDescription(dto.getDescription());
        return book;
    }
}
