package model;

import parser.DicionarioCategoria;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Receita {

    private int id;
    private double valor;
    private String descricao;
    private DicionarioCategoria categoria;
    private LocalDate data;
    private LocalDate criadoem;

    public Receita(double valor, String descricao, DicionarioCategoria categoria, LocalDate data) {
        this.id = 0;
        this.valor = valor;
        this.descricao = descricao;
        this.categoria = categoria;
        this.data = data;
        this.criadoem = LocalDate.now();
    }

    public Receita(int id, double valor, String descricao, DicionarioCategoria categoria, LocalDate data, LocalDate criadoem) {
        this.id = id;
        this.valor = valor;
        this.descricao = descricao;
        this.categoria = categoria;
        this.data = data;
        this.criadoem = criadoem;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setCategoria(DicionarioCategoria categoria) {
        this.categoria = categoria;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public void setCriadoem(LocalDate criadoem) {
        this.criadoem = criadoem;
    }

    public int getId() {
        return id;
    }

    public double getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public DicionarioCategoria getCategoria() {
        return categoria;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalDate getCriadoem() {
        return criadoem;
    }

    @Override
    public String toString() {
        String dataTxt = this.data != null
                ? this.data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                : "sem data";
        String catTxt = this.categoria != null
                ? this.categoria.name().charAt(0) + this.categoria.name().substring(1).toLowerCase()
                : "sem categoria";
        String descTxt = (this.descricao == null || this.descricao.isBlank())
                ? "sem descrição" : this.descricao;

        return String.format("R$ %.2f - %s (%s) - %s", this.valor, descTxt, catTxt, dataTxt);
    }
}
