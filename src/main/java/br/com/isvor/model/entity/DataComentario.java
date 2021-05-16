package br.com.isvor.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DataComentario{

	@JsonProperty("$date")
	private String date;
}
