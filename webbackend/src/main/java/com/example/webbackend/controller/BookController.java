package com.example.webbackend.controller;

import com.example.webbackend.entity.Book;
import org.springframework.data.querydsl.QPageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class BookController {

    private List<Book> books = new ArrayList<>();

    private Long nextId = 1L;

    public BookController() {
        // Add 15 books with varied data for testing
        books.add(new Book(nextId++, "Spring Boot in Action", "Craig Walls", 39.99));
        books.add(new Book(nextId++, "Effective Java", "Joshua Bloch", 45.00));
        books.add(new Book(nextId++, "Clean Code", "Robert Martin", 42.50));
        books.add(new Book(nextId++, "Java Concurrency in Practice", "Brian Goetz", 49.99));
        books.add(new Book(nextId++, "Design Patterns", "Gang of Four", 54.99));
        books.add(new Book(nextId++, "Head First Java", "Kathy Sierra", 35.00));
        books.add(new Book(nextId++, "Spring in Action", "Craig Walls", 44.99));
        books.add(new Book(nextId++, "Clean Architecture", "Robert Martin", 39.99));
        books.add(new Book(nextId++, "Refactoring", "Martin Fowler", 47.50));
        books.add(new Book(nextId++, "The Pragmatic Programmer", "Andrew Hunt", 41.99));
        books.add(new Book(nextId++, "You Don't Know JS", "Kyle Simpson", 29.99));
        books.add(new Book(nextId++, "JavaScript: The Good Parts", "Douglas Crockford", 32.50));
        books.add(new Book(nextId++, "Eloquent JavaScript", "Marijn Haverbeke", 27.99));
        books.add(new Book(nextId++, "Python Crash Course", "Eric Matthes", 38.00));
        books.add(new Book(nextId++, "Automate the Boring Stuff", "Al Sweigart", 33.50));
    }

    // get all books - /api/books
    @GetMapping("/books")
    public List<Book> getBooks() {
        return books;
    }

    // get book by id
    @GetMapping("/books/{id}")
    public Book getBook(@PathVariable Long id) {
        return books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // create a new book
    @PostMapping("/books")
    public List<Book> createBook(@RequestBody Book book) {
        books.add(book);
        return books;
    }

    // search by title
    @GetMapping("/books/search")
    public List<Book> searchByTitle(
            @RequestParam(required = false, defaultValue = "") String title
    ) {
        if(title.isEmpty()) {
            return books;
        }

        return books.stream()
                .filter(book -> book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());

    }

    // price range
    @GetMapping("/books/price-range")
    public List<Book> getBooksByPrice(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        return books.stream()
                .filter(book -> {
                    boolean min = minPrice == null || book.getPrice() >= minPrice;
                    boolean max = maxPrice == null || book.getPrice() <= maxPrice;

                    return min && max;
                }).collect(Collectors.toList());
    }

    // sort
    @GetMapping("/books/sorted")
    public List<Book> getSortedBooks(
            @RequestParam(required = false, defaultValue = "title") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order
    ){
        Comparator<Book> comparator;

        switch(sortBy.toLowerCase()) {
            case "author":
                comparator = Comparator.comparing(Book::getAuthor);
                break;
            case "title":
                comparator = Comparator.comparing(Book::getTitle);
            default:
                comparator = Comparator.comparing(Book::getTitle);
                break;
        }

        if("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        return books.stream().sorted(comparator)
                .collect(Collectors.toList());
    }

    // PUT endpoint, finds book via id, and updates it given JSON
    @PutMapping("/books/{id}")
    public Book updateBook(
            @PathVariable Long id,
            @RequestBody Book book) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(id)) {
                book.setId(id);
                books.set(i, book);
                return book;
            }
        }
        return null;
    }

    // PATCH endpoint, partial update, set field then the value user wants to change.
    @PatchMapping("/books/{id}")
    public Book patchBook(
            @PathVariable Long id,
            @RequestParam(required = true) String field,
            @RequestParam(required = true) String val

    ) {
        for (Book currentBook: books) {
            if (currentBook.getId().equals(id)) {
                switch (field.toLowerCase()) {
                    case "title":
                        currentBook.setTitle(val);
                        break;
                    case "author":
                        currentBook.setAuthor(val);
                        break;
                    case "price":
                        currentBook.setPrice(Double.parseDouble(val));
                        break;
                }
                return currentBook;
            }
        }
        return null;
    }

    // Delete Endpoint
    @DeleteMapping("/books/{id}")
    public Book delBook(
            @PathVariable Long id
    ) {
        for (int i = 0; i < books.size(); i++) {
            if (books.get(i).getId().equals(id)) {
                return books.remove(i);
            }
        }
        return null;
    }

    //GET with pagination
    @GetMapping("/books/page")
    public List<Book> getBookWithPage(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false) int lenPage
    ) {
        int start = page * lenPage;
        int end = start + lenPage;
        int bookSize = books.size();

        List<Book> arrBooksPage = new ArrayList<>();

        for (int i = start; i < end && i < bookSize; i++) {
            arrBooksPage.add(books.get(i));
        }
        return arrBooksPage;
    }

    //GET with filtering, sorting, and page
    @GetMapping("/books/adv")
    public List<Book> advancedBookGet(
            @RequestParam(required = false, defaultValue = "title") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "3") int lenPage
    ) {
        Comparator<Book> comparator;
        switch (sortBy.toLowerCase()) {
            case "title":
                comparator = Comparator.comparing(Book::getTitle);
                break;
            case "author":
            default:
                comparator = Comparator.comparing(Book::getAuthor);
                break;
        }

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        List<Book> sortedBooks = new ArrayList<>(books);
        sortedBooks.sort(comparator);

        int start = page * lenPage;
        int end = Math.min(start + lenPage, sortedBooks.size());
        List<Book> result = new ArrayList<>();

        for (int i = start; i < end; i++) {
            result.add(sortedBooks.get(i));
        }

        return result;
    }
}