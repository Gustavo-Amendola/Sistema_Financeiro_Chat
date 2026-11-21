package sistemaPrincipal;

import controller.FinanceiroController;
import database.ConexaoDB;
import view.ConsoleView;

public class main { // FINALIZADO NO CONSOLE SÓ CONTINUAR FAZER ALGUMAS MUDANÇAS
    public static void main(String[] args) {


        System.out.println("\nIniciando Gestor Financeiro...");

        if(!validarPreRequisitos()){
            System.out.println("❌ Falha na validação de pré-requisitos");
            return;
        }

        ConexaoDB conexaoDB = new ConexaoDB();
        if(!verificarConexaoDB(conexaoDB)){
            exibirMensagemErroConexao();
            return;
        }

        if(!conexaoDB.criarTodasAsTabelas()){
            exibirMensagemErroTabelasSQL();
            conexaoDB.desconectar();
            return;
        }

        FinanceiroController controller = new FinanceiroController(conexaoDB);

        ConsoleView view = new ConsoleView(controller);
        verificarPrimeiraExecucao(conexaoDB,view,controller);

        view.iniciarLoopPrincipal();

        encerrarAplicacao(conexaoDB, view);
    }

    public static boolean verificarConexaoDB(ConexaoDB conexaoDB){
        boolean conectado = conexaoDB.conectar();

        if(conectado){
            System.out.println("✅ Conectado ao banco de dados");
        }else {
            System.out.println("❌ Erro ao conectar ao banco de dados");
        }
        return conectado;
    }

    public static void verificarPrimeiraExecucao(ConexaoDB conexaoDB, ConsoleView view, FinanceiroController controller){
        if(!controller.temSaldoInicial()){
            view.exibirMensagemSaldoInicial();
            view.exibirBoasVindas();
            double saldoInicial = view.solicitarSaldoInicial();
            controller.inicializarSaldoInicial(saldoInicial);
            view.exibirSaldoInicialConfigurado(saldoInicial);
        }else {
            view.exibirBoasVindas();
        }
    }

    public static void exibirMensagemErroConexao(){
        System.out.println("\n❌ Erro ao conectar ao banco de dados! \n");
        System.out.println("Verifique:");
        System.out.println("MySQL esta rodando?");
        System.out.println("Banco 'financeiro' existe?");
        System.out.println("Credenciais estão corretas \n");
        System.out.println("Dicas:");
        System.out.println("- Abra MySQL worbench");
        System.out.println("- Crie banco: CREATE DATABASE financeiro");
        System.out.println("- Tente novamente\n");
    }

    public static void exibirMensagemErroTabelasSQL(){
        System.out.println("\n❌ Erro ao criar tabelas no banco de dados!\n");
        System.out.println("Tente criar manualmente ou reintale o banco");
    }

    public static void encerrarAplicacao(ConexaoDB conexaoDB, ConsoleView view){
        conexaoDB.desconectar();
        view.fecharScanner();
        System.out.println("✅ Aplicação encerrada com sucesso");
    }

    public static void exibirMensagemEncerramento(){
        System.out.println("\n ✅ Aplicação encrrada com sucesso");
        System.out.println("👋 Até a proxima");
    }

    public static boolean validarPreRequisitos(){
        return verificarDriverMySQL();
    }

    public static boolean verificarDriverMySQL(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL encontrado");
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver MySQL não encontrado");
            System.out.println("Adicione o arquivo mysql-connector-java.jar ao projeto ");
            return false;
        }
    }


}
