package br.com.isvor.service.mapper;

import br.com.isvor.model.entity.Response;
import br.com.isvor.model.mapper.ComentarioCurso;
import br.com.isvor.util.Utils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface ComentarioCursoMapper {

    ComentarioCursoMapper INSTANCE = Mappers.getMapper(ComentarioCursoMapper.class);

    default List<ComentarioCurso> responseToComentariosCurso(Response response) {

        return response.getComentarios().stream().map(comentarioItem -> {
            ComentarioCurso comentarioCurso = new ComentarioCurso();
            comentarioCurso.setDescricao(Utils.getStringIfNotNull(comentarioItem.getDescricao()));
            comentarioCurso.setResposta(Utils.getStringIfNotNull(comentarioItem.getResposta()));
            comentarioCurso.setStatus(Utils.getStringIfNotNull(comentarioItem.getStatus()));
            comentarioCurso.setTipoComentario(Utils.getStringIfNotNull(comentarioItem.getTipoComentario()));
            comentarioCurso.setProfissionalId(Utils.getIntegerIfNotNull(comentarioItem.getProfissionalId()));
            comentarioCurso.setCursoId(Utils.getIntegerIfNotNull(response.getCursoId()));
            comentarioCurso.setModeradorId(Utils.getIntegerIfNotNull(comentarioItem.getProfissionalAprovacaoId()));

            if (comentarioItem.getDataAprovacao() != null) {
                comentarioCurso.setDataAprovacao(Utils.convertStringToLocalDateTime(comentarioItem.getDataAprovacao().getDate()));
            }

            if (comentarioItem.getDataComentario() != null) {
                comentarioCurso.setDataComentario(Utils.convertStringToLocalDateTime(comentarioItem.getDataComentario().getDate()));
            }

            return comentarioCurso;
        }).collect(Collectors.toList());
    }
}
