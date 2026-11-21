package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Categoria {

    private int id;
    private String nome;
    private LocalDate criadoEm;

    public Categoria(String nome) {
        this.id = 0;
        this.nome = nome;
        this.criadoEm = LocalDate.now();
    }

    public Categoria(int id, String nome, LocalDate criadoEm){
        this. id = id;
        this.nome = nome;
        this.criadoEm = criadoEm;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDate criadoEm) {
        this.criadoEm = criadoEm;
    }

    @Override
    public String toString() {

        String dataformatada = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(this.criadoEm);

        return this.nome +"(criado em "+dataformatada+")";
    }
}
