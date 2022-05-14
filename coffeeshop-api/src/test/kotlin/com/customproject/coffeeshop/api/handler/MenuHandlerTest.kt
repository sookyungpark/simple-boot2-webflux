package com.customproject.coffeeshop.api.handler

import com.customproject.coffeeshop.domain.Cost
import com.customproject.coffeeshop.domain.Currency
import com.customproject.coffeeshop.domain.response.MenuGetResponse
import com.customproject.coffeeshop.domain.response.MenuListResponse
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.given
import org.apache.http.HttpHeaders
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.request.RequestDocumentation
import org.springframework.restdocs.snippet.Attributes
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


class MenuHandlerTest: BaseHandlerTest() {

    @Autowired
    private val menuHandler: MenuHandler? = null

    @Test
    fun list() {
        // given
        val response1 = MenuListResponse(id = "menuId")
        val expectedBody = arrayOf(response1)

        val response = ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Flux.fromArray(expectedBody), MenuListResponse::class.java)

        // mock
        given(this.menuHandler!!.list(any())).willReturn(response)

        // validate
        webTestClient!!.get()
                .uri("/api/menus")
                .header(HttpHeaders.ACCEPT, "application/json")
                .exchange()
                .expectStatus().isOk
                .expectBody().json(objectMapper.writeValueAsString(expectedBody))
                .consumeWith(
                        WebTestClientRestDocumentation.document(
                                "list-menus",
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                RequestDocumentation.requestParameters(
                                        RequestDocumentation.parameterWithName("limit")
                                                .description("limit count")
                                                .attributes(Attributes.key("required").value(false))
                                                .attributes(Attributes.key("range").value(MenuHandler.LIST_COUNT_RANGE))
                                                .attributes(Attributes.key("defaultValue").value(MenuHandler.LIST_COUNT_RANGE.maximum))
                                                .optional()
                                ),
                                PayloadDocumentation.responseFields(
                                        PayloadDocumentation.fieldWithPath("[].id").description("menu ID")
                                )
                        )
                )
    }

    @Test
    fun get() {
        // given
        val menuId = "menu-id"
        val expectedBody = MenuGetResponse(
                id = menuId,
                name = "menu name",
                cost = Cost(currency = Currency.USD, value = 1.0f))

        val response = ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(expectedBody), MenuGetResponse::class.java)

        // mock
        given(this.menuHandler!!.get(any())).willReturn(response)

        // validate
        webTestClient!!.get().uri("/api/menus/$menuId")
                .headers { headers ->
                    headers.set(HttpHeaders.ACCEPT, "application/json")
                }
                .exchange()
                .expectStatus().isOk
                .expectBody().json(objectMapper.writeValueAsString(expectedBody))
                .consumeWith(
                        WebTestClientRestDocumentation.document(
                                "get-menu",
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                PayloadDocumentation.responseFields(
                                        PayloadDocumentation.fieldWithPath("id").description("id").optional(),
                                        PayloadDocumentation.fieldWithPath("name").description("name").optional(),
                                        PayloadDocumentation.fieldWithPath("cost").description("cost").optional(),
                                        PayloadDocumentation.fieldWithPath("cost.currency").description("cost currency").optional(),
                                        PayloadDocumentation.fieldWithPath("cost.value").description("cost value").optional()
                                )
                        )
                )
    }
}
