package run.halo.equipments.finders;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.extension.ListResult;
import run.halo.equipments.vo.EquipmentGroupVo;
import run.halo.equipments.vo.EquipmentVo;


/**
 * A finder for {@link run.halo.equipments.Equipment}.
 *
 * @author LIlGG
 */
public interface EquipmentFinder {
    
    /**
     * List all equipments.
     *
     * @return a flux of equipment vo
     */
    Flux<EquipmentVo> listAll();
    
    /**
     * List equipments by page.
     *
     * @param page page number
     * @param size page size
     * @return a mono of list result
     */
    Mono<ListResult<EquipmentVo>> list(Integer page, Integer size);
    
    /**
     * List equipments by page and group.
     *
     * @param page  page number
     * @param size  page size
     * @param group group name
     * @return a mono of list result
     */
    Mono<ListResult<EquipmentVo>> list(Integer page, Integer size, String group);
    
    /**
     * List equipments by group.
     *
     * @param group group name
     * @return a flux of equipment vo
     */
    Flux<EquipmentVo> listBy(String group);
    
    /**
     * List all groups.
     *
     * @return a flux of equipment group vo
     */
    Flux<EquipmentGroupVo> groupBy();
}
