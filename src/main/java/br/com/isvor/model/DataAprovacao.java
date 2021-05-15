package br.com.isvor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DataAprovacao{

	@JsonProperty("$date")
	private String date;
}
