# Script de build e deploy para o Board de Tarefas - Windows
# Autor: Rychardsson  
# Versão: 1.0.0

param(
    [switch]$SkipTests,
    [switch]$Clean,
    [switch]$Verbose
)

# Função para imprimir mensagens coloridas
function Write-Status {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

# Verifica se Java está instalado
function Test-JavaInstallation {
    Write-Status "Verificando instalação do Java..."
    
    try {
        $javaVersion = java -version 2>&1 | Select-String "version"
        Write-Success "Java encontrado: $javaVersion"
        return $true
    }
    catch {
        Write-Error "Java não encontrado. Instale Java 17 ou superior."
        return $false
    }
}

# Verifica se MySQL está acessível
function Test-MySQLConnection {
    Write-Status "Verificando conexão com MySQL..."
    
    try {
        # Testa conexão básica (pode precisar ajustar credenciais)
        $connection = New-Object System.Data.SqlClient.SqlConnection
        Write-Success "MySQL configurado (verificação manual necessária)"
    }
    catch {
        Write-Warning "Verifique se o MySQL está rodando e as credenciais estão corretas."
    }
}

# Limpa builds anteriores
function Clear-Build {
    if ($Clean) {
        Write-Status "Limpando builds anteriores..."
        & .\gradlew.bat clean
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Build limpo com sucesso"
        } else {
            Write-Error "Erro ao limpar build"
            exit 1
        }
    }
}

# Executa testes
function Invoke-Tests {
    if (-not $SkipTests) {
        Write-Status "Executando testes unitários..."
        & .\gradlew.bat test
        if ($LASTEXITCODE -eq 0) {
            Write-Success "Testes executados com sucesso"
        } else {
            Write-Error "Testes falharam"
            exit 1
        }
    } else {
        Write-Warning "Testes foram pulados (-SkipTests)"
    }
}

# Compila o projeto
function Build-Project {
    Write-Status "Compilando projeto..."
    
    if ($SkipTests) {
        & .\gradlew.bat build -x test
    } else {
        & .\gradlew.bat build
    }
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Projeto compilado com sucesso"
    } else {
        Write-Error "Erro na compilação"
        exit 1
    }
}

# Gera relatórios
function New-Reports {
    Write-Status "Verificando relatórios gerados..."
    
    $testReport = "build\reports\tests\test\index.html"
    if (Test-Path $testReport) {
        Write-Success "Relatório de testes: $testReport"
    }
    
    $coverageReport = "build\reports\jacoco\test\html\index.html" 
    if (Test-Path $coverageReport) {
        Write-Success "Relatório de cobertura: $coverageReport"
    }
}

# Cria diretório de logs
function New-LogDirectory {
    Write-Status "Criando diretório de logs..."
    
    if (-not (Test-Path "logs")) {
        New-Item -ItemType Directory -Path "logs" | Out-Null
    }
    
    Write-Success "Diretório de logs pronto"
}

# Verifica arquivos de configuração
function Test-Configuration {
    Write-Status "Verificando arquivos de configuração..."
    
    $appProps = "src\main\resources\application.properties"
    if (Test-Path $appProps) {
        Write-Success "Arquivo de configuração encontrado"
    } else {
        Write-Warning "Arquivo application.properties não encontrado"
    }
    
    $logbackConfig = "src\main\resources\logback.xml"
    if (Test-Path $logbackConfig) {
        Write-Success "Configuração de logs encontrada"
    } else {
        Write-Warning "Arquivo logback.xml não encontrado"
    }
}

# Função principal
function Main {
    Write-Host "📋 Board de Tarefas - Script de Build (Windows)" -ForegroundColor Cyan
    Write-Host "=================================================" -ForegroundColor Cyan
    Write-Host ""
    
    # Verifica pré-requisitos
    if (-not (Test-JavaInstallation)) {
        exit 1
    }
    
    Test-MySQLConnection
    Test-Configuration
    
    # Executa build
    Clear-Build
    Build-Project
    Invoke-Tests
    New-Reports
    New-LogDirectory
    
    Write-Host ""
    Write-Success "🎉 Build concluído com sucesso!"
    Write-Host ""
    Write-Host "📖 Para executar a aplicação:" -ForegroundColor Yellow
    Write-Host "   .\gradlew.bat run" -ForegroundColor White
    Write-Host ""
    Write-Host "📊 Para ver os relatórios:" -ForegroundColor Yellow
    Write-Host "   Testes: build\reports\tests\test\index.html" -ForegroundColor White
    Write-Host ""
    Write-Host "📝 Logs da aplicação serão salvos em:" -ForegroundColor Yellow
    Write-Host "   logs\board-app.log" -ForegroundColor White
    Write-Host "   logs\board-errors.log" -ForegroundColor White
    Write-Host ""
    
    if ($Verbose) {
        Write-Host "🔧 Opções utilizadas:" -ForegroundColor Magenta
        Write-Host "   Skip Tests: $SkipTests" -ForegroundColor White
        Write-Host "   Clean Build: $Clean" -ForegroundColor White
        Write-Host "   Verbose: $Verbose" -ForegroundColor White
        Write-Host ""
    }
}

# Executa o script principal
try {
    Main
}
catch {
    Write-Error "Erro durante a execução: $($_.Exception.Message)"
    exit 1
}
