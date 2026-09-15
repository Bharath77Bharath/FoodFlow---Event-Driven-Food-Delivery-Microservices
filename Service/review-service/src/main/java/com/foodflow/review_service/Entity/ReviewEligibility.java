package com.foodflow.review_service.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "review_eligibility",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "orderId")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEligibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private LocalDateTime deliveredAt;
}