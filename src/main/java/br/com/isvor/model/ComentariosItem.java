package br.com.isvor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ComentariosItem{

	@JsonProperty("profissionalId")
	private Integer profissionalId;

	@JsonProperty("dataAprovacao")
	private DataAprovacao dataAprovacao;

	@JsonProperty("dataComentario")
	private DataComentario dataComentario;

	@JsonProperty("profissionalAprovacaoId")
	private Integer profissionalAprovacaoId;

	@JsonProperty("resposta")
	private Object resposta;

	@JsonProperty("tipoComentario")
	private String tipoComentario;

	@JsonProperty("profissonalAprovacao")
	private String profissonalAprovacao;

	@JsonProperty("profissional")
	private String profissional;

	@JsonProperty("comentarioId")
	private String comentarioId;

	@JsonProperty("descricao")
	private String descricao;

	@JsonProperty("status")
	private String status;
}
