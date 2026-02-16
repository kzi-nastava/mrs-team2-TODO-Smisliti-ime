import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../../env/environment';

import { RatingService } from './rating.service';

describe('RatingService', () => {
  let service: RatingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RatingService, ]
    });
    service = TestBed.inject(RatingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  fit('should be created', () => {
    expect(service).toBeTruthy();
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.removeItem('authToken'); // cleanup
    localStorage.removeItem('authToken');
  });

  fit('createRating should send Authorization header from sessionStorage and call reloadRatings', (done) => {
    const token = 'abc123';
    sessionStorage.setItem('authToken', token);

    // spy reloadRatings method
    spyOn<any>(service, 'reloadRatings').and.callThrough();

    const payload = { driverRating: 5, vehicleRating: 5, comment: 'ok', rideId: 42 };

    service.createRating(payload).subscribe({
      next: () => {
        // after flush we will assert
      },
      error: () => fail('should not error')
    });

    const req = httpMock.expectOne(`${environment.apiHost}/api/ratings?rideId=42`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${token}`);
    req.flush({ id: 1, ...payload });

    // reloadRatings should be called
    expect((service as any).reloadRatings).toHaveBeenCalled();
    done();
  });

  fit('createRating should not include Authorization header when token missing', (done) => {
    sessionStorage.removeItem('authToken');
    localStorage.removeItem('authToken');

    const payload = { driverRating: 4, vehicleRating: 4, comment: 'fine', rideId: 100 };

    service.createRating(payload).subscribe({
      next: () => {},
      error: () => fail('should not error')
    });

    const req = httpMock.expectOne(`${environment.apiHost}/api/ratings?rideId=100`);
    expect(req.request.method).toBe('POST');
    // header may be absent or empty depending on implementation
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({ id: 2, ...payload });
    done();
  });
});
