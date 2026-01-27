import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InRideComponent } from './in-ride.component';

describe('InRideComponent', () => {
  let component: InRideComponent;
  let fixture: ComponentFixture<InRideComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [InRideComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InRideComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
