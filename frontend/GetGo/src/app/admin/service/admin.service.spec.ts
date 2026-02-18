import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../../env/environment';

import { AdminService, CreateDriverDTO, CreatedDriverDTO } from './admin.service';

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiHost}/api/admin`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdminService]
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call registerDriver and return CreatedDriverDTO', () => {
    const driverData: CreateDriverDTO = {
      email: 'driver@test.com',
      name: 'Jane',
      surname: 'Smith',
      phone: '555',
      address: '123 St',
      vehicleModel: 'Toyota',
      vehicleType: 'STANDARD',
      vehicleLicensePlate: 'XY789',
      vehicleSeats: 4,
      vehicleHasBabySeats: true,
      vehicleAllowsPets: false
    };

    const mockResponse: CreatedDriverDTO = {
      id: 1,
      email: 'driver@test.com',
      name: 'Jane',
      surname: 'Smith',
      address: '123 St'
    };

    let result: CreatedDriverDTO | undefined;
    service.registerDriver(driverData).subscribe(res => result = res);

    const req = httpMock.expectOne({
      method: 'POST',
      url: `${apiUrl}/drivers/register`
    });
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(driverData);

    req.flush(mockResponse);

    expect(result).toEqual(mockResponse);
  });

  it('should return error when registerDriver fails', () => {
    const driverData: CreateDriverDTO = {
      email: 'driver@test.com',
      name: 'Jane',
      surname: 'Smith',
      phone: '555',
      address: '123 St',
      vehicleModel: 'Toyota',
      vehicleType: 'STANDARD',
      vehicleLicensePlate: 'XY789',
      vehicleSeats: 4,
      vehicleHasBabySeats: true,
      vehicleAllowsPets: false
    };

    let error: any;
    service.registerDriver(driverData).subscribe({
      next: () => fail('should have failed'),
      error: (err) => error = err
    });

    const req = httpMock.expectOne({
      method: 'POST',
      url: `${apiUrl}/drivers/register`
    });
    req.flush('Registration failed', { status: 400, statusText: 'Bad Request' });

    expect(error.status).toBe(400);
  });
});
