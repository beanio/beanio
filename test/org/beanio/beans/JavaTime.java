package org.beanio.beans;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class JavaTime {

    private LocalDateTime localDateTime;
    private LocalDate localDate;

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }
}
