package vn.xuanthai.clinic.booking.service;

import vn.xuanthai.clinic.booking.dto.request.ClinicRequest;
import vn.xuanthai.clinic.booking.dto.response.ClinicResponse;
import java.util.List;

public interface IClinicService {

    ClinicResponse createClinic(ClinicRequest request);

    ClinicResponse getClinicById(Long clinicId);

    List<ClinicResponse> getAllClinics();

    ClinicResponse updateClinic(Long clinicId, ClinicRequest request);

    void deleteClinic(Long clinicId);
}