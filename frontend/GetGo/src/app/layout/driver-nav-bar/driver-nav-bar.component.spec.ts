import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DriverNavBarComponent } from './driver-nav-bar.component';

describe('DriverNavBarComponent', () => {
  let component: DriverNavBarComponent;
  let fixture: ComponentFixture<DriverNavBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DriverNavBarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DriverNavBarComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
