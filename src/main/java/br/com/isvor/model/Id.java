package br.com.isvor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Id{

	@JsonProperty("$oid")
	private String oid;
}
