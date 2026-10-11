package com.wildtrail;

import com.wildtrail.exception.ApiException;
import com.wildtrail.modules.safaris.JdbcPackageRepository;
import com.wildtrail.modules.safaris.PackageService;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

class PackageScheduleTest {
    @Test void packageCannotContinueIntoNextDay() {
        JdbcPackageRepository catalog = mock(JdbcPackageRepository.class);
        when(catalog.destinationExists(1)).thenReturn(true);
        PackageService service = new PackageService(catalog);
        var details = new java.util.HashMap<String, Object>(Map.of(
            "name", "Evening safari", "packageType", "EVENING", "destinationId", 1,
            "durationHours", 2, "startTime", "23:00", "maxGuests", 4,
            "basePrice", 100, "description", "Safari", "imageUrl", "image"));
        assertThrows(ApiException.class, () -> service.createPackage(details));
        verify(catalog, never()).createPackage(anyMap());
        details.put("startTime", "22:00");
        assertDoesNotThrow(() -> service.createPackage(details));
        verify(catalog).createPackage(anyMap());
    }
}
