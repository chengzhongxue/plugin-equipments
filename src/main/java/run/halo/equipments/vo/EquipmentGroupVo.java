package run.halo.equipments.vo;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import run.halo.app.extension.MetadataOperator;
import run.halo.app.theme.finders.vo.ExtensionVoOperator;
import run.halo.equipments.EquipmentGroup;

/**
 * @author LIlGG
 */
@Value
@Builder
public class EquipmentGroupVo implements ExtensionVoOperator {
    MetadataOperator metadata;
    
    EquipmentGroup.EquipmentGroupSpec spec;
    
    EquipmentGroup.PostGroupStatus status;
    
    List<EquipmentVo> equipments;
    
    public static EquipmentGroupVoBuilder from(EquipmentGroup equipmentGroup) {
        return EquipmentGroupVo.builder()
            .metadata(equipmentGroup.getMetadata())
            .spec(equipmentGroup.getSpec())
            .status(equipmentGroup.getStatusOrDefault())
            .equipments(List.of());
    }
}
