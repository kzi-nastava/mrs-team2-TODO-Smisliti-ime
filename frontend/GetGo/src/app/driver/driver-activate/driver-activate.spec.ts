import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverActivate } from './driver-activate';

describe('DriverActivate', () => {
  let component: DriverActivate;
  let fixture: ComponentFixture<DriverActivate>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverActivate]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverActivate);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
