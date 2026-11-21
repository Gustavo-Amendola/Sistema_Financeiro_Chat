package database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;

import database.dao.CategoriaDAO;
import database.dao.GastoDAO;
import database.dao.ReceitaDAO;
import database.dao.SaldoInicialDAO;

public class ConexaoDB {

    private static final String ARQUIVO_CONFIG = "config.properties";

    private Connection connection;
    private String url;
    private String usuario;
    private String senha;

    public boolean conectar() {
        if (!carregarConfiguracao()) {
            return false;
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, usuario, senha);
            return true;
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Driver Mysql não encontrado!");
            return false;
        } catch (SQLException e) {
            System.out.println("❌ Erro ao conectar ao banco de dados");
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean carregarConfiguracao() {
        Path caminho = Path.of(ARQUIVO_CONFIG);

        if (!Files.exists(caminho)) {
            System.out.println("❌ Arquivo " + ARQUIVO_CONFIG + " não encontrado.");
            System.out.println("   Copie config.properties.example para config.properties e preencha usuario e senha.");
            return false;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(caminho)) {
            props.load(in);
        } catch (IOException e) {
            System.out.println("❌ Não foi possível ler " + ARQUIVO_CONFIG);
            return false;
        }

        this.url = props.getProperty("url");
        this.usuario = props.getProperty("usuario");
        this.senha = props.getProperty("senha");

        if (url == null || usuario == null || senha == null || senha.equals("SUA_SENHA_AQUI")) {
            System.out.println("❌ Preencha url, usuario e senha em " + ARQUIVO_CONFIG);
            return false;
        }
        return true;
    }

    public void desconectar() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
                System.out.println("✅ Desconectado do banco de dados");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao desconectar!");
            e.printStackTrace();
        }
    }

    public boolean isConectado() {
        try {
            return this.connection != null && !this.connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public boolean criarTodasAsTabelas() {
        GastoDAO gastoDAO = new GastoDAO(this);
        ReceitaDAO receitaDAO = new ReceitaDAO(this);
        CategoriaDAO categoriaDAO = new CategoriaDAO(this);
        SaldoInicialDAO saldoInicialDAO = new SaldoInicialDAO(this);

        boolean categorias = categoriaDAO.criarTabelasCategoria();
        boolean gastos = gastoDAO.criarTabelasGastos();
        boolean receitas = receitaDAO.criarTabelasReceitas();
        boolean saldoInicial = saldoInicialDAO.criarTabelasSaldoInicial();

        if (categorias && gastos && receitas && saldoInicial) {
            System.out.println("✅ Todas as tabelas criadas com sucesso");
            return true;
        } else {
            System.out.println("❌ Erro ao criar algumas tabelas");
            return false;
        }
    }
}