package controller;


import database.ConexaoDB;
import database.dao.*;
import model.Gasto;
import model.PendenciaBruta;
import model.Receita;
import model.SaldoInicial;
import parser.DicionarioCategoria;
import parser.ParseadorComandos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FinanceiroController {
    private GastoDAO gastoDAO;
    private ReceitaDAO receitaDAO;
    private CategoriaDAO categoriaDAO;
    private SaldoInicialDAO saldoInicialDAO;
    private ParseadorComandos parseador;
    private double saldoInicial;
    private static final List<String> CAMPOS_EDITAVEIS = List.of("data", "valor");

    private int idEdicao = 0;
    private String campoEdicao = null;
    private String tipoEdicao = null;   // "GASTO" ou "RECEITA"

    private PendenciaBrutaDAO brutaDAO;
    private int idExclusao = 0;
    private String tipoExclusao = null;



    public FinanceiroController(ConexaoDB conexaoDB) {
        this.gastoDAO = new GastoDAO(conexaoDB);
        this.receitaDAO = new ReceitaDAO(conexaoDB);
        this.categoriaDAO = new CategoriaDAO(conexaoDB);
        this.saldoInicialDAO = new SaldoInicialDAO(conexaoDB);
        this.parseador = new ParseadorComandos();
        this.brutaDAO = new PendenciaBrutaDAO(conexaoDB);

        SaldoInicial saldo = this.saldoInicialDAO.buscar();
        if(saldo != null){
            this.saldoInicial = saldo.getValor();
        }else{
            this.saldoInicial = 0.0;
        }
    }

    public boolean inicializarSaldoInicial(double valor){
        if(valor <= 0){
            System.out.println("Saldo inicial deve ser maior que zero");
            return false;
        }

        SaldoInicial saldo = new SaldoInicial(valor);
        boolean resultado = this.saldoInicialDAO.salvarSaldoInicial(saldo);

        if(resultado){
            this.saldoInicial = valor;
            System.out.println("Saldo inicial de R$ "+ String.format("%.2f", valor)+" configurado!");
        }
        return resultado;
    }

    public boolean temSaldoInicial(){
        SaldoInicial saldo = this.saldoInicialDAO.buscar();
        return saldo != null;
    }

    public String processarMensagem(String mensagem){
        if(mensagem == null || mensagem.isEmpty()){
            return "Mensagem vazia!";
        }

        mensagem = mensagem.trim();

        if (idEdicao != 0) {
            return continuarEdicao(mensagem);
        }if (idExclusao != 0) {
            return continuarExclusao(mensagem);
        }
        if (mensagem.toLowerCase().startsWith("excluir ")) {
            return iniciarExclusao(mensagem);
        }
        if (mensagem.toLowerCase().startsWith("editar ")) {
            return iniciarEdicao(mensagem);
        }

        if(ehComando(mensagem)){
            return processarComando(mensagem);
        }

        String tipo = this.parseador.extrairTipo(mensagem);

        if(tipo.equals("GASTO")){
            return registrarGasto(mensagem);
        }else if(tipo.equals("RECEITA")){
            return registrarReceita(mensagem);
        }else {
        return registrarBruta(mensagem);
        }
    }

    private String iniciarEdicao(String mensagem) {
        String[] partes = mensagem.trim().toLowerCase().split("\\s+");

        try {
            if (partes.length == 3) {
                if (!partes[1].equals("gasto") && !partes[1].equals("receita") && !partes[1].equals("bruta")) {
                    return "❌ Use: editar gasto <id>, editar receita <id> ou editar bruta <id>";
                }
                tipoEdicao = partes[1].equals("gasto") ? "GASTO"
                        : partes[1].equals("receita") ? "RECEITA" : "BRUTA";
                idEdicao = Integer.parseInt(partes[2]);
                return "O que você quer editar no " + partes[1] + " " + idEdicao + "?\n"
                        + " Campos: " + textoCampos(tipoEdicao) + "\n"
                        + " (ou \"cancelar\")";
            }
            if (partes.length == 2) {
                idEdicao = Integer.parseInt(partes[1]);
                tipoEdicao = null;
                return "O registro " + idEdicao + " é gasto, receita ou bruta?";
            }
        } catch (NumberFormatException e) {
            limparEdicao();
            return "❌ O id precisa ser um número. Use: editar gasto <id>, editar receita <id> ou editar bruta <id>";
        }

        return "❌ Use: editar <id>, editar gasto <id>, editar receita <id> ou editar bruta <id>";
    }

    private String continuarEdicao(String mensagem) {
        String texto = mensagem.trim();
        String t = texto.toLowerCase();

        if (t.equals("cancelar")) {
            limparEdicao();
            return "Edição cancelada.";
        }

        if (tipoEdicao == null) {
            if (!t.equals("gasto") && !t.equals("receita") && !t.equals("bruta")) {
                return "❌ Digite gasto, receita ou bruta (ou \"cancelar\")";
            }
            tipoEdicao = t.equals("gasto") ? "GASTO" : t.equals("receita") ? "RECEITA" : "BRUTA";
            return "O que você quer editar no " + t + " " + idEdicao + "?\n"
                    + " Campos: " + textoCampos(tipoEdicao) + "\n"
                    + " (ou \"cancelar\")";
        }

        if (campoEdicao == null) {
            if (!camposDoTipo(tipoEdicao).contains(t)) {
                return "❌ Campo inválido. Escolha: " + textoCampos(tipoEdicao);
            }
            campoEdicao = t;
            return "Novo valor para " + t + " (" + tipoEdicao.toLowerCase() + " " + idEdicao + "):";
        }

        Object novoValor;
        try {
            novoValor = converterValorEdicao(campoEdicao, texto);
        } catch (IllegalArgumentException e) {
            return "❌ " + e.getMessage() + "\n Tente de novo ou digite \"cancelar\"";
        }

        if (tipoEdicao.equals("BRUTA") && campoEdicao.equals("tipo")) {
            String tipoNovo = (String) novoValor;
            boolean promovida = promoverBruta(idEdicao, tipoNovo);
            int idBruta = idEdicao;
            limparEdicao();

            if (!promovida) {
                return "❌ Não foi possível promover a bruta " + idBruta;
            }
            return "✅ bruta " + idBruta + " virou " + tipoNovo.toLowerCase()
                    + ". Digite \"pendencias\" para conferir.";
        }

        boolean ok;
        switch (tipoEdicao) {
            case "GASTO"   -> ok = this.gastoDAO.atualizarCampo(idEdicao, campoEdicao, novoValor);
            case "RECEITA" -> ok = this.receitaDAO.atualizarCampo(idEdicao, campoEdicao, novoValor);
            default        -> ok = this.brutaDAO.atualizarCampo(idEdicao, campoEdicao, novoValor);
        }

        String rotulo = tipoEdicao.toLowerCase() + " " + idEdicao;
        limparEdicao();

        if (!ok) {
            return "❌ Não foi possível atualizar o " + rotulo;
        }
        return "✅ Registro " + rotulo + " atualizado. Digite \"pendencias\" para conferir.";
    }

    private boolean promoverBruta(int id, String tipo) {
        PendenciaBruta b = this.brutaDAO.buscarPorId(id);
        if (b == null) {
            return false;
        }

        int novoId;
        if (tipo.equals("GASTO")) {
            novoId = this.gastoDAO.salvarGasto(
                    new Gasto(b.getValor(), b.getDescricao(), b.getCategoria(), b.getData()));
        } else {
            novoId = this.receitaDAO.salvarReceita(
                    new Receita(b.getValor(), b.getDescricao(), b.getCategoria(), b.getData()));
        }

        if (novoId == -1) {
            return false;
        }
        return this.brutaDAO.remover(id);
    }

    private List<String> camposDoTipo(String tipo) {
        if (tipo.equals("BRUTA")) {
            return List.of("tipo", "valor", "data", "descricao", "categoria");
        }
        return List.of("valor", "data", "descricao", "categoria");
    }

    private void limparEdicao() {
        idEdicao = 0;
        campoEdicao = null;
        tipoEdicao = null;
    }

    private Object converterValorEdicao(String campo, String texto) {
        if (campo.equals("valor")) {
            try {
                double v = Double.parseDouble(texto.replace(",", "."));
                if (v <= 0) throw new IllegalArgumentException("O valor precisa ser maior que zero");
                return v;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Valor inválido");
            }
        }
        if (campo.equals("data")) {
            LocalDate hoje = LocalDate.now();
            if (texto.matches("\\d{1,2}")) {
                return LocalDate.of(hoje.getYear(), hoje.getMonth(), Integer.parseInt(texto));
            }
            if (texto.matches("\\d{1,2}/\\d{1,2}/\\d{4}")) {
                String[] p = texto.split("/");
                return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
            }
            throw new IllegalArgumentException("Data inválida. Use o dia (ex: 25) ou DD/MM/AAAA");
        }
        if (campo.equals("descricao")) {
            if (texto.isBlank()) {
                throw new IllegalArgumentException("A descrição não pode ser vazia");
            }
            return texto;
        }
        if (campo.equals("categoria")) {
            for (DicionarioCategoria c : DicionarioCategoria.values()) {
                if (c.name().equalsIgnoreCase(texto.trim())) {
                    return c;
                }
            }
            throw new IllegalArgumentException(
                    "Categoria inválida. Escolha: alimentacao, transporte, diversao, moradia, saude, educacao ou outros");
        }
        if (campo.equals("tipo")) {
            if (texto.equalsIgnoreCase("gasto")) return "GASTO";
            if (texto.equalsIgnoreCase("receita")) return "RECEITA";
            throw new IllegalArgumentException("Tipo inválido. Escolha: gasto ou receita");
        }

        throw new IllegalArgumentException("Campo não suportado");
    }

    public boolean ehComando(String mensagem){
        String msg = mensagem.toLowerCase().trim();

        return msg.equals("saldo") || msg.equals("pendencias") || msg.equals("pendências")
                || msg.equals("listar_gastos") || msg.equals("listar_receitas")
                || msg.equals("sair");
    }

    public String processarComando(String comando){
        String cmd = comando.toLowerCase().trim();

        if(cmd.equals("saldo")){
            return comandoSaldo();
        } else if (cmd.equals("pendencias") || cmd.equals("pendências")) {
            return comandoPendencias();
        } else if (cmd.equals("listar_gastos")) {
            return comandoListarGastos();
        } else if (cmd.equals("listar_receitas")) {
            return comandoListarReceitas();
        } else if (cmd.equals("sair")) {
            return "👋 Até logo!\n💰 Seu saldo final: " + formatarMoeda(calcularSaldo());
        } else {
            return "❌ Comando não reconhecido\n Tente: saldo, pendencias, editar, listar_gastos, listar_receitas, sair";
        }

        // Fora do núcleo por enquanto (não apagar):
        // gastos_categoria, gastos_mes, receitas_mes, resumo_dia
    }

    public String registrarGasto(String mensagem){
        double valor = this.parseador.extrairValor(mensagem);
        String descricao = this.parseador.extrairDescricao(mensagem);
        LocalDate data = this.parseador.extrairData(mensagem);
        DicionarioCategoria categoria = this.parseador.extrairCategoria(mensagem);

        if (!this.parseador.validarValor(valor)) {
            valor = 0.0; // sem número: registro fica pendente com valor vazio
        }

        if (data != null && !this.parseador.validarData(data)) {
            return "❌ A data não pode ser futura\n Use datas passadas ou hoje";
        }

        Gasto gasto = new Gasto(valor, descricao, categoria, data);
        int id = gastoDAO.salvarGasto(gasto);

        if (id == -1) {
            return "❌ Não foi possível salvar o registro";
        }

        return "Registro id: " + id + "\n"
                + "tipo: GASTO\n"
                + "valor: " + (valor > 0 ? formatarMoeda(valor) : "") + "\n"
                + "categoria: " + (categoria != null ? categoria.name() : "") + "\n"
                + "descrição: " + descricao + "\n"
                + "data: " + (data != null ? data.getDayOfMonth() : "") + "\n"
                + "mês: " + (data != null ? nomeMes(data.getMonthValue()) : "") + "\n"
                + "ano: " + (data != null ? data.getYear() : "");
    }

    public String registrarReceita(String mensagem){
        double valor = this.parseador.extrairValor(mensagem);
        String descricao = this.parseador.extrairDescricao(mensagem);
        LocalDate data = this.parseador.extrairData(mensagem);
        DicionarioCategoria categoria = this.parseador.extrairCategoria(mensagem);

        if(!this.parseador.validarValor(valor)){
            return "❌ Não consegui identificar o valor\n Tente: \"ganhei 1000 de salario\"";
        }

        if (data != null && !this.parseador.validarData(data)) {
            return "❌ A data não pode ser futura\n Use datas passadas ou hoje";
        }

        Receita receita = new Receita(valor, descricao, categoria, data);
        int id = receitaDAO.salvarReceita(receita);

        if (id == -1) {
            return "❌ Não foi possível salvar a receita";
        }

        return "Registro id: " + id + "\n"
                + "tipo: RECEITA\n"
                + "valor: " + formatarMoeda(valor) + "\n"
                + "categoria: " + (categoria != null ? categoria.name() : "") + "\n"
                + "descrição: " + descricao + "\n"
                + "data: " + (data != null ? data.getDayOfMonth() : "") + "\n"
                + "mês: " + (data != null ? nomeMes(data.getMonthValue()) : "") + "\n"
                + "ano: " + (data != null ? data.getYear() : "");
    }

    public String comandoSaldo(){
        double saldo = calcularSaldo();
        double totalReceita = this.receitaDAO.calcularTotal();
        double totalGastos = this.gastoDAO.calcularTotal();

        return "💰 Seu saldo atual é "+ formatarMoeda(saldo) +"\n"+
                "📊 Receitas: "+ formatarMoeda(totalReceita)+ "\n"+
                "📊 Gastos: "+formatarMoeda(totalGastos);
    }

    public String comandoListarGastos(){
        List<Gasto> gastos = this.gastoDAO.buscarTodos();

        if(gastos.isEmpty()){
            return "📋 Você ainda não tem gastos registrados";
        }

        StringBuilder stringBuilder = new StringBuilder("📋 Seus gastos:\n");
        for (int i =0; i<gastos.size(); i++){
            Gasto gasto = gastos.get(i);
            stringBuilder.append((i+1)).append(". ").append(gasto.toString()).append("\n");
        }

        stringBuilder.append("\nTotal (somente completos): ").append(formatarMoeda(this.gastoDAO.calcularTotal()));
        return stringBuilder.toString();
    }

    private String textoCampos(String tipo) {
        return String.join(", ", camposDoTipo(tipo));
    }

    public String comandoListarReceitas(){
        List<Receita> receitas = this.receitaDAO.buscarTodos();

        if(receitas.isEmpty()){
            return "📋 Você ainda não tem receitas registradas";
        }

        StringBuilder stringBuilder = new StringBuilder("📋 Suas receitas:\n");
        for(int i = 0; i<receitas.size();i++){
            Receita receita = receitas.get(i);
            stringBuilder.append(i+1).append(". ").append(receita.toString()).append("\n");
        }

        stringBuilder.append("\nTotal: ").append(formatarMoeda(this.receitaDAO.calcularTotal()));
        return stringBuilder.toString();
    }

    public  String comandoGastosPorCategoria(String categoria){
        try{
            DicionarioCategoria cat = DicionarioCategoria.valueOf(categoria.toUpperCase());
            List<Gasto> gastos = this.gastoDAO.buscarPorCategoria(cat.name());

            if(gastos.isEmpty()){
                return "📊 Você ainda não tem gastos nesta categoria";
            }

            StringBuilder stringBuilder = new StringBuilder("📊 Gastos em "+ cat.name()+ ":\n");
            for(int i =0; i<gastos.size();i++){
                Gasto gasto = gastos.get(i);
                stringBuilder.append(i+1).append(". ").append(gasto.toString()).append("\n");
            }

            double total = this.gastoDAO.calcularTotalPorCategoria(cat.name());
            stringBuilder.append("\nTotal: ").append(formatarMoeda(total));
            return stringBuilder.toString();
        }catch (IllegalArgumentException e){
            return "❌ Categoria não encontrado\n Use: Alimentacoa, transposte, diversao, moradia, saude, exucacao, outros";
        }
    }

    public String comandoGastosPorMes(String mesStr){
        try{
            int mes = Integer.parseInt(mesStr);
            int ano = LocalDate.now().getYear();

            if(mes<1 || mes>12){
                return "❌ Mês invalido (use 1-12)";
            }

            List<Gasto> gastos = this.gastoDAO.buscarPorMes(mes, ano);

            if(gastos.isEmpty()){
                return "📊 Você não tem gastos neste mês";
            }

            StringBuilder stringBuilder = new StringBuilder("📊 Gastos em "+ obterNomeMes(mes)+ "/"+ ano+":\n");
            for(int i = 0; i<gastos.size(); i++){
                Gasto gasto = gastos.get(i);
                stringBuilder.append(i+1).append(". ").append(gasto.toString()).append("\n");
            }

            double total = this.gastoDAO.calcularTotalPorMes(mes, ano);
            stringBuilder.append("\nTotal: ").append(formatarMoeda(total));
            return stringBuilder.toString();
        } catch (NumberFormatException e){
            return "❌ Mês invalido\n Use: gasto_mes 1 (ou 08, etc)";
        }
    }


    public String comandoReceitasPorMes(String mesStr){
        try{
            int mes = Integer.parseInt(mesStr);
            int ano = LocalDate.now().getYear();

            if(mes<1 || mes>12){
                return "❌ Mês invalido (use 1-12)";
            }

            List<Receita> receitas = this.receitaDAO.buscarPorMes(mes, ano);

            if(receitas.isEmpty()){
                return "📊 Você não tem receitas neste mês";
            }

            StringBuilder stringBuilder = new StringBuilder("📊 Receitas em "+ obterNomeMes(mes)+ "/"+ ano+":\n");
            for(int i = 0; i<receitas.size(); i++){
                Receita receita = receitas.get(i);
                stringBuilder.append(i+1).append(". ").append(receitas.toString()).append("\n");
            }

            double total = this.receitaDAO.calcularTotalPorMes(mes, ano);
            stringBuilder.append("\nTotal: ").append(formatarMoeda(total));
            return stringBuilder.toString();
        } catch (NumberFormatException e){
            return "❌ Mês invalido\n Use: receitas_mes 1 (ou 08, etc)";
        }
    }
    public String comandoResumoDia(String diaStr){
        try{
            int dia = Integer.parseInt(diaStr);
            LocalDate data = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), dia);

            List<Gasto> gastos = this.gastoDAO.buscaPorPeriodo(data, data);
            List<Receita> receitas = this.receitaDAO.buscaPorPeriodo(data, data);

            StringBuilder stringBuilder = new StringBuilder("Resumo do dia "+formatarData(data) + ":\n\n");
            stringBuilder.append("Gastos: \n");
            if(gastos.isEmpty()){
                stringBuilder.append("Nenhum gasto\n");
            }else {
                for(int i =0; i< gastos.size();i++){
                    stringBuilder.append((i+1)).append(". ").append(gastos.get(i).toString()).append("\n");
                }
                stringBuilder.append("Total: ").append(formatarMoeda(this.gastoDAO.calcularTotal())).append("\n");
            }

            stringBuilder.append("\nReceitas:\n");
            if(receitas.isEmpty()){
                stringBuilder.append("Nenhuma receita\n");
            }else {
                for(int i =0; i<receitas.size();i++){
                    stringBuilder.append(i+1).append(". ").append(receitas.get(i).toString()).append("\n");
                }
                stringBuilder.append("Total: ").append(formatarMoeda(this.receitaDAO.calcularTotal())).append("\n");
            }
            double saldoDia =0;
            for(Receita r: receitas){
                saldoDia += r.getValor();
            }
            for(Gasto g: gastos){
                saldoDia -= g.getValor();
            }

            stringBuilder.append("\n💰 Saldo do dia: ").append(formatarMoeda(saldoDia));
            return stringBuilder.toString();
        }catch(NumberFormatException e){
            return "❌ Dia invalido\n Use: resumo_dia 10 (ou 25, etc)";
        }
    }

    public double calcularSaldo(){
        double totalReceitas = this.receitaDAO.calcularTotal();
        double totalGastos = this.gastoDAO.calcularTotal();

        return this.saldoInicial + totalReceitas - totalGastos;
    }

    public String formatarMoeda(double valor){
        return String.format("R$ %.2f", valor);
    }

    public String formatarData(LocalDate data){
        return java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy").format(data);
    }

    public String obterNomeMes(int mes){
        String[] meses = {"Janeiro", "Fevereiro", "Março","Abril",
                "Maio", "Junho", "Julho", "Agosto",
                "Setembro", "Outubro", "Novembro", "Dezembro"};

        if(mes>= 1 && mes<= 12){
            return meses[mes - 1];
        }
        return "Mês invalido";
    }

    public double getSaldoInicial(){
        return this.saldoInicial;
    }

    private String nomeMes(int mes) {
        String[] meses = {"janeiro", "fevereiro", "março", "abril", "maio", "junho",
                "julho", "agosto", "setembro", "outubro", "novembro", "dezembro"};
        return meses[mes - 1];
    }

    public String comandoPendencias() {
        StringBuilder sb = new StringBuilder("⚠️ Pendências:\n");
        boolean achou = false;

        for (Gasto g : this.gastoDAO.buscarTodos()) {
            List<String> faltando = camposFaltando(g.getValor(), g.getData(), g.getDescricao(), g.getCategoria());
            if (!faltando.isEmpty()) {
                achou = true;
                sb.append("GASTO id ").append(g.getId())
                        .append(" - ").append(faltando.contains("valor") ? "sem valor" : formatarMoeda(g.getValor()))
                        .append(" - falta: ").append(String.join(", ", faltando))
                        .append("\n");
            }
        }

        for (Receita r : this.receitaDAO.buscarTodos()) {
            List<String> faltando = camposFaltando(r.getValor(), r.getData(), r.getDescricao(), r.getCategoria());
            if (!faltando.isEmpty()) {
                achou = true;
                sb.append("RECEITA id ").append(r.getId())
                        .append(" - ").append(faltando.contains("valor") ? "sem valor" : formatarMoeda(r.getValor()))
                        .append(" - falta: ").append(String.join(", ", faltando))
                        .append("\n");
            }
        }

        for (PendenciaBruta b : this.brutaDAO.buscarTodas()) {
            List<String> faltando = camposFaltando(b.getValor(), b.getData(), b.getDescricao(), b.getCategoria());
            faltando.add(0, "tipo");
            achou = true;
            sb.append("BRUTA id ").append(b.getId())
                    .append(" - \"").append(b.getMensagem()).append("\"")
                    .append(" - falta: ").append(String.join(", ", faltando))
                    .append("\n");
        }

        return achou ? sb.toString().trim() : "✅ Nenhuma pendência";
    }

    private List<String> camposFaltando(double valor, LocalDate data, String descricao, DicionarioCategoria categoria) {
        List<String> faltando = new ArrayList<>();
        if (valor <= 0) faltando.add("valor");
        if (data == null) faltando.add("data");
        if (descricao == null || descricao.isBlank()) faltando.add("descrição");
        if (categoria == null) faltando.add("categoria");
        return faltando;
    }


    private String registrarBruta(String mensagem) {
        double valor = this.parseador.extrairValor(mensagem);
        if (!this.parseador.validarValor(valor)) {
            valor = 0.0;
        }
        String descricao = this.parseador.extrairDescricao(mensagem);
        DicionarioCategoria categoria = this.parseador.extrairCategoria(mensagem);
        LocalDate data = this.parseador.extrairData(mensagem);

        int id = brutaDAO.salvar(new PendenciaBruta(mensagem, valor, descricao, categoria, data));
        if (id == -1) {
            return "❌ Não foi possível salvar a mensagem";
        }
        return "Pendência id: " + id + " (tipo não identificado)\n"
                + "mensagem: " + mensagem + "\n"
                + "Ainda não sei se é gasto ou receita.";
    }

    private String iniciarExclusao(String mensagem) {
        String[] partes = mensagem.trim().toLowerCase().split("\\s+");
        if (partes.length != 3) {
            return "❌ Use: excluir gasto <id>, excluir receita <id> ou excluir bruta <id>";
        }
        if (!partes[1].equals("gasto") && !partes[1].equals("receita") && !partes[1].equals("bruta")) {
            return "❌ Use: excluir gasto <id>, excluir receita <id> ou excluir bruta <id>";
        }
        int id;
        try {
            id = Integer.parseInt(partes[2]);
        } catch (NumberFormatException e) {
            return "❌ O id precisa ser um número.";
        }
        tipoExclusao = partes[1].equals("gasto") ? "GASTO"
                : partes[1].equals("receita") ? "RECEITA" : "BRUTA";
        idExclusao = id;
        return "Confirma a exclusão do " + partes[1] + " " + id + "? (sim ou não)";
    }

    private String continuarExclusao(String mensagem) {
        String resposta = mensagem.trim().toLowerCase();
        int id = idExclusao;
        String tipo = tipoExclusao;
        idExclusao = 0;
        tipoExclusao = null;

        if (!resposta.equals("sim")) {
            return "Exclusão cancelada.";
        }

        boolean ok = switch (tipo) {
            case "GASTO"   -> this.gastoDAO.excluir(id);
            case "RECEITA" -> this.receitaDAO.excluir(id);
            default        -> this.brutaDAO.remover(id);
        };

        if (!ok) {
            return "❌ Não foi possível excluir o " + tipo.toLowerCase() + " " + id;
        }
        return "✅ Registro " + tipo.toLowerCase() + " " + id + " excluído.";
    }

}
