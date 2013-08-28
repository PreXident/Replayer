package com.paradoxplaza.eu4.replayer.utils;

/**
 * Holder for (in)out parameters.
 */
public class Ref<T> {

    /** Held value. */
    public T val;

    /**
     * Creates Ref to null value.
     */
    public Ref() {
        val = null;
    }

    /**
     * Creates Ref to val.
     * @param val reference value
     */
    public Ref(final T val) {
        this.val = val;
    }

    /**
     * Gets val.
     * @return val
     */
    public T getVal() {
        return val;
    }

    /**
     * Sets val.
     * @param val new value
     */
    public void setVal(final T val) {
        this.val = val;
    }

    /**
     * Sets val.
     * @param val new value
     * @return this
     */
    public Ref<T> withVal(final T val) {
        setVal(val);
        return this;
    }

    @Override
    public boolean equals(final Object obj) {
        Object inner = innerObject();
        if (obj == null) {
            return inner == null;
        } else {
            return obj.equals(inner);
        }
    }

    @Override
    public int hashCode() {
        return val != null ? val.hashCode() : 0;
    }

    /**
     * Returns the inner object in reference hierarchy.
     * @return inner object of reference hierarchy
     */
    private Object innerObject() {
        Object res = val;
        while (res != null && res instanceof Ref) {
            res = ((Ref) res).val;
        }
        return res;
    }
}
