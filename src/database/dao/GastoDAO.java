package database.dao;

import database.ConexaoDB;
import model.Gasto;
import parser.DicionarioCategoria;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GastoDAO {
    private Connection connection;

    public GastoDAO(ConexaoDB conexaoDB){
        this.connection = conexaoDB.getConnection();
    }


    public boolean criarTabelasGastos(){
        String sql = "CREATE TABLE IF NOT EXISTS gastos ("+
                "id INT AUTO_INCREMENT PRIMARY KEY,"+
                "valor DECIMAL(10,2) NOT NULL,"+
                "descricao VARCHAR(255),"+
                "categoria VARCHAR(100) NOT NULL,"+
                "data DATE NOT NULL,"+
                "criado_em DATE NOT NULL"+
                ");";

        try{
            Statement stmt = this.connection.createStatement();
            stmt.execute(sql);
            stmt.close();
            System.out.println("✅ Tabela GASTOS criada com sucesso");
            return true;

        }catch(SQLException e){
            System.out.println("❌ Erro ao criar tabela GASTOS");
            e.printStackTrace();
            return false;
        }
    }

    public int salvarGasto(Gasto gasto) {
        String sql = "INSERT INTO gastos (valor, descricao, categoria, data, criado_em) VALUES (?,?,?,?,?)";
        try (PreparedStatement comando = this.connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setDouble(1, gasto.getValor());
            comando.setString(2, gasto.getDescricao());
            comando.setString(3, gasto.getCategoria() != null ? gasto.getCategoria().name() : null);
            if (gasto.getData() != null) {
                comando.setDate(4, java.sql.Date.valueOf(gasto.getData()));
            } else {
                comando.setNull(4, java.sql.Types.DATE);
            }
            comando.setDate(5, java.sql.Date.valueOf(gasto.getCriadoEm()));

            int linhas = comando.executeUpdate();
            if (linhas > 0) {
                try (ResultSet chaves = comando.getGeneratedKeys()) {
                    if (chaves.next()) {
                        return chaves.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao salvar gasto");
            e.printStackTrace();
        }
        return -1;
    }

    public List<Gasto> buscarTodos(){
        String sql = "SELECT * FROM gastos ORDER BY data DESC";
        List<Gasto> gastos = new ArrayList<>();

        try {
            Statement comando = this.connection.createStatement();
            ResultSet resultado = comando.executeQuery(sql);

            while (resultado.next()){
                int id = resultado.getInt("id");
                double valor = resultado.getDouble("valor");
                String descricao = resultado.getString("descricao");
                String categoriaStr = resultado.getString("categoria");
                String catTexto = resultado.getString("categoria");
                DicionarioCategoria categoria = (catTexto != null && !catTexto.isEmpty())
                        ? DicionarioCategoria.valueOf(catTexto)
                        : null;
                java.sql.Date dataSql = resultado.getDate("data");
                LocalDate data = (dataSql != null) ? dataSql.toLocalDate() : null;
                dataSql = resultado.getDate("criado_em");
                LocalDate criadoEm = dataSql.toLocalDate();

                Gasto gasto = new Gasto(id, valor, descricao, categoria, data, criadoEm);
                gastos.add(gasto);
            }
            comando.close();
        }catch(SQLException e){
            System.out.println("❌ Erro ao buscar gastos");
            e.printStackTrace();
        }

        return gastos;
    }

    public List<Gasto> buscarPorCategoria(String categoria){
        String sql = "SELECT * FROM gastos WHERE categoria = ? ORDER BY data DESC";
        List<Gasto> gastos = new ArrayList<>();

        try{
            PreparedStatement comando = this.connection.prepareStatement(sql);
            comando.setString(1, categoria);
            ResultSet resultado = comando.executeQuery();

            while (resultado.next()){
                int id = resultado.getInt("id");
                double valor = resultado.getDouble("valor");
                String descricao = resultado.getString("descricao");
                String categoriaStr = resultado.getString("categoria");
                DicionarioCategoria cat = DicionarioCategoria.valueOf(categoriaStr);
                java.sql.Date dataSql = resultado.getDate("data");
                LocalDate data = dataSql.toLocalDate();
                dataSql = resultado.getDate("criado_em");
                LocalDate criadoEm = dataSql.toLocalDate();

                Gasto gasto = new Gasto(id, valor, descricao, cat, data, criadoEm);
                gastos.add(gasto);
            }
            comando.close();
        }catch(SQLException e){
            System.out.println("❌ Erro ao buscar gastos por categorias");
            e.printStackTrace();
        }

        return gastos;
    }

    public List<Gasto> buscaPorPeriodo(LocalDate inicio, LocalDate fim){

        String sql = "SELECT * FROM gastos WHERE data BETWEEN ? AND ? ORDER BY data DESC";
        List<Gasto> gastos = new ArrayList<>();

        try{
            PreparedStatement comando = this.connection.prepareStatement(sql);
            comando.setDate(1, java.sql.Date.valueOf(inicio));
            comando.setDate(2, java.sql.Date.valueOf(fim));
            ResultSet resultado = comando.executeQuery();

            while(resultado.next()){
                int id = resultado.getInt("id");
                double valor = resultado.getDouble("valor");
                String descricao = resultado.getString("descricao");
                String categoriaStr = resultado.getString("categoria");
                DicionarioCategoria categoria = DicionarioCategoria.valueOf(categoriaStr);
                java.sql.Date dataSql = resultado.getDate("data");
                LocalDate data = dataSql.toLocalDate();
                dataSql = resultado.getDate("criado_em");
                LocalDate criadoEm = dataSql.toLocalDate();

                Gasto gasto = new Gasto(id, valor, descricao, categoria, data, criadoEm);
                gastos.add(gasto);
            }
            comando.close();
        }catch (SQLException e){
            System.out.println("❌ Erro ao buscar gastos por periodo");
            e.printStackTrace();
        }
        return gastos;
    }

    public List<Gasto> buscarPorMes(int mes, int ano){
        String sql = "SELECT * FROM gastos WHERE MONTH(data) =? AND YEAR(data) = ? ORDER BY data DESC";
        List<Gasto> gastos = new ArrayList<>();

        try{
            PreparedStatement comando = this.connection.prepareStatement(sql);
            comando.setInt(1, mes);
            comando.setInt(2, ano);
            ResultSet resultado = comando.executeQuery();

            while(resultado.next()){
                int id = resultado.getInt("id");
                double valor = resultado.getDouble("valor");
                String descricao = resultado.getString("descricao");
                String categoriaStr = resultado.getString("categoria");
                DicionarioCategoria categoria = DicionarioCategoria.valueOf(categoriaStr);
                java.sql.Date dataSql = resultado.getDate("data");
                LocalDate data = dataSql.toLocalDate();
                dataSql = resultado.getDate("criado_em");
                LocalDate criadoEm = dataSql.toLocalDate();

                Gasto gasto = new Gasto(id, valor, descricao, categoria, data, criadoEm);
                gastos.add(gasto);
            }
            comando.close();
        } catch (SQLException e){
            System.out.println("❌ Erro ao buscar gastos por mês");
            e.printStackTrace();
        }
        return gastos;
    }

    public double calcularTotal(){
        String sql = "SELECT SUM(valor) as total FROM gastos " +
                "WHERE data IS NOT NULL AND descricao IS NOT NULL AND descricao <> '' AND categoria IS NOT NULL";

        try {
            Statement comando = this.connection.createStatement();
            ResultSet resultado = comando.executeQuery(sql);

            if(resultado.next()){
                double total = resultado.getDouble("total");
                comando.close();
                return total;
            }
            comando.close();
        }catch (SQLException e){
            System.out.println("❌ Erro ao calcular total de gastos");
            e.printStackTrace();
        }

        return 0.0;
    }

    public double calcularTotalPorCategoria(String categoria){
        String sql = "SELECT SUM(valor) as total FROM gastos WHERE categoria = ?";

        try {
            PreparedStatement comando = this.connection.prepareStatement(sql);
            comando.setString(1, categoria);
            ResultSet resultado = comando.executeQuery();

            if(resultado.next()){
                double total = resultado.getDouble("total");
                comando.close();
                return total;
            }
            comando.close();
        }catch (SQLException e){
            System.out.println("❌ Erro ao calcular total por categoria");
            e.printStackTrace();
        }
        return 0.0;
    }

    public double calcularTotalPorMes(int mes, int ano){
        String sql = "SELECT SUM(valor) as total FROM gastos WHERE MONTH(data) =? AND YEAR(data) = ?";

        try {
            PreparedStatement comando = this.connection.prepareStatement(sql);
            comando.setInt(1, mes);
            comando.setInt(2, ano);
            ResultSet resultado =comando.executeQuery();

            if(resultado.next()){
                double total = resultado.getDouble("total");
                comando.close();
                return total;
            }
            comando.close();
        }catch (SQLException e){
            System.out.println("❌ Erro ao calcular total por mês");
            e.printStackTrace();
        }
        return 0.0;
    }


    public boolean atualizarCampo(int id, String campo, Object valor) {
        String sql;
        switch (campo) {
            case "valor":     sql = "UPDATE gastos SET valor = ? WHERE id = ?"; break;
            case "data":      sql = "UPDATE gastos SET data = ? WHERE id = ?"; break;
            case "descricao": sql = "UPDATE gastos SET descricao = ? WHERE id = ?"; break;
            case "categoria": sql = "UPDATE gastos SET categoria = ? WHERE id = ?"; break;
            default: return false;
        }
        try (PreparedStatement comando = this.connection.prepareStatement(sql)) {
            if (valor instanceof LocalDate d) {
                comando.setDate(1, java.sql.Date.valueOf(d));
            } else if (valor instanceof DicionarioCategoria c) {
                comando.setString(1, c.name());
            } else {
                comando.setObject(1, valor);
            }
            comando.setInt(2, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erro ao atualizar gasto");
            e.printStackTrace();
            return false;
        }
    }


    public boolean excluir(int id) {
        try (PreparedStatement comando = this.connection.prepareStatement("DELETE FROM gastos WHERE id = ?")) {
            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erro ao excluir gasto");
            e.printStackTrace();
            return false;
        }
    }


}
