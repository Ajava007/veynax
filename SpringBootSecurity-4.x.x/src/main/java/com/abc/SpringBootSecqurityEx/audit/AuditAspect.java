package com.abc.SpringBootSecqurityEx.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditAspect(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Around("execution(* com.abc.SpringBootSecqurityEx.service.*.create*(..)) || " +
            "execution(* com.abc.SpringBootSecqurityEx.service.*.update*(..)) || " +
            "execution(* com.abc.SpringBootSecqurityEx.service.*.delete*(..)) || " +
            "execution(* com.abc.SpringBootSecqurityEx.service.*.save*(..))")
    public Object auditWriteOperation(ProceedingJoinPoint pjp) throws Throwable {
        String actor = currentActor();
        String action = inferAction(pjp.getSignature().getName());
        String entityType = pjp.getTarget().getClass().getSimpleName();
        String ip = currentIp();

        Object result = pjp.proceed();

        try {
            AuditLog log = AuditLog.builder()
                    .actor(actor)
                    .entityType(entityType)
                    .entityId(extractId(result))
                    .action(action)
                    .afterState(safeJson(result))
                    .ipAddress(ip)
                    .build();
            auditLogRepository.save(log);
        } catch (Exception ignored) {
            // Audit failure must never break the business operation
        }
        return result;
    }

    private String currentActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }

    private String inferAction(String methodName) {
        if (methodName.startsWith("create") || methodName.startsWith("save")) return "CREATE";
        if (methodName.startsWith("update")) return "UPDATE";
        if (methodName.startsWith("delete")) return "DELETE";
        return "WRITE";
    }

    private String extractId(Object result) {
        if (result == null) return null;
        try {
            Object id = result.getClass().getMethod("getId").invoke(result);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String safeJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.getClass().getSimpleName();
        }
    }

    private String currentIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String xf = req.getHeader("X-Forwarded-For");
                return xf != null ? xf.split(",")[0].trim() : req.getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
