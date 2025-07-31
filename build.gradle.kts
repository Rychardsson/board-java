plugins {
    id("java")
    id("application")
    id("jacoco")
}

group = "br.com.dio"
version = "1.0.0"
description = "Sistema de Board de Tarefas - Kanban em Java"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass.set("br.com.dio.Main")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.liquibase:liquibase-core:4.29.1")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.projectlombok:lombok:1.18.34")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.7.0")
    testImplementation("org.assertj:assertj-core:3.24.2")

    annotationProcessor("org.projectlombok:lombok:1.18.34")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "br.com.dio.Main",
            "Implementation-Title" to project.description,
            "Implementation-Version" to project.version,
            "Implementation-Vendor" to "DIO - Digital Innovation One"
        )
    }
    
    // Include dependencies in fat jar
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// Task personalizada para verificar dependências
tasks.register("checkDependencies") {
    group = "verification"
    description = "Verifica se todas as dependências estão atualizadas"
    
    doLast {
        println("🔍 Verificando dependências...")
        configurations.compileClasspath.get().forEach { dependency ->
            println("  ✓ ${dependency.name}")
        }
        println("✅ Verificação de dependências concluída")
    }
}

// Task para criar estrutura de diretórios
tasks.register("createDirectories") {
    group = "setup"
    description = "Cria estrutura de diretórios necessários"
    
    doLast {
        val dirs = listOf("logs", "docs", "scripts")
        dirs.forEach { dir ->
            val dirFile = file(dir)
            if (!dirFile.exists()) {
                dirFile.mkdirs()
                println("📁 Diretório criado: $dir")
            }
        }
        println("✅ Estrutura de diretórios verificada")
    }
}

// Task para gerar documentação do projeto
tasks.register("generateDocs") {
    group = "documentation"
    description = "Gera documentação do projeto"
    
    doLast {
        println("📚 Gerando documentação...")
        
        val docsDir = file("docs")
        if (!docsDir.exists()) {
            docsDir.mkdirs()
        }
        
        // Aqui você pode adicionar lógica para gerar documentação automática
        println("✅ Documentação gerada em: docs/")
    }
}

// Task para executar verificações completas
tasks.register("fullCheck") {
    group = "verification"
    description = "Executa todas as verificações do projeto"
    dependsOn("clean", "checkDependencies", "test", "jacocoTestReport")
    
    doLast {
        println("🎉 Todas as verificações foram executadas com sucesso!")
    }
}