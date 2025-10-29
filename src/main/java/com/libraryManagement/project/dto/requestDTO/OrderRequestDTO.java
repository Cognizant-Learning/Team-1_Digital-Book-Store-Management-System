package com.libraryManagement.project.dto.requestDTO;

import com.libraryManagement.project.entity.ShippingAddress;
import com.libraryManagement.project.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDTO {
    //private Long userId;
    private Long cartId;
    private Long addressId;
    private String status;
    private List<OrderItemRequestDTO> items;
}
