import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RideTrackingMapComponent } from './ride-tracking-map.component';

describe('RideTrackingMapComponent', () => {
  let component: RideTrackingMapComponent;
  let fixture: ComponentFixture<RideTrackingMapComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RideTrackingMapComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RideTrackingMapComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
