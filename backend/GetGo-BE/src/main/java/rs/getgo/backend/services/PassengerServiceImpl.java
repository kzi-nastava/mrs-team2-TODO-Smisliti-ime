package rs.getgo.backend.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.getgo.backend.dtos.authentication.UpdatePasswordDTO;
import rs.getgo.backend.dtos.authentication.UpdatedPasswordDTO;
import rs.getgo.backend.dtos.passenger.GetPassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatePassengerDTO;
import rs.getgo.backend.dtos.passenger.UpdatedPassengerDTO;
import rs.getgo.backend.dtos.user.UpdatedProfilePictureDTO;
import rs.getgo.backend.model.entities.Passenger;
import rs.getgo.backend.repositories.PassengerRepository;

@Service
public class PassengerServiceImpl {

    @Autowired
    private PassengerRepository passengerRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public GetPassengerDTO getPassengerById(Long passengerId) {
        Passenger passenger = passengerRepo.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Passenger not found with id: " + passengerId));

        return modelMapper.map(passenger, GetPassengerDTO.class);
    }

    public UpdatedPassengerDTO updateProfile(Long passengerId, UpdatePassengerDTO updatePassengerDTO) {
        Passenger passenger = passengerRepo.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Passenger not found with id: " + passengerId));

        if (updatePassengerDTO.getName() != null && !updatePassengerDTO.getName().trim().isEmpty()) {
            passenger.setName(updatePassengerDTO.getName().trim());
        }
        if (updatePassengerDTO.getSurname() != null && !updatePassengerDTO.getSurname().trim().isEmpty()) {
            passenger.setSurname(updatePassengerDTO.getSurname().trim());
        }
        if (updatePassengerDTO.getPhone() != null && !updatePassengerDTO.getPhone().trim().isEmpty()) {
            passenger.setPhoneNumber(updatePassengerDTO.getPhone().trim());
        }
        if (updatePassengerDTO.getAddress() != null && !updatePassengerDTO.getAddress().trim().isEmpty()) {
            passenger.setAddress(updatePassengerDTO.getAddress().trim());
        }

        Passenger savedPassenger = passengerRepo.save(passenger);
        return modelMapper.map(savedPassenger, UpdatedPassengerDTO.class);
    }

    public UpdatedPasswordDTO updatePassword(Long passengerId, UpdatePasswordDTO updatePasswordDTO) {
        if (!updatePasswordDTO.getPassword().equals(updatePasswordDTO.getConfirmPassword())) {
            return new UpdatedPasswordDTO(false, "Passwords do not match");
        }

        Passenger passenger = passengerRepo.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Passenger not found with id: " + passengerId));

        if (!passwordEncoder.matches(updatePasswordDTO.getOldPassword(), passenger.getPassword())) {
            return new UpdatedPasswordDTO(false, "Old password is incorrect");
        }

        passenger.setPassword(passwordEncoder.encode(updatePasswordDTO.getPassword()));
        passengerRepo.save(passenger);

        return new UpdatedPasswordDTO(true, "Password updated successfully");
    }


    public UpdatedProfilePictureDTO uploadProfilePicture(Long passengerId, MultipartFile file) {
        Passenger passenger = passengerRepo.findById(passengerId)
                .orElseThrow(() -> new RuntimeException("Passenger not found with id: " + passengerId));

        // Delete old picture if exists
        if (passenger.getProfilePictureUrl() != null) {
            fileStorageService.deleteFile(passenger.getProfilePictureUrl());
        }
        String fileUrl = fileStorageService.storeFile(file, "passenger_" + passengerId);
        passenger.setProfilePictureUrl(fileUrl);
        passengerRepo.save(passenger);
        return new UpdatedProfilePictureDTO(
                fileUrl,
                "Profile picture updated successfully");
    }
}
