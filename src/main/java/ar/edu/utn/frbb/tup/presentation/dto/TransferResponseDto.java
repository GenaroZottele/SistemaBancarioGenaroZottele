package ar.edu.utn.frbb.tup.presentation.dto;

public class TransferResponseDto {
    private String estado;
    private String mensaje;


//getters y setters

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}