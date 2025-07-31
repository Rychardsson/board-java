# 📋 Board de Tarefas - Sistema Kanban em Java

Um sistema completo de gerenciamento de tarefas estilo Kanban desenvolvido em Java, seguindo boas práticas de desenvolvimento e arquitetura limpa.

## 🏗️ Arquitetura do Projeto

### Estrutura de Camadas
```
src/main/java/br/com/dio/
├── 📁 config/           # Configurações da aplicação
├── 📁 dto/              # Data Transfer Objects
├── 📁 exception/        # Exceções customizadas
├── 📁 metrics/          # Sistema de métricas e performance
├── 📁 persistence/      # Camada de persistência
│   ├── 📁 config/       # Configuração de banco
│   ├── 📁 converter/    # Conversores de dados
│   ├── 📁 dao/          # Data Access Objects
│   ├── 📁 entity/       # Entidades do domínio
│   └── 📁 migration/    # Estratégias de migração
├── 📁 search/           # Sistema de busca avançada
├── 📁 service/          # Regras de negócio
├── 📁 ui/               # Interface do usuário
├── 📁 validation/       # Validações e regras
└── Main.java            # Ponto de entrada
```

## 🚀 Funcionalidades

### ✅ Funcionalidades Implementadas

#### 📊 Gestão de Boards
- ✅ Criar boards personalizados
- ✅ Configurar colunas customizadas (inicial, pendente, final, cancelamento)
- ✅ Listar todos os boards
- ✅ Excluir boards com confirmação
- ✅ Validação de dados de entrada

#### 🎯 Gestão de Cards
- ✅ Criar cards com título e descrição
- ✅ Mover cards entre colunas
- ✅ Bloquear/desbloquear cards com motivo
- ✅ Cancelar cards
- ✅ Visualizar detalhes completos

#### 🔍 Sistema de Busca Avançada
- ✅ Busca por texto (título/descrição)
- ✅ Filtros por board, coluna, status
- ✅ Busca por cards bloqueados
- ✅ Filtros por data de criação
- ✅ Resultados paginados

#### 📈 Relatórios e Métricas
- ✅ Relatório de produtividade por board
- ✅ Relatório de cards antigos
- ✅ Métricas de performance em tempo real
- ✅ Distribuição de cards por coluna
- ✅ Estatísticas de bloqueio

#### 🔧 Infraestrutura
- ✅ Sistema de logs estruturado (Logback)
- ✅ Configuração centralizada
- ✅ Tratamento de exceções
- ✅ Métricas de performance
- ✅ Validações robustas
- ✅ Testes unitários

## 🛠️ Tecnologias Utilizadas

### Core
- **Java 17+** - Linguagem principal
- **Gradle** - Gerenciamento de dependências e build
- **MySQL** - Banco de dados
- **Liquibase** - Migração de banco

### Bibliotecas
- **Lombok** - Redução de código boilerplate
- **SLF4J + Logback** - Sistema de logs
- **Jackson** - Serialização JSON
- **JUnit 5** - Testes unitários
- **Mockito** - Mocks para testes
- **AssertJ** - Assertions fluentes

## 📦 Instalação e Configuração

### Pré-requisitos
- Java 17 ou superior
- MySQL 8.0+
- Gradle 7.0+

### 1. Clone o Repositório
```bash
git clone https://github.com/Rychardsson/board-java.git
cd board-java
```

### 2. Configure o Banco de Dados
```sql
CREATE DATABASE board;
CREATE USER 'board'@'localhost' IDENTIFIED BY 'board';
GRANT ALL PRIVILEGES ON board.* TO 'board'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure a Aplicação
Edite o arquivo `src/main/resources/application.properties`:
```properties
# Configurações do banco
database.url=jdbc:mysql://localhost/board
database.user=board
database.password=board

# Configurações de logging
logging.level=INFO
```

### 4. Execute a Aplicação
```bash
# Compile o projeto
./gradlew build

# Execute
./gradlew run
```

## 🎮 Como Usar

### Interface Principal
Ao iniciar a aplicação, você verá o menu principal:

```
=== BEM-VINDO AO GERENCIADOR DE BOARDS ===
Versão 1.0.0 - Sistema de Kanban

========================================
           MENU PRINCIPAL
