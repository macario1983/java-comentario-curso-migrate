package br.com.isvor;

import br.com.isvor.model.config.DbConnectionData;
import br.com.isvor.model.entity.Response;
import br.com.isvor.service.ComentarioCursoConverterService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

public class Main {

    private final static ComentarioCursoConverterService service = new ComentarioCursoConverterService();

    public static void main(String[] args) {
        service.execute();
    }
}
