import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../../env/environment';

import { RatingService } from './rating.service';

describe('RatingService', () => {
  let service: RatingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    // ensure storages are clean before each test
    sessionStorage.removeItem('authToken');
    localStorage.removeItem('authToken');
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RatingService, ]
    });
    service = TestBed.inject(RatingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.removeItem('authToken'); // cleanup
    localStorage.removeItem('authToken');
  });

  it('createRating should send Authorization header from sessionStorage and call reloadRatings', () => {
    const token = 'abc123';
    sessionStorage.setItem('authToken', token);

    // spy reloadRatings but don't call through (no side effects)
    spyOn<any>(service, 'reloadRatings');

    const payload = { driverRating: 5, vehicleRating: 5, comment: 'ok', rideId: 42 };

    let responseBody: any = null;
    service.createRating(payload).subscribe(res => responseBody = res);

    const req = httpMock.expectOne(`${environment.apiHost}/api/ratings?rideId=42`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${token}`);

    // send response; this calls the subscribe callback synchronously
    req.flush({ id: 1, ...payload });

    // now we can assert synchronously that subscriber saw the response
    expect(responseBody).toEqual(jasmine.objectContaining({ id: 1, ...payload }));

    // reloadRatings should have been invoked by the tap operator
    expect((service as any).reloadRatings).toHaveBeenCalled();
  });


  it('createRating should not include Authorization header when token missing', () => {
    // Ensure no token anywhere
    sessionStorage.removeItem('authToken');
    localStorage.removeItem('authToken');

    const payload = { driverRating: 4, vehicleRating: 4, comment: 'fine', rideId: 100 };

    let responseBody: any = null;
    // subscribe and capture response synchronously when flush is called
    service.createRating(payload).subscribe(res => responseBody = res, err => fail('should not error'));

    const req = httpMock.expectOne(`${environment.apiHost}/api/ratings?rideId=100`);
    expect(req.request.method).toBe('POST');

    // Authorization header should not be present when no token anywhere
    expect(req.request.headers.has('Authorization')).toBe(false);

    // send mocked response (synchronously triggers subscribe)
    req.flush({ id: 2, ...payload });

    // assert subscriber received expected response body
    expect(responseBody).toEqual(jasmine.objectContaining({ id: 2, ...payload }));
  });

});
