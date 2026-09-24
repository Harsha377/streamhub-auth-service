package com.streamhub.auth.service.impl;

import com.streamhub.auth.service.EmailTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {
    private final SpringTemplateEngine templateEngine;
    @Override
    public String processTemplate(String templateName, Map<String, Object> variables) {
        Context context=new Context();
        context.setVariables(variables);
        return templateEngine.process(templateName,context);
    }
}
