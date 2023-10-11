package run.halo.equipments.finders.impl;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.comparator.Comparators;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.app.theme.finders.Finder;
import run.halo.equipments.Equipment;
import run.halo.equipments.EquipmentGroup;
import run.halo.equipments.finders.EquipmentFinder;
import run.halo.equipments.vo.EquipmentGroupVo;
import run.halo.equipments.vo.EquipmentVo;

/**
 * @author LIlGG
 */
@Finder("equipmentFinder")
public class EquipmentFInderImpl implements EquipmentFinder {
    private final ReactiveExtensionClient client;
    
    public EquipmentFInderImpl(ReactiveExtensionClient client) {
        this.client = client;
    }
    
    @Override
    public Flux<EquipmentVo> listAll() {
        return this.client.list(Equipment.class, null, defaultEquipmentComparator())
            .flatMap(equipment -> Mono.just(EquipmentVo.from(equipment)));
    }
    
    @Override
    public Mono<ListResult<EquipmentVo>> list(Integer page, Integer size) {
        return list(page, size, null);
    }
    
    @Override
    public Mono<ListResult<EquipmentVo>> list(Integer page, Integer size,
        String group) {
        return pageEquipment(page, size, group, null, defaultEquipmentComparator());
    }
    
    private Mono<ListResult<EquipmentVo>> pageEquipment(Integer page, Integer size,
        String group, Predicate<Equipment> equipmentPredicate,
        Comparator<Equipment> comparator) {
        Predicate<Equipment> predicate = equipmentPredicate == null ? equipment -> true
            : equipmentPredicate;
        if (StringUtils.isNotEmpty(group)) {
            predicate = predicate.and(equipment -> {
                String groupName = equipment.getSpec().getGroupName();
                return StringUtils.equals(groupName, group);
            });
        }
        return client.list(Equipment.class, predicate, comparator,
            pageNullSafe(page), sizeNullSafe(size)
        ).flatMap(list -> Flux.fromStream(list.get())
            .concatMap(equipment -> Mono.just(EquipmentVo.from(equipment)))
            .collectList()
            .map(momentVos -> new ListResult<>(list.getPage(), list.getSize(),
                list.getTotal(), momentVos
            ))).defaultIfEmpty(new ListResult<>(page, size, 0L, List.of()));
    }
    
    @Override
    public Flux<EquipmentVo> listBy(String groupName) {
        return client.list(Equipment.class, equipment -> {
            String group = equipment.getSpec().getGroupName();
            return StringUtils.equals(group, groupName);
        }, defaultEquipmentComparator()).flatMap(
            equipment -> Mono.just(EquipmentVo.from(equipment)));
    }
    
    @Override
    public Flux<EquipmentGroupVo> groupBy() {
        return this.client.list(EquipmentGroup.class, null,
            defaultGroupComparator()
        ).concatMap(group -> {
            EquipmentGroupVo.EquipmentGroupVoBuilder builder = EquipmentGroupVo.from(group);
            return this.listBy(group.getMetadata().getName()).collectList().map(
                equipments -> {
                    EquipmentGroup.PostGroupStatus status = group.getStatus();
                    status.setEquipmentCount(equipments.size());
                    builder.status(status);
                    builder.equipments(equipments);
                    return builder.build();
                });
        });
    }
    
    static Comparator<EquipmentGroup> defaultGroupComparator() {
        Function<EquipmentGroup, Integer> priority = group -> group.getSpec()
            .getPriority();
        Function<EquipmentGroup, Instant> createTime = group -> group.getMetadata()
            .getCreationTimestamp();
        Function<EquipmentGroup, String> name = group -> group.getMetadata()
            .getName();
        return Comparator.comparing(priority, Comparators.nullsLow())
            .thenComparing(createTime)
            .thenComparing(name);
    }
    
    static Comparator<Equipment> defaultEquipmentComparator() {
        Function<Equipment, Integer> priority = link -> link.getSpec()
            .getPriority();
        Function<Equipment, Instant> createTime = link -> link.getMetadata()
            .getCreationTimestamp();
        Function<Equipment, String> name = link -> link.getMetadata().getName();
        return Comparator.comparing(priority, Comparators.nullsLow())
            .thenComparing(Comparator.comparing(createTime).reversed())
            .thenComparing(name);
    }
    
    int pageNullSafe(Integer page) {
        return ObjectUtils.defaultIfNull(page, 1);
    }
    
    int sizeNullSafe(Integer size) {
        return ObjectUtils.defaultIfNull(size, 10);
    }
}
