package com.libraryManagement.project.entity;


import jakarta.persistence.*;


@Entity
@Table(name = "reviews")
public class Review {     // Review table

    @Id      //Primary Key
    @Column(name = "reviewId")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String reviewId;

    @ManyToMany
    @JoinColumn(name = "userId" , nullable = false)
    private String userId;

    @ManyToOne
    @JoinColumn(name = "bookId", nullable = false)
    private String bookId;

    @Column(name = "rating" , nullable = false)
    private int rating;

    @Column(name = "comment" , nullable = false)
    private String comment;

    public void review() {}

    public Review(String reviewId, String userId, String bookId, int rating, String comment) {
        this.reviewId = reviewId;
        this.userId = userId;
        this.bookId = bookId;
        this.rating = rating;
        this.comment = comment;
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }



}
