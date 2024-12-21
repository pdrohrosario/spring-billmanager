# Spring Boot Bill Manager

Este documento descreve os requisitos gerais e específicos para uma aplicação Spring Boot para gerenciar contas a pagar.

---

## Requisitos Gerais do Projeto

1. **Utilizar a linguagem de programação Java, versão 17 ou superior.**
2. **Utilizar Spring Boot.**
3. **Utilizar o banco de dados PostgreSQL.**
4. **A aplicação deve ser executada em um container Docker.**
5. **Tanto a aplicação, banco de dados, quanto outros serviços necessários para executar a aplicação, devem ser orquestrados utilizando Docker Compose.**
7. **Utilizar mecanismo de autenticação.**
8. **Organizar o projeto com Domain Driven Design.**
9. **Utilizar o Flyway para criar a estrutura de banco de dados.**
10. **Utilizar JPA.**

---

## Requisitos Específicos

1. **Cadastrar a tabela no banco de dados para armazenar as contas a pagar.**
    - A tabela deve incluir no mínimo os seguintes campos (faça a tipagem conforme achar adequado):
        - `id`
        - `data_vencimento`
        - `data_pagamento`
        - `valor`
        - `descricao`
        - `situacao`

2. **Implementar a entidade "Conta" na aplicação, de acordo com a tabela criada anteriormente.**

3. **Implementar as seguintes APIs:**
    - **Cadastrar conta**
    - **Atualizar conta**
    - **Alterar a situação da conta**
    - **Obter a lista de contas a pagar, com filtro de data de vencimento e descrição**
    - **Obter conta filtrando o id**
    - **Obter valor total pago por período**

4. **Implementar mecanismo para importação de contas a pagar via arquivo CSV.**
    - O arquivo será consumido via API.

---

## Como Executar

1. Clone o repositório para sua máquina local.
2. Certifique-se de ter o Docker e Docker Compose instalados.
4. Execute o comando `docker-compose up --build` para iniciar o ambiente.
5. Utilize as APIs fornecidas para gerenciar contas a pagar.

---

## Collections

Para executar as chamadas da API basta importar o arquivo billmanager.json.

---

## Estrutura do Projeto

O projeto está organizado de acordo com os princípios de Domain Driven Design, utilizando camadas para separar responsabilidades:

- **Domain**: Contém as entidades.
- **Application**: Inclui serviços e lógica de aplicação.
- **Infrastructure**: Contém repositórios, configurações de banco de dados e integrações externas.

---

## Contribuição

1. Faça um fork do repositório.
2. Crie uma branch para suas alterações: `git checkout -b minha-feature`.
3. Faça commit das suas alterações: `git commit -m 'Minha nova feature'`.
4. Envie para o repositório remoto: `git push origin minha-feature`.
5. Abra um Pull Request para revisão.