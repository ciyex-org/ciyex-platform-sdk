package org.ciyex.sdk.rpm;

import org.ciyex.sdk.common.PatientRef;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Generic Remote Patient Monitoring (RPM) device provider interface.
 * Vendor adapters (device platforms, data aggregators) implement this.
 */
public interface RpmProvider {

    /** Enroll a patient in RPM monitoring program. */
    EnrollmentResult enrollPatient(EnrollmentRequest request);

    /** Assign a device to an enrolled patient. */
    DeviceAssignment assignDevice(String patientId, String deviceType, String deviceId);

    /** Get latest readings for a patient. */
    List<DeviceReading> getReadings(String patientId, String deviceType, int limit);

    /** Get alert/threshold violations. */
    List<Alert> getAlerts(String orgAlias, int limit, int offset);

    /** Acknowledge/dismiss an alert. */
    void acknowledgeAlert(String alertId, String note);

    /** Set monitoring thresholds for a patient. */
    void setThresholds(String patientId, String deviceType, Map<String, ThresholdRange> thresholds);

    /** Get CPT billing summary for RPM time tracking. */
    BillingSummary getBillingSummary(String patientId, String monthYear);

    /** Verify connectivity. */
    ConnectionStatus testConnection(Map<String, String> config);

    String vendorId();

    // --- DTOs ---

    record EnrollmentRequest(
            PatientRef patient,
            String orgAlias,
            List<String> deviceTypes, // BLOOD_PRESSURE, GLUCOSE, WEIGHT, PULSE_OX, THERMOMETER
            String programType, // RPM, CCM, RTM
            String consentStatus
    ) {}

    record EnrollmentResult(
            String enrollmentId,
            String status, // ENROLLED, PENDING_CONSENT, DEVICE_PENDING
            List<String> assignedDeviceTypes
    ) {}

    record DeviceAssignment(
            String assignmentId,
            String deviceId,
            String deviceType,
            String serialNumber,
            String status // ASSIGNED, ACTIVE, INACTIVE, RETURNED
    ) {}

    record DeviceReading(
            String readingId,
            String deviceType,
            Map<String, Object> values, // e.g., {systolic: 130, diastolic: 85} or {glucose: 142}
            String unit,
            Instant timestamp,
            String source, // DEVICE, MANUAL
            boolean alertTriggered
    ) {}

    record Alert(
            String alertId,
            String patientId,
            String patientName,
            String deviceType,
            String alertType, // THRESHOLD_HIGH, THRESHOLD_LOW, MISSED_READING, DEVICE_INACTIVE
            String severity, // CRITICAL, WARNING, INFO
            String message,
            Map<String, Object> readingValues,
            Instant triggeredAt,
            boolean acknowledged,
            String acknowledgedBy
    ) {}

    record ThresholdRange(
            Double low,
            Double high,
            Double criticalLow,
            Double criticalHigh
    ) {}

    record BillingSummary(
            String patientId,
            String monthYear,
            int totalMonitoringMinutes,
            int interactiveMinutes,
            boolean deviceSetupBillable, // CPT 99453
            boolean monthlyMonitoringBillable, // CPT 99454 (16+ days)
            boolean treatment20MinBillable, // CPT 99457
            boolean treatmentAdditional20MinBillable, // CPT 99458
            int readingDays
    ) {}

    record ConnectionStatus(boolean connected, String message, int connectedDeviceTypes) {}
}
