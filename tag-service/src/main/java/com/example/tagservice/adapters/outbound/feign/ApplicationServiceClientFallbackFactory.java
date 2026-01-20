package com.example.tagservice.adapters.outbound.feign;

import com.example.tagservice.application.dto.ApplicationInfoDto;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class ApplicationServiceClientFallbackFactory implements FallbackFactory<ApplicationServiceClientFeign> {
    @Override
    public ApplicationServiceClientFeign create(Throwable cause) {
        return new ApplicationServiceClientFeign() {
            @Override
            public List<ApplicationInfoDto> getApplicationsByTag(String tagName) {
                // при недоступности возвращаем пустой список, адаптеры обработают это соответствующим образом
                return Collections.emptyList();
            }
        };
    }
}
