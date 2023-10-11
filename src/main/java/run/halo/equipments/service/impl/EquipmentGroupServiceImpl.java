package run.halo.equipments.service.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.extension.router.IListRequest.QueryListRequest;
import run.halo.equipments.Equipment;
import run.halo.equipments.EquipmentGroup;
import run.halo.equipments.service.EquipmentGroupService;

/**
 * Service implementation for {@link Equipment}.
 *
 * @author LIlGG
 * @since 1.0.0
 */
@Component
public class EquipmentGroupServiceImpl implements EquipmentGroupService {
    
    private final ReactiveExtensionClient client;
    
    public EquipmentGroupServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }
    
    @SuppressWarnings("checkstyle:Indentation")
    @Override
    public Mono<ListResult<EquipmentGroup>> listEquipmentGroup(QueryListRequest query) {
        return this.client.list(EquipmentGroup.class, equipmentListPredicate(query),
            null, query.getPage(), query.getSize()
        ).flatMap(listResult -> Flux.fromStream(
                listResult.get().map(this::populateEquipments))
            .concatMap(Function.identity())
            .collectList()
            .map(groups -> new ListResult<>(listResult.getPage(),
                listResult.getSize(), listResult.getTotal(), groups
            )));
    }
    
    @Override
    public Mono<EquipmentGroup> deleteEquipmentGroup(String name) {
        return this.client.fetch(EquipmentGroup.class, name).flatMap(
            equipmentGroup -> this.client.delete(equipmentGroup)
                .flatMap(deleted -> this.client.list(Equipment.class,
                    (equipment) -> StringUtils.equals(name,
                        equipment.getSpec().getGroupName()
                    ), null
                ).flatMap(this.client::delete).then(Mono.just(deleted))));
    }
    
    private Mono<EquipmentGroup> populateEquipments(EquipmentGroup equipmentGroup) {
        return Mono.just(equipmentGroup).flatMap(fg -> fetchEquipmentCount(fg).doOnNext(
                count -> fg.getStatusOrDefault().setEquipmentCount(count))
            .thenReturn(fg));
    }
    
    @SuppressWarnings("checkstyle:OperatorWrap")
    Mono<Integer> fetchEquipmentCount(EquipmentGroup equipmentGroup) {
        Assert.notNull(equipmentGroup, "The equipmentGroup must not be null.");
        String name = equipmentGroup.getMetadata().getName();
        return client.list(Equipment.class, equipment -> !equipment.isDeleted()
                && equipment.getSpec().getGroupName().equals(name), null)
            .count()
            .defaultIfEmpty(0L)
            .map(Long::intValue);
    }
    
    Predicate<EquipmentGroup> equipmentListPredicate(QueryListRequest query) {
        return labelAndFieldSelectorToPredicate(query.getLabelSelector(),
            query.getFieldSelector()
        );
    }
}
