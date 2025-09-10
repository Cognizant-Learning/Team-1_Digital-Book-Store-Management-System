package com.libraryManagement.project.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "inventory")
public class Inventory
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "book_id", referencedColumnName = "id")
    private Book book;

    @Min(0)
    private int stockQuantity;
    public Inventory() {}

    public Inventory(Long id, Book book, int stockQuantity)
    {
        this.id = id;
        this.book = book;
        this.stockQuantity = stockQuantity;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Book getBook()
    {
        return book;
    }

    public void setBook(Book book)
    {
        this.book = book;
    }

    public int getStockQuantity()
    {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity)
    {
        this.stockQuantity = stockQuantity;
    }

}
