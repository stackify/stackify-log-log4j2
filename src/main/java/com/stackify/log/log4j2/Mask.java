package com.stackify.log.log4j2;

import lombok.Getter;
import lombok.ToString;
import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.config.plugins.PluginValue;

/**
 * Defines a Mask.
 * Enabled can be to true or false.
 * Value can be set to a regex or a built-in mask (CREDITCARD, SSN or IP)
 *
 * @author Darin Howard
 */
@ToString
@Plugin(name = "Mask", category = Node.CATEGORY, elementType = Filter.ELEMENT_TYPE, printObject = true)
public class Mask extends AbstractLifeCycle {

    @Getter
    private String value;

    @Getter
    private boolean enabled;

    @PluginFactory
    public static Mask create(@PluginValue("value") final String value,
                              @PluginAttribute("enabled") final String enabled) {
        return new Mask(value, enabled != null && Boolean.parseBoolean(enabled));
    }

    public Mask(final String value,
                final boolean enabled) {
        this.value = value;
        this.enabled = enabled;
    }

}
