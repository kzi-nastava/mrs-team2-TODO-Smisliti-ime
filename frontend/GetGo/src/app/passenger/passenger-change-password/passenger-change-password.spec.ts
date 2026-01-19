import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PassengerChangePassword } from './passenger-change-password';

describe('PassengerChangePassword', () => {
  let component: PassengerChangePassword;
  let fixture: ComponentFixture<PassengerChangePassword>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PassengerChangePassword]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PassengerChangePassword);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
