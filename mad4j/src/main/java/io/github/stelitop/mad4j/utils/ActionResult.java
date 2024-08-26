package io.github.stelitop.mad4j.utils;

/**
 * Represents the result from a generic action.
 */
public class ActionResult<T> {

    /**
     * Whether the action was successful.
     */
    private boolean isSuccessful;
    /**
     * The error message received in case the result wasn't successful.
     */
    private String errorMessage;
    /**
     * The resulting object.
     */
    private T response;

    /**
     *
     */
    private ActionResult() {

    }

    public static ActionResult<Void> success() {
        ActionResult<Void> ret = new ActionResult<>();
        ret.isSuccessful = true;
        ret.errorMessage = null;
        return ret;
    }

    public static <S> ActionResult<S> success(S response) {
        ActionResult<S> ret = new ActionResult<>();
        ret.isSuccessful = true;
        ret.errorMessage = null;
        ret.response = response;
        return ret;
    }

    public static <S> ActionResult<S> fail(String errorMessage) {
        ActionResult<S> ret = new ActionResult<>();
        ret.isSuccessful = false;
        ret.errorMessage = errorMessage;
        return ret;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public boolean hasFailed() {
        return !isSuccessful;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public T getResponse() {
        return response;
    }
}
