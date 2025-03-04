package ar.edu.utn.frbb.tup.persistence;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractBaseDao {
    /**
     * Base de datos en memoria, mapeada por "nombre de la entidad" -> Mapa de ID -> Objeto
     */
    protected static Map<String, Map<Long, Object>> poorMansDatabase = new HashMap<>();

    /**
     * Cada DAO concreta define el "nombre" de la entidad 
     * para que se use de clave en la DB en memoria.
     */
    protected abstract String getEntityName();

    /**
     * Retorna el mapa en memoria para la entidad en particular (Transfer, Cuenta, etc.)
     */
    protected Map<Long, Object> getInMemoryDatabase() {
        if (poorMansDatabase.get(getEntityName()) == null) {
            poorMansDatabase.put(getEntityName(), new HashMap<>());
        }
        return poorMansDatabase.get(getEntityName());
    }

    /**
     * Guarda (append) una línea de texto en un archivo.
     */
    protected void saveToFile(String filename, String line) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(line + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lee todas las líneas de un archivo de texto y las devuelve en una lista.
     */
    protected List<String> readFromFile(String filename) {
        List<String> lines = new ArrayList<>();
        try {
            if (Files.exists(Paths.get(filename))) {
                lines = Files.readAllLines(Paths.get(filename));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }
}
