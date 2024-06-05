package com.relic.relic.repository;

import com.relic.relic.model.Notification;
import com.relic.relic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByIsAdminNotificationTrue();
    List<Notification> findByUserAndIsReadFalse(User user);
    List<Notification> findByUser(User user);
}
