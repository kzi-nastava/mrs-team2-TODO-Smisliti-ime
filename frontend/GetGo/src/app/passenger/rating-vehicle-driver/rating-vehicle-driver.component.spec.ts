import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError, BehaviorSubject } from 'rxjs';
import { RatingService } from '../../service/rating/rating.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { signal } from '@angular/core';

import { RatingVehicleDriverComponent } from './rating-vehicle-driver.component';
import {GetRatingDTO} from '../../model/rating.model';

describe('RatingVehicleDriverComponent', () => {
  let component: RatingVehicleDriverComponent;
  let fixture: ComponentFixture<RatingVehicleDriverComponent>;
  let ratingServiceSpy: any;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;
  let httpTestingController: HttpTestingController;
  let activatedRouteParams: BehaviorSubject<any>;
  let routerSpy: jasmine.SpyObj<Router>;

  function createComponent(params: any = {}) {
    activatedRouteParams.next(params);
    fixture = TestBed.createComponent(RatingVehicleDriverComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  beforeEach(async () => {
    // Create spies for RatingService methods
    ratingServiceSpy = {
      createRating: jasmine.createSpy('createRating'),
      setDriver: jasmine.createSpy('setDriver'),
      reloadRatings: jasmine.createSpy('reloadRatings'),

      ratings: signal<GetRatingDTO[]>([]),
      avgVehicleRating: signal<number>(0),
      avgDriverRating: signal<number>(0)
    };

    // MatSnackBar spy
    snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    activatedRouteParams = new BehaviorSubject({});

    TestBed.configureTestingModule({
      imports: [RatingVehicleDriverComponent, HttpClientTestingModule],
      providers: [
        { provide: RatingService, useValue: ratingServiceSpy },
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: Router, useValue: routerSpy },
        { provide: ActivatedRoute, useValue: { params: activatedRouteParams }}
      ]
    });

    TestBed.overrideComponent(RatingVehicleDriverComponent, {
      set: {
        providers: [
          { provide: ActivatedRoute, useValue: { params: activatedRouteParams } },
          { provide: Router, useValue: routerSpy }
        ]
      }
    });

    await TestBed.compileComponents();

    TestBed.overrideProvider(MatSnackBar, { useValue: snackBarSpy });
    TestBed.overrideProvider(ActivatedRoute, { useValue: { params: activatedRouteParams } });

    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });


  it('should create', () => {
    createComponent();
    expect(component).toBeTruthy();
  });

  it('should load rideId from route params and fetch driverId', fakeAsync(() => {
    createComponent({ rideId: '123' });

    tick();
    expect(component.rideId).toBe(123);

    const req = httpTestingController.expectOne(req =>
      req.url.includes('/api/completed-rides/123/driver')
    );
    expect(req.request.method).toBe('GET');

    req.flush(42);
    tick();

    expect(ratingServiceSpy.setDriver).toHaveBeenCalledWith(42);
  }));

  it('should handle HTTP error when fetching driverId', fakeAsync(() => {
    createComponent({ rideId: '123' });

    tick();
    expect(component.rideId).toBe(123);

    spyOn(console, 'error');

    const req = httpTestingController.expectOne(req =>
      req.url.includes('/api/completed-rides/123/driver')
    );

    req.error(new ProgressEvent('error'), { status: 404 });
    tick();

    expect(console.error).toHaveBeenCalledWith('Failed to get driverId', jasmine.any(Object));
    expect(ratingServiceSpy.setDriver).not.toHaveBeenCalled();
  }));

  it('should not fetch driverId if rideId is null', fakeAsync(() => {
    createComponent({});

    tick();
    expect(component.rideId).toBeNull();

    httpTestingController.expectNone(req => req.url.includes('/api/completed-rides'));
    expect(ratingServiceSpy.setDriver).not.toHaveBeenCalled();
  }));

  it('should show validation snackbar when fields missing', fakeAsync(() => {
    createComponent({});
    component.driverRating.set(null);
    component.vehicleRating.set(null);
    component.commentText.set('');
    component.submitRating();
    tick();
    expect(snackBarSpy.open).toHaveBeenCalledWith('Please fill all fields', 'Close', jasmine.any(Object));
    expect(ratingServiceSpy.createRating).not.toHaveBeenCalled();
  }));

  it('should submit rating (happy path) and reset signals and show success snackbar', fakeAsync(() => {
    createComponent({});
    component.rideId = 123;

    const returned = { id: 1, passengerId: 5, rideId: 123, driverId: 10, vehicleId: 10, driverRating: 5, vehicleRating: 5, comment: 'ok' };
    ratingServiceSpy.createRating.and.returnValue(of(returned));

    component.driverRating.set(5);
    component.vehicleRating.set(5);
    component.commentText.set('Good ride');

    component.submitRating();
    tick();

    expect(ratingServiceSpy.createRating).toHaveBeenCalled();
    expect(ratingServiceSpy.createRating).toHaveBeenCalledWith(
      jasmine.objectContaining({
        driverRating: 5,
        vehicleRating: 5,
        comment: 'Good ride',
        rideId: 123
      })
    );

    expect(snackBarSpy.open).toHaveBeenCalledWith('Rating submitted successfully!', 'Close', jasmine.any(Object));
    expect(component.driverRating()).toBeNull();
    expect(component.vehicleRating()).toBeNull();
    expect(component.commentText()).toBe('');
  }));

  it('should show server error message when createRating fails with 400 and message', fakeAsync(() => {
    createComponent({});
    component.rideId = 123;

    const serverError = { status: 400, error: { message: 'Ride already rated' } };
    ratingServiceSpy.createRating.and.returnValue(throwError(() => serverError));

    component.driverRating.set(5);
    component.vehicleRating.set(5);
    component.commentText.set('Nice');

    component.submitRating();

    tick();

    expect(snackBarSpy.open).toHaveBeenCalledWith('Ride already rated', 'Close', jasmine.any(Object));
  }));

  it('should show validation snackbar when comment is only whitespace', fakeAsync(() => {
    createComponent({});
    component.rideId = 123;
    component.driverRating.set(5);
    component.vehicleRating.set(4);
    component.commentText.set('    '); // only whitespace

    component.submitRating();
    tick();

    expect(snackBarSpy.open).toHaveBeenCalledWith('Please fill all fields', 'Close', jasmine.any(Object));
    expect(ratingServiceSpy.createRating).not.toHaveBeenCalled();
  }));

  it('should not submit rating when rideId is null', fakeAsync(() => {
    createComponent({});
    component.rideId = null;

    component.driverRating.set(5);
    component.vehicleRating.set(5);
    component.commentText.set('Good');

    component.submitRating();
    tick();

    expect(ratingServiceSpy.createRating).not.toHaveBeenCalled();
    expect(snackBarSpy.open).not.toHaveBeenCalled();
  }));

  it('should show generic error message when createRating fails without specific message', fakeAsync(() => {
    createComponent({});
    component.rideId = 123;

    const serverError = { status: 500, error: {} };
    ratingServiceSpy.createRating.and.returnValue(throwError(() => serverError));

    component.driverRating.set(3);
    component.vehicleRating.set(4);
    component.commentText.set('Average');

    component.submitRating();
    tick();

    expect(snackBarSpy.open).toHaveBeenCalledWith('Something went wrong', 'Close', jasmine.any(Object));
  }));

  it('should show validation snackbar when only driverRating is missing', fakeAsync(() => {
    createComponent({});
    component.driverRating.set(null);
    component.vehicleRating.set(5);
    component.commentText.set('Good');

    component.submitRating();
    tick();

    expect(snackBarSpy.open).toHaveBeenCalledWith('Please fill all fields', 'Close', jasmine.any(Object));
    expect(ratingServiceSpy.createRating).not.toHaveBeenCalled();
  }));

  it('should show validation snackbar when only vehicleRating is missing', fakeAsync(() => {
    createComponent({});
    component.driverRating.set(5);
    component.vehicleRating.set(null);
    component.commentText.set('Good');

    component.submitRating();
    tick();

    expect(snackBarSpy.open).toHaveBeenCalledWith('Please fill all fields', 'Close', jasmine.any(Object));
    expect(ratingServiceSpy.createRating).not.toHaveBeenCalled();
  }));

});
