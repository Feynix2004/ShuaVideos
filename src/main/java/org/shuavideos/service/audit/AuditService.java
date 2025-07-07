package org.shuavideos.service.audit;

public interface AuditService<T,R> {

    /**
     *  审核规范
     * @param task
     * @return
     */
    R audit(T task);
}
