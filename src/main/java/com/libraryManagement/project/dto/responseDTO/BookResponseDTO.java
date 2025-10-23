package com.libraryManagement.project.dto.responseDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookResponseDTO {
    private Long bookId;
    private String title;
    private String authorName;
    private String categoryName;
    private String description;
    private Double price;
    private int stockQuantity;
    private String imageUrl;

    public BookResponseDTO(Long bookId, @NotBlank String title, @NotNull String authorName,
                           @NotNull String categoryName,String description, @NotNull @Positive Double price,
                           int stockQuantity, String imageUrl) {
        this.bookId = bookId;
        this.title = title;
        this.authorName = authorName;
        this.categoryName = categoryName;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
    }

}
