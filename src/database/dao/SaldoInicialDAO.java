package database.dao;

import database.ConexaoDB;
import model.SaldoInicial;

import java.sql.*;
import java.time.LocalDate;
import java.util.Collection;

public class SaldoInicialDAO {

    private Connection connection;

    public SaldoInicialDAO(ConexaoDB conexaoDB){
        this.connection = conexaoDB.getConnection();
    }


    public boolean criarTabelasSaldoInicial(){
        String sql = "CREATE TABLE IF NOT EXISTS saldo_inicial ("+
                "id INT PRIMARY KEY,"+
                "valor DECIMAL(10,2) NOT NULL," +
                "configurado_em DATE NOT NULL"+
                ")";
        try{
            Statement stmt = this.connection.createStatement();
            stmt.execute(sql);
            stmt.close();
            System.out.println("✅ Tabela SALDO_INICIAL criada com sucesso");
            return true;
        }catch (SQLException e) {
            System.out.println("❌ Erro ao criar tabela SALDO_INICIAL");
            e.printStackTrace();
            return false;
        }
    }

    public boolean salvarSaldoInicial(SaldoInicial saldo){
        String sqlVerificar = "SELECT COUNT(*) FROM saldo_inicial WHERE id= 1";

        try{
            Statement comando = this.connection.createStatement();
            ResultSet resultado = comando.executeQuery(sqlVerificar);
            resultado.next();
            int existe = resultado.getInt(1);
            comando.close();

            String sql;
            if(existe > 0){
                sql = "UPDATE saldo_inicial SET valor =?, configurado_em =? WHERE id = 1";
            }else {
                sql = "INSERT INTO saldo_inicial(id, valor, configurado_em) "+
                        "VALUES(1,?,?)";
            }

            PreparedStatement comand = this.connection.prepareStatement(sql);
            comand.setDouble(1, saldo.getValor());
            comand.setDate(2, java.sql.Date.valueOf(saldo.getConfiguradoEm()));

            int result = comand.executeUpdate();
            comando.close();

            if(result > 0){
                System.out.println("✅ Saldo inicial salvo com sucesso");
                return true;
            }
        }catch (SQLException e){
            System.out.println("❌ Erro ao salvar saldo inicial");
            e.printStackTrace();
        }
        return false;
    }

    public SaldoInicial buscar(){
        String sql = "SELECT * FROM saldo_inicial WHERE id = 1";
        try {
            Statement comando = this.connection.createStatement();
            ResultSet resultado = comando.executeQuery(sql);

            if(resultado.next()){
                int id = resultado.getInt("id");
                double valor = resultado.getDouble("valor");
                java.sql.Date datasql = resultado.getDate("configurado_em");
                LocalDate data = datasql.toLocalDate();

                comando.close();
                return new SaldoInicial(id, valor, data);
            }
            comando.close();
        }catch(SQLException e){
            System.out.println("❌ Erro ao buscar saldo inicial");
            e.printStackTrace();
        }
        return null;
    }
}
