# Como gerar o json a partir de uma ou mais classes

Baixe o projeto do repositório git:  
🔗 https://github.com/DSSID-DEV/generator-json.git

---

## 1. Instale o projeto localmente

Rode o comando:

```bash
mvn clean install
```

---

## 2. Utilizando a dependência no seu projeto

Abra o `pom.xml` do projeto onde deseja usar o gerador de JSON e adicione:

```xml
<dependency>
	<groupId>com.dssid.utilities</groupId> 
	<artifactId>generator-json</artifactId> 
	<version>1.0.0</version> 
</dependency>
```

---

## 3. Utilizando em uma máquina remota sem acesso ao GitHub

### Passos:

1. Baixe o projeto na sua máquina local
2. Rode o comando:

```bash
mvn clean install
```

3. Vá até o repositório `.m2` da sua máquina local
4. Verifique o caminho do repositório da máquina remota:

---

### Estrutura esperada:

```
.m2/
└── repository/
    └── com/
        └── dssid/
            └── dev/
                └── generator-json/
                    └── 1.0.0/
```

---

### O que fazer:

- Se **existir a pasta `com`** na máquina remota:
    - Copie apenas a **pasta `dssid`** do seu `.m2/repository/com` local e cole dentro do `com` remoto.

- Se **não existir a pasta `com`**:
    - Copie toda a **pasta `com`** do seu repositório local e cole dentro do `repository` remoto.

---

## 4. Atualize o `pom.xml` do projeto onde deseja gerar os arquivos JSON

---

## 5. Crie uma classe para gerar os arquivos JSON

Você pode criar essa classe dentro de um novo pacote se quiser organizar melhor.

### Exemplo de classe:

```java

import java.util.List;

@Slf4j
public class Utils {

  //Defina um Path onde será criado os arquivos
  private static final String PATH = "src/main/resources/swagger.examples";
  private static final List<Class<?>> CLASSES =
          List.of(ResponsePayment.class, CashInDTO.class, BankCashInDTO.class);

  public static void main(String[] args) throws IOException {

    var geradorJson = new GeneratorJson();
    boolean canRun = geradorJson
            .checkValidityOfClass(List
                    .of(CLASSES));
    if (canRun) geradorJson.now(PATH);
    else log.info("No action was taken, you exited the operation!");
  }
}
```
