import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PassengerRideDetailsComponent } from './ride-details.component';

describe('RideDetailsComponent', () => {
  let component: PassengerRideDetailsComponent;
  let fixture: ComponentFixture<PassengerRideDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PassengerRideDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PassengerRideDetailsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
