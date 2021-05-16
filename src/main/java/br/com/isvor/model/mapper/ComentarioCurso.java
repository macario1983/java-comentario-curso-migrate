package br.com.isvor.model.mapper;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ComentarioCurso {

    private String descricao;

    private String resposta;

    private String status;

    private String tipoComentario;

    private Integer cursoId;

    private Integer profissionalId;

    private Integer moderadorId;

    private LocalDateTime dataAprovacao;

    private LocalDateTime dataComentario;

}
