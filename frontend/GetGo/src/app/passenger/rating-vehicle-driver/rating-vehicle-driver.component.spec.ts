import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RatingVehicleDriverComponent } from './rating-vehicle-driver.component';

describe('RatingVehicleDriverComponent', () => {
  let component: RatingVehicleDriverComponent;
  let fixture: ComponentFixture<RatingVehicleDriverComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RatingVehicleDriverComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RatingVehicleDriverComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
