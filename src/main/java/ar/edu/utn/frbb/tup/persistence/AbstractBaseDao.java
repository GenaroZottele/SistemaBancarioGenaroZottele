package ar.edu.utn.frbb.tup.persistence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractBaseDao {
    protected static Map<String, Map<Long, Object>> poorMansDatabase = new HashMap<>();
    protected abstract String getEntityName();
    private static final String DIRECTORY_PATH = "C:/Users/genar/OneDrive/Documentos/GitHub/SistemaBancarioGenaroZottele/src/main/java/ar.edu.utn.frbb.tup/persistence/data/";

    protected Map<Long, Object> getInMemoryDatabase() {
        if (poorMansDatabase.get(getEntityName()) == null) {
            poorMansDatabase.put(getEntityName(), new HashMap<>());
        }
        return poorMansDatabase.get(getEntityName());
    }

    protected void saveToFile(String filename, String data) {
        ensureFileExists(filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DIRECTORY_PATH + filename, true))) {
            writer.write(data);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected List<String> readFromFile(String filename) {
        ensureFileExists(filename);
        try {
            return Files.lines(Paths.get(DIRECTORY_PATH + filename)).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void ensureFileExists(String filename) {
        File file = new File(DIRECTORY_PATH + filename);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}