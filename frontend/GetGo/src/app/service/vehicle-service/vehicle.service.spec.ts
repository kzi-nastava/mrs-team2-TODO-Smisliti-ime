import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../../env/environment';

import { VehicleService } from './vehicle.service';

describe('VehicleService', () => {
  let service: VehicleService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiHost}/api/vehicles`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [VehicleService]
    });
    service = TestBed.inject(VehicleService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should return vehicle types', () => {
    const mockTypes = ['STANDARD', 'LUXURY', 'VAN', 'SUV'];

    let result: string[] | undefined;
    service.getVehicleTypes().subscribe(res => result = res);

    const req = httpMock.expectOne({
      method: 'GET',
      url: `${baseUrl}/types`
    });

    req.flush(mockTypes);

    expect(result).toEqual(mockTypes);
  });

  it('should return error when getVehicleTypes fails', () => {
    let error: any;
    service.getVehicleTypes().subscribe({
      next: () => fail('should have failed'),
      error: (err) => error = err
    });

    const req = httpMock.expectOne({
      method: 'GET',
      url: `${baseUrl}/types`
    });
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });

    expect(error.status).toBe(500);
  });
});
