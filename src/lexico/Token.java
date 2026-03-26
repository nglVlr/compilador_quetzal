package lexico;

public class Token {

    private final TipoToken tipo;
    private final String    valor;
    private final int       linea;
    private final int       columna;

    public Token(TipoToken tipo, String valor, int linea, int columna) {
        this.tipo    = tipo;
        this.valor   = valor;
        this.linea   = linea;
        this.columna = columna;
    }

    public TipoToken getTipo()    { return tipo;    }
    public String    getValor()   { return valor;   }
    public int       getLinea()   { return linea;   }
    public int       getColumna() { return columna; }

    @Override
    public String toString() {
        return String.format("Token { tipo=%-25s valor=%-22s linea=%-4d col=%d }",
                tipo,
                "\"" + valor + "\"",
                linea,
                columna);
    }
}
