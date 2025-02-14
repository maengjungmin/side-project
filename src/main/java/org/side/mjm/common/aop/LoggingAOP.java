package org.side.mjm.common.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Aspect
@Component
public class LoggingAOP {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAOP.class);

    @Before("@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void logBefore(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String requestUri = request.getRequestURI();
        LOGGER.info("[Request URI] : {}", requestUri);
        Arrays.stream(args)
                .filter(Objects::nonNull)
                .filter(arg -> !(arg instanceof SecurityContextHolderAwareRequestWrapper))
                .forEach(this::printParameterFields);
    }

    @AfterReturning(pointcut = "@annotation(org.springframework.web.bind.annotation.PostMapping)", returning = "responseEntity")
    public void logAfterReturning(JoinPoint joinPoint, ResponseEntity<?> responseEntity) {
        LOGGER.info("◁◁◁◁◁◁[Response] Success.\n");
    }

    @AfterThrowing(pointcut = "@annotation(org.springframework.web.bind.annotation.PostMapping)", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        LOGGER.info("◀◀◀◀◀[Response] Failure.\n" + "Exception : " + ex.getClass().getSimpleName() + "\nMessage : " + ex.getMessage() + "\n");
    }

    private void printParameterFields(Object obj) {
        StringBuilder sb = new StringBuilder();
        StringJoiner sj = new StringJoiner("\n");
        if (obj instanceof List<?> listParameter) {
            Class<?> parameter = listParameter.get(0).getClass();
            sb.append("[Request Parameter] [").append(parameter.getSimpleName()).append("][List] ▷▷▷▷▷\n");
            for (Object item : listParameter) {
                Class<?> clazz = item.getClass();
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    try {
                        sj.add(field.getName() + " : " + field.get(item));
                    } catch (IllegalAccessException e) {
                        LOGGER.error("Parameter field logging failure.");
                    }
                }
            }
        } else {
            Class<?> clazz = obj.getClass();
            Field[] fields = clazz.getDeclaredFields();
            sb.append("[Request Parameter] [").append(clazz.getSimpleName()).append("] ▷▷▷▷▷\n");
            for (Field field : fields) {
                field.setAccessible(true);
                try {
                    sj.add(field.getName() + " : " + field.get(obj));
                } catch (IllegalAccessException e) {
                    LOGGER.error("Parameter field logging failure.");
                }
            }
        }
        sb.append(sj);
        LOGGER.info(sb.toString());
    }
}
