package database.dao;

import database.ConexaoDB;
import model.PendenciaBruta;
import parser.DicionarioCategoria;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PendenciaBrutaDAO {

    private final Connection connection;

    public PendenciaBrutaDAO(ConexaoDB conexao) {
        this.connection = conexao.getConnection();
    }

    public int salvar(PendenciaBruta p) {
        String sql = "INSERT INTO pendencias_brutas (mensagem, valor, descricao, categoria, data, criado_em) " +
                "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement comando = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            comando.setString(1, p.getMensagem());
            comando.setDouble(2, p.getValor() > 0 ? p.getValor() : 0.0);
            comando.setString(3, p.getDescricao());
            comando.setString(4, p.getCategoria() != null ? p.getCategoria().name() : null);
            if (p.getData() != null) {
                comando.setDate(5, Date.valueOf(p.getData()));
            } else {
                comando.setNull(5, Types.DATE);
            }
            comando.setDate(6, Date.valueOf(LocalDate.now()));

            if (comando.executeUpdate() > 0) {
                try (ResultSet chaves = comando.getGeneratedKeys()) {
                    if (chaves.next()) return chaves.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao salvar pendência bruta");
            e.printStackTrace();
        }
        return -1;
    }

    public List<PendenciaBruta> buscarTodas() {
        List<PendenciaBruta> lista = new ArrayList<>();
        String sql = "SELECT * FROM pendencias_brutas ORDER BY id";
        try (Statement comando = connection.createStatement();
             ResultSet rs = comando.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao listar pendências brutas");
            e.printStackTrace();
        }
        return lista;
    }

    public PendenciaBruta buscarPorId(int id) {
        String sql = "SELECT * FROM pendencias_brutas WHERE id = ?";
        try (PreparedStatement comando = connection.prepareStatement(sql)) {
            comando.setInt(1, id);
            try (ResultSet rs = comando.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erro ao buscar pendência bruta");
            e.printStackTrace();
        }
        return null;
    }

    public boolean remover(int id) {
        String sql = "DELETE FROM pendencias_brutas WHERE id = ?";
        try (PreparedStatement comando = connection.prepareStatement(sql)) {
            comando.setInt(1, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erro ao remover pendência bruta");
            e.printStackTrace();
            return false;
        }
    }

    private PendenciaBruta mapear(ResultSet rs) throws SQLException {
        String catTexto = rs.getString("categoria");
        DicionarioCategoria categoria = (catTexto != null && !catTexto.isEmpty())
                ? DicionarioCategoria.valueOf(catTexto) : null;
        java.sql.Date dataSql = rs.getDate("data");
        LocalDate data = dataSql != null ? dataSql.toLocalDate() : null;

        return new PendenciaBruta(
                rs.getInt("id"),
                rs.getString("mensagem"),
                rs.getDouble("valor"),
                rs.getString("descricao"),
                categoria,
                data);
    }

    public boolean atualizarCampo(int id, String campo, Object valor) {
        String sql;
        switch (campo) {
            case "valor":     sql = "UPDATE pendencias_brutas SET valor = ? WHERE id = ?"; break;
            case "data":      sql = "UPDATE pendencias_brutas SET data = ? WHERE id = ?"; break;
            case "descricao": sql = "UPDATE pendencias_brutas SET descricao = ? WHERE id = ?"; break;
            case "categoria": sql = "UPDATE pendencias_brutas SET categoria = ? WHERE id = ?"; break;
            default: return false;
        }
        try (PreparedStatement comando = connection.prepareStatement(sql)) {
            if (valor instanceof LocalDate d) {
                comando.setDate(1, Date.valueOf(d));
            } else if (valor instanceof DicionarioCategoria c) {
                comando.setString(1, c.name());
            } else {
                comando.setObject(1, valor);
            }
            comando.setInt(2, id);
            return comando.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("❌ Erro ao atualizar pendência bruta");
            e.printStackTrace();
            return false;
        }
    }
}