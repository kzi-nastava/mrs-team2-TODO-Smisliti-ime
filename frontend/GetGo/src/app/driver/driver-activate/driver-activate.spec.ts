import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';

import { DriverActivate } from './driver-activate';
import { DriverService } from '../service/driver.service';
import { SnackBarService } from '../../service/snackBar/snackBar.service';

describe('DriverActivate', () => {
  let component: DriverActivate;
  let fixture: ComponentFixture<DriverActivate>;
  let driverServiceSpy: jasmine.SpyObj<DriverService>;
  let snackBarSpy: jasmine.SpyObj<SnackBarService>;
  let routerSpy: jasmine.SpyObj<Router>;

  function setup(token: string | null) {
    const activatedRoute = {
      snapshot: {
        paramMap: {
          get: (key: string) => token
        }
      }
    };

    driverServiceSpy = jasmine.createSpyObj('DriverService', ['validateActivationToken', 'setDriverPassword']);
    snackBarSpy = jasmine.createSpyObj('SnackBarService', ['show']);

    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      imports: [DriverActivate, FormsModule, RouterTestingModule],
      providers: [
        { provide: DriverService, useValue: driverServiceSpy },
        { provide: SnackBarService, useValue: snackBarSpy },
        { provide: ActivatedRoute, useValue: activatedRoute }
      ]
    });

    TestBed.overrideComponent(DriverActivate, {
      set: {
        providers: [
          { provide: ActivatedRoute, useValue: activatedRoute }
        ]
      }
    });
  }

  function createComponent() {
    fixture = TestBed.createComponent(DriverActivate);
    component = fixture.debugElement.componentInstance;
    routerSpy = TestBed.inject(Router) as any;
    spyOn(routerSpy, 'navigate');
  }

  it('should read token from route params and validate it', fakeAsync(() => {
    setup('valid-token');
    driverServiceSpy.validateActivationToken.and.returnValue(of({ valid: true, email: 'd@gmail.com' }));

    createComponent();
    fixture.detectChanges();
    tick();

    expect(component.token).toBe('valid-token');
    expect(driverServiceSpy.validateActivationToken).toHaveBeenCalledWith('valid-token');
    expect(component.isValid).toBeTrue();
    expect(component.driverEmail).toBe('d@gmail.com');
    expect(component.isValidating).toBeFalse();
  }));

  it('should show error when token is missing', fakeAsync(() => {
    setup(null);

    createComponent();
    fixture.detectChanges();
    tick();

    expect(component.token).toBe('');
    expect(component.isValidating).toBeFalse();
    expect(component.validationError).toBe('Invalid activation link');
    expect(driverServiceSpy.validateActivationToken).not.toHaveBeenCalled();
  }));

  it('should show error when token is invalid', fakeAsync(() => {
    setup('bad-token');
    driverServiceSpy.validateActivationToken.and.returnValue(of({ valid: false, email: '', reason: 'Token expired' }));

    createComponent();
    fixture.detectChanges();
    tick();

    expect(component.isValid).toBeFalse();
    expect(component.validationError).toBe('Token expired');
  }));

  it('should show error when server error during token validation', fakeAsync(() => {
    setup('valid-token');
    driverServiceSpy.validateActivationToken.and.returnValue(throwError(() => ({ status: 500 })));
    spyOn(console, 'error');

    createComponent();
    fixture.detectChanges();
    tick();

    expect(component.isValidating).toBeFalse();
    expect(component.validationError).toBe('Failed to validate activation link');
  }));

  describe('setPassword validation', () => {
    beforeEach(fakeAsync(() => {
      setup('valid-token');
      driverServiceSpy.validateActivationToken.and.returnValue(of({ valid: true, email: 'd@gmail.com' }));
      createComponent();
      fixture.detectChanges();
      tick();
    }));

    it('should show error when passwords are empty', () => {
      component.passwordData = { password: '', confirmPassword: '' };
      component.setPassword();
      expect(snackBarSpy.show).toHaveBeenCalledWith('Please fill in all fields');
      expect(driverServiceSpy.setDriverPassword).not.toHaveBeenCalled();
    });

    it('should show error when password is empty', () => {
      component.passwordData = { password: '', confirmPassword: 'aaaaaaaa' };
      component.setPassword();
      expect(snackBarSpy.show).toHaveBeenCalledWith('Please fill in all fields');
    });

    it('should show error when confirmPassword is empty', () => {
      component.passwordData = { password: 'aaaaaaaa', confirmPassword: '' };
      component.setPassword();
      expect(snackBarSpy.show).toHaveBeenCalledWith('Please fill in all fields');
    });

    it('should show error when passwords do not match', () => {
      component.passwordData = { password: 'aaaaaaaa', confirmPassword: 'bbbbbbbb' };
      component.setPassword();
      expect(snackBarSpy.show).toHaveBeenCalledWith('Passwords do not match');
      expect(driverServiceSpy.setDriverPassword).not.toHaveBeenCalled();
    });

    it('should show error when password is too short', () => {
      component.passwordData = { password: 'aaaaaaa', confirmPassword: 'aaaaaaa' };
      component.setPassword();
      expect(snackBarSpy.show).toHaveBeenCalledWith('Password must be at least 8 characters long');
      expect(driverServiceSpy.setDriverPassword).not.toHaveBeenCalled();
    });
  });

  it('should call service and go to login page', fakeAsync(() => {
    setup('valid-token');
    driverServiceSpy.validateActivationToken.and.returnValue(of({ valid: true, email: 'd@gmail.com' }));

    createComponent();
    fixture.detectChanges();
    tick();

    driverServiceSpy.setDriverPassword.and.returnValue(of({ success: true, message: 'OK' }));

    component.passwordData = { password: 'aaaaaaaa', confirmPassword: 'aaaaaaaa' };
    component.setPassword();
    tick();

    expect(driverServiceSpy.setDriverPassword).toHaveBeenCalledWith({
      token: 'valid-token',
      password: 'aaaaaaaa',
      confirmPassword: 'aaaaaaaa'
    });
    expect(snackBarSpy.show).toHaveBeenCalledWith('Account activated successfully! You can now log in.', true);
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/login']);
  }));

  it('should show server message when setPassword returns success false', fakeAsync(() => {
    setup('valid-token');
    driverServiceSpy.validateActivationToken.and.returnValue(of({ valid: true, email: 'd@gmail.com' }));

    createComponent();
    fixture.detectChanges();
    tick();

    driverServiceSpy.setDriverPassword.and.returnValue(of({ success: false, message: 'Token already used' }));

    component.passwordData = { password: 'aaaaaaaa', confirmPassword: 'aaaaaaaa' };
    component.setPassword();
    tick();

    expect(snackBarSpy.show).toHaveBeenCalledWith('Token already used');
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  }));
});
