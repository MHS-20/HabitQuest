package habitquest.avatar.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;

public class ApiResponse<T> extends RepresentationModel {

    @JsonUnwrapped
    private final T content;
    private final Links links;

    private ApiResponse(T content, Links links) {
        this.content = content;
        this.links = links;
    }

    static <T> ApiResponse<T> of(T content, Links links) {
        return new ApiResponse<>(content, links);
    }

    public T getContent() { return content; }
    public Links getLinks() { return links; }
}