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
import java.nio.file.Files;
import java.nio.file.Paths;
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
    private File file;

    public ComentarioCursoConverterService() {
        properties = new Properties();
        dbConnectionData = new DbConnectionData();
        file = new File();
    }

    public void execute() {
        loadProperties();
        readJsonFile(file.getPath());
    }

    private void loadProperties() {
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("config.properties"));
            file.setPath(properties.get("file.path").toString());
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

        PreparedStatement pstmt = null;
        Connection connection = null;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int qtdeTotalRegistrosInseridos = 0;

        String sqlInsert = "INSERT INTO comentario_curso (descricao, resposta, status, tipo_comentario, ativo, data_criacao, data_alteracao, versao, curso_id, moderador_id, profissional_id) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        int count = 0;
        int batchSize = 100;

        try {
            System.out.println("Iniciando procedimento para salvar...");
            connection = getConnection();
            connection.setAutoCommit(false);
            pstmt = connection.prepareStatement(sqlInsert);

            for (ComentarioCurso comentarioCurso : comentariosCurso) {

                pstmt.setString(1, comentarioCurso.getDescricao());
                pstmt.setString(2, comentarioCurso.getResposta());
                pstmt.setString(3, comentarioCurso.getStatus());

                if (Utils.objectIsNotNull(comentarioCurso.getTipoComentario())) {
                    pstmt.setString(4, comentarioCurso.getTipoComentario());
                } else {
                    pstmt.setNull(4, Types.VARCHAR);
                }

                pstmt.setShort(5, (short) 1);
                pstmt.setTimestamp(6, Utils.convertLocalDateTimeToTimeStamp(formatter, comentarioCurso.getDataComentario()));

                if (Utils.objectIsNotNull(comentarioCurso.getDataAprovacao())) {
                    pstmt.setTimestamp(7, Utils.convertLocalDateTimeToTimeStamp(formatter, comentarioCurso.getDataAprovacao()));
                } else {
                    pstmt.setTimestamp(7, Utils.convertLocalDateTimeToTimeStamp(formatter, LocalDateTime.now()));
                }

                pstmt.setShort(8, (short) 1);
                pstmt.setLong(9, comentarioCurso.getCursoId());

                if (Utils.objectIsNotNull(comentarioCurso.getModeradorId())) {
                    pstmt.setLong(10, comentarioCurso.getModeradorId());
                } else {
                    pstmt.setNull(10, Types.BIGINT);
                }

                pstmt.setLong(11, comentarioCurso.getProfissionalId());

                pstmt.addBatch();

                count++;

                if (count % batchSize == 0) {
                    System.out.println("");
                    System.out.println("Inserindo registros...");
                    int[] result = pstmt.executeBatch();
                    System.out.println("Número de registros inseridos: " + result.length);
                    System.out.println("");
                    connection.commit();

                    qtdeTotalRegistrosInseridos += result.length;
                }
            }

            System.out.println("");
            System.out.println("Inserindo registros remanecentes...");
            int[] result = pstmt.executeBatch();
            System.out.println("Número de registros inseridos: " + result.length);
            System.out.println("");
            connection.commit();

            qtdeTotalRegistrosInseridos += result.length;

        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

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
