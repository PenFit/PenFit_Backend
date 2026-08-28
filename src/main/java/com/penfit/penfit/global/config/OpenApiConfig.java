package com.penfit.penfit.global.config;

import com.penfit.penfit.global.enums.DisplayNamed;
import com.penfit.penfit.global.enums.RehearsalOptionCatalog;
import com.penfit.penfit.global.enums.ScenarioCode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    private static final String ENUM_PACKAGE = "com.penfit.penfit.global.enums";
    private static final String ANSWER_REQUEST_SCHEMA = "AnswerSaveRequest";
    private static final String OPTION_CODE_PROPERTY = "optionCode";

    private final Map<Set<String>, Class<?>> enumIndex = buildEnumIndex();

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(info())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, bearerScheme()));
    }

    @Bean
    public OpenApiCustomizer enumDescriptionCustomizer() {
        return openApi -> {
            if (openApi.getComponents() == null || openApi.getComponents().getSchemas() == null) {
                return;
            }
            openApi.getComponents().getSchemas().values().forEach(this::describe);
            applyScenarioOptionGuide(openApi);
        };
    }

    private void applyScenarioOptionGuide(OpenAPI openApi) {
        Schema<?> request = openApi.getComponents().getSchemas().get(ANSWER_REQUEST_SCHEMA);
        if (request == null || request.getProperties() == null) {
            return;
        }
        Schema<?> optionCode = (Schema<?>) request.getProperties().get(OPTION_CODE_PROPERTY);
        if (optionCode == null) {
            return;
        }
        optionCode.setDescription(scenarioOptionGuide());
    }

    private String scenarioOptionGuide() {
        StringBuilder guide = new StringBuilder(
                "상황마다 사용할 수 있는 선택지가 다르다. 경로의 scenarioCode 에 해당하는 값만 보낼 수 있고, "
                        + "허용되지 않은 조합은 RH4001 을 반환한다.");
        for (ScenarioCode scenarioCode : ScenarioCode.values()) {
            guide.append("<br><br>**`")
                    .append(scenarioCode.name())
                    .append("`** ")
                    .append(scenarioCode.getDisplayName());
            for (RehearsalOptionCatalog.Entry entry : RehearsalOptionCatalog.optionsOf(scenarioCode)) {
                guide.append("<br>`")
                        .append(entry.optionCode().name())
                        .append("` ")
                        .append(entry.meaning());
            }
        }
        return guide.toString();
    }

    private void describe(Schema<?> schema) {
        if (schema == null) {
            return;
        }
        applyEnumDescription(schema);
        if (schema.getProperties() != null) {
            schema.getProperties().values().forEach(this::describe);
        }
        if (schema.getItems() != null) {
            describe(schema.getItems());
        }
    }

    private void applyEnumDescription(Schema<?> schema) {
        List<?> values = schema.getEnum();
        if (values == null || values.isEmpty() || schema.getDescription() != null) {
            return;
        }
        Set<String> names = values.stream().map(String::valueOf).collect(Collectors.toCollection(LinkedHashSet::new));
        Class<?> type = enumIndex.get(names);
        if (type == null) {
            return;
        }
        schema.setDescription(Arrays.stream(type.getEnumConstants())
                .map(constant -> "`%s` %s".formatted(
                        ((Enum<?>) constant).name(),
                        ((DisplayNamed) constant).getDisplayName()))
                .collect(Collectors.joining("<br>")));
    }

    private Map<Set<String>, Class<?>> buildEnumIndex() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(
                            org.springframework.beans.factory.annotation.AnnotatedBeanDefinition beanDefinition) {
                        return true;
                    }
                };
        scanner.addIncludeFilter(new AssignableTypeFilter(DisplayNamed.class));

        Map<Set<String>, Class<?>> index = new HashMap<>();
        for (BeanDefinition definition : scanner.findCandidateComponents(ENUM_PACKAGE)) {
            try {
                Class<?> type = Class.forName(definition.getBeanClassName());
                if (!type.isEnum()) {
                    continue;
                }
                Set<String> names = Arrays.stream(type.getEnumConstants())
                        .map(constant -> ((Enum<?>) constant).name())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
                index.put(names, type);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return index;
    }

    private Info info() {
        return new Info()
                .title("PenFit API")
                .version("v1")
                .description("""
                        사회초년생이 가상의 연금계좌로 생애 변화와 시장 하락을 미리 경험하고,
                        AI 분석을 통해 유지 가능한 연금계획과 적합한 상품 후보를 찾는 서비스.
                        """);
    }

    private SecurityScheme bearerScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER);
    }
}
