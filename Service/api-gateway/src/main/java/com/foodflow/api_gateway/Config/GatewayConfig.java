package com.foodflow.api_gateway.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
public class GatewayConfig {

    @Bean
    public RouterFunction<ServerResponse> gatewayRoutes() {

        return route("user-service")
                .route(path("/api/v1/users/**"), http())
                .filter(lb("USER-SERVICE"))
                .build()

                .and(
                        route("auth-service")
                                .route(path("/api/v1/auth/**"), http())
                                .filter(lb("USER-SERVICE"))
                                .build()
                )

                .and(
                        route("restaurant-service")
                                .route(path("/api/v1/restaurants/**"), http())
                                .filter(lb("RESTAURANT-SERVICE"))
                                .build()
                )

                .and(
                        route("menu-service")
                                .route(path("/api/v1/menu-items/**"), http())
                                .filter(lb("RESTAURANT-SERVICE"))
                                .build()
                )

                .and(
                        route("order-service")
                                .route(path("/api/v1/order/**"), http())
                                .filter(lb("ORDER-SERVICE"))
                                .build()
                )

                .and(
                        route("payment-service")
                                .route(path("/api/v1/payments/**"), http())
                                .filter(lb("PAYMENT-SERVICE"))
                                .build()
                )

                .and(
                        route("delivery-service")
                                .route(path("/api/v1/deliveries/**"), http())
                                .filter(lb("DELIVERY-SERVICE"))
                                .build()
                )
                .and(
                        route("delivery-service")
                                .route(path("/api/v1/delivery-partners/**"), http())
                                .filter(lb("DELIVERY-SERVICE"))
                                .build()
                )

                .and(
                        route("inventory-service")
                                .route(path("/api/v1/inventory/**"), http())
                                .filter(lb("INVENTORY-SERVICE"))
                                .build()
                )

                .and(
                        route("notification-service")
                                .route(path("/api/v1/notification/**"), http())
                                .filter(lb("NOTIFICATION-SERVICE"))
                                .build()
                )

                .and(
                        route("review-service")
                                .route(path("/api/v1/review/**"), http())
                                .filter(lb("REVIEW-SERVICE"))
                                .build()
                );
    }
}