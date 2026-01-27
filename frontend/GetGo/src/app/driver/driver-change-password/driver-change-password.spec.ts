import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverChangePassword } from './driver-change-password';

describe('DriverChangePassword', () => {
  let component: DriverChangePassword;
  let fixture: ComponentFixture<DriverChangePassword>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverChangePassword]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverChangePassword);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
