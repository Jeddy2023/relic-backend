package com.relic.relic.model;

import com.relic.relic.enums.Condition;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String shortDescription;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(value = EnumType.STRING)
    private Condition condition;

    private Long minimumBid;

    private Long currentBid;

    private boolean isBiddingClosed = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winning_bidder_id")
    private User winningBidder;

    private Date biddingTimeLimit;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<Bid> bidList;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<Review> reviews;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    private Date createdAt = new Date();

    public int getNumberOfBids() {
        return this.bidList.size();
    }
}
