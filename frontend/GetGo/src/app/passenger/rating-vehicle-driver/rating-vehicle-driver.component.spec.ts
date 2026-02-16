import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RatingService } from '../../service/rating/rating.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute } from '@angular/router'
import { provideRouter } from '@angular/router';

import { RatingVehicleDriverComponent } from './rating-vehicle-driver.component';

describe('RatingVehicleDriverComponent', () => {
  let component: RatingVehicleDriverComponent;
  let fixture: ComponentFixture<RatingVehicleDriverComponent>;
  let ratingServiceSpy: jasmine.SpyObj<RatingService>;
  let snackBarSpy: jasmine.SpyObj<MatSnackBar>;


  beforeEach(async () => {
    // Create spies for RatingService methods
    ratingServiceSpy = jasmine.createSpyObj('RatingService', [
      'createRating',
      'setDriver',
      'reloadRatings'
    ]);

    // Stub the properties that the component uses from RatingService
    (ratingServiceSpy as any).ratings = jasmine.createSpy('ratings').and.returnValue([]);
    (ratingServiceSpy as any).avgVehicleRating = jasmine.createSpy('avgVehicleRating').and.returnValue(0);
    (ratingServiceSpy as any).avgDriverRating = jasmine.createSpy('avgDriverRating').and.returnValue(0);

    // MatSnackBar spy
    snackBarSpy = jasmine.createSpyObj('MatSnackBar', ['open']);

    TestBed.configureTestingModule({
      imports: [RatingVehicleDriverComponent],
      providers: [
        { provide: RatingService, useValue: ratingServiceSpy },
        // provide MatSnackBar but also override below to be safe
        { provide: MatSnackBar, useValue: snackBarSpy },
        { provide: ActivatedRoute, useValue: { params: of({ rideId: '123' }) }},
        provideRouter([])
      ]
    });

    // Ensure the standalone component's injector uses our spy for MatSnackBar
    TestBed.overrideProvider(MatSnackBar, { useValue: snackBarSpy });

    await TestBed.compileComponents();

    fixture = TestBed.createComponent(RatingVehicleDriverComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });


  fit('should create', () => {
    expect(component).toBeTruthy();
  });


  fit('should show validation snackbar when fields missing', () => {
      component.driverRating.set(null);
      component.vehicleRating.set(null);
      component.commentText.set('');
      component.submitRating();
      expect(snackBarSpy.open).toHaveBeenCalledWith('Please fill all fields', 'Close', jasmine.any(Object));
      expect(ratingServiceSpy.createRating).not.toHaveBeenCalled();
  });

  fit('should submit rating (happy path) and reset signals and show success snackbar', fakeAsync(() => {
    const returned = { id: 1, passengerId: 5, rideId: 123, driverId: 10, vehicleId: 10, driverRating: 5, vehicleRating: 5, comment: 'ok' };
    ratingServiceSpy.createRating.and.returnValue(of(returned));

    // set signals
    component.driverRating.set(5);
    component.vehicleRating.set(5);
    component.commentText.set('Good ride');
    // ensure component has rideId from route
    component.rideId = 123;

    // act
    component.submitRating();

    // flush microtasks so subscription runs
    tick();

    // createRating should be called with object containing rideId
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
    // signals reset
    expect(component.driverRating()).toBeNull();
    expect(component.vehicleRating()).toBeNull();
    expect(component.commentText()).toBe('');
  }));

  fit('should show server error message when createRating fails with 400 and message', fakeAsync(() => {
    const serverError = { status: 400, error: { message: 'Ride already rated' } };
    ratingServiceSpy.createRating.and.returnValue(throwError(() => serverError));

    component.driverRating.set(5);
    component.vehicleRating.set(5);
    component.commentText.set('Nice');
    component.rideId = 123;

    component.submitRating();

    tick();

    expect(snackBarSpy.open).toHaveBeenCalledWith('Ride already rated', 'Close', jasmine.any(Object));
  }));

});
