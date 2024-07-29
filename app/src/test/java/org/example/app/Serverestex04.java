package org.example.app;

import com.github.javafaker.Faker;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class Serverestex04 {
    @BeforeAll
    public static void preCondition(){
        baseURI = "http://localhost";
        port = 3000;
    }

    @Test
    public void ex04(){
        Faker faker = new Faker();
        String userName = faker.name().fullName();
        String userEmail = userName.replace(" ", "").toLowerCase() + "@restassuredeta20232.com.br"; // Remover espaços do e-mail
        String productName = faker.book().title();
        String descProdName = faker.book().author();

        Integer prodQtyTotal = 100;
        Integer prodUnitPrice = 100;
        Integer prodCartQty = 10;

        // POST/usuarios - Criando usuário. Início da execução da Latra A.
        String userID = given()
                .body("{\n" +
                        "  \"nome\": \"" + userName + "\",\n" +
                        "  \"email\": \"" + userEmail + "\",\n" +
                        "  \"password\": \"teste\",\n" +
                        "  \"administrador\": \"true\"\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/usuarios")
        .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .extract().path("_id");

        // GET/usuarios/{_id} - Verificação do usuário criado
        given()
                .pathParam("_id", userID)
        .when()
                .get("/usuarios/{_id}")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("nome", is(userName))
                .body("email", is(userEmail));

        // POST/login - Fazendo Login
        String adminToken = given()
                .body("{\n" +
                        "  \"email\": \""+ userEmail + "\",\n" +
                        "  \"password\": \"teste\"\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/login")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Login realizado com sucesso"))
                .extract().path("authorization");

        // POST/produto - Criando produto
        String productID = given()
                .header("authorization", adminToken)
                .body("{\n" +
                        "  \"nome\": \""+ productName + "\",\n" +
                        "  \"preco\": " + prodUnitPrice + ",\n" +
                        "  \"descricao\": \""+descProdName+"\",\n" +
                        "  \"quantidade\": " + prodQtyTotal + "\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/produtos")
        .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .extract().path("_id");

        // GET/produtos/{_id} - Verificação do produto criado
        given()
                .pathParam("_id", productID)
        .when()
                .get("/produtos/{_id}")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("nome", is(productName))
                .body("preco", is(prodUnitPrice))
                .body("descricao", is(descProdName))
                .body("quantidade", is(prodQtyTotal));

        // POST/carrinhos - Criando carrinho
        String cartID = given()
                .header("authorization", adminToken)
                .body("{\n" +
                        "  \"produtos\": [\n" +
                        "    {\n" +
                        "      \"idProduto\": \"" + productID + "\",\n" +
                        "      \"quantidade\": " + prodCartQty + "\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}")
                .contentType(ContentType.JSON)
        .when()
                .post("/carrinhos")
        .then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", is("Cadastro realizado com sucesso"))
                .extract().path("_id");

        // GET/carrinhos/{_id} - Verificação do carrinho criado
        given()
                .pathParam("_id", cartID)
        .when()
                .get("/carrinhos/{_id}")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("_id", is(cartID))
                .body("produtos[0].idProduto", is(productID))
                .body("produtos[0].quantidade", is(prodCartQty))
                .body("precoTotal", is(prodCartQty * prodUnitPrice))
                .body("quantidadeTotal", is(prodCartQty))
                .body("idUsuario", is(userID));

        // DELETE/usuarios/{_id} - Término da Letra A. Tentar excluir um usuário com carrinho vinculado a ele.
        given()
                .pathParam("_id", userID)
        .when()
                .delete("/usuarios/{_id}")
        .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", is("Não é permitido excluir usuário com carrinho cadastrado"));

        // DELETE/carrinhos/concluir-compra - Início da Letra B: Excluir o Usuário cadastrado Após Remover Carrinho de Compra Concluída
        given()
                .header("authorization", adminToken)
        .when()
                .delete("/carrinhos/concluir-compra")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso"));

        // GET/carrinhos/{_id} - Verificação do carrinho após exclusão
        given()
                .pathParam("_id", cartID)
        .when()
                .get("/carrinhos/{_id}")
        .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", is("Carrinho não encontrado"));

        // DELETE/produtos/{_id} - Deletando o produto para reutilizar chamada de POST produto.
        given()
                .header("authorization", adminToken)
                .pathParam("_id", productID)
        .when()
                .delete("/produtos/{_id}")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso"));

        // GET/produtos/{_id} - Verificação do produto após exclusão
        given()
                .pathParam("_id", productID)
        .when()
                .get("/produtos/{_id}")
        .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", is("Produto não encontrado"));

        // DELETE/usuarios/{_id} - Deletando o usuário para reutilizar chamada de POST usuários.
        given()
                .header("authorization", adminToken)
                .pathParam("_id", userID)
        .when()
                .delete("/usuarios/{_id}")
        .then()
                .statusCode(HttpStatus.SC_OK)
                .body("message", is("Registro excluído com sucesso"));

        // GET/usuarios/{_id} - Verificação do usuário após exclusão
        given()
                .pathParam("_id", userID)
        .when()
                .get("/usuarios/{_id}")
        .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body("message", is("Usuário não encontrado"));
    }
}
