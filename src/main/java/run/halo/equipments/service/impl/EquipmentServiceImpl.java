package run.halo.equipments.service.impl;

import static run.halo.app.extension.router.selector.SelectorUtil.labelAndFieldSelectorToPredicate;

import java.util.Comparator;
import java.util.function.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.extension.Extension;
import run.halo.app.extension.ListResult;
import run.halo.app.extension.ReactiveExtensionClient;
import run.halo.equipments.Equipment;
import run.halo.equipments.EquipmentQuery;
import run.halo.equipments.EquipmentSorter;
import run.halo.equipments.service.EquipmentService;

/**
 * Service implementation for {@link Equipment}.
 *
 * @author LIlGG
 * @since 1.0.0
 */
@Component
public class EquipmentServiceImpl implements EquipmentService {
    
    private final ReactiveExtensionClient client;
    
    public EquipmentServiceImpl(ReactiveExtensionClient client) {
        this.client = client;
    }
    
    @Override
    public Mono<ListResult<Equipment>> listEquipment(EquipmentQuery query) {
        Comparator<Equipment> comparator = EquipmentSorter.from(query.getSort(),
            query.getSortOrder()
        );
        return this.client.list(Equipment.class, equipmentListPredicate(query),
            comparator, query.getPage(), query.getSize()
        );
    }
    
    Predicate<Equipment> equipmentListPredicate(EquipmentQuery query) {
        Predicate<Equipment> predicate = equipment -> true;
        String keyword = query.getKeyword();
        
        if (keyword != null) {
            predicate = predicate.and(equipment -> {
                String displayName = equipment.getSpec().getDisplayName();
                return StringUtils.containsIgnoreCase(displayName, keyword);
            });
        }
        
        String groupName = query.getGroup();
        if (groupName != null) {
            predicate = predicate.and(equipment -> {
                String group = equipment.getSpec().getGroupName();
                return StringUtils.equals(group, groupName);
            });
        }
        
        Predicate<Extension> labelAndFieldSelectorPredicate
            = labelAndFieldSelectorToPredicate(query.getLabelSelector(),
            query.getFieldSelector()
        );
        return predicate.and(labelAndFieldSelectorPredicate);
    }
}
