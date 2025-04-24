package frontHandler.adapter;

import dto.HttpRequest;
import frontHandler.ModelView;

import java.io.IOException;

public interface HandlerAdapter {

    boolean supports(Object handler);

    ModelView handle(HttpRequest request, Object handler) throws IOException;
}


