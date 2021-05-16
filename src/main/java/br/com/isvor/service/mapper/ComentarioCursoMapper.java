package br.com.isvor.service.mapper;

import br.com.isvor.model.entity.ComentariosItem;
import br.com.isvor.model.entity.Response;
import br.com.isvor.model.mapper.ComentarioCurso;
import br.com.isvor.util.DateUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface ComentarioCursoMapper {

    ComentarioCursoMapper INSTANCE = Mappers.getMapper(ComentarioCursoMapper.class);

    default List<ComentarioCurso> responseToComentariosCurso(Response response) {

        return response.getComentarios().stream().map(comentarioItem -> {
            ComentarioCurso comentarioCurso = new ComentarioCurso();
            comentarioCurso.setDescricao(comentarioItem.getDescricao() != null ? comentarioItem.getDescricao() : null);
            comentarioCurso.setResposta(comentarioItem.getResposta() != null ? comentarioItem.getResposta().toString() : null);
            comentarioCurso.setStatus(comentarioItem.getStatus() != null ? comentarioItem.getStatus() : null);
            comentarioCurso.setTipoComentario(comentarioItem.getTipoComentario() != null ? comentarioItem.getTipoComentario() : null);
            comentarioCurso.setProfissionalId(comentarioItem.getProfissionalId() != null ? comentarioItem.getProfissionalId() : null);
            comentarioCurso.setCursoId(response.getCursoId() != null ? response.getCursoId() : null);
            comentarioCurso.setModeradorId(comentarioItem.getProfissionalAprovacaoId() != null ? comentarioItem.getProfissionalAprovacaoId() : null);
            comentarioCurso.setDataAprovacao(comentarioItem.getDataAprovacao() != null ? DateUtils.convertStringToLocalDateTime(comentarioItem.getDataAprovacao().getDate()) : null);
            comentarioCurso.setDataComentario(comentarioItem.getDataComentario() != null ? DateUtils.convertStringToLocalDateTime(comentarioItem.getDataComentario().getDate()) : null);
            return comentarioCurso;
        }).collect(Collectors.toList());
    }
}
