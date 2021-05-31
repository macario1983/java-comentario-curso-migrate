package br.com.isvor.service;

import br.com.isvor.model.config.DbConnectionData;
import br.com.isvor.model.config.File;
import br.com.isvor.model.entity.Response;
import br.com.isvor.model.mapper.ComentarioCurso;
import br.com.isvor.service.mapper.ComentarioCursoMapper;
import br.com.isvor.util.Utils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ComentarioCursoConverterService {

    private Properties properties;
    private DbConnectionData dbConnectionData;
    private File fileJson;
    private File fileSql;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ComentarioCursoConverterService() {
        properties = new Properties();
        dbConnectionData = new DbConnectionData();
        fileJson = new File();
        fileSql = new File();
    }

    public void execute() {
        loadProperties();
        readJsonFile(fileJson.getPath());
    }

    private void loadProperties() {
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            fileJson.setPath(properties.get("file.json").toString());
            fileSql.setPath(properties.get("file.sql").toString());
            dbConnectionData.setUrl(properties.get("db.url").toString());
            dbConnectionData.setPort(properties.get("db.port").toString());
            dbConnectionData.setUser(properties.get("db.user").toString());
            dbConnectionData.setPassword(properties.get("db.password").toString());
            dbConnectionData.setSchema(properties.get("db.schema").toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readJsonFile(String jsonFileURI) {

        System.out.println("*****************************************************************************************");
        System.out.println("MIGRAÇÃO DE DADOS");
        System.out.println("*****************************************************************************************");

        String json = null;

        try {
            json = new String(Files.readAllBytes(Paths.get(jsonFileURI)));
            System.out.println("Arquivo carregado...");
            getResponses(json);
        } catch (IOException ex) {
            System.out.println("Arquivo não encontrado!");
            System.exit(1);
        }

        if (json == null) {
            System.out.println("Arquivo não encontrado!");
            System.exit(1);
        }
    }

    private void getResponses(String json) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            System.out.println("Objetos criados...");
            List<Response> responses = objectMapper.readValue(json, new TypeReference<List<Response>>() {
            });
            convertResponsesToComentariosCurso(responses);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private List<Integer> loadCursosId() {

        Connection connection = null;

        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT DISTINCT id FROM curso ORDER BY id;");

            List<Integer> cursosId = new ArrayList<>();

            while (resultSet.next()) {
                cursosId.add(resultSet.getInt(1));
            }

            return cursosId;

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        return Collections.emptyList();
    }

    private void convertResponsesToComentariosCurso(List<Response> responses) {

        System.out.println("Quantidade de comentarios: " + responses
                .stream()
                .map(response -> response.getComentarios().size())
                .reduce(0, Integer::sum));

        List<ComentarioCurso> comentariosCurso = new ArrayList<>(responses.size());
        responses.stream().forEach(response -> comentariosCurso.addAll(ComentarioCursoMapper.INSTANCE.responseToComentariosCurso(response)));

        List<Integer> cursosId = loadCursosId();

        List<Integer> cursosIdNotPresentInComentariosCurso = comentariosCurso
                .stream()
                .filter(comentarioCurso -> !cursosId.contains(comentarioCurso.getCursoId()))
                .map(comentarioCurso -> comentarioCurso.getCursoId())
                .distinct()
                .collect(Collectors.toList());

        List<ComentarioCurso> comentariosCursoPresent = comentariosCurso.stream().filter(comentarioCurso -> cursosId.contains(comentarioCurso.getCursoId())).collect(Collectors.toList());

        System.out.println("*****************************************************************************************");
        System.out.println("Cursos não presentes...");

        cursosIdNotPresentInComentariosCurso.forEach(cursoId -> {
            System.out.println("Curso - " + cursoId);
        });

        System.out.println("*****************************************************************************************");

        saveComentariosCurso(comentariosCursoPresent);
    }

    private void saveComentariosCurso(List<ComentarioCurso> comentariosCurso) {

        try {

            StringBuffer stringBuffer = new StringBuffer();

            for (ComentarioCurso comentarioCurso : comentariosCurso) {

                String sql = String.format("INSERT IGNORE INTO comentario_curso (descricao, resposta, status, tipo_comentario, ativo, data_criacao, data_alteracao, versao, curso_id, moderador_id, profissional_id) " +
                                "VALUES(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s );",
                        addQuote(comentarioCurso.getDescricao()),
                        addQuote(comentarioCurso.getResposta()),
                        addQuote(comentarioCurso.getStatus()),
                        getTipoComentario(comentarioCurso),
                        (short) 1,
                        addQuote(Utils.convertLocalDateTimeToTimeStamp(formatter, comentarioCurso.getDataComentario()).toString()),
                        addQuote(getDataAprovacao(comentarioCurso).toString()),
                        (short) 1,
                        comentarioCurso.getCursoId(),
                        getModeradorId(comentarioCurso),
                        comentarioCurso.getProfissionalId());

                stringBuffer.append(sql);
                stringBuffer.append(System.getProperty("line.separator"));
            }

            Files.write(Paths.get(fileSql.getPath()), stringBuffer.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND, StandardOpenOption.CREATE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTipoComentario(ComentarioCurso comentarioCurso) {

        if (Utils.objectIsNotNull(comentarioCurso.getTipoComentario())) {
            return addQuote(comentarioCurso.getTipoComentario());
        }

        return null;
    }

    private Timestamp getDataAprovacao(ComentarioCurso comentarioCurso) {

        if (Utils.objectIsNotNull(comentarioCurso.getDataAprovacao())) {
            return Utils.convertLocalDateTimeToTimeStamp(formatter, comentarioCurso.getDataAprovacao());
        }

        return Utils.convertLocalDateTimeToTimeStamp(formatter, LocalDateTime.now());
    }

    private Integer getModeradorId(ComentarioCurso comentarioCurso) {

        if (Utils.objectIsNotNull(comentarioCurso.getModeradorId())) {
            return comentarioCurso.getModeradorId();
        }

        return null;
    }

    private String addQuote(String value) {

        if (value == null) {
            return null;
        }

        return '"' + value.trim() + '"';
    }

    private Connection getConnection() throws SQLException {

        try {
            String urlConnection = dbConnectionData.toString();
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(urlConnection, dbConnectionData.getUser(), dbConnectionData.getPassword());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
