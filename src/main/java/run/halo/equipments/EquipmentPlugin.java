package run.halo.equipments;

import org.pf4j.PluginWrapper;
import org.springframework.stereotype.Component;
import run.halo.app.extension.SchemeManager;
import run.halo.app.plugin.BasePlugin;

/**
 * @author ryanwang
 * @since 2.0.0
 */
@Component
public class EquipmentPlugin extends BasePlugin {
    private final SchemeManager schemeManager;
    
    public EquipmentPlugin(PluginWrapper wrapper, SchemeManager schemeManager) {
        super(wrapper);
        this.schemeManager = schemeManager;
    }
    
    @Override
    public void start() {
        schemeManager.register(Equipment.class);
        schemeManager.register(EquipmentGroup.class);
    }
    
    @Override
    public void stop() {
        schemeManager.unregister(schemeManager.get(Equipment.class));
        schemeManager.unregister(schemeManager.get(EquipmentGroup.class));
    }
}
