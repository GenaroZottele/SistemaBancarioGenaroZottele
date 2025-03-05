package ar.edu.utn.frbb.tup.model;

public enum TipoPersona {

    PERSONA_FISICA("F"),
    PERSONA_JURIDICA("J");

    private final String descripcion;

    TipoPersona(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static TipoPersona fromString(String text) {
        for (TipoPersona tipo : TipoPersona.values()) {
            // Se verifica si el texto coincide con la descripción (ej: "F" o "J")
            // o con el nombre de la constante (ej: "PERSONA_FISICA").
            if (tipo.descripcion.equalsIgnoreCase(text) || tipo.name().equalsIgnoreCase(text)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("No se pudo encontrar un TipoPersona con la descripción: " + text);
    }
}
