package database.dao;

import database.ConexaoDB;
import model.Categoria;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    private Connection connection;

    public CategoriaDAO(ConexaoDB conexaoDB){
        this.connection = conexaoDB.getConnection();
    }


    public boolean criarTabelasCategoria(){
        String sql = "CREATE TABLE IF NOT EXISTS categorias ("+
                "id INT AUTO_INCREMENT PRIMARY KEY,"+
                "nome VARCHAR(100) NOT NULL UNIQUE,"+
                "criada_em DATE NOT NULL"+
                ")";

        try{
            Statement stmt = this.connection.createStatement();
            stmt.execute(sql);
            stmt.close();
            System.out.println("✅ Tabela CATEGORIA criada com sucesso");
            return true;
        }catch (SQLException e){
            System.out.println("❌ Erro ao criar tabela CATEGORIAS");
            e.printStackTrace();
            return false;
        }
    }

    public boolean salvarCategoria(Categoria categoria){
        String sql = "INSERT INTO categorias (nome, criado_em) "+
                "VALUES (?, ?)";

        try {
            PreparedStatement comando = this.connection.prepareStatement(sql);
            comando.setString(1, categoria.getNome());
            comando.setDate(2, java.sql.Date.valueOf(categoria.getCriadoEm()));

            int resultado = comando.executeUpdate();
            comando.close();

            if(resultado >0){
                System.out.println("✅ Categoria salva com sucesso");
                return true;
            }
        } catch(SQLException e){
            System.out.println("❌ Erro ao salvar categoria");
            e.printStackTrace();
        }
        return false;
    }

    public List<Categoria> buscarTodos(){
        String sql = "SELECT * FROM categorias ORDER BY nome ASC";
        List<Categoria> categorias = new ArrayList<>();

        try{
            Statement comando = this.connection.createStatement();
            ResultSet resultado = comando.executeQuery(sql);

            while(resultado.next()){
                int id = resultado.getInt("id");
                String nome = resultado.getString("nome");
                java.sql.Date dataSql = resultado.getDate("criada_em");
                LocalDate criadEm = dataSql.toLocalDate();

                Categoria categoria = new Categoria(id, nome, criadEm);
                categorias.add(categoria);
            }
            comando.close();
        }catch(SQLException e){
            System.out.println("❌ Erro ao buscar categorias");
            e.printStackTrace();
        }
        return categorias;
    }

    public Categoria buscarPorNome(String nome){
        String sql = "SELECT * FROM categorias WHERE nome= ?";

        try{
            PreparedStatement comando = this.connection.prepareStatement(sql);
            comando.setString(1, nome);
            ResultSet resultado = comando.executeQuery();

            if(resultado.next()){
                int id = resultado.getInt("id");
                String nomeCat = resultado.getString("nome");
                java.sql.Date dataSql = resultado.getDate("criada_em");
                LocalDate criadoEm = dataSql.toLocalDate();

                comando.close();
                return new Categoria(id, nomeCat, criadoEm);

            }
            comando.close();
        }catch(SQLException e){
            System.out.println("❌ Erro ao buscar categoria por nome");
            e.printStackTrace();
        }
        return null;
    }

    public Categoria buscarPorId(int id) {
        String sql = "SELECT * FROM categorias WHERE id = ?";

        try {
            PreparedStatement comando = this.connection.prepareStatement(sql);
            comando.setInt(1, id);
            ResultSet resultado = comando.executeQuery();

            if (resultado.next()) {
                int idCat = resultado.getInt("id");
                String nome = resultado.getString("nome");
                java.sql.Date dataSql = resultado.getDate("criada_em");
                LocalDate criadoEm = dataSql.toLocalDate();

                comando.close();
                return new Categoria(idCat, nome, criadoEm);
            }

            comando.close();
        } catch (SQLException e) {
            System.out.println("❌ Erro ao buscar categoria por id");
            e.printStackTrace();
        }

        return null;
    }

}
