package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SaldoInicial {

    private int id;
    private double valor;
    private LocalDate configuradoEm;

    public SaldoInicial(double valor) {
        this.id = 0;
        this.valor = valor;
        this.configuradoEm = LocalDate.now();
    }

    public SaldoInicial(int id, double valor, LocalDate configuradoEm) {
        this.id = id;
        this.valor = valor;
        this.configuradoEm = configuradoEm;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public LocalDate getConfiguradoEm() {
        return configuradoEm;
    }

    public void setConfiguradoEm(LocalDate configuradoEm) {
        this.configuradoEm = configuradoEm;
    }

    @Override
    public String toString() {
        String valor = String.format("R$ %.2f", this.valor);
        String dataformatada = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(configuradoEm);

        return String.format("%.2f (Configurado em %s)",
                valor, dataformatada);
    }
}
