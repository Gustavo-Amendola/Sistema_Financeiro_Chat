package parser;

public enum DicionarioCategoria {

    ALIMENTACAO("açougue", "padaria","mercado","restaurante", "supermercado", "café", "lanchonete"),
    TRANSPORTE("uber", "ônibus", "combustível", "táxi", "metrô", "cia aérea", "passagem"),
    DIVERSAO("cinema", "netflix", "game", "show", "festa", "bar"),
    MORADIA("aluguel", "condomínio", "luz", "água", "internet", "telefone", "gás"),
    SAUDE("farmácia", "médico", "academia", "dentista", "hospital", "consulta"),
    EDUCACAO("curso", "livro", "escola", "aula", "material escolar", "faculdade"),
    OUTROS();

    private String[] palavrasChave;

    DicionarioCategoria(String... palavras) {
        this.palavrasChave = palavras;
    }
    public String[] getPalavrasChave() {
        return palavrasChave;
    }

    public static DicionarioCategoria buscarPorPalavra(String palavra){

        if(palavra == null || palavra.isEmpty()){
            return OUTROS;
        }

        String palavraLower = palavra.toLowerCase().trim();

        for(DicionarioCategoria categoria : DicionarioCategoria.values()){
            for(String chave : categoria.getPalavrasChave()){
                if(chave.toLowerCase().equals(palavraLower)){
                    return categoria;
                }
            }
        }
        return OUTROS;
    }

    public static java.util.Set<DicionarioCategoria> encontrarTodas(String texto) {
        java.util.Set<DicionarioCategoria> achadas = java.util.EnumSet.noneOf(DicionarioCategoria.class);
        if (texto == null || texto.isBlank()) {
            return achadas;
        }
        String t = texto.toLowerCase();
        for (DicionarioCategoria cat : values()) {
            for (String chave : cat.palavrasChave) {
                java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                        "(?<!\\p{L})" + java.util.regex.Pattern.quote(chave.toLowerCase()) + "(?!\\p{L})");
                if (p.matcher(t).find()) {
                    achadas.add(cat);
                    break;
                }
            }
        }
        return achadas;
    }

}