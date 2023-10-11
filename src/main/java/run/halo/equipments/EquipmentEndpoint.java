package run.halo.equipments;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;

import lombok.AllArgsConstructor;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.core.extension.endpoint.CustomEndpoint;
import run.halo.app.extension.GroupVersion;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.equipments.service.EquipmentService;

/**
 * A custom endpoint for {@link Equipment}.
 *
 * @author LIlGG
 * @since 1.0.0
 */
@Component
@AllArgsConstructor
public class EquipmentEndpoint implements CustomEndpoint {
    
    private final EquipmentService equipmentService;
    
    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.plugin.halo.run/v1alpha1/Equipment";
        return SpringdocRouteBuilder.route().GET("plugins/PluginEquipments/equipments",
            this::listEquipment, builder -> {
                builder.operationId("ListEquipments")
                    .description("List equipments.")
                    .tag(tag)
                    .response(responseBuilder().implementation(
                        ListResult.generateGenericClass(Equipment.class)));
                QueryParamBuildUtil.buildParametersFromType(builder,
                    EquipmentQuery.class
                );
            }
        ).build();
    }
    
    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.plugin.halo.run/v1alpha1");
    }
    
    private Mono<ServerResponse> listEquipment(ServerRequest serverRequest) {
        EquipmentQuery query = new EquipmentQuery(serverRequest.queryParams());
        return equipmentService.listEquipment(query).flatMap(
            equipments -> ServerResponse.ok().bodyValue(equipments));
    }
    
}
