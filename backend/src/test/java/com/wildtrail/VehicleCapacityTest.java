package com.wildtrail;

import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.resources.JdbcVehicleRepository;
import com.wildtrail.modules.resources.VehicleService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class VehicleCapacityTest {
    @Test void activeBookingsPreventIncompatibleVehicleChanges() {
        JdbcVehicleRepository catalog = mock(JdbcVehicleRepository.class);
        VehicleService service = new VehicleService(catalog);
        var details = new java.util.HashMap<String, Object>(Map.of(
            "name", "Safari jeep", "category", "STANDARD", "capacity", 3,
            "fourByFour", true, "features", "Open roof", "available", true,
            "price", 40, "imageUrl", "image"));
        when(catalog.lockVehicle(1)).thenReturn(true);
        when(catalog.hasIncompatibleActiveBooking(eq(1), eq(3), eq(true), any()))
                .thenReturn(true);
        assertThrows(ApiException.class, () -> service.updateVehicle(1, details));
        verify(catalog, never()).updateVehicle(eq(1), anyMap());
        details.put("capacity", 6);
        when(catalog.updateVehicle(eq(1), anyMap())).thenReturn(true);
        when(catalog.vehicles(false)).thenReturn(java.util.List.of(Map.of("id", 1)));
        assertDoesNotThrow(() -> service.updateVehicle(1, details));
    }
}
