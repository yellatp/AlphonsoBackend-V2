package com.alphonso.profile_service.Entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Portfolio {

	private String linkedIn;
	
	private String gitHub;
	
	private String porfolioId;
	
	private String others;
}
