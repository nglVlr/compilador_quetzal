package semantico;
//Categoria de simbolo registrado en la tabla de simbolos
public enum TipoSimbolo {

    //Variable declarada con tipo primitivo
    VARIABLE("variable"),

    //Parámetro declarado en la firma de una función
    PARAMETRO("parámetro"),

    //Declaración de función con la palabra reservada 'funcion'
    FUNCION("función"),

    //La definición del objeto
    OBJETO_TIPO("tipo de objeto"),

    //Una variable cuyo tipo es un objeto definido por el usuario
    OBJETO_INSTANCIA("instancia de objeto");

    //Campo para mensaje de errores
    private final String descripcion;

    TipoSimbolo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
