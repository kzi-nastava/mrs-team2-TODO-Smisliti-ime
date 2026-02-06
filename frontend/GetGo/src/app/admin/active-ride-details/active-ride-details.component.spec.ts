import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActiveRideDetailsComponent } from './active-ride-details.component';

describe('ActiveRideDetailsComponent', () => {
  let component: ActiveRideDetailsComponent;
  let fixture: ComponentFixture<ActiveRideDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActiveRideDetailsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActiveRideDetailsComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
