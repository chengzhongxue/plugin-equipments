package run.halo.equipments;

import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import org.springframework.util.comparator.Comparators;

/**
 * A sorter for {@link Equipment}.
 *
 * @author LIlGG
 * @since 1.0.0
 */
public enum EquipmentSorter {
    DISPLAY_NAME,
    
    CREATE_TIME;
    
    static final Function<Equipment, String> name = equipment -> equipment.getMetadata()
        .getName();
    
    /**
     * Converts {@link Comparator} from {@link EquipmentSorter} and ascending.
     *
     * @param sorter    a {@link EquipmentSorter}
     * @param ascending ascending if true, otherwise descending
     * @return a {@link Comparator} of {@link Equipment}
     */
    public static Comparator<Equipment> from(EquipmentSorter sorter,
        Boolean ascending) {
        if (Objects.equals(true, ascending)) {
            return from(sorter);
        }
        return from(sorter).reversed();
    }
    
    /**
     * Converts {@link Comparator} from {@link EquipmentSorter}.
     *
     * @param sorter a {@link EquipmentSorter}
     * @return a {@link Comparator} of {@link Equipment}
     */
    static Comparator<Equipment> from(EquipmentSorter sorter) {
        if (sorter == null) {
            return createTimeComparator();
        }
        if (CREATE_TIME.equals(sorter)) {
            Function<Equipment, Instant> comparatorFunc
                = equipment -> equipment.getMetadata().getCreationTimestamp();
            return Comparator.comparing(comparatorFunc).thenComparing(name);
        }
        
        if (DISPLAY_NAME.equals(sorter)) {
            Function<Equipment, String> comparatorFunc = moment -> moment.getSpec()
                .getDisplayName();
            return Comparator.comparing(comparatorFunc, Comparators.nullsLow())
                .thenComparing(name);
        }
        
        throw new IllegalStateException("Unsupported sort value: " + sorter);
    }
    
    /**
     * Converts {@link EquipmentSorter} from string.
     *
     * @param sort sort string
     * @return a {@link EquipmentSorter}
     */
    static EquipmentSorter convertFrom(String sort) {
        for (EquipmentSorter sorter : values()) {
            if (sorter.name().equalsIgnoreCase(sort)) {
                return sorter;
            }
        }
        return null;
    }
    
    /**
     * Creates a {@link Comparator} of {@link Equipment} by creation time.
     *
     * @return a {@link Comparator} of {@link Equipment}
     */
    static Comparator<Equipment> createTimeComparator() {
        Function<Equipment, Instant> comparatorFunc = equipment -> equipment.getMetadata()
            .getCreationTimestamp();
        return Comparator.comparing(comparatorFunc).thenComparing(name);
    }
}
