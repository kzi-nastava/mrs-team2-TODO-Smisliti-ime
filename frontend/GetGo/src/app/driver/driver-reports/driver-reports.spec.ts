import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverReports } from './driver-reports';

describe('DriverReports', () => {
  let component: DriverReports;
  let fixture: ComponentFixture<DriverReports>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverReports]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverReports);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
