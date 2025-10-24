package com.libraryManagement.project.dto.requestDTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookRequestDTO {
    @NotNull(message= "Title cannot be null")
    private String title;

    @NotNull(message = "Author Name cannot be null")
    private String authorName;

    @NotNull(message = "Category Name cannot be null")
    private String categoryName;

    private String description;

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    private Double price;

    private int stockQuantity;
    private String imageUrl;

    public BookRequestDTO(String title, String author, String category, String description) {
        this.title = title;
        this.authorName = author;
        this.categoryName = category;
        this.description = description;
    }
}
