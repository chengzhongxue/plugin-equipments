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
import run.halo.app.extension.router.IListRequest.QueryListRequest;
import run.halo.app.extension.router.QueryParamBuildUtil;
import run.halo.equipments.service.EquipmentGroupService;

/**
 * A custom endpoint for {@link Equipment}.
 *
 * @author LIlGG
 * @since 1.0.0
 */
@Component
@AllArgsConstructor
public class EquipmentGroupEndpoint implements CustomEndpoint {
    
    private final EquipmentGroupService equipmentGroupService;
    
    @Override
    public RouterFunction<ServerResponse> endpoint() {
        final var tag = "api.plugin.halo.run/v1alpha1/EquipmentGroup";
        return SpringdocRouteBuilder.route().GET(
            "plugins/PluginEquipments/equipmentgroups", this::listEquipmentGroup,
            builder -> {
                builder.operationId("ListEquipmentGroups").description(
                    "List equipmentGroups.").tag(tag).response(
                    responseBuilder().implementation(
                        ListResult.generateGenericClass(EquipmentGroup.class)));
                QueryParamBuildUtil.buildParametersFromType(builder,
                    QueryListRequest.class
                );
            }
        ).DELETE("plugins/PluginEquipments/equipmentgroups/{name}",
            this::deleteEquipmentGroup, builder -> builder.operationId(
                    "DeleteEquipmentGroup")
                .description("Delete equipmentGroup.")
                .tag(tag)
                .response(responseBuilder().implementation(
                    ListResult.generateGenericClass(EquipmentGroup.class)))
        ).build();
    }
    
    @Override
    public GroupVersion groupVersion() {
        return GroupVersion.parseAPIVersion("api.plugin.halo.run/v1alpha1");
    }
    
    private Mono<ServerResponse> deleteEquipmentGroup(ServerRequest serverRequest) {
        String name = serverRequest.pathVariable("name");
        return equipmentGroupService.deleteEquipmentGroup(name).flatMap(
            equipmentGroup -> ServerResponse.ok().bodyValue(equipmentGroup));
    }
    
    private Mono<ServerResponse> listEquipmentGroup(ServerRequest serverRequest) {
        QueryListRequest request = new EquipmentQuery(serverRequest.queryParams());
        return equipmentGroupService.listEquipmentGroup(request).flatMap(
            equipmentGroups -> ServerResponse.ok().bodyValue(equipmentGroups));
    }
    
}
