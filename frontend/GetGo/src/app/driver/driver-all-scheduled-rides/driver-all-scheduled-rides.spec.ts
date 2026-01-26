import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverAllScheduledRides } from './driver-all-scheduled-rides';

describe('DriverAllScheduledRides', () => {
  let component: DriverAllScheduledRides;
  let fixture: ComponentFixture<DriverAllScheduledRides>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverAllScheduledRides]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverAllScheduledRides);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
