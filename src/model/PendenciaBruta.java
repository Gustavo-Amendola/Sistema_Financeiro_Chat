package model;

import parser.DicionarioCategoria;

import java.time.LocalDate;

public class PendenciaBruta {

    private int id;
    private String mensagem;
    private double valor;
    private String descricao;
    private DicionarioCategoria categoria;
    private LocalDate data;


    public PendenciaBruta(String mensagem, double valor, String descricao,
                          DicionarioCategoria categoria, LocalDate data) {
        this.mensagem = mensagem;
        this.valor = valor;
        this.descricao = descricao;
        this.categoria = categoria;
        this.data = data;

    }

    public PendenciaBruta(int id, String mensagem, double valor, String descricao,
                          DicionarioCategoria categoria, LocalDate data) {
        this(mensagem, valor, descricao, categoria, data);
        this.id = id;
    }

    public int getId() { return id; }
    public String getMensagem() { return mensagem; }
    public double getValor() { return valor; }
    public String getDescricao() { return descricao; }
    public DicionarioCategoria getCategoria() { return categoria; }
    public LocalDate getData() { return data; }
}
