import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverStartRide } from './driver-start-ride';

describe('DriverStartRide', () => {
  let component: DriverStartRide;
  let fixture: ComponentFixture<DriverStartRide>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverStartRide]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverStartRide);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
