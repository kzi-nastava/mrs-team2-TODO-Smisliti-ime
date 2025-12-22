import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PassengerProfileInfo } from './passenger-profile-info';

describe('PassengerProfileInfo', () => {
  let component: PassengerProfileInfo;
  let fixture: ComponentFixture<PassengerProfileInfo>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PassengerProfileInfo]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PassengerProfileInfo);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
