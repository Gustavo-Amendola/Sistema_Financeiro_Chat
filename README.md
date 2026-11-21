# Gestor Financeiro Pessoal via Chat

**Autor:** Gustavo Amendola

Aplicação de console em Java para registrar e consultar gastos e receitas por meio de mensagens em linguagem natural, com persistência em MySQL.

Projeto desenvolvido para a disciplina de Projeto em Computação Aplicada, Ciências da Computação, Universidade Nove de Julho.

## Tecnologias

- Java 17
- MySQL 8.0 ou superior
- JDBC (driver MySQL Connector/J 9.7.0)
- Arquitetura MVC com padrão DAO

## Pré-requisitos

- JDK 17
- MySQL Server em execução
- MySQL Connector/J 9.7.0 (arquivo `.jar`)

## Configuração

### 1. Criar o banco de dados

No MySQL, crie o banco:

```sql
CREATE DATABASE financeiro;
```

As tabelas são criadas automaticamente pela aplicação na primeira execução.

### 2. Configurar as credenciais

Copie o arquivo de exemplo e preencha com os dados do seu MySQL:

```bash
cp config.properties.example config.properties
```

Edite o `config.properties`:

```properties
usuario=root
senha=SUA_SENHA
url=jdbc:mysql://localhost:3306/financeiro
```

O arquivo `config.properties` não é versionado e deve permanecer apenas na sua máquina.

### 3. Adicionar o driver MySQL

Adicione o `mysql-connector-j-9.7.0.jar` ao classpath do projeto. No IntelliJ: **File → Project Structure → Libraries → + → Java** e selecione o arquivo.

## Execução

Execute a classe `sistemaPrincipal.main` pelo IntelliJ ou pelo terminal.

Ao iniciar, a aplicação verifica o driver, conecta ao banco, cria as tabelas e, na primeira execução, solicita o saldo inicial.

## Uso

Escreva as movimentações em frases naturais:

```
gastei 35 reais no almoço hoje
ganhei 1000 de salario
```

Comandos disponíveis: `saldo`, `pendencias`, `editar`, `excluir`, `listar_gastos`, `listar_receitas` e `sair`.

Mensagens sem palavra de tipo (gasto ou receita) são guardadas como pendências, para classificação posterior.

## Limitações conhecidas

- Valores com vírgula decimal (ex.: `71,90`) não são lidos corretamente; use ponto (`71.90`).
- Datas inválidas na edição (ex.: dia 31 em mês de 30 dias) geram erro não tratado.
- Não há autenticação: a aplicação é de uso individual, local.
