package com.alphonso.moodle_employer_service.Employer.Entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "employer")
public class Employer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    private String companyName;

    private String name;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL)
    private List<Requisition> requisitions = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
}