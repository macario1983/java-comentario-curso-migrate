package br.com.isvor;

import br.com.isvor.model.DbConnectionData;
import br.com.isvor.model.Response;
import br.com.isvor.service.ComentarioCursoConverterService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {

    private final static ComentarioCursoConverterService service = new ComentarioCursoConverterService();

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("*****************************************************************************************");
        System.out.println("MIGRAÇÃO DE DADOS");
        System.out.println("*****************************************************************************************");

        String jsonFile = getJsonFile();
        String json = null;

        try {
            json = new String(Files.readAllBytes(Paths.get(jsonFile)));
        } catch (IOException ex) {
            System.out.println("Arquivo não encontrado!");
            System.exit(1);
        }

        List<Response> responses = null;

        try {
            responses = service.converter(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("Arquivo carregado...");
        System.out.println("Quantidade de comentarios: " + responses
                                                            .stream()
                                                            .map(response -> response.getComentarios().size())
                                                            .reduce(0, Integer::sum));

        DbConnectionData dbConnectionData = getDataSource(scanner);

    }

    private static String getJsonFile() {
        System.out.println("");
        //System.out.println("Informe o caminho completo do arquivo JSON a ser importado:");
        //String jsonFile = scanner.nextLine();
        String jsonFile = "/home/diego/Área de Trabalho/comentarioCurso.json";
        return jsonFile;
    }

    private static DbConnectionData getDataSource(Scanner scanner) {
        System.out.println("");
        System.out.println("Informe os dados de conexão com o Banco de Dados:");
        System.out.println("");

        System.out.println("1. URL do Servidor:");
        String url = scanner.nextLine();

        System.out.println("2. Porta:");
        String porta = scanner.nextLine();

        System.out.println("3. Usuário:");
        String usuario = scanner.nextLine();

        System.out.println("4. Senha:");
        String senha = scanner.nextLine();

        System.out.println("5. Nome do Banco de Dados:");
        String database = scanner.nextLine();

        return new DbConnectionData(url, porta, usuario, senha, database);
    }

    public static int saveBannerstoDB(List<Object> banners, DbConnectionData dbConnection) throws Exception {
        int qtdeTotalRegistrosInseridos = 0;
        Connection connection = null;
        PreparedStatement pstmt = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            String urlConexao = "jdbc:mysql://" + dbConnection.getUrl() + ":" + dbConnection.getPort() + "/" + dbConnection.getDatabase() + "?useSSL=false";
            connection = DriverManager.getConnection(urlConexao, dbConnection.getUser(), dbConnection.getPassword());

            String sqlInsert = "INSERT INTO comentario_curso (descricao, resposta, status, tipo_comentario, ativo, data_criacao, data_alteracao, versao, curso_id, moderador_id, profissional_id) VALUES('', '', '', '', 0, '', '', 0, 0, 0, 0);";
            int count = 0;
            int batchSize = 100;

            connection.setAutoCommit(false);
            pstmt = connection.prepareStatement(sqlInsert);

            String bannerId = null;
            String profissionalId = null;
            String dataClique = null;

            for (Banner banner : banners) {
                bannerId = banner.getBannerId().get$numberLong();

                for (Clique clique : banner.getCliques()) {
                    profissionalId = clique.getProfissionalId().get$numberLong();

                    dataClique = clique.getDataClique().get$date().substring(0, clique.getDataClique().get$date().lastIndexOf(".")).replaceAll("T", " ");

                    pstmt.setLong(1, Long.valueOf(bannerId));
                    pstmt.setLong(2, Long.valueOf(profissionalId));
                    pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.parse(dataClique, formatter)));

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
            }

            System.out.println("");
            System.out.println("Inserindo registros remanecentes...");
            int[] result = pstmt.executeBatch();
            System.out.println("Número de registros inseridos: " + result.length);
            System.out.println("");
            connection.commit();

            qtdeTotalRegistrosInseridos += result.length;
        } catch (Exception ex) {
            ex.printStackTrace();
            connection.rollback();
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

        return qtdeTotalRegistrosInseridos;
    }
}
