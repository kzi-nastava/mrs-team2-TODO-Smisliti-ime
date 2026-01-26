import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduledRideDetails } from './scheduled-ride-details';

describe('ScheduledRideDetails', () => {
  let component: ScheduledRideDetails;
  let fixture: ComponentFixture<ScheduledRideDetails>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ScheduledRideDetails]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ScheduledRideDetails);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
