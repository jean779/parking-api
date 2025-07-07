# Estapar Parking Management API

Projeto desenvolvido como parte de um teste técnico para vaga de Desenvolvedor Backend Java/Kotlin Sênior.

## 🚀 Objetivo
Criar um sistema de gestão de estacionamentos com controle de:
- Entrada e saída de veículos
- Ocupação de vagas
- Faturamento por setor

O sistema consome dados via webhook e inicializa a configuração da garagem com base em um simulador fornecido.

---

## 📄 Tecnologias Utilizadas
- Java 21
- Spring Boot 3.x
- Gradle
- PostgreSQL
- Docker
- JUnit 5 / Mockito / MockMvc

---

## 🏗️ Funcionalidades

### Integração com simulador da garagem
- Ao iniciar a aplicação, os dados da garagem são importados via `GET /garage`
- Armazena setores e vagas em banco de dados

### Webhook de eventos de veículos
- Endpoint: `POST /webhook`
- Eventos suportados:
  - `ENTRY`: entrada do veículo
  - `PARKED`: associação do veículo a uma vaga
  - `EXIT`: saída e cálculo de preço

### Regras de Negócio Implementadas
- Carência de 15 minutos sem cobrança
- Cobrança pró-rata a cada 15 minutos após a 1ª hora
- Preço dinâmico conforme lotação do setor
- Fechamento do setor ao atingir 100% da capacidade
- Verificação de horário de funcionamento dos setores

---

## 🔁 Perfis da aplicação

| Perfil   | Finalidade                      | Banco        |
|----------|----------------------------------|--------------|
| `dev`    | Desenvolvimento local            | PostgreSQL   |
| `docker` | Execução via Docker              | PostgreSQL   |
| `test`   | Execução de testes automatizados | H2 in-memory |

---

## 🔎 Consultas REST

### Placa
- `GET /parking-status/plate?license_plate=XXX`
- Retorna status da placa: tempo estacionado e valor atual

### Histórico de Placa
- `GET /parking-status/plate-history?licensePlate=XXX&page=0&size=10`
- Lista entradas e saídas de uma placa paginadas

### Vaga
- `GET /parking-status/spot?lat=...&lng=...`
- Retorna se a vaga está ocupada e tempo de uso

### Faturamento
- `GET /revenue?date=YYYY-MM-DD&sector=A`
- Retorna faturamento do setor no dia

---

## 🐳 Como rodar com Docker

### Pré-requisitos:
- Docker e Docker Compose instalados

### Passos:

1. Clone o repositório:
```bash
git clone <seu-repo>
cd <seu-repo>/docker
```

2. Suba os containers:
```bash
docker-compose up --build
```

3. A aplicação ficará disponível em:
```
http://localhost:8080
```

4. O simulador de garagem estará acessível via:
- Configuração: `GET http://localhost:3000/garage`
- Webhook: `POST http://localhost:3003/webhook`

### 🔧 Estrutura dos serviços Docker

- **estapar-db**: banco de dados PostgreSQL
  - Porta local: `5433` (mapeada para `5432` no container)
- **garage**: simulador fornecido pela Estapar
  - Expõe `/garage` e `/webhook`
- **estapar-app**: aplicação Spring Boot
  - Usa o profile `docker` com as configurações corretas

---

## 🧪 Testes

### ✅ Testes Unitários
- Executados com `./gradlew test`
- Usam JUnit e Mockito para validar regras de negócio como:
  - Entrada duplicada
  - Setor fechado
  - Cálculo de preço

### 🧪 Testes de Integração
- Utilizam `@SpringBootTest` com `MockMvc` e banco H2
- Valida o funcionamento completo dos endpoints e regras

#### Execução manual:
```bash
./gradlew integrationTest
```

> ⚠️ Os testes de integração **não são executados no GitHub Actions** por padrão para evitar falhas por dependências externas.

---

## 📈 Melhorias Futuras
- Suporte a múltiplas garagens
- Agendamento de expurgo de dados antigos
- Dashboard com métricas (Prometheus/Grafana)
- Notificação em tempo real via WebSocket

---

## 🚫 Possíveis Erros Validados
- Entrada duplicada
- Placa inválida
- Spot inexistente ou já ocupado
- Setor fechado por horário ou lotação
- Saída sem entrada registrada

---

## 📦 Estrutura de Pacotes
- `controller` → endpoints REST
- `business` → regras de negócio
- `service` → serviços auxiliares (cálculo, integração)
- `dto` → objetos de entrada/saída
- `repository` → persistência (Spring Data JPA)

---

> Projeto desenvolvido com foco em boas práticas, testes, cobertura das regras propostas e arquitetura limpa para evolução futura.
