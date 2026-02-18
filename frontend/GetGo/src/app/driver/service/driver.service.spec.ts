import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { environment } from '../../../env/environment';

import { DriverService, ValidateTokenDTO, SetPasswordDTO, SetPasswordResponseDTO } from './driver.service';

describe('DriverService', () => {
  let service: DriverService;
  let httpMock: HttpTestingController;
  const apiUrl = `${environment.apiHost}/api/drivers`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DriverService]
    });
    service = TestBed.inject(DriverService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should call validateActivationToken and return valid token response', () => {
    const token = 'valid-token';
    const mockResponse: ValidateTokenDTO = { valid: true, email: 'd@gmail.com' };

    let result: ValidateTokenDTO | undefined;
    service.validateActivationToken(token).subscribe(res => result = res);

    const req = httpMock.expectOne({
      method: 'GET',
      url: `${apiUrl}/activate/${token}`
    });

    req.flush(mockResponse);

    expect(result).toEqual(mockResponse);
  });

  it('should return invalid token response', () => {
    const token = 'expired-token';
    const mockResponse: ValidateTokenDTO = { valid: false, email: '', reason: 'Token expired' };

    let result: ValidateTokenDTO | undefined;
    service.validateActivationToken(token).subscribe(res => result = res);

    const req = httpMock.expectOne({
      method: 'GET',
      url: `${apiUrl}/activate/${token}`
    });

    req.flush(mockResponse);

    expect(result).toEqual(mockResponse);
    expect(result!.valid).toBeFalse();
  });

  it('should return error when token validation fails', () => {
    const token = 'bad-token';

    let error: any;
    service.validateActivationToken(token).subscribe({
      next: () => fail('should have failed'),
      error: (err) => error = err
    });

    const req = httpMock.expectOne({
      method: 'GET',
      url: `${apiUrl}/activate/${token}`
    });

    req.flush('Not found', { status: 404, statusText: 'Not Found' });

    expect(error.status).toBe(404);
  });

  it('should call setDriverPassword and return success', () => {
    const payload: SetPasswordDTO = {
      token: 'valid-token',
      password: 'securePass1',
      confirmPassword: 'securePass1'
    };
    const mockResponse: SetPasswordResponseDTO = { success: true, message: 'OK' };

    let result: SetPasswordResponseDTO | undefined;
    service.setDriverPassword(payload).subscribe(res => result = res);

    const req = httpMock.expectOne({
      method: 'POST',
      url: `${apiUrl}/activate`
    });

    expect(req.request.body).toEqual(payload);

    req.flush(mockResponse);

    expect(result).toEqual(mockResponse);
    expect(result!.success).toBeTrue();
  });

  it('should return failure response when passwords do not match', () => {
    const payload: SetPasswordDTO = {
      token: 'valid-token',
      password: 'pass1',
      confirmPassword: 'pass2'
    };
    const mockResponse: SetPasswordResponseDTO = { success: false, message: 'Passwords do not match' };

    let result: SetPasswordResponseDTO | undefined;
    service.setDriverPassword(payload).subscribe(res => result = res);

    const req = httpMock.expectOne({
      method: 'POST',
      url: `${apiUrl}/activate`
    });

    req.flush(mockResponse);

    expect(result!.success).toBeFalse();
    expect(result!.message).toBe('Passwords do not match');
  });

  it('should return error when setting password fails', () => {
    const payload: SetPasswordDTO = {
      token: 'expired-token',
      password: 'securePass1',
      confirmPassword: 'securePass1'
    };

    let error: any;
    service.setDriverPassword(payload).subscribe({
      next: () => fail('should have failed'),
      error: (err) => error = err
    });

    const req = httpMock.expectOne({
      method: 'POST',
      url: `${apiUrl}/activate`
    });

    req.flush('Token expired', { status: 400, statusText: 'Bad Request' });

    expect(error.status).toBe(400);
  });
});
