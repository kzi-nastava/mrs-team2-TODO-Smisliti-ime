import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PassengerReports } from './passenger-reports';

describe('PassengerReports', () => {
  let component: PassengerReports;
  let fixture: ComponentFixture<PassengerReports>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PassengerReports]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PassengerReports);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