========================================
1 📝 Criar um novo board
2 📋 Selecionar um board existente  
3 🗑️  Excluir um board
4 📊 Listar todos os boards
5 📈 Relatórios
6 ⚡ Métricas de performance
7 🚪 Sair
========================================
```

### Criando um Board
1. Escolha a opção 1
2. Digite o nome do board
3. Defina quantas colunas adicionais deseja
4. Configure os nomes das colunas

### Gerenciando Cards
Dentro de um board você pode:
- Criar novos cards
- Mover cards entre colunas
- Bloquear/desbloquear cards
- Visualizar informações detalhadas

### Relatórios Disponíveis
- **Produtividade**: Mostra distribuição de cards por coluna
- **Cards Antigos**: Identifica cards que precisam atenção
- **Métricas**: Performance do sistema em tempo real

## 🧪 Testes

### Executar Todos os Testes
```bash
./gradlew test
```

### Executar Testes Específicos
```bash
./gradlew test --tests EntityValidatorTest
./gradlew test --tests MetricsCollectorTest
```

### Relatório de Cobertura
```bash
./gradlew jacocoTestReport
```

## 📊 Arquitetura e Padrões

### Padrões Implementados

#### 1. **Repository Pattern**
- `BoardDAO`, `CardDAO` para acesso a dados
- Separação clara entre lógica de negócio e persistência

#### 2. **Service Layer**
- `BoardService`, `CardService` para regras de negócio
- `ReportService` para geração de relatórios

#### 3. **Builder Pattern**
- `CardSearchCriteria.builder()` para busca avançada
- Construção fluente de objetos complexos

#### 4. **Singleton Pattern**
- `ApplicationConfig` para configurações
- `MetricsCollector` para coleta de métricas

#### 5. **Strategy Pattern**
- `MigrationStrategy` para evolução do banco
- Flexibilidade para diferentes estratégias

### Princípios SOLID

#### Single Responsibility Principle (SRP)
- Cada classe tem uma responsabilidade específica
- `EntityValidator` apenas para validações
- `MetricsCollector` apenas para métricas

#### Open/Closed Principle (OCP)
- Extensível via interfaces e abstrações
- Novos tipos de relatórios podem ser adicionados facilmente

#### Liskov Substitution Principle (LSP)
- Implementações podem ser substituídas sem quebrar o sistema
- DAOs implementam contratos bem definidos

#### Interface Segregation Principle (ISP)
- Interfaces específicas para cada necessidade
- Clientes não dependem de métodos que não usam

#### Dependency Inversion Principle (DIP)
- Dependências injetadas via construtor
- Abstrações não dependem de implementações concretas

## 🔍 Monitoramento e Logs

### Sistema de Logs
- **Console**: Logs informativos durante execução
- **Arquivo**: `logs/board-app.log` para histórico
- **Erros**: `logs/board-errors.log` para troubleshooting

### Métricas de Performance
- Tempo de execução de operações
- Identificação de operações lentas
- Estatísticas detalhadas por operação

### Configuração de Logs
```xml
<!-- Níveis disponíveis: TRACE, DEBUG, INFO, WARN, ERROR -->
<root level="INFO">
    <appender-ref ref="CONSOLE"/>
    <appender-ref ref="FILE"/>
</root>
```

## 🔒 Segurança e Validação

### Validações Implementadas
- **Entrada de dados**: Validação de tipos e formatos
- **Regras de negócio**: Validação de estados válidos
- **Integridade**: Verificação de relacionamentos

### Tratamento de Erros
- Exceções específicas para cada tipo de erro
- Mensagens claras para o usuário
- Logs detalhados para desenvolvedores

## 📈 Performance

### Otimizações Implementadas
- **Conexões de banco**: Gerenciamento eficiente
- **Queries**: Otimizadas para performance
- **Memória**: Controle de uso de recursos
- **Logs**: Configuráveis por nível

### Métricas Coletadas
- Tempo de execução de operações críticas
- Uso de recursos do sistema
- Identificação de gargalos

## 🤝 Contribuindo

### Como Contribuir
1. Fork o projeto
2. Crie uma branch para sua feature
3. Implemente seguindo os padrões do projeto
4. Adicione testes para nova funcionalidade
5. Execute todos os testes
6. Submeta um Pull Request

### Padrões de Código
- Use Lombok para reduzir boilerplate
- Adicione logs apropriados
- Implemente validações
- Escreva testes unitários
- Documente métodos públicos

## 📋 Roadmap

### 🔄 Próximas Funcionalidades
- [ ] API REST para integração
- [ ] Interface web com Spring Boot
- [ ] Notificações em tempo real
- [ ] Integração com sistemas externos
- [ ] Dashboard avançado
- [ ] Autenticação e autorização
- [ ] Backup automático
- [ ] Exportação de relatórios (PDF/Excel)

### 🏗️ Melhorias Técnicas
- [ ] Cache de consultas frequentes
- [ ] Pool de conexões avançado
- [ ] Suporte a múltiplos bancos
- [ ] Containerização com Docker
- [ ] CI/CD pipeline
- [ ] Documentação da API
- [ ] Testes de integração
- [ ] Testes de performance

## 📄 Licença

Este projeto está licenciado sob a MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👨‍💻 Autor

**Rychardsson**
- GitHub: [@Rychardsson](https://github.com/Rychardsson)
- LinkedIn: [Rychardsson](https://linkedin.com/in/rychardsson)

## 🙏 Agradecimentos

- DIO (Digital Innovation One) pela inspiração do projeto
- Comunidade Java pelas melhores práticas
- Contribuidores que ajudaram a melhorar o projeto

---

⭐ **Se este projeto foi útil para você, considere dar uma estrela!** ⭐
