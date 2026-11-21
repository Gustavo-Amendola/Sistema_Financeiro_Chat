package parser;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseadorComandos {

    public double extrairValor(String mensagem){
        if(mensagem == null || mensagem.isEmpty()){
            return -1;
        }

        Pattern pattern = Pattern.compile("\\d+\\.?\\d*");
        Matcher matcher = pattern.matcher(mensagem);

        if(matcher.find()){
            return Double.parseDouble(matcher.group());
        }
        return -1;
    }


    public LocalDate extrairData(String mensagem){
        if(mensagem == null || mensagem.isEmpty()){
            return null;
        }

        String msg = mensagem.toLowerCase();

        if(msg.contains("hoje")){
            return LocalDate.now();
        }

        if(msg.contains("ontem")){
            return LocalDate.now().minusDays(1);
        }

        Pattern patternDia = Pattern.compile("dia\\s+(\\d{1,2})");
        Matcher matcherDia = patternDia.matcher(msg);
        if(matcherDia.find()){
            int dia = Integer.parseInt(matcherDia.group(1));
            LocalDate hoje = LocalDate.now();
            return LocalDate.of(hoje.getYear(), hoje.getMonth(), dia);
        }

        Pattern patternData = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})");
        Matcher matcherData = patternData.matcher(msg);
        if(matcherData.find()){
            int dia = Integer.parseInt(matcherData.group(1));
            int mes = Integer.parseInt(matcherData.group(2));
            int ano = Integer.parseInt(matcherData.group(3));
            return LocalDate.of(ano,mes,dia);
        }

        return null;
    }

    public String extrairDescricao(String mensagem){
        if(mensagem == null || mensagem.isEmpty()){
            return "";
        }
        String descricao = mensagem.toLowerCase();

        descricao = descricao.replaceAll("\\b(gastei|gasto|paguei|ganhei|recebi|entrou)\\b","");

        descricao = descricao.replaceAll("\\b(reais|de|em|no|na|nos|nas)\\b","");

        descricao = descricao.replaceAll("\\d+\\.?\\d*","");

        descricao = descricao.replaceAll("\\b(hoje|ontem|dia)\\b","");
        descricao = descricao.replaceAll("\\d{1,2}/\\d{1,2}/\\d{4}","");

        descricao = descricao.trim().replaceAll("\\s+", " ");

        return descricao;
    }

    public String extrairTipo(String mensagem){
        if(mensagem == null || mensagem.isEmpty()){
            return "DESCONHECIDO";
        }

        String msg = mensagem.toLowerCase();

        if(msg.contains("gastei") || msg.contains("gasto") || msg.contains("paguei")){
            return  "GASTO";
        }

        if(mensagem.contains("ganhei") || msg.contains("recebi") || msg.contains("entrou")){
            return  "RECEITA";
        }

        return "DESCONHECIDO";
    }

    public DicionarioCategoria extrairCategoria(String mensagem){
        java.util.Set<DicionarioCategoria> achadas = DicionarioCategoria.encontrarTodas(mensagem);

        if (achadas.isEmpty()) {
            return DicionarioCategoria.OUTROS;
        }
        if (achadas.size() == 1) {
            return achadas.iterator().next();
        }
        return null; // duas ou mais categorias: fica pendente
    }

    public boolean validarValor(double valor){
        return valor > 0;
    }

    public boolean validarData(LocalDate data){
        if(data == null){
            return false;
        }

        return data.isBefore(LocalDate.now()) || data.isEqual(LocalDate.now());
    }

    public boolean validarTipo(String tipo){
        if(tipo == null || tipo.isEmpty()){
            return false;
        }

        return tipo.equals("GASTOS") || tipo.equals("RECEITA");
    }

    public boolean validarCategoria(DicionarioCategoria categoria){
        return categoria != null;
    }


}
