package run.halo.equipments.service;

import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.equipments.Equipment;
import run.halo.equipments.EquipmentQuery;

/**
 * A service for {@link Equipment}.
 *
 * @author LIlGG
 * @since 1.0.0
 */
public interface EquipmentService {
    
    /**
     * List equipments.
     *
     * @param query query
     * @return a mono of list result
     */
    Mono<ListResult<Equipment>> listEquipment(EquipmentQuery query);
}
