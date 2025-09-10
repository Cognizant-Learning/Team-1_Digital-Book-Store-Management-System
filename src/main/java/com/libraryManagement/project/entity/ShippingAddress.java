package com.libraryManagement.project.entity;
import jakarta.persistence.*;
@Entity
@Table(name = "shipping_address")
public class ShippingAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 100)
    private String addressLine1;
    @Column(length = 100)
    private String addressLine2;
    @Column(length = 50)
    private String city;
    @Column(length = 50)
    private String country;
    @Column(length = 100)
    private String fullName;
    @Column(length = 255)
    private String phone;
    @Column(length = 255)
    private String postalCode;
    @Column(length = 50)
    private String state;
// WANT TO CHANGE THIS TO ORDER_ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAddressLine1() { return addressLine1; }
    public void setAddressLine1(String addressLine1) { this.addressLine1 = addressLine1; }
    public String getAddressLine2() { return addressLine2; }
    public void setAddressLine2(String addressLine2) { this.addressLine2 = addressLine2; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}