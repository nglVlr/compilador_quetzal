package semantico;

import java.util.List;

//Representa el ingreso de un único simbolo dentro de la tabla de simbolos
public class SimboloEntrada {
    //Campos base de todos los simbolos
    private final String nombre;
    private final String tipo;
    private final String tipoElementoLista;
    private final boolean mutable;
    private final TipoSimbolo tipoSimbolo;
    private final int linea;
    private final List<SimboloEntrada> parametros;
    private String tipoRetorno;

    //Constructor para una nueva entrada de simbolo
    public SimboloEntrada(
            String nombre,
            String tipo,
            String tipoElementoLista,
            boolean mutable,
            TipoSimbolo tipoSimbolo,
            int linea,
            List<SimboloEntrada> parametros,
            String tipoRetorno
    ){
        this.nombre = nombre;
        this.tipo = tipo;
        this.tipoElementoLista = tipoElementoLista;
        this.mutable = mutable;
        this.tipoSimbolo = tipoSimbolo;
        this.linea = linea;
        this.parametros = parametros;
        this.tipoRetorno = tipoRetorno;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public String getTipoElementoLista() {
        return tipoElementoLista;
    }

    public boolean esMutable() {
        return mutable;
    }

    public TipoSimbolo getTipoSimbolo() {
        return tipoSimbolo;
    }

    public int getLinea() {
        return linea;
    }

    public List<SimboloEntrada> getParametros() {
        return parametros;
    }

    public String getTipoRetorno() {
        return tipoRetorno;
    }

    //Metodos de conveniencia para simbolos declarado segun su categoria
    public boolean esFuncion(){
        return tipoSimbolo == TipoSimbolo.FUNCION;
    }

    public boolean esListaTipada(){
        return "lista".equals(tipo) && tipoElementoLista != null;
    }

    public boolean esObjeto(){
        return tipoSimbolo == TipoSimbolo.OBJETO_TIPO ||
        tipoSimbolo == TipoSimbolo.OBJETO_INSTANCIA;
    }

    //toString para depuracion
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SimboloEntrada {");
        sb.append(" nombre='").append(nombre).append("'");
        sb.append(", tipo='").append(tipo).append("'");
        if (tipoElementoLista != null){
            sb.append("<, tipoElementoLista='>").append(tipoElementoLista).append(">");
        }
        sb.append(", mutable=").append(mutable);
        sb.append(", categoria=").append(tipoSimbolo.getDescripcion());
        sb.append(", linea=").append(linea);
        if (tipoRetorno != null){
            sb.append(", retorna='").append(tipoRetorno).append("'");
        }
        if (parametros != null){
            sb.append(", params=").append(parametros.size());
        }
        sb.append(" }");
        return sb.toString();
    }
}
