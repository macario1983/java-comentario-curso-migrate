package br.com.isvor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Response{

	@JsonProperty("curso")
	private String curso;

	@JsonProperty("_id")
	private Id id;

	@JsonProperty("cursoId")
	private Integer cursoId;

	@JsonProperty("_class")
	private String clazz;

	@JsonProperty("comentarios")
	private List<ComentariosItem> comentarios;
}
