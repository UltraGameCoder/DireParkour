package net.direskies.direparkour.util;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MaterialUtil {

    //Future-proof list of pressure plates.
    public static final Set<Material> PRESSURE_PLATES = Collections.unmodifiableSet(
            EnumSet.copyOf(
                    Arrays.stream(Material.values())
                            .filter(material -> material.name().contains("PRESSURE_PLATE"))
                            .collect(Collectors.toList())
            )
    );
}
