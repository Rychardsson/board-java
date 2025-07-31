#!/bin/bash

# Script de build e deploy para o Board de Tarefas
# Autor: Rychardsson
# Versão: 1.0.0

set -e

echo "🚀 Iniciando build do Board de Tarefas..."

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Função para imprimir mensagens coloridas
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Verifica se Java está instalado
check_java() {
    print_status "Verificando instalação do Java..."
    if ! command -v java &> /dev/null; then
        print_error "Java não encontrado. Instale Java 17 ou superior."
        exit 1
    fi
    
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    print_success "Java encontrado: $JAVA_VERSION"
}

# Verifica se MySQL está rodando
check_mysql() {
    print_status "Verificando conexão com MySQL..."
    if ! command -v mysql &> /dev/null; then
        print_warning "MySQL client não encontrado. Verifique se o MySQL está instalado."
        return
    fi
    
    if mysql -u board -p'board' -e "SELECT 1" &> /dev/null; then
        print_success "Conexão com MySQL estabelecida"
    else
        print_warning "Não foi possível conectar ao MySQL. Verifique as credenciais."
    fi
}

# Limpa builds anteriores
clean_build() {
    print_status "Limpando builds anteriores..."
    ./gradlew clean
    print_success "Build limpo"
}

# Executa testes
run_tests() {
    print_status "Executando testes unitários..."
    ./gradlew test
    print_success "Testes executados com sucesso"
}

# Compila o projeto
compile_project() {
    print_status "Compilando projeto..."
    ./gradlew build -x test
    print_success "Projeto compilado com sucesso"
}

# Gera relatórios
generate_reports() {
    print_status "Gerando relatórios..."
    
    # Relatório de testes
    if [ -f "build/reports/tests/test/index.html" ]; then
        print_success "Relatório de testes: build/reports/tests/test/index.html"
    fi
    
    # Relatório de cobertura (se disponível)
    if [ -f "build/reports/jacoco/test/html/index.html" ]; then
        print_success "Relatório de cobertura: build/reports/jacoco/test/html/index.html"
    fi
}

# Cria diretório de logs
create_log_dir() {
    print_status "Criando diretório de logs..."
    mkdir -p logs
    print_success "Diretório de logs criado"
}

# Verifica arquivos de configuração
check_config() {
    print_status "Verificando arquivos de configuração..."
    
    if [ ! -f "src/main/resources/application.properties" ]; then
        print_warning "Arquivo application.properties não encontrado"
    else
        print_success "Arquivo de configuração encontrado"
    fi
    
    if [ ! -f "src/main/resources/logback.xml" ]; then
        print_warning "Arquivo logback.xml não encontrado"
    else
        print_success "Configuração de logs encontrada"
    fi
}

# Função principal
main() {
    echo "📋 Board de Tarefas - Script de Build"
    echo "====================================="
    
    check_java
    check_mysql
    check_config
    clean_build
    compile_project
    run_tests
    generate_reports
    create_log_dir
    
    echo ""
    print_success "🎉 Build concluído com sucesso!"
    echo ""
    echo "📖 Para executar a aplicação:"
    echo "   ./gradlew run"
    echo ""
    echo "📊 Para ver os relatórios:"
    echo "   Testes: file://$(pwd)/build/reports/tests/test/index.html"
    echo ""
    echo "📝 Logs da aplicação serão salvos em:"
    echo "   logs/board-app.log"
    echo "   logs/board-errors.log"
    echo ""
}

# Executa o script principal
main "$@"
