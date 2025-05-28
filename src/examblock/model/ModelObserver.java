package examblock.model;

/**
 * Base for all Model Observers
 */
public interface ModelObserver {

    /**
     * override this
     * @param property what happened
     */
    void modelChanged(String property);
}
