package com.alphonso.user_service.Model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user", indexes = { @Index(name = "idx_user_email", columnList = "email") })
public class UsersReg {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false, length = 200)
	private String email;

	@Column(name = "password_hash")
	private String password;

	@Column(name = "first_name", length = 100)
	private String firstName;

	@Column(name = "last_name", length = 100)
	private String lastName;

	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	@Column(nullable = false)
	private boolean isVerified = false;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Provider provider = Provider.LOCAL;

	public enum Provider {
		LOCAL, GOOGLE
	}

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")
	private RoleCategory category; 

}
