package view;

import controller.FinanceiroController;
import java.util.Scanner;

public class ConsoleView {

    private FinanceiroController controller;
    private Scanner scanner;

    public ConsoleView(FinanceiroController controller) {
        this.controller = controller;
        this.scanner = new Scanner(System.in);
    }

    public void iniciarLoopPrincipal(){

        while(true){
            exibirSemQuebra("> ");
            String entrada = solicitarEntrada();

            if(entrada.equals("sair")){
                exibirAteLogoComSaldoFinal(this.controller.calcularSaldo());
                break;
            }

            String resposta = this.controller.processarMensagem(entrada);
            exibir(resposta);
            exibirLinha();
        }
        scanner.close();;
    }

    public String solicitarEntrada(){
        return this.scanner.nextLine().trim();
    }

    public double solicitarSaldoInicial(){
        while(true){
            try{
                exibirSemQuebra("Qual é seu saldo inicial? ");
                String entrada = solicitarEntrada();
                double valor = Double.parseDouble(entrada.replace(",","."));

                if(valor <=0){
                    exibir("❌ Saldo deve ser maior que zero!");
                    continue;
                }

                return valor;
            }catch (NumberFormatException e){
                exibir("❌ Digite um numero valido!");
            }
        }
    }

    public boolean solicitarSim(String pergunta){
        exibirSemQuebra(pergunta + "(S/N): ");
        String resposta = solicitarEntrada().toUpperCase();
        return resposta.equals("S");
    }

    public void exibir(String mensagem){
        System.out.println(mensagem);
    }

    public void exibirSemQuebra(String mensagem){
        System.out.print(mensagem);
    }

    public void exibirLinha(){
        System.out.println();
    }

    public void exibirSeparador(){
        System.out.println("=====================================");
    }

    public void exibirBoasVindas(){
        exibirSeparador();
        exibir("    GESTOR FINANCEIRO");
        exibirSeparador();
        exibirLinha();
        exibir("Escreva o que gastou ou recebeu, por exemplo:");
        exibir("  \"gastei 10 no mercado\"");
        exibir("  \"ganhei 1000 de salario\"");
        exibirLinha();
        exibir("Comandos: saldo, pendencias, editar, sair");
        exibirSeparador();
        exibirLinha();
    }

    public void exibirMensagemSaldoInicial(){
        exibirLinha();
        exibir("🎯 Primeira vez usando? Configure seus saldo inicial.");
        exibirLinha();
    }

    public void exibirBoasVindasRetorno(){
        exibirLinha();
        exibir("✅ Bem-vindo de volta!");
        exibir("Seus dados foram recuperados");
        exibirLinha();
    }

    public void exibirAteLogoComSaldoFinal(double saldo) {
        exibirLinha();
        exibir("👋 Até logo!");
        exibir("💰 Seu saldo final: " + this.controller.formatarMoeda(saldo));
        exibirLinha();
    }

    public void exibirSaldoInicialConfigurado(double valor) {
        exibirLinha();
        exibir("✅ Saldo inicial de " + this.controller.formatarMoeda(valor) + " configurado!");
        exibir("🎉 Pronto para começar!");
        exibirLinha();
    }

    public void exibirErro(String mensagem){
        exibir("❌ "+ mensagem);
    }

    public void exibirErroValor(){
        exibirErro("Não consegui identificar o valor");
        exibir("💡 Tente: \"gastei 10 reais em mercado\"");
    }

    public void exibirErroCategoria(){
        exibirErro("Não consegui identificara categoria");
        exibir("💡 Tente com: mercado, açougue, uber, cinema, etc");
    }

    public void exibirErroData(){
        exibirErro("A data não pode ser futura");
        exibir("💡 Use datas passadas ou hoje");
    }

    public void exibirErroTipo(){
        exibirErro("não entendi se é gasto ou receita");
        exibir("💡 Use: \"gastei 10\" ou \"ganhei 100\"");
    }

    public void exibirErroComandoDesconhecido(){
        exibirErro("Comando não reconhecido");
        exibir("💡 Tente: saldo, listar_gastos, listar_receitas,");
        exibir(" gastos_categoria, gastos_mes, resumo_dia, sair");
    }

    public void exibirSucesso(String mensagem){
        exibir("✅ "+mensagem);
    }

    public void exibirSaldoAtualizado(double saldoNovo){
        exibir("💰 Seu saldo agora é "+ this.controller.formatarMoeda(saldoNovo));
    }

    public void exibirSaldo(double saldoInicial, double totalReceitas, double totalGastos){
        double saldo = saldoInicial + totalReceitas - totalGastos;

        exibir("💰 Seu saldo atual é " + this.controller.formatarMoeda(saldo));
        exibir("📊 Saldo inicial: "+ this.controller.formatarMoeda(saldoInicial));
        exibir("📊 Receitas: "+ this.controller.formatarMoeda(totalReceitas));
        exibir("📊 Gastos: "+ this.controller.formatarMoeda(totalGastos));
    }

    public void exibirListaGastos(String listaFormatada){
        exibir(listaFormatada);
    }

    public void exibirListaReceitas(String listaFormatada){
        exibir(listaFormatada);
    }

    public void exibirGastosPorCategoria(String listaFormatada){
        exibir(listaFormatada);
    }

    public void exibirGastosPorMes(String listaFormatada){
        exibir(listaFormatada);
    }

    public void exibirReceitasPorMes(String listaFormatada){
        exibir(listaFormatada);
    }

    public void exibirResumoDia(String resumoFormatado){
        exibir(resumoFormatado);
    }

    public void fecharScanner(){
        if(this.scanner != null){
            this.scanner.close();
        }
    }





}
