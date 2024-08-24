package com.yourcompany.surveys.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "surveys")
@EntityListeners(AuditingEntityListener.class)
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;
    @Column(nullable = false)
    private String description;

    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModifiedDate;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(nullable = false)
    private List<Question> questions;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewRating> reviewRatings;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Participation> participations;
}