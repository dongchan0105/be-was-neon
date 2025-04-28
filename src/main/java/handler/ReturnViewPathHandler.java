package handler;

import java.util.Map;

public interface ReturnViewPathHandler<T> {
    /**
     * @param paramMap
     * @param model
     * @return viewName
     */
    String process(T paramMap, Map<String, Object> model);
}
