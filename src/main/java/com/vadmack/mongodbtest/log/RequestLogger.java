package com.vadmack.mongodbtest.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Component
@Aspect
public class RequestLogger {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Around("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object applicationLogger(ProceedingJoinPoint pjt) throws Throwable {
        StopWatch watch = new StopWatch();
        watch.start();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String httpMethod = request.getMethod();
        String requestUri = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort() + request.getContextPath() + request.getRequestURI();
        String queryString = request.getQueryString();
        if (queryString != null) {
            requestUri += "?" + queryString;
        }
        String clientIp = request.getRemoteAddr();
        String username = request.getRemoteUser();

        // response
        Object object = pjt.proceed();

        watch.stop();
        log.debug(String.format("\n" +
                        "%-20s" + httpMethod + "\n" +
                        "%-20s" + requestUri + "\n" +
                        "%-20s" + clientIp + "\n" +
                        "%-20s" + username + "\n" +
                        "%-20s" + watch.getTotalTimeMillis() + "ms",
                "Method:",
                "Request URI:",
                "Client address:",
                "Username:",
                "Processed in:"
        ));
        return object;
    }
}
