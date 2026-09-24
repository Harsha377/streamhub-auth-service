package com.streamhub.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends  BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mobile_number",unique = true,length = 15)
    private String mobileNumber;
    @Column(name = "full_name",length = 100)
    private String fullName;
    @Column(name = "email",nullable = false,unique = true)
    private String email;
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

}
