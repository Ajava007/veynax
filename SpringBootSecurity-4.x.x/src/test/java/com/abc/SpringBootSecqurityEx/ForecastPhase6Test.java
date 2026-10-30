package com.abc.SpringBootSecqurityEx;

import com.abc.SpringBootSecqurityEx.entity.Forecast;
import com.abc.SpringBootSecqurityEx.entity.LineTarget;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ForecastPhase6Test {
    @Test
    void forecastCombining() {
        int historical = 100, dealer = 80, adjustment = 20;
        int finalForecast = historical + dealer + adjustment;
        assertEquals(200, finalForecast);
        Forecast f = Forecast.builder().historicalDemand(historical)
            .dealerForecast(dealer).managerAdjustment(adjustment)
            .finalForecast(finalForecast).status("DRAFT").build();
        assertEquals(200, f.getFinalForecast());
    }

    @Test
    void forecastApprovalStates() {
        Forecast f = new Forecast();
        f.setStatus("DRAFT");
        f.setStatus("PENDING_APPROVAL");
        f.setApprovedBy("manager1");
        f.setStatus("APPROVED");
        assertEquals("APPROVED", f.getStatus());
        assertEquals("manager1", f.getApprovedBy());
    }

    @Test
    void lineTargetPlanVsActual() {
        LineTarget t = LineTarget.builder().targetDate(LocalDate.now())
            .lineCode("LINE-A").modelCode("ALLION")
            .targetQuantity(50).actualQuantity(45)
            .rejectedQuantity(2).downtimeMinutes(30).build();
        int variance = t.getTargetQuantity() - t.getActualQuantity();
        assertEquals(5, variance);
        assertEquals(2, t.getRejectedQuantity());
    }
}
