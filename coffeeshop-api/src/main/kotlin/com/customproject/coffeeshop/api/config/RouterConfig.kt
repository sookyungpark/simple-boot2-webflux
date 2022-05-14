package com.customproject.coffeeshop.api.config

import com.customproject.coffeeshop.api.filter.InternalAuthCheckFilter
import com.customproject.coffeeshop.api.filter.UserAuthCheckFilter
import com.customproject.coffeeshop.api.handler.*
import com.customproject.coffeeshop.api.handler.internal.InternalOrderHandler
import com.customproject.coffeeshop.api.handler.internal.InternalUserHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunctions.resources
import org.springframework.web.reactive.function.server.router

@Configuration
public class RouterConfig(private val warmupHandler: WarmupHandler,
                          private val managementHandler: ManagementHandler,
                          private val orderHandler: OrderHandler,
                          private val userHandler: UserHandler,
                          private val menuHandler: MenuHandler,
                          private val internalUserHandler: InternalUserHandler,
                          private val internalOrderHandler: InternalOrderHandler,
                          private val userAuthCheckFilter: UserAuthCheckFilter,
                          private val internalAuthCheckFilter: InternalAuthCheckFilter) {

    @Bean
    @Order(100)
    fun faviconRouter() = resources("/favicon.ico**", ClassPathResource("/static/favicon.ico"))

    @Bean
    @Order(100)
    fun resourcesRouter() = resources("/docs/**", ClassPathResource("/public/docs/"))

    @Bean
    @Order(100)
    fun commonRouter() = router {
        GET("/warmup", warmupHandler::warmup)
        GET("/management/prometheus", managementHandler::scrape)
    }

    @Bean
    @Order(1)
    fun apiRouter() = router {
        ("/api").nest {
            ("/menus").nest {
                GET(accept(MediaType.APPLICATION_JSON) and "", menuHandler::list)
                GET(accept(MediaType.APPLICATION_JSON) and "/{id}", menuHandler::get)
                POST(contentType(MediaType.APPLICATION_JSON) and "/search", menuHandler::search)
            }
        }
    }

    @Bean
    @Order(5)
    fun apiWithAuthRouter() = router {
        ("/api").nest {
            ("/orders").nest {
                POST(contentType(MediaType.APPLICATION_JSON) and "", orderHandler::create)
                PUT(contentType(MediaType.APPLICATION_JSON) and "/{id}", orderHandler::update)
            }
            ("/users").nest {
                GET(accept(MediaType.APPLICATION_JSON) and "/profile", userHandler::getProfile)
            }
        }
    }
    // uncomment on use
    //.filter(userAuthCheckFilter)

    @Bean
    @Order(10)
    fun internalApiRouter() = router {
        ("/api/internal").nest {
            ("/orders").nest {
                GET(accept(MediaType.APPLICATION_JSON) and "", internalOrderHandler::list)
                GET(accept(MediaType.APPLICATION_JSON) and "/{id}", internalOrderHandler::get)
            }
            ("/users").nest {
                GET(accept(MediaType.APPLICATION_JSON) and "", internalUserHandler::list)
            }

        }
    }
    // uncomment on use
    //.filter(internalAuthCheckFilter)
}
