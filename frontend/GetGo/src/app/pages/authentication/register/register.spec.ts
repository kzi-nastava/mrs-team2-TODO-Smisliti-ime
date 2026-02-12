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
      imports: [ReactiveFormsModule, HttpClientTestingModule, RouterTestingModule.withRoutes([])],
      declarations: [RegisterComponent],
      providers: [{ provide: SnackBarService, useValue: snackBarSpy }]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);

    fixture.detectChanges();
  }));

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call showFormErrors when form is invalid on submit', () => {
    // Ensure form is invalid (defaults are empty)
    component.form.patchValue({ email: '', firstName: '' });
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
      phoneNumber: '0600000000',
      password: 'pw',
      confirmPassword: 'pw'
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
});
