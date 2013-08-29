package com.paradoxplaza.eu4.replayer.events;

/**
 * Building added/removed from province.
 */
public class Building extends ProvinceEvent {

    /** Possible types of building event. */
    enum Type { BUILT, DESTROYED;
        static Type fromString(final String string) {
            switch (string) {
                case "yes":
                    return BUILT;
                case "no":
                    return DESTROYED;
                default:
                    throw new IllegalArgumentException(String.format("Unknown building type %1$s", string));
            }
        }
    }

    /** Building name. */
    final String building;

    /** Building type. */
    final Type type;

    /**
     * Only constructor
     * @param id province id
     * @param name province name
     * @param building building name
     * @param type building event type
     */
    public Building(final String id, final String name, final String building, final String type) {
        super(id, name);
        this.building = building;
        this.type = Type.fromString(type);
    }

    @Override
    public String toString() {
        return String.format("Building %1$s %2$s in province %3$s (%4$s)", building, type, id, name);
    }
}
