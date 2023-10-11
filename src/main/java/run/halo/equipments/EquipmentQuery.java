package run.halo.equipments;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import run.halo.app.extension.router.IListRequest;

/**
 * A query object for {@link Equipment} list.
 *
 * @author LIlGG
 * @since 1.0.0
 */
public class EquipmentQuery extends IListRequest.QueryListRequest {
    
    public EquipmentQuery(MultiValueMap<String, String> queryParams) {
        super(queryParams);
    }
    
    @Schema(description = "Equipments filtered by group.")
    public String getGroup() {
        return StringUtils.defaultIfBlank(queryParams.getFirst("group"), null);
    }
    
    @Nullable
    @Schema(description = "Equipments filtered by keyword.")
    public String getKeyword() {
        return StringUtils.defaultIfBlank(queryParams.getFirst("keyword"),
            null
        );
    }
    
    @Schema(description = "Equipment collation.")
    public EquipmentSorter getSort() {
        String sort = queryParams.getFirst("sort");
        return EquipmentSorter.convertFrom(sort);
    }
    
    @Schema(description = "ascending order If it is true; otherwise, it is in descending order.")
    public Boolean getSortOrder() {
        String sortOrder = queryParams.getFirst("sortOrder");
        return convertBooleanOrNull(sortOrder);
    }
    
    private Boolean convertBooleanOrNull(String value) {
        return StringUtils.isBlank(value) ? null : Boolean.parseBoolean(value);
    }
}
