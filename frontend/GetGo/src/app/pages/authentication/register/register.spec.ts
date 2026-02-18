import { ComponentFixture, TestBed, fakeAsync, tick, waitForAsync } from '@angular/core/testing';
import { RegisterComponent } from './register';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { SnackBarService } from '../../../service/snackBar/snackBar.service';
import { environment } from '../../../../env/environment';

describe('RegisterComponent', () => {
  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let httpMock: HttpTestingController;
  let snackBarSpy: jasmine.SpyObj<SnackBarService>;
  let router: Router;

  beforeEach(waitForAsync(() => {
    snackBarSpy = jasmine.createSpyObj('SnackBarService', ['show', 'showFormErrors']);

    TestBed.configureTestingModule({
      imports: [
        RegisterComponent,
        ReactiveFormsModule,
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([])
      ],
      providers: [{ provide: SnackBarService, useValue: snackBarSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    fixture.detectChanges();
  }));

  afterEach(() => {
    if (httpMock) {
      httpMock.verify();
    }
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call showFormErrors when form is invalid on submit', () => {
    // Ensure form is invalid (component has no validators by default), force invalid state
    component.form.setErrors({ invalid: true });
    component.submit();

    expect(snackBarSpy.showFormErrors).toHaveBeenCalled();
  });

  it('should send FormData with expected fields on successful submit', fakeAsync(() => {
    // Fill valid form values
    component.form.patchValue({
      email: 'test@example.com',
      firstName: 'john',
      lastName: 'doe',
      address: 'Some address',
      phoneNumber: '+381600000000',
      password: 'strongpassword',
      confirmPassword: 'strongpassword'
    });

    // Attach a mock file
    const blob = new Blob(['dummy'], { type: 'image/png' });
    const file = new File([blob], 'avatar.png', { type: 'image/png' });
    component.selectedFile = file;

    // Spy router.navigate
    const navigateSpy = spyOn(router, 'navigate');

    component.submit();

    // Expect a POST to the register endpoint
    const req = httpMock.expectOne(`${environment.apiHost}/api/auth/register`);
    expect(req.request.method).toBe('POST');

    const body = req.request.body as FormData;
    expect(body instanceof FormData).toBe(true);

    // Check that keys exist and values match
    expect(body.get('email')).toBe('test@example.com');
    expect(body.get('name')).toBe('John'); // component capitalizes
    expect(body.get('surname')).toBe('Doe');
    expect(body.get('password')).toBe('strongpassword');
    expect(body.get('phone')).toBe('+381600000000');
    expect(body.get('address')).toBe('Some address');
    const appendedFile = body.get('file') as File;
    expect(appendedFile).toBeTruthy();
    expect((appendedFile as File).name).toBe('avatar.png');

    // Flush success response
    req.flush({});
    tick();

    expect(component.isSubmitting).toBe(false);
    expect(snackBarSpy.show).toHaveBeenCalledWith('Registration successful! Please login.', true, 3000);
    expect(navigateSpy).toHaveBeenCalledWith(['/login']);
  }));

  it('should handle server error and show messages', fakeAsync(() => {
    component.form.patchValue({
      email: 'err@example.com',
      firstName: 'Jane',
      lastName: 'Doe',
      address: 'Addr',
      phoneNumber: '0612345678',
      password: 'longpassword',
      confirmPassword: 'longpassword'
    });

    component.submit();

    const req = httpMock.expectOne(`${environment.apiHost}/api/auth/register`);
    expect(req.request.method).toBe('POST');

    // Simulate 400 with structured errors
    req.flush({ errors: [{ message: 'Email already exists' }] }, { status: 400, statusText: 'Bad Request' });
    tick();

    expect(component.isSubmitting).toBe(false);
    expect(snackBarSpy.show).toHaveBeenCalledWith(['Email already exists']);
  }));

  it('should validate individual form controls (email, names, address, phone, password)', () => {
    const email = component.form.get('email');
    const firstName = component.form.get('firstName');
    const lastName = component.form.get('lastName');
    const address = component.form.get('address');
    const phone = component.form.get('phoneNumber');
    const password = component.form.get('password');
    const confirm = component.form.get('confirmPassword');

    // Initially all controls are empty and invalid
    expect(email?.hasError('required')).toBeTrue();
    expect(firstName?.hasError('required')).toBeTrue();
    expect(lastName?.hasError('required')).toBeTrue();
    expect(address?.hasError('required')).toBeTrue();
    expect(phone?.hasError('required')).toBeTrue();
    expect(password?.hasError('required')).toBeTrue();
    expect(confirm?.hasError('required')).toBeTrue();

    // Invalid email format
    email?.setValue('not-an-email');
    expect(email?.hasError('email') || email?.hasError('pattern')).toBeTrue();

    // Valid email
    email?.setValue('valid@example.com');
    expect(email?.valid).toBeTrue();

    // Name length validations
    firstName?.setValue('A');
    expect(firstName?.hasError('minlength')).toBeTrue();
    firstName?.setValue('Anna');
    expect(firstName?.valid).toBeTrue();

    lastName?.setValue('B');
    expect(lastName?.hasError('minlength')).toBeTrue();
    lastName?.setValue('Brown');
    expect(lastName?.valid).toBeTrue();

    // Address
    address?.setValue('');
    expect(address?.hasError('required')).toBeTrue();
    address?.setValue('123 Main St');
    expect(address?.valid).toBeTrue();

    // Phone pattern
    phone?.setValue('12345');
    expect(phone?.hasError('pattern')).toBeTrue();
    phone?.setValue('+381612345678');
    expect(phone?.valid).toBeTrue();
    phone?.setValue('0612345678');
    expect(phone?.valid).toBeTrue();

    // Password length and matching
    password?.setValue('short');
    expect(password?.hasError('minlength')).toBeTrue();
    password?.setValue('longenough');
    expect(password?.valid).toBeTrue();

    confirm?.setValue('different');
    // Form-level validator should flag mismatch
    expect(component.form.hasError('passwordsDontMatch')).toBeTrue();
    confirm?.setValue('longenough');
    expect(component.form.hasError('passwordsDontMatch')).toBeFalse();
  });

  it('should have submit button disabled while submitting and show text change', () => {
    // Use the existing fixture to check the initial (not submitting) state
    const el: HTMLElement = fixture.nativeElement;
    const btn: HTMLButtonElement | null = el.querySelector('button[type="submit"]');
    expect(btn).toBeTruthy();
    if (!btn) return;

    // Initially not submitting
    expect(component.isSubmitting).toBeFalse();
    expect(btn.disabled).toBeFalse();
    expect(btn.textContent?.trim()).toBe('Register');

    // Create a fresh component instance and set isSubmitting BEFORE first change detection
    const fixtureSubmitting = TestBed.createComponent(RegisterComponent);
    const compSubmitting = fixtureSubmitting.componentInstance;
    compSubmitting.isSubmitting = true;
    fixtureSubmitting.detectChanges();

    const btnSubmitting: HTMLButtonElement | null = fixtureSubmitting.nativeElement.querySelector('button[type="submit"]');
    expect(btnSubmitting).toBeTruthy();
    if (!btnSubmitting) return;
    expect(btnSubmitting.disabled).toBeTrue();
    expect(btnSubmitting.textContent?.trim()).toBe('Registering...');

    // Create another fresh instance for the not-submitting state to ensure clean checks
    const fixtureNotSubmitting = TestBed.createComponent(RegisterComponent);
    const compNotSubmitting = fixtureNotSubmitting.componentInstance;
    compNotSubmitting.isSubmitting = false;
    fixtureNotSubmitting.detectChanges();

    const btnNotSubmitting: HTMLButtonElement | null = fixtureNotSubmitting.nativeElement.querySelector('button[type="submit"]');
    expect(btnNotSubmitting).toBeTruthy();
    if (!btnNotSubmitting) return;
    expect(btnNotSubmitting.disabled).toBeFalse();
    expect(btnNotSubmitting.textContent?.trim()).toBe('Register');
  });

  it('should not call HTTP when form invalid and should call showFormErrors', () => {
    // ensure form invalid
    component.form.reset();
    fixture.detectChanges();

    component.submit();

    // no http requests should be made
    httpMock.expectNone(`${environment.apiHost}/api/auth/register`);
    expect(snackBarSpy.showFormErrors).toHaveBeenCalled();
  });

  it('should mark form valid when all controls populated correctly', () => {
    component.form.patchValue({
      email: 'ok@example.com',
      firstName: 'John',
      lastName: 'Doe',
      address: 'Some address',
      phoneNumber: '+381612345678',
      password: 'strongpass',
      confirmPassword: 'strongpass'
    });

    fixture.detectChanges();
    expect(component.form.valid).toBeTrue();
  });
});
