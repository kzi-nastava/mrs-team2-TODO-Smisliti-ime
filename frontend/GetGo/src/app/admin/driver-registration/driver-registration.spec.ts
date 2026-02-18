import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';

import { DriverRegistration } from './driver-registration';
import { AdminService, CreateDriverDTO, CreatedDriverDTO } from '../service/admin.service';
import { VehicleService } from '../../service/vehicle-service/vehicle.service';
import { SnackBarService } from '../../service/snackBar/snackBar.service';

describe('DriverRegistration', () => {
  let component: DriverRegistration;
  let fixture: ComponentFixture<DriverRegistration>;
  let adminServiceSpy: jasmine.SpyObj<AdminService>;
  let vehicleServiceSpy: jasmine.SpyObj<VehicleService>;
  let snackBarSpy: jasmine.SpyObj<SnackBarService>;

  beforeEach(async () => {
    adminServiceSpy = jasmine.createSpyObj('AdminService', ['registerDriver']);
    vehicleServiceSpy = jasmine.createSpyObj('VehicleService', ['getVehicleTypes']);
    snackBarSpy = jasmine.createSpyObj('SnackBarService', ['show']);

    vehicleServiceSpy.getVehicleTypes.and.returnValue(of(['STANDARD', 'LUXURY', 'VAN']));

    await TestBed.configureTestingModule({
      imports: [DriverRegistration, FormsModule],
      providers: [
        { provide: AdminService, useValue: adminServiceSpy },
        { provide: VehicleService, useValue: vehicleServiceSpy },
        { provide: SnackBarService, useValue: snackBarSpy },
        { provide: ActivatedRoute, useValue: {} }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverRegistration);
    component = fixture.debugElement.componentInstance;
    fixture.detectChanges();
  });

  it('should load vehicle types on init', () => {
    expect(component).toBeTruthy();
    expect(vehicleServiceSpy.getVehicleTypes).toHaveBeenCalled();
    expect(component.vehicleTypes).toEqual(['STANDARD', 'LUXURY', 'VAN']);
  });

  it('should start on driver tab', () => {
    expect(component.activeTab).toBe('driver');
  });

  it('should not go to vehicle tab when email is empty', () => {
    component.driverData = { email: '', firstName: 'Driverfn', lastName: 'Driverln', phone: '0659483728', address: 'Driver Address 123' };
    component.goToVehicle();
    expect(component.activeTab).toBe('driver');
    expect(snackBarSpy.show).toHaveBeenCalledWith('Please fill in all driver fields');
  });

  it('should not go to vehicle tab when firstName is empty', () => {
    component.driverData = { email: 'd@gmail.com', firstName: '', lastName: 'Driverln', phone: '0659483728', address: 'Driver Address 123' };
    component.goToVehicle();
    expect(component.activeTab).toBe('driver');
    expect(snackBarSpy.show).toHaveBeenCalled();
  });

  it('should not go to vehicle tab when lastName is empty', () => {
    component.driverData = { email: 'd@gmail.com', firstName: 'Driverfn', lastName: '', phone: '0659483728', address: 'Driver Address 123' };
    component.goToVehicle();
    expect(component.activeTab).toBe('driver');
  });

  it('should go to vehicle tab when fields are filled', () => {
    component.driverData = { email: 'd@gmail.com', firstName: 'Driverfn', lastName: 'Driverln', phone: '0659483728', address: 'Driver Address 123' };
    component.goToVehicle();
    expect(component.activeTab).toBe('vehicle');
  });

  it('should go back to driver tab', () => {
    component.activeTab = 'vehicle';
    component.goToDriver();
    expect(component.activeTab).toBe('driver');
  });

  it('should call registerDriver reset forms on register success', fakeAsync(() => {
    component.driverData = { email: 'd@gmail.com', firstName: 'Driverfn', lastName: 'Driverln', phone: '0659483728', address: 'Driver Address 123' };
    component.vehicleData = { model: 'Toyota Corolla', type: 'SUV', registrationNumber: '123456', seats: 2 as any, allowsBabies: true, allowsPets: true };

    const response: CreatedDriverDTO = { id: 1, email: 'd@gmail.com', name: 'Driverfn', surname: 'Driverln', address: 'Driver Address 123' };
    adminServiceSpy.registerDriver.and.returnValue(of(response));

    component.onRegister();
    tick();

    const expectedDTO: CreateDriverDTO = {
      email: 'd@gmail.com',
      name: 'Driverfn',
      surname: 'Driverln',
      phone: '0659483728',
      address: 'Driver Address 123',
      vehicleModel: 'Toyota Corolla',
      vehicleType: 'SUV',
      vehicleLicensePlate: '123456',
      vehicleSeats: 2,
      vehicleHasBabySeats: true,
      vehicleAllowsPets: true
    };

    expect(adminServiceSpy.registerDriver).toHaveBeenCalledWith(expectedDTO);
    expect(snackBarSpy.show).toHaveBeenCalledWith('Driver registered successfully! Activation email sent to d@gmail.com');

    expect(component.driverData.email).toBe('');
    expect(component.driverData.firstName).toBe('');
    expect(component.vehicleData.model).toBe('');
    expect(component.vehicleData.seats).toBeNull();
    expect(component.activeTab).toBe('driver');
  }));

  it('should show error snackbar on register failure', fakeAsync(() => {
    component.driverData = { email: 'd@gmail.com', firstName: 'Driverfn', lastName: 'Driverln', phone: '0659483728', address: 'Driver Address 123' };
    component.vehicleData = { model: 'Toyota Corolla', type: 'SUV', registrationNumber: '123456', seats: 2 as any, allowsBabies: true, allowsPets: true };

    adminServiceSpy.registerDriver.and.returnValue(throwError(() => ({ status: 500 })));

    component.onRegister();
    tick();

    expect(snackBarSpy.show).toHaveBeenCalledWith('Failed to register driver. Please try again.');
  }));
});
